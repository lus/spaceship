package pm.lus.spaceship.endpoint.annotation.path;

import java.lang.annotation.*;

/**
 * Introduces a path with the POST request method
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
public @interface Post {

    // The path to introduce
    String value();

}
