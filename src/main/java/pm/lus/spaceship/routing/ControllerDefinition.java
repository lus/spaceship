package pm.lus.spaceship.routing;

import pm.lus.spaceship.endpoint.Controller;
import pm.lus.spaceship.endpoint.annotation.ControllerOptions;

/**
 * Represents a definition of an endpoint controller for the {@link Router} to work with
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class ControllerDefinition {

    private final Controller instance;
    private final String basePath;

    private ControllerDefinition(final Controller instance, final String basePath) {
        this.instance = instance;
        this.basePath = basePath;
    }

    protected static ControllerDefinition build(final Controller instance) {
        final Class<? extends Controller> clazz = instance.getClass();
        if (clazz.isAnnotationPresent(ControllerOptions.class)) {
            final ControllerOptions annotation = clazz.getDeclaredAnnotation(ControllerOptions.class);
            return new ControllerDefinition(
                    instance,
                    annotation.basePath()
            );
        }

        return new ControllerDefinition(instance, "");
    }

    public Controller getInstance() {
        return this.instance;
    }

    public String getBasePath() {
        return this.basePath;
    }

}
