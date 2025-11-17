package com.reftch.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

public class HttpUtils {

    private static final Logger logger = Logger.getLogger(HttpUtils.class.getName());

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Redirects to the specified location with 302 status
     * 
     * @param exchange the HTTP exchange
     * @param location the redirect location
     * @throws IOException if unable to send response
     */
    public static void redirectTo(HttpExchange exchange, String location) {
        if (exchange == null || location == null) {
            throw new IllegalArgumentException("Exchange and location cannot be null");
        }

        try {
            exchange.getResponseHeaders().add("Location", location);
            exchange.sendResponseHeaders(302, -1);
        } catch (IOException e) {
            logger.log(Level.INFO, "Failed redirect to " + location, e);
        }
    }

    /**
     * Sends a plain text response to the client.
     *
     * @param exchange   The HTTP exchange to send the response through.
     * @param message    The message to send in the body.
     * @param statusCode The HTTP status code to set.
     */
    public static void sendResponse(HttpExchange exchange, String message, int statusCode) {
        try {
            exchange.sendResponseHeaders(statusCode, message.length());
            OutputStream os = exchange.getResponseBody();
            os.write(message.getBytes());
            os.close();
        } catch (IOException e) {
            logger.log(null, "Failed send reponse", e);
        }
    }

    // Method to generate a cryptographically secure random string for state
    // parameter
    public static String generateRandomState(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

}
