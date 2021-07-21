package pm.lus.spaceship.routing.definition.middleware;

import pm.lus.spaceship.middleware.Middleware;
import pm.lus.spaceship.middleware.annotation.MiddlewareOptions;
import pm.lus.spaceship.routing.Router;
import pm.lus.spaceship.routing.definition.DefinitionBuildingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Represents a definition of a middleware for the {@link Router} to work with
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class MiddlewareDefinition {

    private final Middleware instance;
    private final boolean runByDefault;
    private final String[] pathBoundaries;
    private final boolean forced;
    private final MiddlewareOptions.Phase phase;
    private final MiddlewareOptions.ChainPosition chainPosition;

    private MiddlewareDefinition(
            final Middleware instance,
            final boolean runByDefault,
            final String[] pathBoundaries,
            final boolean forced,
            final MiddlewareOptions.Phase phase,
            final MiddlewareOptions.ChainPosition chainPosition
    ) {
        this.instance = instance;
        this.runByDefault = runByDefault;
        this.pathBoundaries = pathBoundaries;
        this.forced = forced;
        this.phase = phase;
        this.chainPosition = chainPosition;
    }

    public static MiddlewareDefinition build(final Class<? extends Middleware> clazz) throws DefinitionBuildingException {
        // Try to instantiate the middleware class
        final Middleware instance;
        try {
            final Constructor<? extends Middleware> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new DefinitionBuildingException("no empty constructor present to use for instantiation");
        }

        // Apply addition options to the middleware if the corresponding annotation is present
        if (clazz.isAnnotationPresent(MiddlewareOptions.class)) {
            final MiddlewareOptions options = clazz.getDeclaredAnnotation(MiddlewareOptions.class);
            new MiddlewareDefinition(
                    instance,
                    options.runByDefault(),
                    options.pathBoundaries(),
                    options.forced(),
                    options.phase(),
                    options.chainPosition()
            );
        }

        return new MiddlewareDefinition(
                instance,
                false,
                new String[]{},
                false,
                MiddlewareOptions.Phase.BEFORE_ENDPOINT,
                MiddlewareOptions.ChainPosition.NEUTRAL
        );
    }

    public Middleware getInstance() {
        return this.instance;
    }

    public boolean shouldRunByDefault() {
        return this.runByDefault;
    }

    public String[] getPathBoundaries() {
        return this.pathBoundaries;
    }

    public boolean isForced() {
        return this.forced;
    }

    public MiddlewareOptions.Phase getPhase() {
        return this.phase;
    }

    public MiddlewareOptions.ChainPosition getChainPosition() {
        return this.chainPosition;
    }

}
