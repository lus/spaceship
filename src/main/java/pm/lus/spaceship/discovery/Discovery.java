package pm.lus.spaceship.discovery;

import pm.lus.spaceship.discovery.impl.PackageExplorer;
import pm.lus.spaceship.discovery.impl.StaticExplorer;
import pm.lus.spaceship.endpoint.Controller;
import pm.lus.spaceship.middleware.Middleware;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides helper method to access the bundled {@link Explorer} implementations more intuitive
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class Discovery {

    private Discovery() {
    }

    /**
     * Creates a new explorer that explores a package for middleware and controller classes
     *
     * @param packageName The name of the package to explore
     * @return The created explorer
     */
    public static Explorer packageDiscovery(final String packageName) {
        return new PackageExplorer(packageName);
    }

    /**
     * Creates a new explorer with static middleware and controller class types
     * Invalid class types (neither {@link Middleware} nor {@link Controller}) will be silently omitted
     *
     * @param types The class types to use
     * @return The created explorer
     */
    public static Explorer staticDiscovery(final Class<?>... types) {
        final Set<Class<? extends Middleware>> middlewares = new HashSet<>();
        final Set<Class<? extends Controller>> controllers = new HashSet<>();

        for (final Class<?> type : types) {
            if (Middleware.class.isAssignableFrom(type)) {
                middlewares.add((Class<? extends Middleware>) type);
            }
            if (Controller.class.isAssignableFrom(type)) {
                controllers.add((Class<? extends Controller>) type);
            }
        }

        return new StaticExplorer(middlewares, controllers);
    }

}
