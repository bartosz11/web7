package me.bartosz1.web7;

import me.bartosz1.web7.handlers.WebEndpointHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class WebServer implements Runnable {

    private final int PORT;
    private final HashMap<Pattern, WebEndpointData> endpoints = new HashMap<>();
    public static final String BRAND = "web7/0.0.8";
    private WebEndpointHandler methodNotAllowedHandler;
    private WebEndpointHandler routeNotFoundHandler;
    //currently final, might change this at some point
    private final ExecutorService executorService;
    private boolean started = false;

    public WebServer(int port) {
        validatePort(port);
        this.PORT = port;
        executorService = Executors.newFixedThreadPool(5, new HandlerThreadFactory("web7-handler-%d"));
    }

    public WebServer(int port, int handlerThreadAmt) {
        validatePort(port);
        this.PORT = port;
        executorService = Executors.newFixedThreadPool(handlerThreadAmt, new HandlerThreadFactory("web7-handler-%d"));
    }

    public void trace(String path) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod(HttpRequestMethod.TRACE));
    }

    public void get(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod(HttpRequestMethod.GET).setHandler(handler));
    }

    public void post(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod(HttpRequestMethod.POST).setHandler(handler));
    }

    public void put(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod(HttpRequestMethod.PUT).setHandler(handler));
    }

    public void delete(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod(HttpRequestMethod.DELETE).setHandler(handler));
    }

    public void options(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod(HttpRequestMethod.OPTIONS).setHandler(handler));
    }

    public void any(String path, WebEndpointHandler handler) {
        addEndpoint(path, new WebEndpointData().setEndpoint(path).setRequestMethod(HttpRequestMethod.ANY).setHandler(handler));
    }

    public void unmap(String path) {
        endpoints.remove(Pattern.compile(path.replaceAll("(\\$[^/]+)", "([^/]+)")));
    }

    private void addEndpoint(String path, WebEndpointData endpointData) {
        Pattern key = Pattern.compile(path.replaceAll("(\\$[^/]+)", "([^/]+)"));
        if (!endpoints.containsKey(key)) {
            String[] split = path.split("/");
            for (int i = 0; i < split.length; i++) {
                String current = split[i];
                int index = current.indexOf('$');
                if (index != -1) {
                    String name = current.substring(index + 1);
                    endpointData.getPathVariables().put(name, i);
                }
            }
            endpoints.put(key, endpointData);
        } else throw new IllegalArgumentException("Mapping "+path+" is already present");
    }

    public WebServer start() {
        if (started) throw new IllegalStateException("Webserver has already started! (WebServer class instance can't be reused once shut down)");
        Thread mainThread = new Thread(this, "web7-main");
        mainThread.start();
        addShutdownHook(this);
        started = true;
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
                }
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

    //Suppressed so IntellIJ stops crying, needs to be done this way if I want to keep compatibility with Java 7 / older Android API versions
    //(compatibility might be dropped if Google deprecates Android APIs lower than 24)
    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
    private void addShutdownHook(WebServer webServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                webServer.shutdown();
            }
        }));
    }

    //Technically I can just use short and check if it's negative only but shorts are kinda inconvenient
    private void validatePort(int port) {
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Invalid port: "+port+" (range of valid ports: 1-65535)");
    }

}
