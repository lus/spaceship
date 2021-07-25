package pm.lus.spaceship.app;

import pm.lus.spaceship.app.config.DependencyInjectionConfig;
import pm.lus.spaceship.app.config.DiscoveryConfig;
import pm.lus.spaceship.app.config.ParameterAdapterConfig;
import pm.lus.spaceship.app.config.RouterConfig;
import pm.lus.spaceship.request.context.RequestContext;
import pm.lus.spaceship.request.meta.HttpStatusCode;
import pm.lus.spaceship.request.processing.RequestProcessor;
import pm.lus.spaceship.routing.Router;
import pm.lus.spaceship.routing.RouterFactory;
import pm.lus.spaceship.server.HttpServer;
import pm.lus.spaceship.server.impl.DefaultHttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Represents the spaceship app bootstrapper
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class Spaceship {

    private final HttpServer server;
    private final InetSocketAddress address;
    private final Executor requestExecutor;
    private final Consumer<RequestContext> notFoundHandler;
    private final BiFunction<RequestContext, Throwable, Boolean> throwableHandler;

    private final RouterConfig routerConfig;
    private final DiscoveryConfig discoveryConfig;
    private final DependencyInjectionConfig dependencyInjectionConfig;
    private final ParameterAdapterConfig parameterAdapterConfig;

    private Spaceship(
            final HttpServer server,
            final InetSocketAddress address,
            final Executor requestExecutor,
            final Consumer<RequestContext> notFoundHandler,
            final BiFunction<RequestContext, Throwable, Boolean> throwableHandler
    ) {
        this.server = server;
        this.address = address;
        this.requestExecutor = requestExecutor;
        this.notFoundHandler = notFoundHandler;
        this.throwableHandler = throwableHandler;

        this.routerConfig = new RouterConfig();
        this.discoveryConfig = new DiscoveryConfig();
        this.dependencyInjectionConfig = new DependencyInjectionConfig();
        this.parameterAdapterConfig = new ParameterAdapterConfig();
    }

    /**
     * @return A builder object which helps to build a new spaceship app bootstrapper
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return The router configuration section
     */
    public RouterConfig routing() {
        return this.routerConfig;
    }

    /**
     * @return The discovery configuration section
     */
    public DiscoveryConfig discovery() {
        return this.discoveryConfig;
    }

    /**
     * @return The dependency injection configuration section
     */
    public DependencyInjectionConfig dependencies() {
        return this.dependencyInjectionConfig;
    }

    /**
     * @return The parameter adapter configuration section
     */
    public ParameterAdapterConfig parameterAdapters() {
        return this.parameterAdapterConfig;
    }

    /**
     * Builds and starts the application using the pre-configured data
     *
     * @throws Exception Any {@link HttpServer} implementation specific exception
     */
    public void start() throws Exception {
        this.server.start(this.address, this.requestExecutor);

        final Router router = RouterFactory.create(
                this.routerConfig.getRouterSettings(),
                this.parameterAdapterConfig.getAdapterRegistry(),
                this.dependencyInjectionConfig.getInjector(),
                this.discoveryConfig.findMiddlewares(),
                this.discoveryConfig.findControllers()
        );

        final RequestProcessor requestProcessor = new RequestProcessor(
                this.server,
                router,
                this.notFoundHandler,
                this.throwableHandler
        );
        requestProcessor.setup();
    }

    /**
     * Shuts down the application
     *
     * @throws Exception Any {@link HttpServer} implementation specific exception
     */
    public void shutdown() throws Exception {
        this.server.stop();
    }

    /**
     * Helps to build a {@link Spaceship} app bootstrapper object
     *
     * @author Lukas Schulte Pelkum
     * @version 0.1.0
     * @since 0.1.0
     */
    public static class Builder {

        private HttpServer server;
        private InetSocketAddress address;
        private Executor requestExecutor;
        private Consumer<RequestContext> notFoundHandler;
        private BiFunction<RequestContext, Throwable, Boolean> throwableHandler;

        private Builder() {
            this.server = new DefaultHttpServer();
            this.address = new InetSocketAddress(8080);
            this.requestExecutor = Executors.newCachedThreadPool();
            this.notFoundHandler = ctx -> ctx.status(HttpStatusCode.NOT_FOUND).body("not found");
            this.throwableHandler = (ctx, throwable) -> {
                ctx.status(HttpStatusCode.INTERNAL_SERVER_ERROR).body(throwable.getMessage());
                return false;
            };
        }

        /**
         * Defines a custom {@link HttpServer} implementation to use
         * If not manually changed, the default {@link DefaultHttpServer} implementation (based on {@link com.sun.net.httpserver.HttpServer}) is used
         *
         * @param server The custom HTTP server implementation
         * @return The new builder state
         */
        public Builder server(final HttpServer server) {
            this.server = server;
            return this;
        }

        /**
         * Defines a custom address to listen on
         * If not manually changed, the application listens on port 8080
         *
         * @param address The custom address to listen on
         * @return The new builder state
         */
        public Builder listen(final InetSocketAddress address) {
            this.address = address;
            return this;
        }

        /**
         * Serves as a convenient shortcut for listening on a custom port using {@link Spaceship.Builder#listen(InetSocketAddress)}
         * If not manually changed, the application listens on port 8080
         *
         * @param port The custom port to listen on
         * @return The new builder state
         */
        public Builder listen(final int port) {
            return this.listen(new InetSocketAddress(port));
        }

        /**
         * Defines a custom executor for the {@link HttpServer} to handle requests with
         * If not manually changed, a cached thread pool, which should be perfect for most cases, is used
         *
         * @param requestExecutor The custom executor for the HTTP server to handle with
         * @return The new builder state
         */
        public Builder requestExecutor(final Executor requestExecutor) {
            this.requestExecutor = requestExecutor;
            return this;
        }

        /**
         * Defines a custom handler to execute when no endpoint was found
         * If not manually changed, a simple 404 response saying 'not found' is sent
         *
         * @param notFoundHandler The custom handler to execute when no endpoint was found
         * @return The new builder state
         */
        public Builder notFoundHandler(final Consumer<RequestContext> notFoundHandler) {
            this.notFoundHandler = notFoundHandler;
            return this;
        }

        /**
         * Defines a custom handler to execute when any middleware and/or endpoint threw a {@link Throwable}
         * If not manually changed, a simple 500 response containing the message of the throwable is sent
         *
         * @param throwableHandler The custom handler to execute when any middleware and/or endpoint threw a throwable
         * @return The new builder state
         */
        public Builder throwableHandler(final BiFunction<RequestContext, Throwable, Boolean> throwableHandler) {
            this.throwableHandler = throwableHandler;
            return this;
        }

        /**
         * Builds the app bootstrapper
         *
         * @return The built app bootstrapper
         */
        public Spaceship build() {
            return new Spaceship(
                    this.server,
                    this.address,
                    this.requestExecutor,
                    this.notFoundHandler,
                    this.throwableHandler
            );
        }

    }

}
