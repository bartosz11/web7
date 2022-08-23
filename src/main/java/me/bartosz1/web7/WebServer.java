package me.bartosz1.web7;

import me.bartosz1.web7.handlers.OptionsEndpointHandler;
import me.bartosz1.web7.handlers.TraceEndpointHandler;
import me.bartosz1.web7.handlers.WebEndpointHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;

public class WebServer implements Runnable {

    private final int PORT;
    private final HashMap<String, WebEndpointData> endpoints = new HashMap<>();
    private static final TraceEndpointHandler TRACE_ENDPOINT_HANDLER = new TraceEndpointHandler();
    private static final OptionsEndpointHandler OPTIONS_ENDPOINT_HANDLER = new OptionsEndpointHandler();
    public static final String BRAND = "web7/0.0.4";
    private WebEndpointHandler methodNotAllowedHandler;

    public WebServer(int port) {
        this.PORT = port;
    }

    private void handleRequest(Socket socket) throws IOException {
        InputStreamReader in = new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(in);
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        String[] split = bufferedReader.readLine().split("\\s+");
        Response response = new Response();
        String method = split[0].toUpperCase(Locale.ROOT);
        String endpoint = split[1].split("\\?")[0];
        String protocol = split[2];
        if (endpoints.containsKey(endpoint)) {
            WebEndpointData endpointData = endpoints.get(endpoint);
            Request request = ParsingUtils.parseRequest(bufferedReader, socket.getInetAddress(), split, endpointData);
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
                response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
            }
        } else {
            response.setStatus(HttpStatus.NOT_FOUND);
            response.setBody("404 - Endpoint not found");
        }
        ParsingUtils.parseResponse(response, printWriter, protocol);
    }

    public void trace(String path) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod("TRACE"));
    }

    public void get(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod("GET").setHandler(handler));
    }

    public void post(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod("POST").setHandler(handler));
    }

    public void put(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod("PUT").setHandler(handler));
    }

    public void delete(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod("PUT").setHandler(handler));
    }

    public void options(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod("OPTIONS").setHandler(handler));
    }

    private void addEndpoint(String path, WebEndpointData endpointData) {
        endpoints.put(path, endpointData);
    }

    public WebServer start() {
        new Thread(this, "web7-main").start();
        return this;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                handleRequest(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public WebServer setMethodNotAllowedHandler(WebEndpointHandler methodNotAllowedHandler) {
        this.methodNotAllowedHandler = methodNotAllowedHandler;
        return this;
    }
}
