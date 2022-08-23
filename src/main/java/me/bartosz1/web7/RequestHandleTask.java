package me.bartosz1.web7;

import me.bartosz1.web7.handlers.OptionsEndpointHandler;
import me.bartosz1.web7.handlers.TraceEndpointHandler;
import me.bartosz1.web7.handlers.WebEndpointHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;

public class RequestHandleTask implements Runnable {
    private final Socket socket;
    private final HashMap<String, WebEndpointData> endpoints;
    private final WebEndpointHandler methodNotAllowedHandler;
    private final WebEndpointHandler routeNotFoundHandler;
    private static final TraceEndpointHandler TRACE_ENDPOINT_HANDLER = new TraceEndpointHandler();
    private static final OptionsEndpointHandler OPTIONS_ENDPOINT_HANDLER = new OptionsEndpointHandler();

    public RequestHandleTask(Socket socket, HashMap<String, WebEndpointData> endpoints, WebEndpointHandler methodNotAllowedHandler, WebEndpointHandler routeNotFoundHandler) {
        this.socket = socket;
        this.endpoints = endpoints;
        this.methodNotAllowedHandler = methodNotAllowedHandler;
        this.routeNotFoundHandler = routeNotFoundHandler;
    }

    @Override
    public void run() {
        try {
            InputStreamReader in = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            String[] split = bufferedReader.readLine().split("\\s+");
            Response response = new Response();
            String method = split[0].toUpperCase(Locale.ROOT);
            String endpoint = split[1].split("\\?")[0];
            String protocol = split[2];
            Request request = ParsingUtils.parseRequest(bufferedReader, socket.getInetAddress(), split, null);
            if (endpoints.containsKey(endpoint)) {
                WebEndpointData endpointData = endpoints.get(endpoint);
                request.setEndpointData(endpointData);
                if (method.equals(endpointData.getRequestMethod()) || method.equals("OPTIONS") || method.equals("HEAD") || endpointData.getRequestMethod().equalsIgnoreCase("ANY")) {
                    switch (method) {
                        case "TRACE":
                            TRACE_ENDPOINT_HANDLER.handle(request, response);
                            break;
                        case "OPTIONS":
                            if (endpointData.getRequestMethod().equals("OPTIONS") && endpointData.getHandler() != null)
                                endpointData.getHandler().handle(request, response);
                            else OPTIONS_ENDPOINT_HANDLER.handle(request, response);
                            break;
                        default:
                            WebEndpointHandler handler = endpointData.getHandler();
                            if (handler != null) handler.handle(request, response);
                            //HEAD requests shouldn't return body
                            if (method.toUpperCase(Locale.ROOT).equals("HEAD")) {
                                response.setBody(null);
                                response.setContentType(null);
                                response.getHeaders().remove("Content-Length");
                            }
                            break;
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
            ParsingUtils.parseResponse(response, printWriter, protocol);
            //we probably should do something here
        } catch (IOException ignored) {}
    }
}
