package pm.lus.spaceship.middleware.annotation;

import java.lang.annotation.*;

/**
 * Applies additional options to a middleware implementation
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MiddlewareOptions {

    // Whether or not to run this middleware in combination with every endpoint that does not explicitly block it
    boolean runByDefault() default false;

    // Whether or not to run this middleware in combination with a cancelled context; allows context resumption
    boolean forced() default false;

    // The phase to execute this middleware (BEFORE or AFTER the endpoint)
    Phase phase() default Phase.BEFORE_ENDPOINT;

    // The position in the chain this middleware should be placed in
    ChainPosition chainPosition() default ChainPosition.NEUTRAL;

    enum Phase {
        BEFORE_ENDPOINT,
        AFTER_ENDPOINT
    }

    enum ChainPosition {

        VERY_BEGINNING(0),
        BEGINNING(1),
        NEUTRAL(2),
        END(3),
        VERY_END(4);

        private final int representation;

        ChainPosition(final int representation) {
            this.representation = representation;
        }

        public int getRepresentation() {
            return this.representation;
        }

    }

}
