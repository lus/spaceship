package pm.lus.spaceship.endpoint.annotation.path;

import pm.lus.spaceship.request.meta.RequestMethod;

import java.lang.annotation.*;

/**
 * Introduces a path with one or more request methods
 * If no method is given, the default value (all methods) will be used
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@PathAnnotation
public @interface Path {

    // The path to introduce
    String value();

    // The request methods to listen to
    RequestMethod[] methods() default {
            RequestMethod.GET,
            RequestMethod.HEAD,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.CONNECT,
            RequestMethod.OPTIONS,
            RequestMethod.TRACE,
            RequestMethod.PATCH
    };

}
