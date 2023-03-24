package me.bartosz1.web7.handlers;

import me.bartosz1.web7.Request;
import me.bartosz1.web7.Response;

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
