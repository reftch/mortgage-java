package com.reftch.html.parser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced HTML template parser supporting nested conditionals and template
 * substitutions.
 * 
 * <p>
 * This parser supports:
 * </p>
 * <ul>
 * <li>Template expressions: <code>{{ key }}</code></li>
 * <li>Nested conditional blocks:
 * <code>{{#if condition}} ... {{/if}}</code></li>
 * <li>Conditional evaluation using equality (<code>==</code>) or existence
 * checks</li>
 * </ul>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * String html = "&lt;p&gt;Hello {{ name }}!&lt;/p&gt;&lt;div&gt;{{#if showMessage}}&lt;p&gt;Message: {{ message }}&lt;/p&gt;{{/if}}&lt;/div&gt;";
 * Map&lt;String, String&gt; values = Map.of("name", "John", "showMessage", "true", "message", "Welcome!");
 * String result = HtmlTemplateParser.parseHtmlTemplate(html, values);
 * </pre>
 */
public class HtmlTemplateParser {

    /**
     * Regular expression pattern used to match template expressions like
     * <code>{{ key }}</code>.
     * It captures content between double curly braces.
     */
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+?)\\s*\\}}");

    /**
     * Regular expression pattern used to match opening conditional blocks:
     * <code>{{#if condition}}</code>.
     */
    private static final Pattern OPEN_IF_PATTERN = Pattern.compile("\\{\\{#if\\s+([^}]+)\\}\\}");

    /**
     * Regular expression pattern used to match closing conditional blocks:
     * <code>{{/if}}</code>.
     */
    private static final Pattern CLOSE_IF_PATTERN = Pattern.compile("\\{\\{/if\\}\\}");

    /**
     * Parses an HTML string by replacing template expressions and evaluating
     * conditional blocks.
     *
     * @param html   The raw HTML string containing template expressions and
     *               conditionals.
     * @param values A map of keys to their corresponding values for substitution.
     * @return The processed HTML string with substitutions and conditional logic
     *         applied.
     *         Returns the original input if either argument is null.
     */
    public static String parseHtmlTemplate(String html, Map<String, String> values) {
        if (html == null || values == null) {
            return html;
        }

        var processed = processNestedConditionals(html, values);
        return processTemplateExpressions(processed, values);
    }

    /**
     * Processes nested conditional blocks in the input HTML.
     * 
     * <p>
     * This method recursively evaluates {{#if ...}} ... {{/if}} blocks, handling
     * nesting correctly.
     * </p>
     * 
     * @param html   The input HTML string containing conditional blocks.
     * @param values The map used for evaluating conditions and substituting
     *               variables.
     * @return The HTML string with processed conditional blocks.
     */
    static String processNestedConditionals(String html, Map<String, String> values) {
        var startIndex = html.indexOf("{{#if");
        if (startIndex == -1) {
            return html;
        }

        var result = new StringBuilder();
        var lastIndex = 0;

        while (startIndex != -1) {
            // Append text before condition
            result.append(html, lastIndex, startIndex);

            var openMatcher = OPEN_IF_PATTERN.matcher(html);
            openMatcher.region(startIndex, html.length());

            if (!openMatcher.find()) {
                break;
            }

            var condition = openMatcher.group(1).trim();
            var openEnd = openMatcher.end();

            // Find matching {{/if}} taking nesting into account
            var searchIndex = openEnd;
            var depth = 1;
            var closeStart = -1;

            while (depth > 0) {
                var nextOpen = OPEN_IF_PATTERN.matcher(html);
                nextOpen.region(searchIndex, html.length());
                var nextClose = CLOSE_IF_PATTERN.matcher(html);
                nextClose.region(searchIndex, html.length());

                var hasOpen = nextOpen.find();
                var hasClose = nextClose.find();

                if (!hasClose) {
                    break;
                }

                if (hasOpen && nextOpen.start() < nextClose.start()) {
                    depth++;
                    searchIndex = nextOpen.end();
                } else {
                    depth--;
                    if (depth == 0) {
                        closeStart = nextClose.start();
                        searchIndex = nextClose.end();
                    } else {
                        searchIndex = nextClose.end();
                    }
                }
            }

            if (closeStart == -1) {
                break; // unmatched {{#if}}
            }

            var inner = html.substring(openEnd, closeStart);
            var processedInner = processNestedConditionals(inner, values);

            var conditionMet = evaluateCondition(condition, values);
            if (conditionMet) {
                result.append(processedInner);
            }

            lastIndex = html.indexOf("{{/if}}", closeStart) + "{{/if}}".length();
            startIndex = html.indexOf("{{#if", lastIndex);
        }

        // Append remainder
        result.append(html.substring(lastIndex));
        return result.toString();
    }

    /**
     * Evaluates a conditional expression based on the provided values map.
     *
     * <p>
     * Supports two types of conditions:
     * </p>
     * <ul>
     * <li><strong>Equality check</strong>: e.g.,
     * <code>status == "active"</code></li>
     * <li><strong>Existence check</strong>: e.g., <code>showMessage</code></li>
     * </ul>
     *
     * @param condition The condition string to evaluate.
     * @param values    The map of variable names to their values.
     * @return True if the condition evaluates to true; false otherwise.
     */
    static boolean evaluateCondition(String condition, Map<String, String> values) {
        if (condition.contains("==")) {
            var parts = condition.split("\\s*==\\s*", 2);
            if (parts.length < 2) {
                return false;
            }
            var key = parts[0].trim();
            var expected = parts[1].trim().replaceAll("^\"|\"$", "");
            var actual = values.get(key);
            return expected.equals(actual);
        } else if (condition.contains("!=")) {
            var parts = condition.split("\\s*!=\\s*", 2);
            if (parts.length < 2) {
                return false;
            }
            var key = parts[0].trim();
            var expected = parts[1].trim().replaceAll("^\"|\"$", "");
            var actual = values.get(key);
            return !expected.equals(actual);
        } else {
            var value = values.get(condition);
            return value != null && !value.isEmpty();
        }
    }

    /**
     * Replaces all template placeholders like <code>{{ key }}</code> with values
     * from the provided map.
     *
     * @param html   The input HTML string containing template expressions.
     * @param values The map of keys to their corresponding replacement values.
     * @return The HTML string with all template placeholders replaced.
     */
    static String processTemplateExpressions(String html, Map<String, String> values) {
        var matcher = TEMPLATE_PATTERN.matcher(html);
        var sb = new StringBuffer();

        while (matcher.find()) {
            var key = matcher.group(1).trim();
            var replacement = values.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
