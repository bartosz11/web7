package me.bartosz1.web7;

import me.bartosz1.web7.handlers.WebEndpointHandler;

import java.util.HashMap;

public class WebEndpointData {

    private WebEndpointHandler handler;
    private HttpRequestMethod requestMethod;
    private String endpoint;
    private final HashMap<String, Integer> pathVariables = new HashMap<>();

    public WebEndpointData setHandler(WebEndpointHandler handler) {
        this.handler = handler;
        return this;
    }


    public WebEndpointData setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }



    public WebEndpointHandler getHandler() {
        return handler;
    }


    public String getEndpoint() {
        return endpoint;
    }

    public HashMap<String, Integer> getPathVariables() {
        return pathVariables;
    }

    public HttpRequestMethod getRequestMethod() {
        return requestMethod;
    }

    public WebEndpointData setRequestMethod(HttpRequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }
}
