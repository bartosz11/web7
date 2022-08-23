package me.bartosz1.web7;

import java.net.InetAddress;
import java.util.Map;

public class Request {

    private final Map<String, String> headers;
    private final Object body;
    private final InetAddress remoteAddress;
    private final String userAgent;
    private final String requestMethod;
    private final String protocol;
    private final String path;
    private WebEndpointData endpointData;
    private final Map<String, String> params;
    private final String rawRequest;
    private boolean endpointDataSet = false;

    public Request(Map<String, String> headers, Object body, InetAddress remoteAddress, String userAgent, String[] split, WebEndpointData endpointData, Map<String, String> params, String rawRequest) {
        this.headers = headers;
        this.body = body;
        this.remoteAddress = remoteAddress;
        this.userAgent = userAgent;
        this.requestMethod = split[0];
        this.protocol = split[2];
        this.path = split[1];
        if (endpointData != null) {
            this.endpointData = endpointData;
            this.endpointDataSet = true;
        }
        this.params = params;
        this.rawRequest = rawRequest;
    }

    public Object getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRequestMethod() {
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

    public Map<String, String> getParams() {
        return params;
    }

    public String getRawRequest() {
        return rawRequest;
    }
    //will work only one time, object is read only
    public Request setEndpointData(WebEndpointData endpointData) {
        if (!endpointDataSet) {
            this.endpointData = endpointData;
            this.endpointDataSet = true;
        }
        return this;
    }
}
