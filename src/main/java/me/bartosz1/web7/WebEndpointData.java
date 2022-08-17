package me.bartosz1.web7;

import me.bartosz1.web7.handlers.WebEndpointHandler;

public class WebEndpointData {

    private WebEndpointHandler handler;
    private String requestMethod;
    private String endpoint;

    public WebEndpointData setHandler(WebEndpointHandler handler) {
        this.handler = handler;
        return this;
    }

    public WebEndpointData setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    public WebEndpointData setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getRequestMethod() {
        return requestMethod;
    }


    public WebEndpointHandler getHandler() {
        return handler;
    }


    public String getEndpoint() {
        return endpoint;
    }

}
