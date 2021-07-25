package pm.lus.spaceship.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pm.lus.spaceship.middleware.Middleware;
import pm.lus.spaceship.request.HttpRequest;
import pm.lus.spaceship.routing.definition.controller.ControllerDefinition;
import pm.lus.spaceship.routing.definition.endpoint.EndpointDefinition;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterDefinition;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterParseException;
import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.ParameterAdapter;
import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.ParameterAdapterRegistry;
import pm.lus.spaceship.routing.definition.endpoint.path.PathDefinition;
import pm.lus.spaceship.routing.definition.middleware.MiddlewareDefinition;
import pm.lus.spaceship.util.collection.Pair;

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
     * This should be done whenever a new {@link ParameterAdapter} is registered
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
        final Optional<Pair<EndpointDefinition, List<Object>>> optionalEndpoint = this.findEndpoint(request);
        if (optionalEndpoint.isEmpty()) {
            return Optional.empty();
        }
        final EndpointDefinition endpoint = optionalEndpoint.get().getA();

        // Assemble and sort the middleware phases
        final List<MiddlewareDefinition> middlewaresBeforeEndpoint = new ArrayList<>();
        final List<MiddlewareDefinition> middlewaresAfterEndpoint = new ArrayList<>();
        for (final MiddlewareDefinition middleware : this.middlewares) {
            final Class<? extends Middleware> type = middleware.getInstance().getClass();
            if ((middleware.shouldRunByDefault() && !endpoint.getBlockedMiddlewares().contains(type)) || endpoint.getMiddlewares().contains(type)) {
                switch (middleware.getPhase()) {
                    case BEFORE_ENDPOINT:
                        middlewaresBeforeEndpoint.add(middleware);
                        break;
                    case AFTER_ENDPOINT:
                        middlewaresAfterEndpoint.add(middleware);
                }
            }
        }
        middlewaresBeforeEndpoint.sort(Comparator.comparingInt(middlewareDefinition -> middlewareDefinition.getChainPosition().getRepresentation()));
        middlewaresAfterEndpoint.sort(Comparator.comparingInt(middlewareDefinition -> middlewareDefinition.getChainPosition().getRepresentation()));

        return Optional.of(new ExecutionChain(middlewaresBeforeEndpoint, endpoint, optionalEndpoint.get().getB(), middlewaresAfterEndpoint));
    }

    private Optional<Pair<EndpointDefinition, List<Object>>> findEndpoint(final HttpRequest request) {
        final Map<EndpointDefinition, List<Object>> matchingEndpoints = this.findMatchingEndpoints(request);

        // No endpoints match
        if (matchingEndpoints.isEmpty()) {
            return Optional.empty();
        }

        // We already determined the only matching endpoint
        if (matchingEndpoints.size() == 1) {
            final Map.Entry<EndpointDefinition, List<Object>> entry = matchingEndpoints.entrySet().stream().findFirst().get();
            return Optional.of(new Pair<>(entry.getKey(), entry.getValue()));
        }

        // Find the endpoint with the highest priority
        int parameter = 0;
        while (matchingEndpoints.size() > 1) {
            boolean parametersLeft = false;
            Integer weightToKeep = null;
            if (this.sortedEndpoints.containsKey(parameter)) {
                for (final EndpointDefinition endpoint : this.sortedEndpoints.get(parameter)) {
                    // Only basically matching endpoints should be checked again
                    if (!matchingEndpoints.containsKey(endpoint)) {
                        continue;
                    }

                    // Only continue if the endpoint defined the current parameter
                    if (parameter >= endpoint.getParameters().size()) {
                        continue;
                    }
                    parametersLeft = true;

                    // Remove the current endpoint from the matching endpoints if its weight is below the highest one of this cycle
                    final int currentWeight = this.adapters.get(endpoint.getParameters().get(parameter).getType()).get().getWeight();
                    if (weightToKeep == null) {
                        weightToKeep = currentWeight;
                    } else if (weightToKeep != currentWeight) {
                        matchingEndpoints.remove(endpoint);
                    }
                }
            }

            // We have no parameters to check left but there are multiple results
            if (!parametersLeft) {
                LOGGER.error(
                        "multiple matching endpoints and no parameter to check left; check '{}'",
                        matchingEndpoints.keySet().stream()
                                .map(endpoint -> endpoint.getMethod().getDeclaringClass().getName() + "#" + endpoint.getMethod().getName())
                                .collect(Collectors.joining(", ")));
                break;
            }

            parameter++;
        }

        return matchingEndpoints.entrySet().stream().findFirst().map(entry -> new Pair<>(entry.getKey(), entry.getValue()));
    }

    private Map<EndpointDefinition, List<Object>> findMatchingEndpoints(final HttpRequest request) {
        final Map<EndpointDefinition, List<Object>> results = new HashMap<>();

        endpoints:
        for (final EndpointDefinition endpoint : this.usedEndpoints) {
            // The endpoint has to receive the used request method
            if (!endpoint.getMethods().contains(request.getMethod())) {
                continue;
            }

            // Process the raw string on the path representation of the endpoint
            final PathDefinition.ProcessingResult result = endpoint.getPath().process(
                    request.getRequestURI().getRawPath(),
                    this.settings.doIgnoreTrailingSlashes(),
                    this.settings.doIgnoreStackedSlashes(),
                    this.settings.doIgnoreCase()
            );
            if (!result.matches()) {
                continue;
            }

            // Literal endpoints immediately match
            if (endpoint.getParameters().isEmpty()) {
                return Collections.singletonMap(endpoint, Collections.emptyList());
            }

            // Try to parse the parameters one by one
            final List<Object> parsedParameters = new ArrayList<>();
            for (int i = 0; i < endpoint.getParameters().size(); i++) {
                final ParameterDefinition parameter = endpoint.getParameters().get(i);

                // We can just add null because the processing method already checks parameter requirements
                if (i >= result.getParameters().size()) {
                    parsedParameters.add(null);
                    continue;
                }

                // Try to find the parameter adapter responsible for the type of the current parameter
                final Optional<ParameterAdapterRegistry.Entry> optionalAdapter = this.adapters.get(parameter.getType());
                if (optionalAdapter.isEmpty()) {
                    continue endpoints;
                }
                final ParameterAdapter<?> adapter = optionalAdapter.get().getAdapter();

                // Try to parse the parameter
                final Object parsed;
                try {
                    parsed = adapter.parse(result.getParameters().get(i));
                } catch (final ParameterParseException ignored) {
                    continue endpoints;
                }
                parsedParameters.add(parsed);
            }

            results.put(endpoint, parsedParameters);
        }

        return results;
    }

}
