package pm.lus.spaceship.middleware;

import pm.lus.spaceship.request.context.RequestContext;

/**
 * Represents a middleware called before or after the endpoint
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public interface Middleware {

    /**
     * Applies this middleware on a request context
     *
     * @param context The request context to apply this middleware on
     * @throws Throwable Any implementation-specific exception and/or error; will be delegated to the corresponding handler
     */
    void apply(RequestContext context) throws Throwable;

}
