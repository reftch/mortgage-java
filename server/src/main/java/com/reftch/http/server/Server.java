package com.reftch.http.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.reftch.config.ConfigurationService;
import com.reftch.http.server.handler.RequestProcessor;
import com.sun.net.httpserver.HttpServer;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private HttpServer httpServer;
    private final ConfigurationService config;
    private final RequestProcessor requestProcessor;
    private final DependencyInjector dependencyInjector;

    long start = System.nanoTime();

    /**
     * Private constructor
     *
     * @throws IOException if the configuration file cannot be found or read
     */
    private Server() {
        this.config = ConfigurationService.getInstance();
        this.requestProcessor = new RequestProcessor(); 
        this.dependencyInjector = new DependencyInjector(requestProcessor);

        dependencyInjector.registerServices();
        dependencyInjector.registerControllers();

        start();
    }

    public static Server run() {
        return new Server();
    }

    public HttpServer start() {
        int port = config.getInt("server.port");

        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            logger.log(Level.INFO, "HttpServer created on port {0}", port);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during start server", e);
        }

        // Create a single handler for all routes
        httpServer.createContext("/", exchange -> requestProcessor.handleRequest(exchange));
        // httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        httpServer.start();

        long end = System.nanoTime();
        long elapsedMs = (end - start) / 1_000_000;
        logger.log(Level.INFO, "Server started on {0}:{1}/ in {2} ms",
                new Object[] { "http://localhost", String.valueOf(port), elapsedMs });

        return httpServer;
    }
}
