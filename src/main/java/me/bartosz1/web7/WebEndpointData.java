package me.bartosz1.web7;

import me.bartosz1.web7.handlers.WebEndpointHandler;

import java.util.Map;

public class WebEndpointData {

    private final WebEndpointHandler handler;
    private final HttpRequestMethod requestMethod;
    private final String endpoint;
    private final Map<String, Integer> pathVariableIndexes;

    public WebEndpointData(WebEndpointHandler handler, HttpRequestMethod requestMethod, String endpoint, Map<String, Integer> pathVariables) {
        this.handler = handler;
        this.requestMethod = requestMethod;
        this.endpoint = endpoint;
        this.pathVariableIndexes = pathVariables;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public HttpRequestMethod getRequestMethod() {
        return requestMethod;
    }

    public WebEndpointHandler getHandler() {
        return handler;
    }

    public Map<String, Integer> getPathVariableIndexes() {
        return pathVariableIndexes;
    }
}
