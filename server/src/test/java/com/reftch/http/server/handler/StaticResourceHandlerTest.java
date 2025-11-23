package com.reftch.http.server.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

@ExtendWith(MockitoExtension.class)
class StaticResourceHandlerTest {

    private StaticResourceHandler handler;

    @Mock
    private HttpExchange exchange;

    @Mock
    private Headers headers;

    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() {
        handler = new StaticResourceHandler();
        responseBody = new ByteArrayOutputStream();
    }

    @Test
    void testHandleRequest_Success() throws IOException {
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        handler.handleRequest(exchange, "/static/test.txt");

        verify(headers).set("Content-Type", "text/plain");
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        assertTrue(responseBody.toString().contains("This is a static test file."));
    }

    @Test
    void testHandleRequest_Index() throws IOException {
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        handler.handleRequest(exchange, "/static/");

        verify(headers).set("Content-Type", "text/html");
        verify(exchange).sendResponseHeaders(eq(200), anyLong());
        assertTrue(responseBody.toString().contains("<h1>Index</h1>"));
    }

    @Test
    void testHandleRequest_NotFound() throws IOException {
        when(exchange.getResponseBody()).thenReturn(responseBody);

        handler.handleRequest(exchange, "/static/non-existent.txt");

        verify(exchange).sendResponseHeaders(eq(404), anyLong());
        assertTrue(responseBody.toString().contains("404 - Resource Not Found"));
    }

    @Test
    void testContentType() throws IOException {
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        handler.handleRequest(exchange, "/static/index.html");

        verify(headers).set("Content-Type", "text/html");
    }
}
