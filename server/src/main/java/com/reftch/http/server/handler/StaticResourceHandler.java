package com.reftch.http.server.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;

/**
 * Handles static resource serving for HTTP requests.
 * Provides functionality to serve files from classpath resources with proper
 * content type detection, caching headers, and error handling.
 */
public class StaticResourceHandler {

    private static final Map<String, String> contentTypeMap = initializeContentTypeMap();

    /**
     * Handles requests for static resources by serving files from the classpath.
     * 
     * @param exchange the HTTP exchange object containing request and response
     *                 information
     * @param path     the requested path for the static resource
     * @throws IOException if there are issues reading the resource or sending the
     *                     response
     */
    public void handleRequest(HttpExchange exchange, String path) throws IOException {
        String resourcePath = prepareResourcePath(path);

        if (resourcePath.isEmpty()) {
            resourcePath = "index.html";
        }

        String contentType = determineContentType(resourcePath);

        // Try to load the file from classpath (e.g., resources/static/)
        String resourceLocation = "/static/" + resourcePath;
        InputStream inputStream = getResourceStream(resourceLocation);

        try {
            if (inputStream == null) {
                // File not found
                sendNotFoundResponse(exchange);
                return;
            }

            byte[] response = readResourceContent(inputStream);
            sendSuccessfulResponse(exchange, response, contentType, resourcePath);

        } catch (IOException | URISyntaxException e) {
            
            throw new IOException("Error reading resource: " + resourcePath, e);
        } finally {
            closeInputStream(inputStream);
        }
    }

    /**
     * Prepares the resource path by removing static prefix and normalizing slashes.
     * 
     * @param path the original request path
     * @return the cleaned resource path
     */
    private String prepareResourcePath(String path) {
        return path.replaceFirst("/static", "").replaceAll("^/+", "");
    }

    /**
     * Determines the content type for a given resource path.
     * 
     * @param resourcePath the path of the resource
     * @return the content type string, or "application/octet-stream" if unknown
     */
    private String determineContentType(String resourcePath) {
        String contentType = getContentType(resourcePath);
        return contentType != null ? contentType : "application/octet-stream";
    }

    /**
     * Retrieves an input stream for the specified resource location.
     * 
     * @param resourceLocation the full resource path including leading slash
     * @return InputStream for the resource, or null if not found
     */
    private InputStream getResourceStream(String resourceLocation) {
        return getClass().getResourceAsStream(resourceLocation);
    }

    /**
     * Reads the complete content of a resource into a byte array.
     * 
     * @param inputStream the input stream to read from
     * @return byte array containing the complete resource content
     * @throws IOException if there's an error reading the stream
     */
    private byte[] readResourceContent(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        return outputStream.toByteArray();
    }

    /**
     * Sends a 404 Not Found response to the client.
     * 
     * @param exchange the HTTP exchange object
     * @throws IOException if there's an error sending the response
     */
    private void sendNotFoundResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, 0);
    }

    /**
     * Sends a successful response with the resource content.
     * 
     * @param exchange     the HTTP exchange object
     * @param response     the byte array containing the resource content
     * @param contentType  the content type header value
     * @param resourcePath the path of the resource for metadata retrieval
     * @throws IOException        if there's an error sending the response
     * @throws URISyntaxException if there's an issue with the resource URI
     */
    private void sendSuccessfulResponse(HttpExchange exchange, byte[] response, String contentType, String resourcePath)
            throws IOException, URISyntaxException {
        exchange.getResponseHeaders().set("Content-Type", contentType);

        // Set cache control header
        exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");

        // Send response
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }

    /**
     * Closes the input stream safely, logging any errors.
     * 
     * @param inputStream the input stream to close
     */
    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                System.err.println("Error closing resource stream for: " + inputStream);
            }
        }
    }

    /**
     * Gets the content type for a given file extension.
     * 
     * @param path the file path
     * @return the content type string, or null if unknown
     */
    private String getContentType(String path) {
        return Optional.ofNullable(path)
                .filter(p -> p.lastIndexOf('.') != -1)
                .map(p -> p.substring(p.lastIndexOf('.')))
                .map(String::toLowerCase)
                .map(extension -> contentTypeMap.getOrDefault(extension, "application/octet-stream"))
                .orElse(null);
    }

    /**
     * Initializes the content type mapping map with common file extensions.
     * 
     * @return initialized map of file extensions to content types
     */
    private static Map<String, String> initializeContentTypeMap() {
        Map<String, String> map = new HashMap<>();
        map.put(".css", "text/css");
        map.put(".js", "application/javascript");
        map.put(".html", "text/html");
        map.put(".htm", "text/html");
        map.put(".png", "image/png");
        map.put(".jpg", "image/jpeg");
        map.put(".jpeg", "image/jpeg");
        map.put(".gif", "image/gif");
        map.put(".svg", "image/svg+xml");
        map.put(".ico", "image/x-icon");
        map.put(".json", "application/json");
        map.put(".xml", "application/xml");
        map.put(".txt", "text/plain");
        map.put(".pdf", "application/pdf");
        map.put(".zip", "application/zip");
        map.put(".mp4", "video/mp4");
        map.put(".webm", "video/webm");
        map.put(".ogg", "video/ogg");
        map.put(".mp3", "audio/mpeg");
        map.put(".wav", "audio/wav");
        map.put(".woff", "font/woff");
        map.put(".woff2", "font/woff2");
        map.put(".ttf", "font/ttf");
        map.put(".eot", "application/vnd.ms-fontobject");
        return map;
    }
}
