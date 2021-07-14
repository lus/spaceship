package pm.lus.spaceship.endpoint.annotation.path;

import java.lang.annotation.*;

/**
 * Marks an annotation as a path annotation
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@interface PathAnnotation {
}
