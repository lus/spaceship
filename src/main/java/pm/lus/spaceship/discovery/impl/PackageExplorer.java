package pm.lus.spaceship.discovery.impl;

import org.reflections.Reflections;
import pm.lus.spaceship.controller.Controller;
import pm.lus.spaceship.discovery.Explorer;
import pm.lus.spaceship.middleware.Middleware;

import java.util.Set;

/**
 * Implements the {@link Explorer} interface using {@link Reflections} to provide a way to find middleware and controller classes using a package name
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class PackageExplorer implements Explorer {

    private final Reflections reflections;

    public PackageExplorer(final String packageName) {
        this.reflections = new Reflections(packageName);
    }

    @Override
    public Set<Class<? extends Middleware>> findMiddlewares() {
        return this.reflections.getSubTypesOf(Middleware.class);
    }

    @Override
    public Set<Class<? extends Controller>> findControllers() {
        return this.reflections.getSubTypesOf(Controller.class);
    }

}
