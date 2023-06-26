package one.bartosz.web7;

import one.bartosz.web7.handlers.WebEndpointHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class WebServer implements Runnable {

    public static final String BRAND = "web7/0.3.2";
    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());
    private final int PORT;
    private final HashMap<Pattern, WebEndpointData> endpoints = new HashMap<>();
    //currently final, might change this at some point
    private final ThreadPoolExecutor threadPoolExecutor;
    private final List<RequestFilter> beforeRequestFilters = new ArrayList<>();
    private final List<RequestFilter> afterRequestFilters = new ArrayList<>();
    private WebEndpointHandler methodNotAllowedHandler;
    private WebEndpointHandler routeNotFoundHandler;
    private ServerSocket serverSocket;
    private boolean started = false;
    private final CountDownLatch startLatch = new CountDownLatch(1);

    /**
     * @param port The port to bind the web server to. Needs to fit in 1-65535 range.
     */
    public WebServer(int port) {
        validatePort(port);
        this.PORT = port;
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5, new HandlerThreadFactory("web7-handler-%d"));
    }

    /**
     * @param port             The port to bind the web server to. Needs to fit in 1-65535 range.
     * @param handlerThreadAmt Amount of handler threads to start.
     */
    public WebServer(int port, int handlerThreadAmt) {
        validatePort(port);
        this.PORT = port;
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(handlerThreadAmt, new HandlerThreadFactory("web7-handler-%d"));
    }

    /**
     * Adds a trace endpoint on specified path
     *
     * @param path Path this endpoint should be available on
     */
    public void trace(String path) {
        addEndpoint(path, HttpRequestMethod.TRACE, null);
    }

    /**
     * Adds an endpoint with only GET method allowed.
     *
     * @param path    Path this endpoint should be available on
     * @param handler Handler for this endpoint
     */
    public void get(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.GET, handler);
    }

    /**
     * Adds an endpoint with only POST method allowed.
     *
     * @param path    Path this endpoint should be available on
     * @param handler Handler for this endpoint
     */
    public void post(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.POST, handler);
    }

    /**
     * Adds an endpoint with only PUT method allowed.
     *
     * @param path    Path this endpoint should be available on
     * @param handler Handler for this endpoint
     */
    public void put(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.PUT, handler);
    }

    /**
     * Adds an endpoint with only DELETE method allowed.
     *
     * @param path    Path this endpoint should be available on
     * @param handler Handler for this endpoint
     */
    public void delete(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.DELETE, handler);
    }

    /**
     * Adds an endpoint with only OPTIONS method allowed.
     *
     * @param path    Path this endpoint should be available on
     * @param handler Handler for this endpoint
     */
    public void options(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.OPTIONS, handler);
    }

    /**
     * Adds an endpoint with any request method allowed.
     *
     * @param path    Path this endpoint should be available on
     * @param handler Handler for this endpoint
     */
    public void any(String path, WebEndpointHandler handler) {
        addEndpoint(path, HttpRequestMethod.ANY, handler);
    }

    /**
     * Removes an endpoint.
     *
     * @param path Path used by the endpoint to unmap
     */
    public void unmap(String path) {
        endpoints.remove(Pattern.compile(path.replaceAll("(\\$[^/]+)", "([^/]+)")));
    }

    private void addEndpoint(String path, HttpRequestMethod requestMethod, WebEndpointHandler handler) {
        Pattern key = Pattern.compile(path.replaceAll("(\\$[^/]+)", "([^/]+)"));
        if (!endpoints.containsKey(key)) {
            HashMap<String, Integer> pathVariableIndexes = new HashMap<>();
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

    /**
     * Starts the web server. Can be only called once per instance of this class.
     *
     * @return the WebServer instance that has been started
     */
    public WebServer start() {
        if (started)
            throw new IllegalStateException("Webserver has already started! (WebServer class instance can't be reused once shut down)");
        Thread mainThread = new Thread(this, "web7-main");
        mainThread.start();
        try {
            startLatch.await();
        } catch (InterruptedException ignored) {
        }
        addShutdownHook(this);
        started = true;
        LOGGER.info("WebServer started listening for connections on port " + PORT + "!");
        return this;
    }

    /**
     * Starts the web server. Can be only called once per instance of this class.
     * @param c Function to call after starting the server. Note that the server starting process is asynchronous.
     */
    public void start(Callback c) {
        start();
        c.callback();
    }

    /**
     * Shuts down the web server.
     */
    public void shutdown() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                //I guess we can safely ignore that
            } catch (IOException ignored) {
            }
        }
        if (!threadPoolExecutor.isShutdown()) threadPoolExecutor.shutdown();
        endpoints.clear();
        LOGGER.info("WebServer shut down successfully.");
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            while (!threadPoolExecutor.isShutdown() && !serverSocket.isClosed()) {
                if (startLatch.getCount() == 1) startLatch.countDown();
                Socket socket = serverSocket.accept();
                threadPoolExecutor.execute(new RequestHandleTask(socket, endpoints, methodNotAllowedHandler, routeNotFoundHandler, beforeRequestFilters, afterRequestFilters));
            }
        } catch (IOException e) {
            //subject-to-change?
            if (!e.getMessage().contains("accept failed")) {
                LOGGER.severe("web7: An error occurred");
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the handler used for requests that used wrong request methods.
     *
     * @param methodNotAllowedHandler The handler to set
     * @return WebServer instance this method was called on (builder method)
     */
    public WebServer setMethodNotAllowedHandler(WebEndpointHandler methodNotAllowedHandler) {
        this.methodNotAllowedHandler = methodNotAllowedHandler;
        return this;
    }

    /**
     * Sets the handler used for requests that tried to utilize a nonexistent endpoint.
     *
     * @param routeNotFoundHandler The handler to set
     * @return WebServer instance this method was called on (builder method)
     */
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
    private void addShutdownHook(final WebServer webServer) {
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
        LOGGER.fine("Port " + port + " is valid");
    }

}
