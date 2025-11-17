package com.reftch.annotation;

import java.lang.annotation.*;

/**
 * Annotation used to specify packages to scan for HTTP controllers and components.
 *
 * When a class is annotated with @Scan, it indicates that the specified packages
 * should be scanned for classes annotated with @Controller or other component annotations.
 * This allows the framework to automatically discover and register HTTP endpoints
 * without requiring explicit configuration.
 *
 * The annotation must be applied at the class level and accepts an array of package
 * names to scan for components.
 *
 * Example usage:
 * <pre>
 * &#64;Scan({"com.example.controller", "com.example.service"})
 * public class ApplicationConfig {
 *     // This configuration will scan the specified packages
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scan {
    /**
     * Array of package names to scan for HTTP controllers and components.
     *
     * This parameter specifies which packages should be scanned for classes
     * annotated with @Controller or other framework annotations. The scanning
     * process will recursively search through the specified packages and their
     * sub-packages.
     *
     * @return Array of package names to scan (default is empty array)
     */
    String[] value();
}