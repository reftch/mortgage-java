package com.reftch.http.server;

import java.util.List;

public record ReflectionEntry(
        String name,
        boolean allDeclaredConstructors,
        boolean allPublicConstructors,
        boolean allDeclaredMethods,
        boolean allPublicMethods,
        boolean allPrivateMethods,
        List<Field> fields) {
    public record Field(String name) {
    }
}
