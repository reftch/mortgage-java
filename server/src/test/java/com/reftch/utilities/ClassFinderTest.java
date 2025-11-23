package com.reftch.utilities;

import org.junit.jupiter.api.Test;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import static org.junit.jupiter.api.Assertions.*;

class ClassFinderTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
    }

    @TestAnnotation
    static class AnnotatedClass {
        static Class<?> callFindClassByAnnotation() {
            return ClassFinder.findClassByAnnotation(TestAnnotation.class);
        }
    }

    static class NonAnnotatedClass {
        static Class<?> callFindClassByAnnotation() {
            return ClassFinder.findClassByAnnotation(TestAnnotation.class);
        }
    }

    @Test
    void testFindClassByAnnotation_Found() {
        Class<?> found = AnnotatedClass.callFindClassByAnnotation();
        assertEquals(AnnotatedClass.class, found, "Should find the annotated class in the stack");
    }

    @Test
    void testFindClassByAnnotation_NotFound() {
        // This calls it from NonAnnotatedClass -> ClassFinderTest -> ...
        // ClassFinderTest is NOT annotated.
        Class<?> found = NonAnnotatedClass.callFindClassByAnnotation();
        assertNull(found, "Should not find any annotated class");
    }

    @Test
    void testFindMainClass() {
        // This test is best-effort as we can't easily control the whole stack to
        // guarantee a main class exists or not
        // in the test runner environment. However, we can ensure it doesn't throw
        // exceptions.
        assertDoesNotThrow(() -> {
            Class<?> _ = ClassFinder.findMainClass();
            // In many test environments, the worker process has a main method, so this
            // might not be null.
            // But we won't assert it is not null to avoid flakiness across different
            // runners.
        });
    }

    @Test
    void testGetMainClassSearchInfo() {
        String info = ClassFinder.getMainClassSearchInfo();
        assertNotNull(info);
        assertTrue(info.contains("Main class search initiated"));
        assertTrue(info.contains("Stack trace contains"));
    }
}
