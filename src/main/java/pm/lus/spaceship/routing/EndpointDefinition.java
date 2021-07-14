package pm.lus.spaceship.routing;

import pm.lus.spaceship.endpoint.annotation.path.PathAnnotations;
import pm.lus.spaceship.request.RequestMethod;
import pm.lus.spaceship.request.context.RequestContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final ArrayList<Class<?>> parameterTypes;
    private final String path;
    private final RequestMethod[] methods;

    private EndpointDefinition(
            final Method method,
            final ArrayList<Class<?>> parameterTypes,
            final String path,
            final RequestMethod[] methods
    ) {
        this.method = method;
        this.parameterTypes = parameterTypes;
        this.path = path;
        this.methods = methods;
    }

    protected static EndpointDefinition build(final ControllerDefinition controllerDefinition, final Method endpoint) {
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
                    new ArrayList<>(Arrays.asList(Arrays.copyOfRange(endpoint.getParameterTypes(), 1, endpoint.getParameterCount()))),
                    controllerDefinition.getBasePath() + PathAnnotations.getPath(annotationToUse),
                    PathAnnotations.getRequestMethods(annotationToUse)
            );
        }

        return new EndpointDefinition(
                endpoint,
                new ArrayList<>(),
                controllerDefinition.getBasePath(),
                new RequestMethod[]{RequestMethod.GET}
        );
    }

    public Method getMethod() {
        return this.method;
    }

    public List<Class<?>> getParameterTypes() {
        return this.parameterTypes;
    }

    public String getPath() {
        return this.path;
    }

    public RequestMethod[] getMethods() {
        return this.methods;
    }

}
