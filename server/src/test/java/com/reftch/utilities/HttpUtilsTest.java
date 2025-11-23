package com.reftch.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

@ExtendWith(MockitoExtension.class)
class HttpUtilsTest {

    @Mock
    private HttpExchange exchange;

    @Mock
    private Headers headers;

    @Test
    void testRedirectTo_Success() throws IOException {
        when(exchange.getResponseHeaders()).thenReturn(headers);

        String location = "http://example.com";
        HttpUtils.redirectTo(exchange, location);

        verify(headers).add("Location", location);
        verify(exchange).sendResponseHeaders(302, -1);
    }

    @Test
    void testRedirectTo_NullArguments() {
        assertThrows(IllegalArgumentException.class, () -> HttpUtils.redirectTo(null, "loc"));
        assertThrows(IllegalArgumentException.class, () -> HttpUtils.redirectTo(exchange, null));
    }

    @Test
    void testSendResponse_Success() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);

        String message = "Hello World";
        int statusCode = 200;

        HttpUtils.sendResponse(exchange, message, statusCode);

        verify(exchange).sendResponseHeaders(eq(statusCode), eq((long) message.length()));
        assertEquals(message, outputStream.toString());
    }

    @Test
    void testGenerateRandomState() {
        int length = 10;
        String state = HttpUtils.generateRandomState(length);

        assertNotNull(state);
        assertEquals(length, state.length());
        assertTrue(state.matches("[A-Za-z0-9]+"));
    }

    @Test
    void testGenerateRandomState_DifferentResults() {
        String state1 = HttpUtils.generateRandomState(10);
        String state2 = HttpUtils.generateRandomState(10);

        assertNotEquals(state1, state2);
    }
}
