package pm.lus.spaceship.server.impl;

import com.sun.net.httpserver.HttpExchange;
import pm.lus.spaceship.request.HttpRequest;
import pm.lus.spaceship.request.RequestMethod;
import pm.lus.spaceship.server.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Implements the {@link HttpServer} interface using the default {@link com.sun.net.httpserver.HttpServer}
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class DefaultHttpServer implements HttpServer {

    private com.sun.net.httpserver.HttpServer server;
    private final Map<HttpRequest, HttpExchange> requests;

    private final List<Consumer<HttpRequest>> subscribers;

    public DefaultHttpServer() {
        this.requests = new ConcurrentHashMap<>();
        this.subscribers = new ArrayList<>();
    }

    @Override
    public void start(final InetSocketAddress address) throws IOException {
        this.server = com.sun.net.httpserver.HttpServer.create(address, 0);
        this.server.start();
        this.server.createContext("/", this::acceptRequest);
    }

    @Override
    public void stop() {
        this.server.stop(0);
    }

    @Override
    public void accept(final Consumer<HttpRequest> subscriber) {
        this.subscribers.add(subscriber);
    }

    @Override
    public void respond(final HttpRequest request) throws IOException {
        final HttpExchange exchange = this.requests.get(request);
        if (exchange == null) {
            throw new NullPointerException("no mapped http exchange found");
        }

        final byte[] responseBody = request.getResponseBody();
        exchange.sendResponseHeaders(request.getResponseStatus(), responseBody.length);
        exchange.getResponseBody().write(responseBody);
    }

    private void acceptRequest(final HttpExchange exchange) {
        final HttpRequest request = new HttpRequest(
                RequestMethod.valueOf(exchange.getRequestMethod()),
                exchange.getProtocol(),
                exchange.getLocalAddress(),
                exchange.getRemoteAddress(),
                exchange.getRequestURI(),
                exchange.getRequestHeaders(),
                exchange.getRequestBody(),
                exchange.getResponseCode(),
                exchange.getResponseHeaders(),
                new byte[]{32}
        );
        this.requests.put(request, exchange);
        this.subscribers.forEach(subscriber -> subscriber.accept(request));
    }

}
