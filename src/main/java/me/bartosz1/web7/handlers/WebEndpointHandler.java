package me.bartosz1.web7.handlers;

import me.bartosz1.web7.Request;
import me.bartosz1.web7.Response;

public interface WebEndpointHandler {

    void handle(Request request, Response response);
}
