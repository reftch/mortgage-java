package com.reftch.utilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Utility class for finding and analyzing Java classes.
 * Provides functionality to detect main classes from the calling context
 * and analyze class information.
 */
public class ClassFinder {
    
    /**
     * Attempts to find the main class by analyzing the current thread's stack trace.
     * This method searches through the call stack to identify a class that contains
     * a main method, which is typically the entry point of the application.
     *
     * @return the Class object representing the main class, or null if no main class
     *         can be determined from the current stack trace
     * @throws SecurityException if security manager denies access to class loading
     */
    public static Class<?> findMainClass() {
        // Try to detect main class based on calling context stack trace
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        
        // Iterate through the stack trace from newest to oldest
        for (int i = stack.length - 1; i >= 0; i--) {
            try {
                String className = stack[i].getClassName();
                Class<?> clazz = Class.forName(className);
                
                // Check if this class has a main method
                Method mainMethod = findMainMethod(clazz);
                if (mainMethod != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                // Ignore classes that cannot be loaded and continue searching
                // This is expected for system classes in the stack trace
            } catch (SecurityException e) {
                // Security restrictions prevent class loading - continue searching
                // This can happen in restricted environments
            }
        }
        
        // If no main class found in the stack trace, return null
        return null;
    }

    public static Class<?> findClassByAnnotation(Class<? extends Annotation> annotationClass) {
        // Try to detect main class based on calling context stack trace
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // Iterate through the stack trace from newest to oldest
        for (int i = stack.length - 1; i >= 0; i--) {
            try {
                String className = stack[i].getClassName();
                // System.out.println("Class name :" + className);
                // System.out.println("Clazz name :" + stack[i]);
                Class<?> clazz = Class.forName(className);
                // System.out.println("Clazz :" + clazz.getCanonicalName());
                // System.out.println("Is annotated: " + clazz.isAnnotationPresent(annotationClass));

                if (clazz.isAnnotationPresent(annotationClass)) {
                    return clazz;
                }

            } catch (ClassNotFoundException e) {
                // Ignore classes that cannot be loaded and continue searching
                // This is expected for system classes in the stack trace
            } catch (SecurityException e) {
                // Security restrictions prevent class loading - continue searching
                // This can happen in restricted environments
            }
        }

        // If no main class found in the stack trace, return null
        return null;
    }
    
    /**
     * Helper method to find the main method in a given class.
     * The main method must have the signature: public static void main(String[] args)
     *
     * @param clazz the class to search for the main method
     * @return the Method object representing the main method, or null if not found
     */
    private static Method findMainMethod(Class<?> clazz) {
        try {
            return clazz.getMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            // No main method found in this class, continue searching
            return null;
        } catch (SecurityException e) {
            // Security restrictions prevent method access
            return null;
        }
    }
        
    /**
     * Gets detailed information about the main class detection process.
     * This method can be useful for debugging or logging purposes to understand
     * why a main class couldn't be determined.
     *
     * @return a string describing the search process and results
     */
    public static String getMainClassSearchInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Main class search initiated at: ").append(System.currentTimeMillis()).append("\n");
        
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        info.append("Stack trace contains ").append(stack.length).append(" elements\n");
        
        // Log the first few elements for debugging
        int logCount = Math.min(5, stack.length);
        info.append("First ").append(logCount).append(" stack elements:\n");
        
        for (int i = 0; i < logCount; i++) {
            info.append("  ").append(i).append(": ").append(stack[i].toString()).append("\n");
        }
        
        return info.toString();
    }
}
