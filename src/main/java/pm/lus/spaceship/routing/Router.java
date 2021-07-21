package pm.lus.spaceship.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pm.lus.spaceship.middleware.Middleware;
import pm.lus.spaceship.request.HttpRequest;
import pm.lus.spaceship.routing.definition.controller.ControllerDefinition;
import pm.lus.spaceship.routing.definition.endpoint.EndpointDefinition;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterAdapterRegistry;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterDefinition;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterParseException;
import pm.lus.spaceship.routing.definition.endpoint.path.PathDefinition;
import pm.lus.spaceship.routing.definition.middleware.MiddlewareDefinition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bundles the discovered middlewares and controllers, reads out endpoints and helps to build execution chains
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class Router {

    private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

    private final RouterSettings settings;
    private final ParameterAdapterRegistry adapters;

    private final Set<MiddlewareDefinition> middlewares;
    private final Set<ControllerDefinition> controllers;
    private final Set<EndpointDefinition> endpoints;

    private final Set<EndpointDefinition> usedEndpoints;
    private final Map<Integer, List<EndpointDefinition>> sortedEndpoints;

    Router(
            final RouterSettings settings,
            final ParameterAdapterRegistry adapters,
            final Set<MiddlewareDefinition> middlewares,
            final Set<ControllerDefinition> controllers,
            final Set<EndpointDefinition> endpoints) {
        this.settings = settings;
        this.adapters = adapters;

        this.middlewares = middlewares;
        this.controllers = controllers;
        this.endpoints = endpoints;

        this.usedEndpoints = new HashSet<>();
        this.sortedEndpoints = new HashMap<>();
    }

    /**
     * Sorts all endpoints depending on their individual parameter weight
     * This is mandatory for parameter parsing to work properly
     * This should be done whenever a new {@link pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterAdapter} is registered
     */
    public void sortEndpoints() {
        this.usedEndpoints.clear();
        this.sortedEndpoints.clear();

        this.endpoints.forEach(endpoint -> {
            // Check if all used parameters have a registered type adapter
            final Optional<ParameterDefinition> unregisteredParameter = endpoint.getParameters().stream()
                    .filter(parameter -> this.adapters.get(parameter.getType()).isEmpty())
                    .findFirst();
            if (unregisteredParameter.isPresent()) {
                LOGGER.error(
                        "parameter type '{}' in endpoint handler '{}#{}' has no registered type adapter; skipping endpoint!",
                        unregisteredParameter.get().getType().getName(),
                        endpoint.getMethod().getDeclaringClass().getName(),
                        endpoint.getMethod().getName()
                );
                return;
            }

            // Add this endpoint to the set of used endpoints
            this.usedEndpoints.add(endpoint);

            // Add the endpoint to every list it belongs to
            for (int i = 0; i < endpoint.getParameters().size(); i++) {
                if (!this.sortedEndpoints.containsKey(i)) {
                    this.sortedEndpoints.put(i, new ArrayList<>());
                }
                this.sortedEndpoints.get(i).add(endpoint);
            }
        });

        // Sort every list
        this.sortedEndpoints.forEach((parameterIndex, definitions) ->
                definitions.sort((first, second) -> {
                    final Class<?> firstType = first.getParameters().get(parameterIndex).getType();
                    final Class<?> secondType = second.getParameters().get(parameterIndex).getType();
                    return this.adapters.get(firstType).get().compareTo(this.adapters.get(secondType).get());
                })
        );
    }

    /**
     * Tries to build an execution chain using a received {@link HttpRequest}
     *
     * @param request The received HTTP request
     * @return The optional execution chain if an endpoint handler was found
     */
    public Optional<ExecutionChain> routeRequest(final HttpRequest request) {
        // Check if an endpoint matches the requested route
        final Optional<EndpointDefinition> optionalEndpoint = this.findEndpoint(request);
        if (optionalEndpoint.isEmpty()) {
            return Optional.empty();
        }
        final EndpointDefinition endpoint = optionalEndpoint.get();

        // Assemble and sort the middleware phases
        final List<MiddlewareDefinition> middlewaresBeforeEndpoint = new ArrayList<>();
        final List<MiddlewareDefinition> middlewaresAfterEndpoint = new ArrayList<>();
        for (final MiddlewareDefinition middleware : this.middlewares) {
            final Class<? extends Middleware> type = middleware.getInstance().getClass();
            if ((middleware.shouldRunByDefault() && !endpoint.getBlockedMiddlewares().contains(type)) || endpoint.getMiddlewares().contains(type)) {
                switch (middleware.getPhase()) {
                    case BEFORE_ENDPOINT:
                        middlewaresBeforeEndpoint.add(middleware);
                    case AFTER_ENDPOINT:
                        middlewaresAfterEndpoint.add(middleware);
                }
            }
        }
        middlewaresBeforeEndpoint.sort(Comparator.comparingInt(middlewareDefinition -> middlewareDefinition.getChainPosition().getRepresentation()));
        middlewaresAfterEndpoint.sort(Comparator.comparingInt(middlewareDefinition -> middlewareDefinition.getChainPosition().getRepresentation()));

        return Optional.of(new ExecutionChain(middlewaresBeforeEndpoint, endpoint, middlewaresAfterEndpoint));
    }

    private Optional<EndpointDefinition> findEndpoint(final HttpRequest request) {
        final Map<EndpointDefinition, PathDefinition.ProcessingResult> processingResults = new HashMap<>();

        // Find all endpoints that match the requested path
        for (final EndpointDefinition endpoint : this.usedEndpoints) {
            if (!endpoint.getMethods().contains(request.getMethod())) {
                continue;
            }

            final PathDefinition.ProcessingResult result = endpoint.getPath().process(
                    request.getRequestURI().getRawPath(),
                    this.settings.doIgnoreTrailingSlashes(),
                    this.settings.doIgnoreStackedSlashes(),
                    this.settings.doIgnoreCase()
            );

            if (result.matches()) {
                // An endpoint with no parameters is immediately used to allow overloading
                if (endpoint.getParameters().isEmpty()) {
                    return Optional.of(endpoint);
                }

                processingResults.put(endpoint, result);
            }
        }

        // No endpoint matched the requested path
        if (processingResults.isEmpty()) {
            return Optional.empty();
        }

        // Try to substitute the endpoint handler to call while parsing the parameters in the correct order
        final Map<EndpointDefinition, List<Object>> parsedParameters = new HashMap<>();
        int parameterIndex = 0;
        while (processingResults.size() > 1) {
            boolean foundOne = false;
            if (this.sortedEndpoints.containsKey(parameterIndex)) {
                for (final EndpointDefinition definition : this.sortedEndpoints.get(parameterIndex)) {
                    if (!processingResults.containsKey(definition)) {
                        continue;
                    }

                    // Check if a parameter type adapter is registered for the parameter handled in this cycle
                    final Optional<ParameterAdapterRegistry.Entry> adapter = this.adapters.get(definition.getParameters().get(parameterIndex).getType());
                    if (adapter.isEmpty()) {
                        processingResults.remove(definition);
                        continue;
                    }

                    // Try to parse the parameter using the found adapter
                    try {
                        final Object parsed = adapter.get().getAdapter().parse(processingResults.get(definition).getParameters().get(parameterIndex));
                        if (!parsedParameters.containsKey(definition)) {
                            parsedParameters.put(definition, new ArrayList<>());
                        }
                        parsedParameters.get(definition).add(parsed);
                        foundOne = true;
                    } catch (final ParameterParseException exception) {
                        processingResults.remove(definition);
                    }
                }
            }

            // If a cycle ends and results in no found matching endpoint handler, it means that we cannot identify one unique one to call
            if (!foundOne) {
                LOGGER.error(
                        "could not uniquely identify one endpoint; check [{}]",
                        processingResults.keySet().stream()
                                .map(definition -> definition.getMethod().getDeclaringClass().getName() + "#" + definition.getMethod().getName())
                                .collect(Collectors.joining(", "))
                );
                return Optional.empty();
            }

            // Go on with the next parameter
            parameterIndex++;
        }

        return processingResults.keySet().stream().findFirst();
    }

}
