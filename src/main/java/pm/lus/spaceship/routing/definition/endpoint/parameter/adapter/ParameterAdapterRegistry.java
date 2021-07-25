package pm.lus.spaceship.routing.definition.endpoint.parameter.adapter;

import pm.lus.spaceship.routing.definition.endpoint.parameter.adapter.impl.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of parameter adapters
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class ParameterAdapterRegistry {

    private final Map<Class<?>, Entry> adapters;

    public ParameterAdapterRegistry() {
        this.adapters = new ConcurrentHashMap<>();

        this.register(String.class, new StringParameterAdapter(), 0);
        this.register(Boolean.class, new BooleanParameterAdapter(), 10);
        this.register(Double.class, new NonInfiniteDoubleParameterAdapter(), 20);
        this.register(Float.class, new NonInfiniteFloatParameterAdapter(), 30);
        this.register(Long.class, new LongParameterAdapter(), 40);
        this.register(Integer.class, new IntegerParameterAdapter(), 50);
        this.register(Short.class, new ShortParameterAdapter(), 60);
    }

    /**
     * Registers a new parameter type adapter
     *
     * @param type    The type to be adapted
     * @param adapter The corresponding type adapter
     * @param weight  The weight of the adapter (required for the endpoint handler mapping)
     * @param <T>     The generic type
     */
    public <T> void register(final Class<? extends T> type, final ParameterAdapter<T> adapter, final int weight) {
        this.adapters.put(type, new Entry(adapter, weight));
    }

    /**
     * Reads a type adapter entry
     *
     * @param type The type to retrieve the adapter entry from
     * @return The optional type adapter entry
     */
    public Optional<Entry> get(final Class<?> type) {
        return Optional.ofNullable(this.adapters.get(type));
    }

    /**
     * Removes a type adapter entry
     *
     * @param type The type to remove the adapter entry from
     * @return The optional previous type adapter entry
     */
    public Optional<Entry> unregister(final Class<?> type) {
        return Optional.ofNullable(this.adapters.remove(type));
    }

    /**
     * Represents an entry of the {@link ParameterAdapterRegistry}
     *
     * @author Lukas Schulte Pelkum
     * @version 0.1.0
     * @since 0.1.0
     */
    public static class Entry implements Comparable<Entry> {

        private final ParameterAdapter<?> adapter;
        private final int weight;

        private Entry(final ParameterAdapter<?> adapter, final int weight) {
            this.adapter = adapter;
            this.weight = weight;
        }

        public ParameterAdapter<?> getAdapter() {
            return this.adapter;
        }

        public <T> ParameterAdapter<T> getAndCastAdapter() {
            return (ParameterAdapter<T>) this.adapter;
        }

        public int getWeight() {
            return this.weight;
        }

        @Override
        public int compareTo(final Entry entry) {
            return Integer.compare(entry.getWeight(), this.weight);
        }

    }

}
