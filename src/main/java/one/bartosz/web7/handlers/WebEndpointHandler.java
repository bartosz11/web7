package one.bartosz.web7.handlers;

import one.bartosz.web7.Request;
import one.bartosz.web7.Response;

/**
 * Representation of all endpoint handlers.
 */
public interface WebEndpointHandler {
    /**
     * @param request  The incoming request
     * @param response Response returned to the client
     */
    void handle(Request request, Response response);
}
