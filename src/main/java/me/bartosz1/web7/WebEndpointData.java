package me.bartosz1.web7;

import me.bartosz1.web7.handlers.WebEndpointHandler;

import java.util.Map;

/**
 * Representation of an endpoint describing it.
 */
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

    /**
     * @return The endpoint that was set
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @return Allowed request method
     */
    public HttpRequestMethod getRequestMethod() {
        return requestMethod;
    }

    /**
     * @return Handler of the endpoint. Nullable.
     */
    public WebEndpointHandler getHandler() {
        return handler;
    }

    /**
     * Used internally. Indexes of split by / where a $ was found
     */
    public Map<String, Integer> getPathVariableIndexes() {
        return pathVariableIndexes;
    }
}
