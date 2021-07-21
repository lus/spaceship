package pm.lus.spaceship.discovery.impl;

import pm.lus.spaceship.controller.Controller;
import pm.lus.spaceship.discovery.Explorer;
import pm.lus.spaceship.middleware.Middleware;

import java.util.Set;

/**
 * Implements the {@link Explorer} interface to provide a way to use static middleware and controller classes
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class StaticExplorer implements Explorer {

    private final Set<Class<? extends Middleware>> middlewares;
    private final Set<Class<? extends Controller>> controllers;

    public StaticExplorer(final Set<Class<? extends Middleware>> middlewares, final Set<Class<? extends Controller>> controllers) {
        this.middlewares = middlewares;
        this.controllers = controllers;
    }

    @Override
    public Set<Class<? extends Middleware>> findMiddlewares() {
        return this.middlewares;
    }

    @Override
    public Set<Class<? extends Controller>> findControllers() {
        return this.controllers;
    }

}
