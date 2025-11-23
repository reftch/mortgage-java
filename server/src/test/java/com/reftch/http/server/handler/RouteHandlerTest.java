package com.reftch.http.server.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;

class RouteHandlerTest {

    @Test
    void testSimplePath() {
        RouteHandler handler = new RouteHandler(new Object(), null, HttpMethod.GET, "/users");

        assertEquals("/users", handler.getPath());
        assertTrue(handler.pathParams.isEmpty());

        Matcher matcher = handler.pattern.matcher("/users");
        assertTrue(matcher.matches());

        Matcher nonMatch = handler.pattern.matcher("/users/123");
        assertFalse(nonMatch.matches());
    }

    @Test
    void testPathWithParameters() {
        RouteHandler handler = new RouteHandler(new Object(), null, HttpMethod.GET, "/users/{id}");

        assertEquals("/users/{id}", handler.getPath());
        assertEquals(1, handler.pathParams.size());
        assertEquals("id", handler.pathParams.get(0));

        Matcher matcher = handler.pattern.matcher("/users/123");
        assertTrue(matcher.matches());
        assertEquals("123", matcher.group(1));

        Matcher nonMatch = handler.pattern.matcher("/users");
        assertFalse(nonMatch.matches());
    }

    @Test
    void testMultipleParameters() {
        RouteHandler handler = new RouteHandler(new Object(), null, HttpMethod.GET, "/users/{userId}/posts/{postId}");

        assertEquals(2, handler.pathParams.size());
        assertEquals("userId", handler.pathParams.get(0));
        assertEquals("postId", handler.pathParams.get(1));

        Matcher matcher = handler.pattern.matcher("/users/123/posts/456");
        assertTrue(matcher.matches());
        assertEquals("123", matcher.group(1));
        assertEquals("456", matcher.group(2));
    }

    @Test
    void testSpecialCharacters() {
        RouteHandler handler = new RouteHandler(new Object(), null, HttpMethod.GET, "/files/{filename}.txt");

        assertEquals(1, handler.pathParams.size());
        assertEquals("filename", handler.pathParams.get(0));

        Matcher matcher = handler.pattern.matcher("/files/document.txt");
        assertTrue(matcher.matches());
        assertEquals("document", matcher.group(1));

        // Should not match if extension is different
        assertFalse(handler.pattern.matcher("/files/document.pdf").matches());
    }
}
