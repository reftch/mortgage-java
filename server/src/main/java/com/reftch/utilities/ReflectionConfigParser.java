package com.reftch.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.reftch.annotation.WebApplication;
import com.reftch.http.server.ReflectionEntry;

public class ReflectionConfigParser {

    private static final Logger logger = Logger.getLogger(ReflectionConfigParser.class.getName());
     
    public List<ReflectionEntry> getReflectionEntries() {
        var entries = new ArrayList<ReflectionEntry>();

        Class<?> mainClass = ClassFinder.findClassByAnnotation(WebApplication.class);
        String json = readJsonFile(mainClass);

        if (json == null || !isValidJsonArray(json)) {
            logger.info(() -> "Invalid JSON format or empty file");
            return entries;
        }

        String content = removeOuterBrackets(json);
        if (content.isEmpty()) {
            return entries;
        }

        return parseEntries(content);
    }

    private String readJsonFile(Class<?> mainClass) {
        try (InputStream is = mainClass.getResourceAsStream("/META-INF/native-image/reflect-config.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
            return jsonContent.toString().trim();
        } catch (IOException e) {
            logger.info(() -> "No classes found " + e.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            logger.info(() -> "Error reading reflect-config.json: " + e.getMessage());
            return null;
        }
    }

    boolean isValidJsonArray(String json) {
        return !json.isEmpty() && json.startsWith("[");
    }

    String removeOuterBrackets(String json) {
        return json.substring(1, json.length() - 1).trim();
    }

    List<ReflectionEntry> parseEntries(String content) {
        var entries = new ArrayList<ReflectionEntry>();
        int objStart = 0;

        while (objStart < content.length()) {
            int nextObjStart = content.indexOf('{', objStart);
            if (nextObjStart == -1)
                break;

            int braceCount = 1;
            int i = nextObjStart + 1;

            while (i < content.length() && braceCount > 0) {
                if (content.charAt(i) == '{') {
                    braceCount++;
                } else if (content.charAt(i) == '}') {
                    braceCount--;
                }
                i++;
            }

            if (braceCount == 0) {
                String obj = content.substring(nextObjStart, i);
                try {
                    ReflectionEntry entry = parseEntry(obj);
                    if (entry != null) {
                        entries.add(entry);
                    }
                } catch (Exception e) {
                    logger.info(() -> "Error parsing object: " + e.getMessage());
                }

                objStart = i;
            } else {
                objStart = nextObjStart + 1;
            }
        }

        return entries;
    }

    ReflectionEntry parseEntry(String obj) {
        String name = parseValue(obj, "\"name\"");
        if (name == null)
            return null;

        boolean allDeclaredConstructors = parseBoolean(obj, "\"allDeclaredConstructors\"");
        boolean allPublicConstructors = parseBoolean(obj, "\"allPublicConstructors\"");
        boolean allDeclaredMethods = parseBoolean(obj, "\"allDeclaredMethods\"");
        boolean allPublicMethods = parseBoolean(obj, "\"allPublicMethods\"");
        boolean allPrivateMethods = parseBoolean(obj, "\"allPrivateMethods\"");

        List<ReflectionEntry.Field> fields = new ArrayList<>();
        int fieldsStart = obj.indexOf("\"fields\"");
        if (fieldsStart != -1) {
            try {
                int fieldsEnd = obj.indexOf("]", fieldsStart);
                if (fieldsEnd != -1) {
                    String fieldsJson = obj.substring(fieldsStart, fieldsEnd + 1);
                    fields = parseFields(fieldsJson);
                }
            } catch (Exception e) {
                logger.info(() -> "Error parsing fields: " + e.getMessage());
            }
        }

        return new ReflectionEntry(name, allDeclaredConstructors, allPublicConstructors,
                allDeclaredMethods, allPublicMethods, allPrivateMethods, fields);
    }

    // Helper methods
    String parseValue(String json, String key) {
        int startIndex = json.indexOf(key);
        if (startIndex == -1)
            return null;

        int valueStart = json.indexOf('"', startIndex + key.length());
        if (valueStart == -1)
            return null;

        int valueEnd = json.indexOf('"', valueStart + 1);
        if (valueEnd == -1)
            return null;

        return json.substring(valueStart + 1, valueEnd).trim();
    }

    boolean parseBoolean(String json, String key) {
        String value = parseValue(json, key);
        return "true".equalsIgnoreCase(value);
    }

    List<ReflectionEntry.Field> parseFields(String fieldsJson) {
        List<ReflectionEntry.Field> fields = new ArrayList<>();
        int startIndex = 0;
        while (startIndex < fieldsJson.length()) {
            int fieldStart = fieldsJson.indexOf('{', startIndex);
            if (fieldStart == -1)
                break;

            int fieldEnd = fieldsJson.indexOf('}', fieldStart);
            if (fieldEnd == -1)
                break;

            String fieldObj = fieldsJson.substring(fieldStart, fieldEnd + 1);
            String name = parseValue(fieldObj, "\"name\"");
            fields.add(new ReflectionEntry.Field(name));

            startIndex = fieldEnd + 1;
        }
        return fields;
    }
}
