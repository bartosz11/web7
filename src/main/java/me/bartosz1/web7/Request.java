package me.bartosz1.web7;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A class representing incoming HTTP requests. Read-only.
 */
public class Request {

    private final HttpRequestMethod requestMethod;
    private final String contextPath;
    private final String protocol;
    private final Map<String, String> headers;
    private final byte[] body;
    private final WebEndpointData endpointData;
    private final Map<String, String> requestParams;
    private final Map<String, String> pathVariables;
    private final String rawRequest;
    private final InetAddress ipAddress;

    public Request(HttpRequestMethod requestMethod, String contextPath, String protocol, Map<String, String> headers, byte[] body, WebEndpointData endpointData, Map<String, String> requestParams, Map<String, String> pathVariables, String rawRequest, InetAddress ipAddress) {
        this.requestMethod = requestMethod;
        this.contextPath = contextPath;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
        this.endpointData = endpointData;
        this.requestParams = requestParams;
        this.pathVariables = pathVariables;
        this.rawRequest = rawRequest;
        this.ipAddress = ipAddress;
    }

    /**
     * @return HTTP method used to make the request
     */
    public HttpRequestMethod getRequestMethod() {
        return requestMethod;
    }

    /**
     * @return Full path the client used to make the request, for example "/1/greet" when endpoint is "/$id/greet"
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * @return Protocol used to make the request. web7 supports only HTTP version 1 at the moment.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @return A map of headers sent by the client
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @return Request body as byte array, useful when dealing with files
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * @return Data about the endpoint request was made to. Can be null if none of the endpoints match context path client used.
     */
    public WebEndpointData getEndpointData() {
        return endpointData;
    }

    /**
     * @return A map of URL query parameters
     */
    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    /**
     * @return A map of path variables
     */
    public Map<String, String> getPathVariables() {
        return pathVariables;
    }

    /**
     * @return Raw HTTP request including body. Body might not be the same as the actual body if encoded.
     */
    public String getRawRequest() {
        return rawRequest;
    }

    /**
     * @return IP address client used to send the request.
     */
    public InetAddress getIPAddress() {
        return ipAddress;
    }

    /**
     * @return Request body as a String.
     */
    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    /**
     * @param name Name of the header
     * @return Value of the header with specified name or null if not found
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * @return Value of "Content-Type" header or null if client didn't include it
     */
    public String getContentType() {
        return headers.get("Content-Type");
    }
}
