package pm.lus.spaceship.routing.definition.controller;

import pm.lus.spaceship.controller.Controller;
import pm.lus.spaceship.controller.annotation.ControllerOptions;
import pm.lus.spaceship.routing.Router;
import pm.lus.spaceship.routing.definition.DefinitionBuildingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

    public static ControllerDefinition build(final Class<? extends Controller> clazz) throws DefinitionBuildingException {
        // Try to instantiate the controller class
        final Controller instance;
        try {
            final Constructor<? extends Controller> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new DefinitionBuildingException("no empty constructor present to use for instantiation");
        }

        // Apply addition options to the controller if the corresponding annotation is present
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
