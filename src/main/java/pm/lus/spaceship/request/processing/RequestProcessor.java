package pm.lus.spaceship.request.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pm.lus.spaceship.controller.Controller;
import pm.lus.spaceship.request.HttpRequest;
import pm.lus.spaceship.request.context.RequestContext;
import pm.lus.spaceship.request.meta.HttpStatusCode;
import pm.lus.spaceship.routing.ExecutionChain;
import pm.lus.spaceship.routing.Router;
import pm.lus.spaceship.routing.definition.middleware.MiddlewareDefinition;
import pm.lus.spaceship.server.HttpServer;
import pm.lus.spaceship.util.collection.ArrayUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Receives incoming {@link HttpRequest}s, routes them and goes through the execution chain
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class RequestProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestProcessor.class);

    private final HttpServer server;
    private final Router router;

    private final Map<RequestContext, HttpRequest> requests;

    private final Consumer<RequestContext> notFoundHandler;
    private final BiFunction<RequestContext, Throwable, Boolean> throwableHandler;

    public RequestProcessor(
            final HttpServer server,
            final Router router,
            final Consumer<RequestContext> notFoundHandler,
            final BiFunction<RequestContext, Throwable, Boolean> throwableHandler
    ) {
        this.server = server;
        this.router = router;

        this.notFoundHandler = notFoundHandler;
        this.throwableHandler = throwableHandler;

        this.requests = new HashMap<>();
    }

    /**
     * Sorts all registered endpoints of the router and registers a new request handler on the {@link HttpServer}
     */
    public void setup() {
        this.router.sortEndpoints();
        this.server.accept(request -> {
            try {
                this.acceptRequest(request);
            } catch (final Exception exception) {
                LOGGER.error("error while accepting request", exception);
                request.setResponseStatus(HttpStatusCode.INTERNAL_SERVER_ERROR);
                request.setResponseBody("error while accepting request".getBytes(StandardCharsets.UTF_8));
                try {
                    this.server.respond(request);
                } catch (final Exception inner) {
                    inner.printStackTrace();
                }
            }
        });
    }

    private void acceptRequest(final HttpRequest request) throws IOException {
        final RequestContext context = RequestContext.unmarshal(request);
        this.requests.put(context, request);

        // Try to route the request
        final Optional<ExecutionChain> optionalExecutionChain = this.router.routeRequest(request);
        if (optionalExecutionChain.isEmpty()) {
            this.notFoundHandler.accept(context);
            this.respond(context);
            return;
        }
        final ExecutionChain executionChain = optionalExecutionChain.get();

        // Run all middlewares that should run before the endpoint
        for (final MiddlewareDefinition middleware : executionChain.getMiddlewaresBeforeEndpoint()) {
            // Only continue if the context is still alive OR the middleware forces execution
            if (context.isCancelled() && !middleware.isForced()) {
                continue;
            }

            // Apply the middleware or call the throwable handler otherwise
            try {
                middleware.getInstance().apply(context);
            } catch (final Throwable throwable) {
                context.setCancelled(true);
                if (!this.throwableHandler.apply(context, throwable)) {
                    this.respond(context);
                    return;
                }
            }
        }

        // Try to run the endpoint itself or call the throwable handler otherwise
        try {
            final Controller controller = executionChain.getEndpoint().getControllerInstance();
            final Method method = executionChain.getEndpoint().getMethod();

            final boolean wasAccessible = method.canAccess(controller);
            if (!wasAccessible) {
                method.setAccessible(true);
            }

            final Object[] parameters = ArrayUtils.merge(new Object[]{context}, executionChain.getParameters().toArray());
            method.invoke(controller, parameters);

            if (!wasAccessible) {
                method.setAccessible(false);
            }
        } catch (final IllegalAccessException exception) {
            LOGGER.error("error while calling endpoint", exception);
        } catch (final InvocationTargetException exception) {
            final Throwable cause = exception.getCause();
            context.setCancelled(true);
            if (!this.throwableHandler.apply(context, cause != null ? cause : exception)) {
                this.respond(context);
                return;
            }
        }

        for (final MiddlewareDefinition middleware : executionChain.getMiddlewaresAfterEndpoint()) {
            if (context.isCancelled() && !middleware.isForced()) {
                continue;
            }

            try {
                middleware.getInstance().apply(context);
            } catch (final Throwable throwable) {
                context.setCancelled(true);
                if (!this.throwableHandler.apply(context, throwable)) {
                    this.respond(context);
                    return;
                }
            }
        }

        this.respond(context);
    }

    private void respond(final RequestContext ctx) {
        if (!this.requests.containsKey(ctx)) {
            return;
        }

        final HttpRequest request = this.requests.get(ctx);
        this.requests.remove(ctx);

        request.getResponseHeaders().putAll(ctx.getResponseHeaders());
        request.setResponseStatus(ctx.getResponseStatus());
        request.setResponseBody(ctx.getResponseBody());

        try {
            this.server.respond(request);
        } catch (final Exception exception) {
            LOGGER.error("error while sending response", exception);
        }
    }

}
