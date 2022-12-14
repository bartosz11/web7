package me.bartosz1.web7.handlers;

import me.bartosz1.web7.Request;
import me.bartosz1.web7.Response;

public class TraceEndpointHandler implements WebEndpointHandler {

    @Override
    public void handle(Request request, Response response) {
        response.setContentType("message/http");
        response.setBody(request.getRawRequest());
    }
}
