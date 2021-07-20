package pm.lus.spaceship.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pm.lus.spaceship.endpoint.Controller;
import pm.lus.spaceship.middleware.Middleware;
import pm.lus.spaceship.routing.endpoint.controller.ControllerDefinition;
import pm.lus.spaceship.routing.endpoint.endpoint.EndpointDefinition;
import pm.lus.spaceship.routing.endpoint.endpoint.PathDefinition;
import pm.lus.spaceship.routing.endpoint.parameter.ParameterAdapterRegistry;
import pm.lus.spaceship.routing.endpoint.parameter.ParameterDefinition;
import pm.lus.spaceship.routing.middleware.MiddlewareDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a way of creating a new {@link Router} while building and validating all needed definitions
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class RouterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterFactory.class);

    private static boolean performInterferenceValidation = true;

    private RouterFactory() {
    }

    /**
     * Provides a way to specify whether or not to perform interference validation for newly added endpoints in case the validation is broken
     *
     * @param performInterferenceValidation Whether or not to perform interference validation for newly added endpoints
     * @deprecated Only use this method if you have no other way of achieving your goal!
     */
    @Deprecated
    public static void setPerformInterferenceValidation(final boolean performInterferenceValidation) {
        RouterFactory.performInterferenceValidation = performInterferenceValidation;
    }

    /**
     * Builds middleware, controller and endpoint definitions using the base classes, validates them and creates a new router using them
     *
     * @param settings                 The router settings to apply
     * @param parameterAdapterRegistry The parameter adapter registry to use
     * @param middlewares              The middleware base classes to use
     * @param controllers              The controller base classes to use
     * @return The created router
     */
    public static Router create(final RouterSettings settings, final ParameterAdapterRegistry parameterAdapterRegistry, final Set<Class<? extends Middleware>> middlewares, final Set<Class<? extends Controller>> controllers) {
        // Create the middleware definitions
        final Set<MiddlewareDefinition> middlewareDefinitions = new HashSet<>();
        middlewares.forEach(middlewareClass -> {
            final Middleware instance;
            try {
                final Constructor<? extends Middleware> constructor = middlewareClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                instance = constructor.newInstance();
            } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                LOGGER.error("middleware '{}' does not have an empty constructor to instantiate it; ignoring!", middlewareClass.getName());
                return;
            }

            middlewareDefinitions.add(MiddlewareDefinition.build(instance));
        });

        // Create the controller definitions
        final Set<ControllerDefinition> controllerDefinitions = new HashSet<>();
        controllers.forEach(controllerClass -> {
            final Controller instance;
            try {
                final Constructor<? extends Controller> constructor = controllerClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                instance = constructor.newInstance();
            } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                LOGGER.error("controller '{}' does not have an empty constructor to instantiate it; ignoring!", controllerClass.getName());
                return;
            }

            controllerDefinitions.add(ControllerDefinition.build(instance));
        });

        // Create the endpoint definitions
        final Set<EndpointDefinition> endpointDefinitions = new HashSet<>();
        controllerDefinitions.forEach(controllerDefinition -> {
            methods:
            for (final Method method : controllerDefinition.getInstance().getClass().getDeclaredMethods()) {
                final EndpointDefinition definition = EndpointDefinition.build(controllerDefinition, method);
                if (definition == null) {
                    continue;
                }

                // Check if the method signature has another parameter count than the route definition
                if (definition.getPath().getParameterCount() != definition.getParameters().length) {
                    LOGGER.error(
                            "endpoint handler '{}#{}' defines more/less arguments than present in its route definition; ignoring!",
                            method.getDeclaringClass().getName(),
                            method.getName()
                    );
                    continue;
                }

                // Check if the endpoint handler defines mandatory parameters after optional parameters, which is unsupported
                boolean optionalsIntroduced = false;
                for (final ParameterDefinition parameter : definition.getParameters()) {
                    if (optionalsIntroduced && !parameter.isOptional()) {
                        LOGGER.error(
                                "endpoint handler '{}#{}' uses mandatory parameter(s) after optional parameter(s), which is unsupported; ignoring!",
                                method.getDeclaringClass().getName(),
                                method.getName()
                        );
                        continue methods;
                    }
                    if (parameter.isOptional()) {
                        optionalsIntroduced = true;
                    }
                }

                // Check if the endpoint definition interferes with another, already registered one
                for (final EndpointDefinition other : endpointDefinitions) {
                    if (doInterfere(settings, definition, other)) {
                        LOGGER.error(
                                "endpoint handler '{}#{}' interferes with another one ('{}#{}'); ignoring!",
                                method.getDeclaringClass().getName(),
                                method.getName(),
                                other.getMethod().getDeclaringClass().getName(),
                                other.getMethod().getName()
                        );
                        continue methods;
                    }
                }

                endpointDefinitions.add(definition);
            }
        });

        return new Router(settings, parameterAdapterRegistry, middlewareDefinitions, controllerDefinitions, endpointDefinitions);
    }

    private static boolean doInterfere(final RouterSettings settings, final EndpointDefinition first, final EndpointDefinition second) {
        if (!RouterFactory.performInterferenceValidation) {
            return false;
        }

        if (first.getMethods().stream().noneMatch(method -> second.getMethods().contains(method))) {
            return false;
        }

        String firstPath = first.getPath().getRaw();
        String secondPath = second.getPath().getRaw();

        // Find the maximum amount of mandatory parameters of one of the two paths
        final long firstMandatory = Arrays.stream(first.getParameters()).filter(definition -> !definition.isOptional()).count();
        final long secondMandatory = Arrays.stream(second.getParameters()).filter(definition -> !definition.isOptional()).count();
        final int amountToLimitTo = (int) Math.max(firstMandatory, secondMandatory);

        // Replace all surplus parameters in the first path
        final AtomicInteger found = new AtomicInteger();
        firstPath = PathDefinition.PARAMETER_REGEX.matcher(firstPath).replaceAll(result -> {
            if (found.incrementAndGet() > amountToLimitTo) {
                return "";
            }
            return result.group();
        });

        // Replace all surplus parameters in the second path
        found.set(0);
        secondPath = PathDefinition.PARAMETER_REGEX.matcher(secondPath).replaceAll(result -> {
            if (found.incrementAndGet() > amountToLimitTo) {
                return "";
            }
            return result.group();
        });

        // Remove trailing slashes if the router ignores them
        if (settings.doIgnoreTrailingSlashes()) {
            firstPath = firstPath.replaceAll("/+$", "");
            secondPath = secondPath.replaceAll("/+$", "");
        }

        // Remove stacked slashes ('//...') if the router ignores them
        if (settings.doIgnoreStackedSlashes()) {
            firstPath = firstPath.replaceAll("/+", "/");
            secondPath = secondPath.replaceAll("/+", "/");
        }

        // Check if the paths interfere after processing
        final boolean pathsInterfere = settings.doIgnoreCase() ? firstPath.equalsIgnoreCase(secondPath) : firstPath.equals(secondPath);
        if (!pathsInterfere) {
            return false;
        }

        // Check if the parameter types are the same
        final Class<?>[] firstParameterTypes = Arrays.stream(Arrays.copyOfRange(first.getParameters(), 0, amountToLimitTo))
                .map(ParameterDefinition::getType)
                .toArray(Class<?>[]::new);
        final Class<?>[] secondParameterTypes = Arrays.stream(Arrays.copyOfRange(second.getParameters(), 0, amountToLimitTo))
                .map(ParameterDefinition::getType)
                .toArray(Class<?>[]::new);
        return Arrays.equals(firstParameterTypes, secondParameterTypes);
    }

}
