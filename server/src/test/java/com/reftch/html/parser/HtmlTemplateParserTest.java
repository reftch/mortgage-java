package com.reftch.html.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HtmlTemplateParserTest {

    @Mock
    private Map<String, String> values;

    @Test
    void testParseHtmlTemplate_NullHtml_ReturnsNull() {
        // Given
        String html = null;
        Map<String, String> values = new HashMap<>();

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertNull(result);
    }

    @Test
    void testParseHtmlTemplate_NullValues_ReturnsOriginalHtml() {
        // Given
        String html = "<p>Hello {{ name }}</p>";
        Map<String, String> values = null;

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals(html, result);
    }

    @Test
    void testParseHtmlTemplate_EmptyHtml_ReturnsEmpty() {
        // Given
        String html = "";
        Map<String, String> values = new HashMap<>();

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals(html, result);
    }

    @Test
    void testParseHtmlTemplate_NoTemplateExpressions_ReturnsOriginal() {
        // Given
        String html = "<p>Hello World</p>";
        Map<String, String> values = new HashMap<>();

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals(html, result);
    }

    @Test
    void testParseHtmlTemplate_SingleExpression_ReplaceValue() {
        // Given
        String html = "<p>Hello {{ name }}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("name", "John");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Hello John</p>", result);
    }

    @Test
    void testParseHtmlTemplate_MultipleExpressions_ReplaceAll() {
        // Given
        String html = "<p>Hello {{ name }}, you are {{ age }} years old</p>";
        Map<String, String> values = new HashMap<>();
        values.put("name", "John");
        values.put("age", "25");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Hello John, you are 25 years old</p>", result);
    }

    @Test
    void testParseHtmlTemplate_ExpressionNotFound_KeepsOriginal() {
        // Given
        String html = "<p>Hello {{ name }}</p>";
        Map<String, String> values = new HashMap<>();

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Hello {{ name }}</p>", result);
    }

    @Test
    void testParseHtmlTemplate_ExpressionWithWhitespace_ReplaceValue() {
        // Given
        String html = "<p>Hello {{  name  }}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("name", "John");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Hello John</p>", result);
    }

    @Test
    void testParseHtmlTemplate_ExpressionWithSpecialChars_EscapesCorrectly() {
        // Given
        String html = "<p>Value: {{ value }}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("value", "Hello $100");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Value: Hello $100</p>", result);
    }

    @Test
    void testParseHtmlTemplate_SingleConditional_TrueCondition() {
        // Given
        String html = "<p>{{#if user.isLoggedIn}}Welcome {{ name }}{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "true");
        values.put("name", "John");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Welcome John</p>", result);
    }

    @Test
    void testParseHtmlTemplate_SingleConditional_FalseCondition() {
        // Given
        String html = "<p>{{#if user.isLoggedIn == true}}Welcome {{ name }}{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "false");
        values.put("name", "John");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p></p>", result);
    }

    @Test
    void testParseHtmlTemplate_SingleConditional_ExistsCondition_True() {
        // Given
        String html = "<p>{{#if user.isLoggedIn}}Welcome {{ name }}{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "true");
        values.put("name", "John");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Welcome John</p>", result);
    }

    @Test
    void testParseHtmlTemplate_SingleConditional_ExistsCondition_False() {
        // Given
        String html = "<p>{{#if user.isLoggedIn}}Welcome {{ name }}{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "");
        values.put("name", "John");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p></p>", result);
    }

    @Test
    void testParseHtmlTemplate_SingleConditional_Equality_True() {
        // Given
        String html = "<p>{{#if user.role == \"admin\"}}Admin User{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "admin");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Admin User</p>", result);
    }

    @Test
    void testParseHtmlTemplate_SingleConditional_Equality_False() {
        // Given
        String html = "<p>{{#if user.role == \"admin\"}}Admin User{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "user");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p></p>", result);
    }

    @Test
    void testParseHtmlTemplate_SingleConditional_Equality_QuotedStringWithSpaces() {
        // Given
        String html = "<p>{{#if user.role == \"admin role\"}}Admin User{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "admin role");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Admin User</p>", result);
    }

    @Test
    void testParseHtmlTemplate_NestedConditionals() {
        // Given
        String html = "<p>{{#if user.isLoggedIn}}{{#if user.role == \"admin\"}}Admin{{/if}}{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "true");
        values.put("user.role", "admin");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Admin</p>", result);
    }

    @Test
    void testParseHtmlTemplate_NestedConditionals_FalseOuter() {
        // Given
        String html = "<p>{{#if user.isLoggedIn == true}}{{#if user.role == \"admin\"}}Admin{{/if}}{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "false");
        values.put("user.role", "admin");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p></p>", result);
    }

    @Test
    void testParseHtmlTemplate_NestedConditionals_FalseInner() {
        // Given
        String html = "<p>{{#if user.isLoggedIn == true}}{{#if user.role == \"admin\"}}Admin{{/if}}{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "true");
        values.put("user.role", "user");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p></p>", result);
    }

    @Test
    void testParseHtmlTemplate_MixedConditionalsAndExpressions() {
        // Given
        String html = "<p>{{#if user.isLoggedIn}}Hello {{ name }}!{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "true");
        values.put("name", "John");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>Hello John!</p>", result);
    }

    @Test
    void testParseHtmlTemplate_MixedConditionalsAndExpressions_WithFalseCondition() {
        // Given
        String html = "<p>{{#if user.isLoggedIn == true}}Hello {{ name }}!{{/if}}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "false");
        values.put("name", "John");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p></p>", result);
    }

    @Test
    void testParseHtmlTemplate_ExpressionWithSpecialCharacters() {
        // Given
        String html = "<p>{{ name }}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("name", "John & Jane");

        // When
        String result = HtmlTemplateParser.parseHtmlTemplate(html, values);

        // Then
        assertEquals("<p>John & Jane</p>", result);
    }

    @Test
    void testProcessTemplateExpressions_EmptyResultWhenNoMatches() {
        // Given
        String html = "<p>Hello World</p>";
        Map<String, String> values = new HashMap<>();

        // When
        String result = HtmlTemplateParser.processTemplateExpressions(html, values);

        // Then
        assertEquals(html, result);
    }

    @Test
    void testProcessTemplateExpressions_WithMultipleMatches() {
        // Given
        String html = "<p>{{ a }} and {{ b }}</p>";
        Map<String, String> values = new HashMap<>();
        values.put("a", "Apple");
        values.put("b", "Banana");

        // When
        String result = HtmlTemplateParser.processTemplateExpressions(html, values);

        // Then
        assertEquals("<p>Apple and Banana</p>", result);
    }

    @Test
    void testProcessNestedConditionals_EmptyResultWhenNoConditionals() {
        // Given
        String html = "<p>Hello World</p>";
        Map<String, String> values = new HashMap<>();

        // When
        String result = HtmlTemplateParser.processNestedConditionals(html, values);

        // Then
        assertEquals(html, result);
    }

    @Test
    void testEvaluateCondition_Exists_True() {
        // Given
        String condition = "user.isLoggedIn";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "true");

        // When
        boolean result = HtmlTemplateParser.evaluateCondition(condition, values);

        // Then
        assertTrue(result);
    }

    @Test
    void testEvaluateCondition_Exists_False() {
        // Given
        String condition = "user.isLoggedIn";
        Map<String, String> values = new HashMap<>();
        values.put("user.isLoggedIn", "");

        // When
        boolean result = HtmlTemplateParser.evaluateCondition(condition, values);

        // Then
        assertFalse(result);
    }

    @Test
    void testEvaluateCondition_Equality_True() {
        // Given
        String condition = "user.role == \"admin\"";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "admin");

        // When
        boolean result = HtmlTemplateParser.evaluateCondition(condition, values);

        // Then
        assertTrue(result);
    }

    @Test
    void testEvaluateCondition_Equality_False() {
        // Given
        String condition = "user.role == \"admin\"";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "user");

        // When
        boolean result = HtmlTemplateParser.evaluateCondition(condition, values);

        // Then
        assertFalse(result);
    }

    @Test
    void testEvaluateCondition_Equality_WithSpaces() {
        // Given
        String condition = "user.role == \"admin role\"";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "admin role");

        // When
        boolean result = HtmlTemplateParser.evaluateCondition(condition, values);

        // Then
        assertTrue(result);
    }

    @Test
    void testEvaluateCondition_InvalidCondition_ReturnsFalse() {
        // Given
        String condition = "user.role == \"admin\" extra";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "admin");

        // When
        boolean result = HtmlTemplateParser.evaluateCondition(condition, values);

        // Then
        assertFalse(result);
    }

    @Test
    void testEvaluateCondition_NotEquals_ReturnsTrue() {
        // Given
        String condition = "user.role != \"admin\"";
        // String condition = "user.role";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "admin_test");

        // When
        boolean result = HtmlTemplateParser.evaluateCondition(condition, values);

        // Then
        assertTrue(result);
    }

    @Test
    void testEvaluateCondition_NotEquals_ReturnsFalse() {
        // Given
        String condition = "user.role != \"admin\"";
        // String condition = "user.role";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "admin");

        // When
        boolean result = HtmlTemplateParser.evaluateCondition(condition, values);

        // Then
        assertFalse(result);
    }

    @Test
    void testEvaluateCondition_InvalidFormat_ReturnsFalse() {
        // Given
        String condition = "user.role";
        Map<String, String> values = new HashMap<>();
        values.put("user.role", "admin");

        // When
        boolean result = HtmlTemplateParser.evaluateCondition(condition, values);

        // Then
        assertTrue(result);
    }
}
