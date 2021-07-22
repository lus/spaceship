package pm.lus.spaceship.dependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pm.lus.spaceship.dependency.annotation.Dependency;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of dependencies to inject and provides methods to register and inject them
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class DependencyInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyInjector.class);

    private final Map<String, Map<Class<?>, Object>> dependencies;

    public DependencyInjector() {
        this.dependencies = new HashMap<>();
    }

    /**
     * Registers a new dependency to inject
     *
     * @param namespace The namespace to register the dependency in
     * @param type      The type of the dependency the field has to match
     * @param instance  The instance to inject
     * @param <T>       The type of the dependency the field has to match
     */
    public <T> void register(final String namespace, final Class<? extends T> type, final T instance) {
        if (!this.dependencies.containsKey(namespace)) {
            this.dependencies.put(namespace, new HashMap<>());
        }
        this.dependencies.get(namespace).put(type, instance);
    }

    /**
     * Registers a new dependency to inject
     * The root namespace is used by this method
     *
     * @param type     The type of the dependency the field has to match
     * @param instance The instance to inject
     * @param <T>      The type of the dependency the field has to match
     */
    public <T> void register(final Class<? extends T> type, final T instance) {
        this.register("", type, instance);
    }

    /**
     * Registers a new dependency to inject
     *
     * @param namespace The namespace to register the dependency in
     * @param instance  The instance to inject
     */
    public void register(final String namespace, final Object instance) {
        this.register(namespace, instance.getClass(), instance);
    }

    /**
     * Registers a new dependency to inject
     * The root namespace is used by this method
     *
     * @param instance The instance to inject
     */
    public void register(final Object instance) {
        this.register(instance.getClass(), instance);
    }

    /**
     * Performs dependency injection using all currently registered dependencies
     *
     * @param instance The instance to inject the dependencies to
     */
    public void inject(final Object instance) {
        for (final Field field : instance.getClass().getDeclaredFields()) {
            // Only fields with the '@Dependency' annotation are respected
            if (!field.isAnnotationPresent(Dependency.class)) {
                continue;
            }

            // Dependencies may live in another namespace than the root one to allow different instances of the same type
            final String namespace = field.getAnnotation(Dependency.class).value();
            if (!this.dependencies.containsKey(namespace) || !this.dependencies.get(namespace).containsKey(field.getType())) {
                LOGGER.warn(
                        "could not inject dependency '{}' in class '{}' because no corresponding instance was registered",
                        field.getName(),
                        instance.getClass().getName()
                );
                continue;
            }

            // Try to set the instance into the field
            try {
                field.setAccessible(true);
                field.set(instance, this.dependencies.get(namespace).get(field.getType()));
            } catch (final IllegalAccessException exception) {
                LOGGER.error(String.format("could not inject dependency '%s' in class '%s'", field.getName(), instance.getClass().getName()), exception);
            }
        }
    }

}
