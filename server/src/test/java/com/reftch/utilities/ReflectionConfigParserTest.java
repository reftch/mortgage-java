package com.reftch.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.reftch.http.server.ReflectionEntry;

class ReflectionConfigParserTest {

    ReflectionConfigParser parser = new ReflectionConfigParser();

    @Test
    void testIsValidJsonArray() {
        assertTrue(parser.isValidJsonArray("[{}]"));
        assertFalse(parser.isValidJsonArray(""));
        assertFalse(parser.isValidJsonArray("{}"));
        assertFalse(parser.isValidJsonArray("null"));
    }

    @Test
    void testRemoveOuterBrackets() {
        String json = "[ {\"key\":\"value\"} ]";
        String expected = " {\"key\":\"value\"} ";
        assertEquals(expected.trim(), parser.removeOuterBrackets(json));
    }

    @Test
    void testParseValue() {
        String json = "{\"name\":\"testName\",\"other\":true}";
        assertEquals("testName", parser.parseValue(json, "\"name\""));
        assertNull(parser.parseValue(json, "\"missingKey\""));
        assertNull(parser.parseValue("invalid json", "\"name\""));
    }

    @Test
    void testParseBoolean() {
        String jsonTrue = "{\"allPublicConstructors\":\"true\"}";
        String jsonFalse = "{\"allPublicConstructors\":\"false\"}";
        String jsonMissing = "{}";

        assertTrue(parser.parseBoolean(jsonTrue, "\"allPublicConstructors\""));
        assertFalse(parser.parseBoolean(jsonFalse, "\"allPublicConstructors\""));
        assertFalse(parser.parseBoolean(jsonMissing, "\"allPublicConstructors\""));
    }

    @Test
    void testParseFields() {
        String fieldsJson = "[{\"name\":\"field1\"},{\"name\":\"field2\"}]";
        var fields = parser.parseFields(fieldsJson);
        assertEquals(2, fields.size());
        assertEquals("field1", fields.get(0).name());
        assertEquals("field2", fields.get(1).name());
    }

    @Test
    void testParseEntryWithFields() {
        String json = "{\"name\":\"testClass\",\"allDeclaredConstructors\":\"true\",\"allPublicConstructors\":\"true\","
                + "\"allDeclaredMethods\":\"true\",\"allPublicMethods\":\"true\",\"allPrivateMethods\":\"false\","
                + "\"fields\":[{\"name\":\"fieldA\"}]}";
        var entry = parser.parseEntry(json);
        assertNotNull(entry);
        assertEquals("testClass", entry.name());
        assertTrue(entry.allDeclaredConstructors());
        assertTrue(entry.allPublicConstructors());
        assertTrue(entry.allDeclaredMethods());
        assertTrue(entry.allPublicMethods());
        assertFalse(entry.allPrivateMethods());
        assertEquals(1, entry.fields().size());
        assertEquals("fieldA", entry.fields().get(0).name());
    }

    @Test
    void testParseEntryWithoutNameReturnsNull() {
        String json = "{\"allDeclaredConstructors\":\"true\"}";
        assertNull(parser.parseEntry(json));
    }

    @Test
    void testParseEntriesMultipleObjects() {
        String content = "{\"name\":\"A\",\"allDeclaredConstructors\":\"true\"},"
                + "{\"name\":\"B\",\"allPublicConstructors\":\"false\"}";
        var entries = parser.parseEntries(content);
        assertEquals(2, entries.size());
        assertEquals("A", entries.get(0).name());
        assertEquals("B", entries.get(1).name());
    }

    @Test
    void testParseEntriesHandlesInvalidObjectGracefully() {
        String content = "{\"name\":\"Valid\"}, {\"name\":}";
        var entries = parser.parseEntries(content);
        assertEquals(1, entries.size(), "Should parse one valid entry and skip invalid");
    }

    @Test
    void testIsValidJsonArrayEmptyAndBrackets() {
        assertFalse(parser.isValidJsonArray("")); // empty string
        assertFalse(parser.isValidJsonArray(" ")); // whitespace only
        assertFalse(parser.isValidJsonArray("{}")); // object, not array
        assertTrue(parser.isValidJsonArray("[]")); // empty array, valid JSON array
    }

    @Test
    void testRemoveOuterBracketsEmptyArray() {
        String json = "[]";
        String result = parser.removeOuterBrackets(json);
        assertEquals("", result);
    }

    @Test
    void testParseFieldsWithMalformedField() {
        String fieldsJson = "[{\"name\":\"field1\"},{\"bad_field\":true}]";
        var fields = parser.parseFields(fieldsJson);
        assertEquals(2, fields.size());
        assertEquals("field1", fields.get(0).name());
        assertNull(fields.get(1).name());
    }

    @Test
    void testParseEntryMissingFieldsSection() {
        String json = "{\"name\":\"testWithoutFields\"}";
        var entry = parser.parseEntry(json);
        assertNotNull(entry);
        // No fields should be present
        assertTrue(entry.fields().isEmpty());
    }

    @Test
    void testParseEntriesWithEmptyContent() {
        List<ReflectionEntry> entries = parser.parseEntries("");
        assertTrue(entries.isEmpty());
    }

    @Test
    void testParseEntriesWithWhitespaceOnly() {
        List<ReflectionEntry> entries = parser.parseEntries("     ");
        assertTrue(entries.isEmpty());
    }

    @Test
    void testParseEntriesWithInvalidJsonFragment() {
        String json = "{\"name\":\"validEntry\"},{\"name\":}";
        List<ReflectionEntry> entries = parser.parseEntries(json);
        assertEquals(1, entries.size());
        assertEquals("validEntry", entries.get(0).name());
    }

    @Test
    void testParseBooleanCaseInsensitivity() {
        String jsonTrue = "{\"allPublicConstructors\":\"TRUE\"}";
        String jsonFalse = "{\"allPublicConstructors\":\"FALSE\"}";
        assertTrue(parser.parseBoolean(jsonTrue, "\"allPublicConstructors\""));
        assertFalse(parser.parseBoolean(jsonFalse, "\"allPublicConstructors\""));
    }

    @Test
    void testParseValueReturnsNullWhenQuotesMissing() {
        String json = "{\"name\":testValue}";
        assertNull(parser.parseValue(json, "\"name\""));
    }

    @Test
    void testParseEntryThrowsAndRecoversOnFieldParsingException() {
        String json = "{\"name\":\"testClass\",\"fields\":[{\"badField\":\"foo\"}]}";
        var entry = parser.parseEntry(json);
        // Normal parsing should produce an entry but fields with null names
        assertNotNull(entry);
        assertEquals("testClass", entry.name());
    }

    @Test
    void testRemoveOuterBracketsThrowsExceptionOnShortInput() {
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            parser.removeOuterBrackets("");
        });
    }

    @Test
    void testParseEntryWithAllBooleansFalse() {
        String json = "{\"name\":\"testClass\", \"allDeclaredConstructors\":\"false\", \"allPublicConstructors\":\"false\", "
                + "\"allDeclaredMethods\":\"false\", \"allPublicMethods\":\"false\", \"allPrivateMethods\":\"false\"}";
        var entry = parser.parseEntry(json);
        assertNotNull(entry);
        assertFalse(entry.allDeclaredConstructors());
        assertFalse(entry.allPublicConstructors());
        assertFalse(entry.allDeclaredMethods());
        assertFalse(entry.allPublicMethods());
        assertFalse(entry.allPrivateMethods());
    }

}
