package pm.lus.spaceship.dependency.annotation;

import java.lang.annotation.*;

/**
 * Marks a field as a dependency the {@link pm.lus.spaceship.dependency.DependencyInjector} should inject
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Dependency {

    // An optional namespace to take the dependency from
    String value() default "";

}
