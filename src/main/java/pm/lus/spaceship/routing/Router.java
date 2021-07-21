package pm.lus.spaceship.routing;

import pm.lus.spaceship.routing.definition.controller.ControllerDefinition;
import pm.lus.spaceship.routing.definition.endpoint.EndpointDefinition;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterAdapterRegistry;
import pm.lus.spaceship.routing.definition.middleware.MiddlewareDefinition;

import java.util.Set;

/**
 * Bundles the discovered middlewares and controllers, reads out endpoints and helps to build execution chains
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class Router {

    private final RouterSettings settings;
    private final ParameterAdapterRegistry adapters;

    private final Set<MiddlewareDefinition> middlewares;
    private final Set<ControllerDefinition> controllers;
    private final Set<EndpointDefinition> endpoints;

    Router(final RouterSettings settings, final ParameterAdapterRegistry adapters, final Set<MiddlewareDefinition> middlewares, final Set<ControllerDefinition> controllers, final Set<EndpointDefinition> endpoints) {
        this.settings = settings;
        this.adapters = adapters;

        this.middlewares = middlewares;
        this.controllers = controllers;
        this.endpoints = endpoints;
    }

}
