package pm.lus.spaceship.routing.endpoint.endpoint;

import pm.lus.spaceship.endpoint.annotation.path.PathAnnotations;
import pm.lus.spaceship.request.RequestMethod;
import pm.lus.spaceship.request.context.RequestContext;
import pm.lus.spaceship.routing.Router;
import pm.lus.spaceship.routing.endpoint.controller.ControllerDefinition;
import pm.lus.spaceship.routing.endpoint.parameter.ParameterDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
    private final ParameterDefinition[] parameters;
    private final PathDefinition path;
    private final List<RequestMethod> methods;

    private EndpointDefinition(
            final Method method,
            final ParameterDefinition[] parameters,
            final PathDefinition path,
            final List<RequestMethod> methods
    ) {
        this.method = method;
        this.parameters = parameters;
        this.path = path;
        this.methods = methods;
    }

    public static EndpointDefinition build(final ControllerDefinition controllerDefinition, final Method endpoint) {
        Annotation annotationToUse = null;
        for (final Annotation annotation : endpoint.getDeclaredAnnotations()) {
            if (PathAnnotations.isPathAnnotation(annotation)) {
                annotationToUse = annotation;
            }
        }

        if (endpoint.getParameterCount() < 1 || endpoint.getParameterTypes()[0] != RequestContext.class) {
            return null;
        }

        if (annotationToUse != null) {
            return new EndpointDefinition(
                    endpoint,
                    Arrays.stream(Arrays.copyOfRange(endpoint.getParameters(), 1, endpoint.getParameterCount()))
                            .map(ParameterDefinition::build)
                            .toArray(ParameterDefinition[]::new),
                    PathDefinition.build(controllerDefinition.getBasePath() + PathAnnotations.getPath(annotationToUse)),
                    Arrays.asList(PathAnnotations.getRequestMethods(annotationToUse))
            );
        }

        return new EndpointDefinition(
                endpoint,
                new ParameterDefinition[]{},
                PathDefinition.build(controllerDefinition.getBasePath()),
                Collections.singletonList(RequestMethod.GET)
        );
    }

    public Method getMethod() {
        return this.method;
    }

    public ParameterDefinition[] getParameters() {
        return this.parameters;
    }

    public PathDefinition getPath() {
        return this.path;
    }

    public List<RequestMethod> getMethods() {
        return this.methods;
    }

}
