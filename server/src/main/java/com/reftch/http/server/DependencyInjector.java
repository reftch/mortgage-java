package com.reftch.http.server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.reftch.annotation.Controller;
import com.reftch.annotation.Inject;
import com.reftch.annotation.Route;
import com.reftch.annotation.Service;
import com.reftch.http.server.handler.HttpMethod;
import com.reftch.http.server.handler.RequestProcessor;
import com.reftch.http.server.handler.RouteHandler;
import com.reftch.utilities.ReflectionConfigParser;

public class DependencyInjector {
    private static final Logger logger = Logger.getLogger(DependencyInjector.class.getName());

    private final Map<String, Object> services = new HashMap<>();
    private final RequestProcessor requestProcessor;
    private final List<ReflectionEntry> reflectionEntries;

    public DependencyInjector(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
        ReflectionConfigParser parser = new ReflectionConfigParser();
        this.reflectionEntries = parser.getReflectionEntries();

        registerServices();
    }

    public void registerServices() {
        try {
            // Phase 1: Instantiate Services
            instantiateServices();

            // Phase 2: Inject dependencies into Services
            injectServiceDependencies();

            // Phase 3: Register Controllers
            registerControllers();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during registering services", e);
        }
    }

    private void instantiateServices() {
        for (var entry : reflectionEntries) {
            try {
                Class<?> clazz = Class.forName(entry.name());
                if (clazz.isAnnotationPresent(Service.class)) {
                    logger.log(Level.INFO, "Registering service: {0}", clazz.getName());
                    services.put(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to instantiate service: " + entry.name(), e);
            }
        }
    }

    private void injectServiceDependencies() {
        for (Object service : services.values()) {
            try {
                injectDependencies(service, service.getClass());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to inject dependencies for service: " + service.getClass().getName(),
                        e);
            }
        }
    }

    private void registerControllers() {
        for (var entry : reflectionEntries) {
            try {
                Class<?> clazz = Class.forName(entry.name());
                if (clazz.isAnnotationPresent(Controller.class)) {
                    logger.log(Level.INFO, "Registering controller: {0}", clazz.getName());
                    Object controller = clazz.getDeclaredConstructor().newInstance();
                    injectDependencies(controller, clazz);
                    registerRoutes(controller);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to instantiate controller: " + entry.name(), e);
            }
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

}