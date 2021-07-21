package pm.lus.spaceship.controller.annotation;

import java.lang.annotation.*;

/**
 * Applies additional options to a controller class
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ControllerOptions {

    // The path (sometimes named 'group') the controller will be limited to
    String basePath() default "";

}
