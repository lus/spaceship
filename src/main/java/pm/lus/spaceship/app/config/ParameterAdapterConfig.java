package pm.lus.spaceship.app.config;

import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterAdapter;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterAdapterRegistry;

/**
 * Helps configuring the {@link ParameterAdapterRegistry} by wrapping it using the builder state
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class ParameterAdapterConfig {

    private final ParameterAdapterRegistry adapterRegistry;

    public ParameterAdapterConfig() {
        this.adapterRegistry = new ParameterAdapterRegistry();
    }

    /**
     * Registers a new parameter type adapter
     *
     * @param type    The type to be adapted
     * @param adapter The corresponding type adapter
     * @param weight  The weight of the adapter (required for the endpoint handler mapping)
     * @param <T>     The generic type
     * @return The new builder state
     */
    public <T> ParameterAdapterConfig register(final Class<? extends T> type, final ParameterAdapter<T> adapter, final int weight) {
        this.adapterRegistry.register(type, adapter, weight);
        return this;
    }

    /**
     * Removes a type adapter entry
     *
     * @param type The type to remove the adapter entry from
     * @return The new builder state
     */
    public ParameterAdapterConfig unregister(final Class<?> type) {
        this.adapterRegistry.unregister(type);
        return this;
    }

    public ParameterAdapterRegistry getAdapterRegistry() {
        return this.adapterRegistry;
    }

}
