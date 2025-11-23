package com.reftch.http.server.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteHandler {
    Object controller;
    Method method;
    HttpMethod httpMethod;
    String path;
    Pattern pattern;
    List<String> pathParams = new ArrayList<>();

    public RouteHandler(Object controller, Method method, HttpMethod httpMethod, String path) {
        this.controller = controller;
        this.method = method;
        this.httpMethod = httpMethod;
        this.path = path;

        // Parse path for parameters and create proper regex pattern
        parsePathAndCreatePattern(path);
    }

    private void parsePathAndCreatePattern(String path) {
        // Find all parameter placeholders like {id}
        List<String> params = new ArrayList<>();
        Pattern paramPattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = paramPattern.matcher(path);

        while (matcher.find()) {
            params.add(matcher.group(1));
        }

        this.pathParams = params;

        // Escape special regex characters except for curly braces {} so placeholders
        // remain intact
        String regex = path.replaceAll("([\\\\.$|()\\[\\]^+*?])", "\\\\$1");

        // Replace {param} with capturing groups
        for (String param : params) {
            regex = regex.replace("{" + param + "}", "([^/]+)");
        }

        // Add anchors
        regex = "^" + regex + "$";

        this.pattern = Pattern.compile(regex);
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }
}
