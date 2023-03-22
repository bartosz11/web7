package me.bartosz1.web7;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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

    public HttpRequestMethod getRequestMethod() {
        return requestMethod;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public WebEndpointData getEndpointData() {
        return endpointData;
    }

    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }

    public String getRawRequest() {
        return rawRequest;
    }

    public InetAddress getIPAddress() {
        return ipAddress;
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getContentType() {
        return headers.get("Content-Type");
    }
}
