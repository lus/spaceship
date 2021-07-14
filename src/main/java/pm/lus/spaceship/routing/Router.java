package pm.lus.spaceship.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pm.lus.spaceship.endpoint.Controller;
import pm.lus.spaceship.middleware.Middleware;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Bundles the discovered middlewares and controllers, reads out endpoints and helps to build execution chains
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class Router {

    private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

    private final Set<MiddlewareDefinition> middlewares;
    private final Set<ControllerDefinition> controllers;
    private final Set<EndpointDefinition> endpoints;

    private Router(final Set<MiddlewareDefinition> middlewares, final Set<ControllerDefinition> controllers, final Set<EndpointDefinition> endpoints) {
        this.middlewares = middlewares;
        this.controllers = controllers;
        this.endpoints = endpoints;
    }

    /**
     * Builds middleware, controller and endpoint definitions using the base classes and creates a new router using them
     *
     * @param middlewares The middleware base classes to use
     * @param controllers The controller base classes to use
     * @return The created router
     */
    public static Router initialize(final Set<Class<? extends Middleware>> middlewares, final Set<Class<? extends Controller>> controllers) {
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
            for (final Method method : controllerDefinition.getInstance().getClass().getDeclaredMethods()) {
                final EndpointDefinition definition = EndpointDefinition.build(controllerDefinition, method);
                if (definition != null) {
                    endpointDefinitions.add(definition);
                }
            }
        });

        return new Router(middlewareDefinitions, controllerDefinitions, endpointDefinitions);
    }

    // TODO: Implement execution chain building

}
