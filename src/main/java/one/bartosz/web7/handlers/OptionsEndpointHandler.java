package one.bartosz.web7.handlers;

import one.bartosz.web7.HttpRequestMethod;
import one.bartosz.web7.Request;
import one.bartosz.web7.Response;

/**
 * A class containing a handler for OPTIONS HTTP requests used if the endpoint wasn't registered for OPTIONS method or if it was and supplied handler is null.
 */
public class OptionsEndpointHandler implements WebEndpointHandler {

    @Override
    public void handle(Request request, Response response) {
        HttpRequestMethod supportedRequestMethod = request.getEndpointData().getRequestMethod();
        if (request.getContextPath().equals("*") || supportedRequestMethod == HttpRequestMethod.ANY)
            response.getHeaders().put("Allow", "GET,HEAD,POST,PUT,DELETE,OPTIONS,TRACE");
        else
            response.getHeaders().put("Allow", supportedRequestMethod + ",HEAD,OPTIONS");
    }
}
