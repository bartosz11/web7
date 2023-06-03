package one.bartosz.web7.handlers;

import one.bartosz.web7.Request;
import one.bartosz.web7.Response;

/**
 * A handler for all TRACE HTTP requests. This can't be changed.
 */
public class TraceEndpointHandler implements WebEndpointHandler {

    @Override
    public void handle(Request request, Response response) {
        response.setContentType("message/http");
        response.setBody(request.getRawRequest());
    }
}
