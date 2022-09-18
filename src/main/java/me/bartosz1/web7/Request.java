package me.bartosz1.web7;

import java.net.InetAddress;
import java.util.Map;

public class Request {

    private final Map<String, String> headers;
    private final String body;
    private final InetAddress remoteAddress;
    private final String userAgent;
    private final HttpRequestMethod requestMethod;
    private final String protocol;
    private final String path;
    private final WebEndpointData endpointData;
    private final Map<String, String> urlParams;
    private final Map<String, String> pathVariables;
    private final String rawRequest;

    public Request(Map<String, String> headers, String body, InetAddress remoteAddress, String userAgent, HttpRequestMethod requestMethod, String protocol, String path, WebEndpointData endpointData, Map<String, String> params, Map<String, String> pathVariables, String rawRequest) {
        this.headers = headers;
        this.body = body;
        this.remoteAddress = remoteAddress;
        this.userAgent = userAgent;
        this.requestMethod = requestMethod;
        this.protocol = protocol;
        this.path = path;
        this.pathVariables = pathVariables;
        this.endpointData = endpointData;
        this.urlParams = params;
        this.rawRequest = rawRequest;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpRequestMethod getRequestMethod() {
        return requestMethod;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    public String getPath() {
        return path;
    }

    public WebEndpointData getEndpointData() {
        return endpointData;
    }

    public Map<String, String> getUrlParameters() {
        return urlParams;
    }

    public String getRawRequest() {
        return rawRequest;
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }
}
