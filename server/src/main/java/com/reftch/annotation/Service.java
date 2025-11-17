package com.reftch.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation used to mark classes as services.
 * 
 * When a class is annotated with @Service, it indicates that the class
 * contains business logic and should be managed as a service component.
 * This annotation is typically used in conjunction with dependency injection
 * to automatically wire up service instances.
 * 
 * The annotation can be applied at the class level and supports an optional
 * value parameter that can be used to specify a custom name for the service.
 * 
 * Example usage:
 * <pre>
 * &#64;Service("userService")
 * public class UserService {
 *     // Service implementation
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    /**
     * Optional custom name for the service.
     * 
     * This value can be used to specify a custom bean name when the service
     * is managed by a dependency injection container. If not specified,
     * the class name will be used as the default service name (with the first
     * letter converted to lowercase).
     * 
     * @return The custom service name (default is empty string)
     */
    String value() default "";
}