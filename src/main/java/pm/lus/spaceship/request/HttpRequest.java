package pm.lus.spaceship.request;

import pm.lus.spaceship.request.meta.RequestMethod;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An own implementation of a HTTP request to make multiple server implementations possible
 *
 * @author Lukas Schulte Pelkum
 * @version 0.1.0
 * @since 0.1.0
 */
public class HttpRequest {

    private final RequestMethod method;
    private final String protocol;
    private final InetSocketAddress localAddress;
    private final InetSocketAddress remoteAddress;
    private final URI requestURI;
    private final Map<String, List<String>> requestHeaders;
    private final InputStream requestBody;

    private int responseStatus;
    private final Map<String, List<String>> responseHeaders;
    private byte[] responseBody;

    public HttpRequest(
            final RequestMethod method,
            final String protocol,
            final InetSocketAddress localAddress,
            final InetSocketAddress remoteAddress,
            final URI requestURI,
            final Map<String, List<String>> requestHeaders,
            final InputStream requestBody,
            final int responseStatus,
            final Map<String, List<String>> responseHeaders,
            final byte[] responseBody
    ) {
        this.method = method;
        this.protocol = protocol;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.requestURI = requestURI;
        this.requestHeaders = Collections.unmodifiableMap(requestHeaders);
        this.requestBody = requestBody;
        this.responseStatus = responseStatus;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public RequestMethod getMethod() {
        return this.method;
    }

    public String getProtocol() {
        return this.protocol;
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

    public Map<String, List<String>> getRequestHeaders() {
        return this.requestHeaders;
    }

    public InputStream getRequestBody() {
        return this.requestBody;
    }

    public byte[] readRequestBody() throws IOException {
        return this.requestBody.readAllBytes();
    }

    public int getResponseStatus() {
        return this.responseStatus;
    }

    public void setResponseStatus(final int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return this.responseHeaders;
    }

    public byte[] getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(final byte[] responseBody) {
        this.responseBody = responseBody;
    }

}
