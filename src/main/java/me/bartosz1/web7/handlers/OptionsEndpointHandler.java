package me.bartosz1.web7.handlers;

import me.bartosz1.web7.Request;
import me.bartosz1.web7.Response;

public class OptionsEndpointHandler implements WebEndpointHandler {

    @Override
    public void handle(Request request, Response response) {
        String supportedRequestMethod = request.getEndpointData().getRequestMethod();
        if (request.getPath().equals("*") || supportedRequestMethod.equals("ANY"))
            response.getHeaders().put("Allow", "GET,HEAD,POST,PUT,DELETE,OPTIONS,TRACE");
        else
            response.getHeaders().put("Allow", supportedRequestMethod + ",HEAD,OPTIONS");
    }
}
