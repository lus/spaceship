package pm.lus.spaceship.app.config;

import pm.lus.spaceship.dependency.DependencyInjector;

/**
 * Helps configuring the {@link DependencyInjector} by wrapping it using the builder state
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class DependencyInjectionConfig {

    private final DependencyInjector injector;

    public DependencyInjectionConfig() {
        this.injector = new DependencyInjector();
    }

    /**
     * Registers a new dependency to inject
     *
     * @param namespace The namespace to register the dependency in
     * @param type      The type of the dependency the field has to match
     * @param instance  The instance to inject
     * @param <T>       The type of the dependency the field has to match
     * @return The new builder state
     */
    public <T> DependencyInjectionConfig inject(final String namespace, final Class<? extends T> type, final T instance) {
        this.injector.register(namespace, type, instance);
        return this;
    }

    /**
     * Registers a new dependency to inject
     * The root namespace is used by this method
     *
     * @param type     The type of the dependency the field has to match
     * @param instance The instance to inject
     * @param <T>      The type of the dependency the field has to match
     * @return The new builder state
     */
    public <T> DependencyInjectionConfig inject(final Class<? extends T> type, final T instance) {
        this.injector.register(type, instance);
        return this;
    }

    /**
     * Registers a new dependency to inject
     *
     * @param namespace The namespace to register the dependency in
     * @param instance  The instance to inject
     * @return The new builder state
     */
    public DependencyInjectionConfig inject(final String namespace, final Object instance) {
        this.injector.register(namespace, instance);
        return this;
    }

    /**
     * Registers a new dependency to inject
     * The root namespace is used by this method
     *
     * @param instance The instance to inject
     * @return The new builder state
     */
    public DependencyInjectionConfig inject(final Object instance) {
        this.injector.register(instance);
        return this;
    }
    
    public DependencyInjector getInjector() {
        return this.injector;
    }

}
