package pm.lus.spaceship.server;

import pm.lus.spaceship.request.HttpRequest;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * Represents a generic interface for a HTTP server spaceship is based on
 * This allows the flexible use of completely custom HTTP server implementations for performance and/or usability reasons
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public interface HttpServer {

    /**
     * Asynchronously starts this HTTP server
     *
     * @param address The socket address the server should listen on
     * @throws Exception Any implementation-specific exception during the startup period
     */
    void start(InetSocketAddress address) throws Exception;

    /**
     * Gracefully and synchronously shuts down this HTTP server
     *
     * @throws Exception Any implementation-specific exception during the shutdown period
     */
    void stop() throws Exception;

    /**
     * Adds a new request subscriber to this HTTP server
     *
     * @param subscriber The subscriber to be called whenever a new HTTP request comes in
     */
    void accept(Consumer<HttpRequest> subscriber);

    /**
     * Responds to a given HTTP request
     *
     * @param request The request to respond to
     * @throws Exception Any implementation-specific exception during the response period
     */
    void respond(HttpRequest request) throws Exception;

}
