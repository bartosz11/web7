package me.bartosz1.web7;

import me.bartosz1.web7.handlers.OptionsEndpointHandler;
import me.bartosz1.web7.handlers.TraceEndpointHandler;
import me.bartosz1.web7.handlers.WebEndpointHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer implements Runnable {

    private final int PORT;
    private final HashMap<String, WebEndpointData> endpoints = new HashMap<>();
    private static final TraceEndpointHandler TRACE_ENDPOINT_HANDLER = new TraceEndpointHandler();
    private static final OptionsEndpointHandler OPTIONS_ENDPOINT_HANDLER = new OptionsEndpointHandler();
    public static final String BRAND = "web7/0.0.6";
    private WebEndpointHandler methodNotAllowedHandler;
    private WebEndpointHandler routeNotFoundHandler;
    //currently final, might change this at some point
    private final ExecutorService executorService;

    public WebServer(int port) {
        this.PORT = port;
        executorService = Executors.newFixedThreadPool(10, new HandlerThreadFactory("web7-handler-%d"));
    }

    public WebServer(int port, int handlerThreadAmt) {
        this.PORT = port;
        executorService = Executors.newFixedThreadPool(handlerThreadAmt, new HandlerThreadFactory("web7-handler-%d"));
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

    public void any(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod("ANY").setHandler(handler));
    }

    public void unmap(String path) {
        endpoints.remove(path);
    }

    private void addEndpoint(String path, WebEndpointData endpointData) {
        endpoints.put(path, endpointData);
    }

    public WebServer start() {
        Thread mainThread = new Thread(this, "web7-main");
        mainThread.start();
        addShutdownHook(this);
        return this;
    }

    public void shutdown() {
        endpoints.clear();
        executorService.shutdown();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                if (!executorService.isShutdown()) {
                    Socket socket = serverSocket.accept();
                    executorService.execute(new RequestHandleTask(socket, endpoints, methodNotAllowedHandler, routeNotFoundHandler));
                } else {
                    serverSocket.close();
                    break;
                };
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WebServer setMethodNotAllowedHandler(WebEndpointHandler methodNotAllowedHandler) {
        this.methodNotAllowedHandler = methodNotAllowedHandler;
        return this;
    }

    public WebServer setRouteNotFoundHandler(WebEndpointHandler routeNotFoundHandler) {
        this.routeNotFoundHandler = routeNotFoundHandler;
        return this;
    }

    private void addShutdownHook(WebServer webServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                webServer.shutdown();
            }
        }));
    }

}
