package pm.lus.spaceship.routing;

import pm.lus.spaceship.routing.definition.endpoint.EndpointDefinition;
import pm.lus.spaceship.routing.definition.middleware.MiddlewareDefinition;

import java.util.List;

/**
 * Represents an execution chain assembled by the {@link Router}
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class ExecutionChain {

    private final List<MiddlewareDefinition> middlewaresBeforeEndpoint;
    private final EndpointDefinition endpoint;
    private final List<MiddlewareDefinition> middlewaresAfterEndpoint;

    ExecutionChain(final List<MiddlewareDefinition> middlewaresBeforeEndpoint, final EndpointDefinition endpoint, final List<MiddlewareDefinition> middlewaresAfterEndpoint) {
        this.middlewaresBeforeEndpoint = middlewaresBeforeEndpoint;
        this.endpoint = endpoint;
        this.middlewaresAfterEndpoint = middlewaresAfterEndpoint;
    }

    public List<MiddlewareDefinition> getMiddlewaresBeforeEndpoint() {
        return this.middlewaresBeforeEndpoint;
    }

    public EndpointDefinition getEndpoint() {
        return this.endpoint;
    }

    public List<MiddlewareDefinition> getMiddlewaresAfterEndpoint() {
        return this.middlewaresAfterEndpoint;
    }

}
