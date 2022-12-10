package me.bartosz1.web7;

import me.bartosz1.web7.handlers.OptionsEndpointHandler;
import me.bartosz1.web7.handlers.TraceEndpointHandler;
import me.bartosz1.web7.handlers.WebEndpointHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class RequestHandleTask implements Runnable {
    private static final TraceEndpointHandler TRACE_ENDPOINT_HANDLER = new TraceEndpointHandler();
    private static final OptionsEndpointHandler OPTIONS_ENDPOINT_HANDLER = new OptionsEndpointHandler();
    private static final Logger LOGGER = Logger.getLogger(RequestHandleTask.class.getName());
    private final Socket socket;
    private final HashMap<Pattern, WebEndpointData> endpoints;
    private final WebEndpointHandler methodNotAllowedHandler;
    private final WebEndpointHandler routeNotFoundHandler;
    private final List<RequestFilter> beforeFilters;
    private final List<RequestFilter> afterFilters;

    public RequestHandleTask(Socket socket, HashMap<Pattern, WebEndpointData> endpoints, WebEndpointHandler methodNotAllowedHandler, WebEndpointHandler routeNotFoundHandler, List<RequestFilter> beforeFilters, List<RequestFilter> afterFilters) {
        this.socket = socket;
        this.endpoints = endpoints;
        this.methodNotAllowedHandler = methodNotAllowedHandler;
        this.routeNotFoundHandler = routeNotFoundHandler;
        this.beforeFilters = beforeFilters;
        this.afterFilters = afterFilters;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            StringTokenizer tokenizer = new StringTokenizer(bufferedReader.readLine());
            HttpRequestMethod method = HttpRequestMethod.valueOf(tokenizer.nextToken().toUpperCase(Locale.ROOT));
            String s = tokenizer.nextToken();
            String endpoint = s.split("\\?")[0];
            String protocol = tokenizer.nextToken();
            WebEndpointData endpointData = findEndpoint(endpoint);
            Request request = ParsingUtils.parseRequest(bufferedReader, socket.getInetAddress(), method, s, protocol, endpointData);
            Response response = new Response(printWriter);
            if (endpointData != null) {
                if (method == endpointData.getRequestMethod() || method == HttpRequestMethod.OPTIONS || method == HttpRequestMethod.HEAD || endpointData.getRequestMethod() == HttpRequestMethod.ANY) {
                    for (RequestFilter filter : beforeFilters) {
                        if (filter != null) filter.filter(request, response);
                    }
                    switch (method) {
                        case TRACE:
                            TRACE_ENDPOINT_HANDLER.handle(request, response);
                            break;
                        case OPTIONS:
                            if (endpointData.getRequestMethod() == HttpRequestMethod.OPTIONS && endpointData.getHandler() != null)
                                endpointData.getHandler().handle(request, response);
                            else OPTIONS_ENDPOINT_HANDLER.handle(request, response);
                            break;
                        default:
                            WebEndpointHandler handler = endpointData.getHandler();
                            if (handler != null) handler.handle(request, response);
                            //HEAD requests shouldn't return body, actually maybe this shouldn't be enforced on library level /shrug
                            if (method == HttpRequestMethod.HEAD) {
                                response.setBody(null);
                                response.setContentType(null);
                                response.getHeaders().remove("Content-Length");
                            }
                            break;
                    }
                    for (RequestFilter filter : afterFilters) {
                        if (filter != null) filter.filter(request, response);
                    }
                } else {
                    if (methodNotAllowedHandler != null) methodNotAllowedHandler.handle(request, response);
                    //code should be forced in both cases
                    response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                }
            } else {
                if (routeNotFoundHandler != null) routeNotFoundHandler.handle(request, response);
                response.setStatus(HttpStatus.NOT_FOUND);
            }
            ParsingUtils.parseResponse(response, printWriter);
        } catch (IOException e) {
            //not much but always helpful if something stupid happens
            LOGGER.severe("An error occured while handling request. Response probably wasn't sent to the client.");
            e.printStackTrace();
        }
    }

    private WebEndpointData findEndpoint(String path) {
        List<Map.Entry<Pattern, WebEndpointData>> entries = new ArrayList<>(endpoints.entrySet());
        for (Map.Entry<Pattern, WebEndpointData> entry : entries) {
            Pattern key = entry.getKey();
            if (key.matcher(path).matches()) {
                return entry.getValue();
            }
        }
        return null;
    }
}
