package com.reftch.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.reftch.html.parser.HtmlTemplateParser;

/**
 * Utility class for reading resource files from the classpath.
 * This service provides methods to load and read file contents that are
 * packaged within the application.
 */
public class ResourceService {

    // Cache to store previously loaded file contents
    private static final ConcurrentHashMap<String, String> fileCache = new ConcurrentHashMap<>();
    private final String fileCachePrefix = getClass().getName();

    private static volatile ResourceService instance;

    private ResourceService() {
    }

    public static synchronized ResourceService getInstance() {
         if (instance == null) {
            synchronized (ResourceService.class) {
                if (instance == null) {
                    instance = new ResourceService();
                }
            }
        }
        return instance;
    }

    /**
     * Reads the complete content of a file from the classpath.
     * 
     * @param clazz    The class whose class loader will be used to locate the
     *                 resource
     * @param filePath The path to the resource file relative to the classpath
     * @return A string containing the complete content of the file
     * @throws IOException              If an I/O error occurs while reading the
     *                                  file
     * @throws IllegalArgumentException If the specified file is not found in the
     *                                  classpath
     * 
     * @example
     *          // Reading a properties file
     *          String content = ResourceService.getFileContent(MyClass.class,
     *          "config/application.properties");
     * 
     *          // Reading a JSON file
     *          String jsonContent = ResourceService.getFileContent(MyClass.class,
     *          "data/sample.json");
     * 
     * @note The file must be located in the classpath (e.g., src/main/resources)
     * @note The method automatically handles UTF-8 encoding
     * @note The returned string includes newlines at the end of each line
     */
    public String getFileContent(String filePath) {
        // Create a cache key based on class and file path
        var cacheKey = fileCachePrefix + "::" + filePath;

        // Check if content is already cached
        var cachedContent = fileCache.get(cacheKey);
        if (cachedContent != null) {
            return cachedContent;
        }

        // Load file content if not cached
        var inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            var stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            var content = stringBuilder.toString();
            
            // Cache the loaded content
            fileCache.put(cacheKey, content);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    public String getFileContent(String filePath, Map<String, String> values) {
        var htmlTemplate = getFileContent(filePath);
        String result = HtmlTemplateParser.parseHtmlTemplate(htmlTemplate, values);
        return result;
    }

    /**
     * Clears the file cache, removing all cached content.
     * This method can be useful when you want to force reloading of files.
     */
    public static void clearCache() {
        fileCache.clear();
    }

    /**
     * Removes a specific file from the cache.
     * 
     * @param clazz    The class whose class loader was used to locate the resource
     * @param filePath The path to the resource file relative to the classpath
     */
    public static void removeFromCache(Class<?> clazz, String filePath) {
        String cacheKey = clazz.getName() + "::" + filePath;
        fileCache.remove(cacheKey);
    }

    /**
     * Gets the current size of the cache.
     * 
     * @return The number of cached file contents
     */
    public static int getCacheSize() {
        return fileCache.size();
    }
}
