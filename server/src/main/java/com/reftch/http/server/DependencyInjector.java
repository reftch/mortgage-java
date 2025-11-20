package com.reftch.http.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.reftch.annotation.Controller;
import com.reftch.annotation.Inject;
import com.reftch.annotation.Route;
import com.reftch.annotation.Service;
import com.reftch.annotation.WebApplication;
import com.reftch.http.server.handler.HttpMethod;
import com.reftch.http.server.handler.RequestProcessor;
import com.reftch.http.server.handler.RouteHandler;
import com.reftch.utilities.ClassFinder;

public class DependencyInjector {
    private static final Logger logger = Logger.getLogger(DependencyInjector.class.getName());

    private final Map<String, Object> services = new HashMap<>();
    private final RequestProcessor requestProcessor;
    private final List<ReflectionEntry> reflectionEntries;

    public DependencyInjector(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
        this.reflectionEntries = getReflectionEntries();

        registerServices();
    }

    public void registerServices() {
        reflectionEntries.forEach(c -> System.out.println(c));

        try {
            for (var entry : reflectionEntries) {
                Class<?> clazz = Class.forName(entry.name());
                if (clazz.isAnnotationPresent(Service.class)) {
                    logger.log(Level.INFO, "Registering service: {0}", clazz.getName());
                    services.put(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
                } else if (clazz.isAnnotationPresent(Controller.class)) {
                    logger.log(Level.INFO, "Registering controller: {0}", clazz.getName());
                    Object controller = clazz.getDeclaredConstructor().newInstance();
                    injectDependencies(controller, clazz);
                    registerRoutes(controller);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during registering services", e);
        }
    }

    private void injectDependencies(Object instance, Class<?> clazz) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                var fieldName = field.getType().getName();
                Object serviceInstance = services.get(fieldName);
                if (serviceInstance != null) {
                    field.set(instance, serviceInstance);
                    logger.info(() -> "Injected service: " + fieldName);
                }
            }
        }
    }

    // Method to register controller
    private void registerRoutes(Object controller) {
        Class<?> controllerClass = controller.getClass();
        String basePath = "";

        // Get base path from Controller annotation
        if (controllerClass.isAnnotationPresent(Controller.class)) {
            basePath = controllerClass.getAnnotation(Controller.class).basePath();
            if (basePath.isEmpty() || !basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            if (basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
        }

        // Scan methods for Route annotations
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Route.class)) {
                Route route = method.getAnnotation(Route.class);
                HttpMethod httpMethod = HttpMethod.valueOf(route.method().toUpperCase());
                String path = route.path();

                // Combine base path and method path
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                String fullPath = basePath + path;
                logger.log(Level.INFO, "Registering route: {0} {1} -> {2}.{3}()",
                        new Object[] { httpMethod.name(), fullPath, controllerClass.getName(), method.getName() });
                requestProcessor.getRouteHandlers().add(new RouteHandler(controller, method, httpMethod, fullPath));
            }
        }
    }

    private List<ReflectionEntry> getReflectionEntries() {
        var entries = new ArrayList<ReflectionEntry>();

        // Load reflection-config.json from resources
        Class<?> mainClass = ClassFinder.findClassByAnnotation(WebApplication.class);
        try (InputStream is = mainClass.getResourceAsStream("/META-INF/native-image/reflect-config.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }

            String json = jsonContent.toString().trim();

            // Handle case where JSON might be malformed or empty
            if (json.isEmpty() || !json.startsWith("[")) {
                logger.info(() -> "Invalid JSON format or empty file");
                return entries;
            }

            // Remove outer brackets and split into objects
            String content = json.substring(1, json.length() - 1).trim();
            if (content.isEmpty()) {
                return entries;
            }

            // Split by object boundaries (this is a simplified approach)
            int objStart = 0;

            while (objStart < content.length()) {
                // Find next object start
                int nextObjStart = content.indexOf('{', objStart);
                if (nextObjStart == -1)
                    break;

                // Find matching closing brace
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
                        // Parse the object manually
                        String name = parseValue(obj, "\"name\"");
                        if (name == null) {
                            objStart = i;
                            continue;
                        }

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
                                // Log error but continue parsing
                                logger.info(() -> "Error parsing fields: " + e.getMessage());
                            }
                        }

                        entries.add(new ReflectionEntry(name, allDeclaredConstructors, allPublicConstructors,
                                allDeclaredMethods, allPublicMethods, allPrivateMethods, fields));
                    } catch (Exception e) {
                        logger.info(() -> "Error parsing object: " + e.getMessage());
                    }

                    objStart = i;
                } else {
                    objStart = nextObjStart + 1;
                }
            }

        } catch (IOException e) {
            logger.info(() -> "No classes found " + e.getLocalizedMessage());
        } catch (Exception e) {
            logger.info(() -> "Error parsing reflect-config.json: " + e.getMessage());
        }

        return entries;
    }

    // Helper method to extract a value from JSON string
    private String parseValue(String json, String key) {
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

    // Helper method to extract a boolean value from JSON string
    private boolean parseBoolean(String json, String key) {
        String value = parseValue(json, key);
        return "true".equalsIgnoreCase(value);
    }

    // Helper method to parse fields array
    private List<ReflectionEntry.Field> parseFields(String fieldsJson) {
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