package pm.lus.spaceship.endpoint.annotation.path;

import pm.lus.spaceship.request.meta.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides some helper methods to work with path annotations as annotations may not implement interfaces (sadly)
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class PathAnnotations {

    // Look up and cache the value methods of the path annotations once at startup for performance reasons
    private static final Map<Class<? extends Annotation>, Method> VALUE_FIELDS = new ConcurrentHashMap<>() {
        {
            try {
                this.put(Path.class, Path.class.getDeclaredMethod("value"));
                this.put(Get.class, Get.class.getDeclaredMethod("value"));
                this.put(Head.class, Head.class.getDeclaredMethod("value"));
                this.put(Post.class, Post.class.getDeclaredMethod("value"));
                this.put(Put.class, Put.class.getDeclaredMethod("value"));
                this.put(Delete.class, Delete.class.getDeclaredMethod("value"));
                this.put(Connect.class, Connect.class.getDeclaredMethod("value"));
                this.put(Options.class, Options.class.getDeclaredMethod("value"));
                this.put(Trace.class, Trace.class.getDeclaredMethod("value"));
                this.put(Patch.class, Patch.class.getDeclaredMethod("value"));
            } catch (final NoSuchMethodException ignored) {
            }
        }
    };

    private PathAnnotations() {
    }

    public static boolean isPathAnnotation(final Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(PathAnnotation.class);
    }

    public static String getPath(final Annotation annotation) {
        String path = "";
        if (VALUE_FIELDS.containsKey(annotation.annotationType())) {
            try {
                path = (String) VALUE_FIELDS.get(annotation.annotationType()).invoke(annotation);
            } catch (final InvocationTargetException | IllegalAccessException ignored) {
            }
        }
        return path;
    }

    public static RequestMethod[] getRequestMethods(final Annotation annotation) {
        if (annotation instanceof Path) {
            return ((Path) annotation).methods();
        }
        if (annotation instanceof Get) {
            return new RequestMethod[]{RequestMethod.GET};
        }
        if (annotation instanceof Head) {
            return new RequestMethod[]{RequestMethod.HEAD};
        }
        if (annotation instanceof Post) {
            return new RequestMethod[]{RequestMethod.POST};
        }
        if (annotation instanceof Put) {
            return new RequestMethod[]{RequestMethod.PUT};
        }
        if (annotation instanceof Delete) {
            return new RequestMethod[]{RequestMethod.DELETE};
        }
        if (annotation instanceof Connect) {
            return new RequestMethod[]{RequestMethod.CONNECT};
        }
        if (annotation instanceof Options) {
            return new RequestMethod[]{RequestMethod.OPTIONS};
        }
        if (annotation instanceof Trace) {
            return new RequestMethod[]{RequestMethod.TRACE};
        }
        if (annotation instanceof Patch) {
            return new RequestMethod[]{RequestMethod.PATCH};
        }

        return new RequestMethod[]{};
    }

}
