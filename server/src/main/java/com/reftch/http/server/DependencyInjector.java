package com.reftch.http.server;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.reftch.annotation.Controller;
import com.reftch.annotation.Inject;
import com.reftch.annotation.Route;
import com.reftch.annotation.Scan;
import com.reftch.annotation.Service;
import com.reftch.http.server.handler.HttpMethod;
import com.reftch.http.server.handler.RequestProcessor;
import com.reftch.http.server.handler.RouteHandler;
import com.reftch.utilities.ClassFinder;
import com.reftch.utilities.PackageScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DependencyInjector {
    private static final Logger logger = Logger.getLogger(DependencyInjector.class.getName());

    private final Map<String, Object> services = new HashMap<>();
    private final RequestProcessor requestProcessor;

    public DependencyInjector(RequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    public void registerServices() {
        Class<?> mainClass = ClassFinder.findMainClass();
        Scan scanAnnotation = mainClass.getAnnotation(Scan.class);
        if (scanAnnotation != null) {
            String[] packageNames = scanAnnotation.value();
            try {
                for (String pkg : packageNames) {
                    for (Class<?> clazz : PackageScanner.getClassesForPackage(pkg)) {
                        if (clazz.isAnnotationPresent(Service.class)) {
                            logger.log(Level.INFO, "Registering service: {0}", clazz.getName());
                            services.put(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error encountered", e);
            }
        }
    }

    /**
     * Initializes all controllers found in packages marked with the {@code @Scan}
     * annotation.
     *
     * <p>
     * Scans for classes annotated with {@code @Controller}, creates instances,
     * and registers them accordingly.
     * </p>
     */
    public void registerControllers() {
        Class<?> mainClass = ClassFinder.findMainClass();
        Scan scanAnnotation = mainClass.getAnnotation(Scan.class);
        if (scanAnnotation != null) {
            String[] packageNames = scanAnnotation.value();
            try {
                for (String pkg : packageNames) {
                    for (Class<?> clazz : PackageScanner.getClassesForPackage(pkg)) {
                        if (clazz.isAnnotationPresent(Controller.class)) {
                            logger.log(Level.INFO, "Registering controller: {0}", clazz.getName());
                            Object controller = clazz.getDeclaredConstructor().newInstance();
                            injectDependencies(controller);
                            registerRoutes(controller);
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error encountered", e);
            }
        }
    }

    private void injectDependencies(Object instance) throws IllegalAccessException {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object serviceInstance = services.get(field.getType().getName());
                if (serviceInstance != null) {
                    field.set(instance, serviceInstance);
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