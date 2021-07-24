package pm.lus.spaceship.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pm.lus.spaceship.controller.Controller;
import pm.lus.spaceship.dependency.DependencyInjector;
import pm.lus.spaceship.middleware.Middleware;
import pm.lus.spaceship.routing.definition.controller.ControllerDefinition;
import pm.lus.spaceship.routing.definition.endpoint.EndpointDefinition;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterAdapterRegistry;
import pm.lus.spaceship.routing.definition.endpoint.path.parts.EmptyPart;
import pm.lus.spaceship.routing.definition.endpoint.path.parts.LiteralPart;
import pm.lus.spaceship.routing.definition.endpoint.path.parts.ParameterPart;
import pm.lus.spaceship.routing.definition.endpoint.path.parts.PathPart;
import pm.lus.spaceship.routing.definition.middleware.MiddlewareDefinition;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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
     * @param dependencyInjector       The dependency injector to use to inject dependencies into the instantiated objects
     * @param middlewares              The middleware base classes to use
     * @param controllers              The controller base classes to use
     * @return The created router
     */
    public static Router create(
            final RouterSettings settings,
            final ParameterAdapterRegistry parameterAdapterRegistry,
            final DependencyInjector dependencyInjector,
            final Set<Class<? extends Middleware>> middlewares,
            final Set<Class<? extends Controller>> controllers
    ) {
        // Create the middleware definitions
        final Set<MiddlewareDefinition> middlewareDefinitions = new HashSet<>();
        middlewares.forEach(middlewareClass -> {
            final MiddlewareDefinition definition;
            try {
                definition = MiddlewareDefinition.build(middlewareClass);
            } catch (final Exception exception) {
                LOGGER.error(
                        String.format("could not define middleware '%s'; skipping!", middlewareClass.getName()),
                        exception
                );
                return;
            }
            dependencyInjector.inject(definition.getInstance());
            middlewareDefinitions.add(definition);
        });

        // Create the controller definitions
        final Set<ControllerDefinition> controllerDefinitions = new HashSet<>();
        controllers.forEach(controllerClass -> {
            final ControllerDefinition definition;
            try {
                definition = ControllerDefinition.build(controllerClass);
            } catch (final Exception exception) {
                LOGGER.error(
                        String.format("could not define controller '%s'; skipping!", controllerClass.getName()),
                        exception
                );
                return;
            }
            dependencyInjector.inject(definition.getInstance());
            controllerDefinitions.add(definition);
        });

        // Create the endpoint definitions
        final Set<EndpointDefinition> endpointDefinitions = new HashSet<>();
        controllerDefinitions.forEach(controllerDefinition -> {
            methods:
            for (final Method method : controllerDefinition.getInstance().getClass().getDeclaredMethods()) {
                // Try to define the endpoint
                final EndpointDefinition definition;
                try {
                    definition = EndpointDefinition.build(controllerDefinition, method);
                } catch (final Exception exception) {
                    LOGGER.error(
                            String.format("could not define endpoint handler '%s#%s'; skipping!", method.getDeclaringClass().getName(), method.getName()),
                            exception
                    );
                    continue;
                }
                if (definition == null) {
                    continue;
                }

                // Check if the endpoint definition interferes with another, already registered one
                for (final EndpointDefinition other : endpointDefinitions) {
                    if (doInterfere(settings, definition, other)) {
                        LOGGER.error(
                                "endpoint handler '{}#{}' interferes with another one ('{}#{}'); skipping!",
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

        final List<PathPart> firstParts = new ArrayList<>(first.getPath().getParts());
        final List<PathPart> secondParts = new ArrayList<>(second.getPath().getParts());

        // Remove the potential leading slash out of both lists
        if (firstParts.size() > 0 && firstParts.get(0) instanceof EmptyPart) {
            firstParts.remove(0);
        }
        if (secondParts.size() > 0 && secondParts.get(0) instanceof EmptyPart) {
            secondParts.remove(0);
        }

        // Both paths match the root path (/)
        if (firstParts.isEmpty() && secondParts.isEmpty()) {
            return true;
        }

        // Remove any empty path parts out of the part lists if stacked slashes should be ignored
        if (settings.doIgnoreStackedSlashes()) {
            for (final ListIterator<PathPart> iterator = firstParts.listIterator(); iterator.hasNext(); ) {
                if (iterator.nextIndex() < firstParts.size() - 1 & iterator.next() instanceof EmptyPart) {
                    iterator.remove();
                }
            }

            for (final ListIterator<PathPart> iterator = secondParts.listIterator(); iterator.hasNext(); ) {
                if (iterator.nextIndex() < secondParts.size() - 1 & iterator.next() instanceof EmptyPart) {
                    iterator.remove();
                }
            }
        }

        // Remove any trailing empty path parts out of the part lists if trailing slashes should be ignored
        if (settings.doIgnoreTrailingSlashes()) {
            for (final ListIterator<PathPart> iterator = firstParts.listIterator(firstParts.size()); iterator.hasPrevious(); ) {
                if (iterator.previous() instanceof EmptyPart) {
                    iterator.remove();
                } else {
                    break;
                }
            }

            for (final ListIterator<PathPart> iterator = secondParts.listIterator(secondParts.size()); iterator.hasPrevious(); ) {
                if (iterator.previous() instanceof EmptyPart) {
                    iterator.remove();
                } else {
                    break;
                }
            }
        }

        // Determine the maximum amount of required parameters of one of the two part lists
        final long firstRequiredParameters = firstParts.stream().filter(part -> part instanceof ParameterPart && !((ParameterPart) part).isOptional()).count();
        final long secondRequiredParameters = secondParts.stream().filter(part -> part instanceof ParameterPart && !((ParameterPart) part).isOptional()).count();
        final long limit = Math.max(firstRequiredParameters, secondRequiredParameters);

        // Remove any surplus parameters from both lists
        firstParts.removeAll(
                firstParts.stream()
                        .filter(part -> part instanceof ParameterPart)
                        .skip(limit)
                        .collect(Collectors.toList())
        );
        secondParts.removeAll(
                secondParts.stream()
                        .filter(part -> part instanceof ParameterPart)
                        .skip(limit)
                        .collect(Collectors.toList())
        );

        // If the remaining lists don't equal in length, they cannot interfere each other
        if (firstParts.size() != secondParts.size()) {
            return false;
        }

        // Compare every single part of both lists
        for (int i = 0; i < firstParts.size(); i++) {
            final PathPart firstPart = firstParts.get(i);
            final PathPart secondPart = secondParts.get(i);

            final boolean matches;
            if (settings.doIgnoreCase() && firstPart instanceof LiteralPart && secondPart instanceof LiteralPart) {
                matches = ((LiteralPart) firstPart).matchesCaseInsensitive(secondPart);
            } else {
                matches = firstPart.matches(secondPart);
            }

            if (!matches) {
                return false;
            }
        }

        return true;
    }

}
