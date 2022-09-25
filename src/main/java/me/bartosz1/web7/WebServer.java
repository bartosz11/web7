package me.bartosz1.web7;

import me.bartosz1.web7.handlers.WebEndpointHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class WebServer implements Runnable {

    private final int PORT;
    private final HashMap<Pattern, WebEndpointData> endpoints = new HashMap<>();
    public static final String BRAND = "web7/0.0.9";
    private WebEndpointHandler methodNotAllowedHandler;
    private WebEndpointHandler routeNotFoundHandler;
    //currently final, might change this at some point
    private final ExecutorService executorService;

    private final List<RequestFilter> beforeRequestFilters = new ArrayList<>();
    private final List<RequestFilter> afterRequestFilters = new ArrayList<>();
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
        addEndpoint(path, HttpRequestMethod.TRACE, null);
    }

    public void get(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.GET, handler);
    }

    public void post(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.POST, handler);
    }

    public void put(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.PUT, handler);
    }

    public void delete(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.DELETE, handler);
    }

    public void options(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.OPTIONS, handler);
    }

    public void any(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.ANY, handler);
    }

    public void unmap(String path) {
        endpoints.remove(Pattern.compile(path.replaceAll("(\\$[^/]+)", "([^/]+)")));
    }

    private void addEndpoint(String path, HttpRequestMethod requestMethod, WebEndpointHandler handler) {
        Pattern key = Pattern.compile(path.replaceAll("(\\$[^/]+)", "([^/]+)"));
        HashMap<String, Integer> pathVariableIndexes = new HashMap<>();
        if (!endpoints.containsKey(key)) {
            String[] split = path.split("/");
            for (int i = 0; i < split.length; i++) {
                String current = split[i];
                int index = current.indexOf('$');
                if (index != -1) {
                    String name = current.substring(index + 1);
                    pathVariableIndexes.put(name, i);
                }
            }
            WebEndpointData endpointData = new WebEndpointData(handler, requestMethod, path, Collections.unmodifiableMap(pathVariableIndexes));
            endpoints.put(key, endpointData);
        } else throw new IllegalArgumentException("Mapping " + path + " is already present");
    }

    public WebServer start() {
        if (started)
            throw new IllegalStateException("Webserver has already started! (WebServer class instance can't be reused once shut down)");
        Thread mainThread = new Thread(this, "web7-main");
        mainThread.start();
        addShutdownHook(this);
        started = true;
        return this;
    }

    public void shutdown() {
        executorService.shutdown();
        endpoints.clear();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!executorService.isShutdown()) {
                Socket socket = serverSocket.accept();
                executorService.execute(new RequestHandleTask(socket, endpoints, methodNotAllowedHandler, routeNotFoundHandler, beforeRequestFilters, afterRequestFilters));
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

    public WebServer addBeforeRequestFilter(RequestFilter requestFilter) {
        beforeRequestFilters.add(requestFilter);
        return this;
    }

    public WebServer addBeforeRequestFilterBefore(RequestFilter requestFilter, RequestFilter before) {
        beforeRequestFilters.add(beforeRequestFilters.indexOf(before), requestFilter);
        return this;
    }

    public WebServer addBeforeRequestFilterAfter(RequestFilter requestFilter, RequestFilter after) {
        beforeRequestFilters.add(beforeRequestFilters.indexOf(after) + 1, requestFilter);
        return this;
    }

    public WebServer addAfterRequestFilter(RequestFilter requestFilter) {
        afterRequestFilters.add(requestFilter);
        return this;
    }

    public WebServer addAfterRequestFilterBefore(RequestFilter requestFilter, RequestFilter before) {
        afterRequestFilters.add(afterRequestFilters.indexOf(before), requestFilter);
        return this;
    }

    public WebServer addAfterRequestFilterAfter(RequestFilter requestFilter, RequestFilter after) {
        afterRequestFilters.add(afterRequestFilters.indexOf(after) + 1, requestFilter);
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
        if (port < 1 || port > 65535)
            throw new IllegalArgumentException("Invalid port: " + port + " (range of valid ports: 1-65535)");
    }

}
