package pm.lus.spaceship.endpoint.annotation;

import pm.lus.spaceship.middleware.Middleware;

import java.lang.annotation.*;

/**
 * Applies additional options to an endpoint method
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EndpointOptions {

    // Middlewares that explicitly should be ran in combination with this endpoint
    Class<? extends Middleware>[] middlewares() default {};

    // Middlewares that explicitly should NOT be ran in combination with this endpoint
    Class<? extends Middleware>[] blockedMiddlewares() default {};

}
