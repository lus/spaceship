package pm.lus.spaceship.request.context;

import pm.lus.spaceship.request.HttpRequest;
import pm.lus.spaceship.request.meta.RequestMethod;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Provides a way to pass around request and response data across multiple middlewares and the endpoint to allow 'building' the response across the execution chain
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class RequestContext {

    private final RequestMethod method;
    private final InetSocketAddress localAddress;
    private final InetSocketAddress remoteAddress;
    private final URI requestURI;
    private final Map<String, List<String>> requestHeaders;
    private final byte[] requestBody;

    private final Map<String, List<String>> responseHeaders;
    private int responseStatus;
    private byte[] responseBody;
    private final DataContainer locals;

    private boolean cancelled;

    public RequestContext(
            final RequestMethod method,
            final InetSocketAddress localAddress,
            final InetSocketAddress remoteAddress,
            final URI requestURI,
            final Map<String, List<String>> requestHeaders,
            final byte[] requestBody
    ) {
        this.method = method;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.requestURI = requestURI;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;

        this.responseHeaders = new HashMap<>();
        this.responseStatus = 200;
        this.responseBody = new byte[]{};
        this.locals = new DataContainer();

        this.cancelled = false;
    }

    public static RequestContext unmarshal(final HttpRequest request) throws IOException {
        return new RequestContext(
                request.getMethod(),
                request.getLocalAddress(),
                request.getRemoteAddress(),
                request.getRequestURI(),
                request.getRequestHeaders(),
                request.readRequestBody()
        );
    }

    public RequestMethod getMethod() {
        return this.method;
    }

    public InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public URI getRequestURI() {
        return this.requestURI;
    }

    public String getRawPath() {
        return this.requestURI.getRawPath();
    }

    public String getDecodedPath() {
        return this.requestURI.getPath();
    }

    public Map<String, List<String>> getRequestHeaders() {
        return Collections.unmodifiableMap(this.requestHeaders);
    }

    public List<String> getRequestHeaders(final String key) {
        return this.requestHeaders.containsKey(key) ? Collections.unmodifiableList(this.requestHeaders.get(key)) : Collections.emptyList();
    }

    public Optional<String> getRequestHeader(final String key) {
        return this.getRequestHeaders(key).stream().findFirst();
    }

    public byte[] getRequestBody() {
        return this.requestBody;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return this.responseHeaders;
    }

    public RequestContext header(final String key, final String value) {
        if (!this.responseHeaders.containsKey(key)) {
            this.responseHeaders.put(key, new ArrayList<>());
        }
        this.responseHeaders.get(key).add(value);
        return this;
    }

    public int getResponseStatus() {
        return this.responseStatus;
    }

    public RequestContext status(final int status) {
        this.responseStatus = status;
        return this;
    }

    public byte[] getResponseBody() {
        return this.responseBody;
    }

    public RequestContext body(final byte[] body) {
        this.responseBody = body;
        return this;
    }

    public RequestContext body(final String body) {
        return this.body(body.getBytes(StandardCharsets.UTF_8));
    }

    public DataContainer locals() {
        return this.locals;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

}
