package com.reftch.utilities;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class JsonParser {

    public static String parseString(String json, String key) {
        // Find the key
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return null;
        }

        // Find the start of the value (after the colon)
        int valueStart = keyIndex + searchKey.length();

        // Skip whitespace
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        // Handle string values (start with quote)
        if (valueStart < json.length() && json.charAt(valueStart) == '"') {
            int startQuote = valueStart;
            int endQuote = json.indexOf('"', startQuote + 1);

            // Handle escaped quotes
            while (endQuote != -1 && endQuote > 0 && json.charAt(endQuote - 1) == '\\') {
                endQuote = json.indexOf('"', endQuote + 1);
            }

            if (endQuote != -1) {
                return json.substring(startQuote + 1, endQuote);
            }
        }

        return null;
    }

    public static int parseInt(String json, String key) {
        // Find the key
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return -1;
        }

        // Find the start of the value (after the colon)
        int valueStart = keyIndex + searchKey.length();

        // Skip whitespace
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        // Find the end of the number (space or comma or })
        int valueEnd = valueStart;
        while (valueEnd < json.length() &&
                (Character.isDigit(json.charAt(valueEnd)) || json.charAt(valueEnd) == '-')) {
            valueEnd++;
        }

        try {
            return Integer.parseInt(json.substring(valueStart, valueEnd).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String writeValueAsString(Object obj) throws Exception {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof String) {
            return "\"" + escapeString((String) obj) + "\"";
        }

        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        if (obj instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            return "\"" + sdf.format((Date) obj) + "\"";
        }

        if (obj instanceof Collection) {
            return writeCollectionAsString((Collection<?>) obj);
        }

        if (obj.getClass().isArray()) {
            return writeArrayAsString(obj);
        }

        return writeObjectAsString(obj);
    }

    private static String writeCollectionAsString(Collection<?> collection) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;
        for (Object item : collection) {
            if (!first) {
                sb.append(",");
            }
            sb.append(writeValueAsString(item));
            first = false;
        }

        sb.append("]");
        return sb.toString();
    }

    private static String writeArrayAsString(Object array) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        int length = java.lang.reflect.Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            Object item = java.lang.reflect.Array.get(array, i);
            sb.append(writeValueAsString(item));
        }

        sb.append("]");
        return sb.toString();
    }

    private static String writeObjectAsString(Object obj) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        boolean first = true;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value == null) {
                    continue;
                }

                if (!first) {
                    sb.append(",");
                }

                sb.append("\"").append(field.getName()).append("\":");
                sb.append(writeValueAsString(value));
                first = false;
            } catch (IllegalAccessException e) {
                // Skip inaccessible fields
            }
        }

        sb.append("}");
        return sb.toString();
    }

    private static String escapeString(String str) {
        if (str == null)
            return "";

        StringBuilder escaped = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
    }

}
