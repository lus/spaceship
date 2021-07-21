package pm.lus.spaceship.routing.definition.endpoint;

import pm.lus.spaceship.endpoint.annotation.path.PathAnnotations;
import pm.lus.spaceship.request.RequestMethod;
import pm.lus.spaceship.request.context.RequestContext;
import pm.lus.spaceship.routing.Router;
import pm.lus.spaceship.routing.definition.DefinitionBuildingException;
import pm.lus.spaceship.routing.definition.controller.ControllerDefinition;
import pm.lus.spaceship.routing.definition.endpoint.parameter.ParameterDefinition;
import pm.lus.spaceship.routing.definition.endpoint.path.PathDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a definition of an endpoint for the {@link Router} to work with
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class EndpointDefinition {

    private final Method method;
    private final PathDefinition path;
    private final List<RequestMethod> methods;

    private EndpointDefinition(
            final Method method,
            final PathDefinition path,
            final List<RequestMethod> methods
    ) {
        this.method = method;
        this.path = path;
        this.methods = methods;
    }

    public static EndpointDefinition build(final ControllerDefinition controllerDefinition, final Method endpoint) throws DefinitionBuildingException {
        // Ignore the method if it does not accept a request context as first parameter
        if (endpoint.getParameterCount() < 1 || endpoint.getParameterTypes()[0] != RequestContext.class) {
            return null;
        }

        // Try to find the path annotation to use
        Annotation annotationToUse = null;
        for (final Annotation annotation : endpoint.getDeclaredAnnotations()) {
            if (PathAnnotations.isPathAnnotation(annotation)) {
                annotationToUse = annotation;
            }
        }

        // Try to build the parameter definitions
        final List<ParameterDefinition> parameters = new ArrayList<>();
        for (final Parameter parameter : Arrays.copyOfRange(endpoint.getParameters(), 1, endpoint.getParameterCount())) {
            try {
                parameters.add(ParameterDefinition.build(parameter));
            } catch (final Exception exception) {
                throw new DefinitionBuildingException(String.format("failed to build parameter definition for parameter '%s'", parameter.getName()), exception);
            }
        }

        // Build the raw path declaration
        final String path = annotationToUse != null
                ? controllerDefinition.getBasePath() + PathAnnotations.getPath(annotationToUse)
                : controllerDefinition.getBasePath();

        // Try to build the path definition
        final PathDefinition pathDefinition;
        try {
            pathDefinition = PathDefinition.build(path, parameters);
        } catch (final Exception exception) {
            throw new DefinitionBuildingException(String.format("failed to build path definition for path '%s'", path), exception);
        }

        // Assemble the accepted request methods
        final List<RequestMethod> methods = annotationToUse != null
                ? Arrays.asList(PathAnnotations.getRequestMethods(annotationToUse))
                : Collections.singletonList(RequestMethod.GET);

        return new EndpointDefinition(
                endpoint,
                pathDefinition,
                methods
        );
    }

    public Method getMethod() {
        return this.method;
    }

    public PathDefinition getPath() {
        return this.path;
    }

    public List<RequestMethod> getMethods() {
        return this.methods;
    }

}
