package com.reftch.http.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;

public class RequestProcessor {

    private StaticResourceHandler staticHandler = new StaticResourceHandler();
    private List<RouteHandler> routeHandlers = new ArrayList<>();

    public List<RouteHandler> getRouteHandlers() {
        return routeHandlers;
    }

    public void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // Handle static resources
        if (path.startsWith("/static")) {
            staticHandler.handleRequest(exchange, path);
            return;
        } 

        if (path.startsWith("/robots.txt")) {
            staticHandler.handleRequest(exchange, "/static/robots.txt");
            return;
        }

        // Find matching route
        RouteHandler handler = findMatchingRoute(method, path);
        if (handler != null) {
            try {
                // Prepare parameters for method call
                Map<String, Object> pathParams = new HashMap<>();
                Matcher matcher = handler.pattern.matcher(path);

                if (matcher.matches()) {
                    // Extract parameter values
                    for (int i = 0; i < handler.pathParams.size(); i++) {
                        String paramName = handler.pathParams.get(i);
                        String paramValue = matcher.group(i + 1); // Groups start at 1
                        pathParams.put(paramName, paramValue);
                    }
                }

                // Call controller method
                Object result = callControllerMethod(handler, pathParams, exchange);

                // Send response
                sendResponse(exchange, result);
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, 0);
                exchange.getResponseBody().write(("Internal Server Error: " + e.getMessage()).getBytes());
            }
        } else {
            // Handle 404
            String response = "<html><body><h1>404 - Not Found</h1></body></html>";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        }
    }

    private RouteHandler findMatchingRoute(String method, String path) {
        for (RouteHandler handler : routeHandlers) {
            if (handler.httpMethod.name().equals(method) &&
                    matchesPath(handler.path, path)) {
                return handler;
            }
        }
        return null;
    }

    // Check if path matches pattern (simplified version)
    private boolean matchesPath(String routePattern, String requestPath) {
        // Create a simple regex pattern for matching
        Pattern pattern = Pattern.compile("^" +
                routePattern.replaceAll("\\{([^}]+)\\}", "([^/]+)") + "$");
        return pattern.matcher(requestPath).matches();
    }

    // Call controller method with parameters
    private Object callControllerMethod(RouteHandler handler, Map<String, Object> pathParams, HttpExchange exchange)
            throws Exception {
        Object[] params = new Object[handler.method.getParameterCount()];

        Class<?>[] paramTypes = handler.method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == HttpExchange.class) {
                params[i] = exchange;
            } else if (paramTypes[i] == Map.class) {
                params[i] = pathParams;
            } else if (paramTypes[i] == String.class) {
                params[i] = "";
            }
        }

        handler.method.setAccessible(true);
        return handler.method.invoke(handler.controller, params);
    }

    // Send response to client
    private void sendResponse(HttpExchange exchange, Object result) throws IOException {
        String response;

        if (result == null) {
            response = "";
        } else if (result instanceof String) {
            response = (String) result;
        } else {
            // Convert to JSON or other format
            response = result.toString();
        }

        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}
