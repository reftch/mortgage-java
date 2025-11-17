package com.reftch.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigurationServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void testLoadConfig() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        // Verify loaded values
        assertEquals("8080", service.getValue("server.port"));
        assertEquals("localhost", service.getValue("server.host"));
        assertEquals("jdbc:h2:mem:testdb", service.getValue("database.url"));
        assertEquals("true", service.getValue("database.enabled"));

        // Test default fallback
        assertEquals("", service.getValue("non.existent.key"));
    }

    @Test
    void testGetInt() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        assertEquals(8080, service.getInt("server.port"));
        assertEquals(0, service.getInt("non.existent.key"));
    }

    @Test
    void testGetBoolean() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        assertTrue(service.getBoolean("database.enabled"));
        assertFalse(service.getBoolean("non.existent.key"));
    }

    @Test
    void testGetAll() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        Map<String, String> all = service.getAll();
        assertNotNull(all);
        assertTrue(all.containsKey("server.port"));
        assertTrue(all.containsKey("database.url"));
    }

    @Test
    void testGetWithQuotedValues() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        assertEquals("INFO", service.getValue("logging.level"));
    }

    @Test
    void testEmptyOrCommentLinesIgnored() throws IOException {
        ConfigurationService service = ConfigurationService.getInstance();

        assertEquals("8080", service.getValue("server.port"));
        assertEquals("jdbc:h2:mem:testdb", service.getValue("database.url"));
    }
}
