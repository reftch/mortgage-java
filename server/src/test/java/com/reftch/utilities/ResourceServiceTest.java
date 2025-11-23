package com.reftch.utilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResourceServiceTest {

    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceService = ResourceService.getInstance();
        ResourceService.clearCache();
    }

    @Test
    void testGetInstance() {
        ResourceService instance1 = ResourceService.getInstance();
        ResourceService instance2 = ResourceService.getInstance();
        assertSame(instance1, instance2, "Should return the same instance (singleton)");
    }

    @Test
    void testGetFileContent_Success() {
        String content = resourceService.getFileContent("test-resource.txt");
        assertNotNull(content);
        assertTrue(content.contains("Hello, this is a test resource file."));
        assertTrue(content.contains("End of file."));
    }

    @Test
    void testGetFileContent_NotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            resourceService.getFileContent("non-existent-file.txt");
        });
    }

    @Test
    void testGetFileContent_Caching() {
        assertEquals(0, ResourceService.getCacheSize());

        String content1 = resourceService.getFileContent("test-resource.txt");
        assertEquals(1, ResourceService.getCacheSize());

        String content2 = resourceService.getFileContent("test-resource.txt");
        assertEquals(1, ResourceService.getCacheSize());
        assertSame(content1, content2, "Should return cached content object");
    }

    @Test
    void testGetFileContent_WithTemplate() {
        Map<String, String> values = new HashMap<>();
        values.put("name", "World");
        values.put("showDetails", "true");
        values.put("details", "Some details");

        String content = resourceService.getFileContent("test-template.html", values);

        assertNotNull(content);
        assertTrue(content.contains("Hello World!"));
        assertTrue(content.contains("Details: Some details"));
    }

    @Test
    void testClearCache() {
        resourceService.getFileContent("test-resource.txt");
        assertEquals(1, ResourceService.getCacheSize());

        ResourceService.clearCache();
        assertEquals(0, ResourceService.getCacheSize());
    }

    @Test
    void testRemoveFromCache() {
        resourceService.getFileContent("test-resource.txt");
        assertEquals(1, ResourceService.getCacheSize());

        ResourceService.removeFromCache(ResourceService.class, "test-resource.txt");
        assertEquals(0, ResourceService.getCacheSize());
    }
}
