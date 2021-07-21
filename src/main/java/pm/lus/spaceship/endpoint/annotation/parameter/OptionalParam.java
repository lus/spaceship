package pm.lus.spaceship.endpoint.annotation.parameter;

import java.lang.annotation.*;

/**
 * Marks an endpoint parameter as optional and provides the possibility to define a default value for it
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OptionalParam {

    // The default string representation of the optional parameter
    String value() default "";

}
