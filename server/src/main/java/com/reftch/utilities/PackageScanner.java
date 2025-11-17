package com.reftch.utilities;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class for scanning and loading classes from packages.
 * Provides functionality to discover all classes within a specified package,
 * whether they are in JAR files or class directories.
 */
public class PackageScanner {
    
    /**
     * Retrieves all classes from the specified package, including those in JAR files
     * and class directories. This method handles both jar-based and file-system based
     * package scanning.
     *
     * @param packageName the name of the package to scan for classes
     * @return a list of Class objects representing all found classes in the package
     * @throws IOException if there's an issue reading resources or files
     * @throws ClassNotFoundException if a class cannot be loaded
     */
    public static List<Class<?>> getClassesForPackage(String packageName)
            throws IOException, ClassNotFoundException {
        
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be null or empty");
        }
        
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        
        // Get all resources for the package path
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            processResource(resource, path, packageName, classes);
        }
        
        return classes;
    }
    
    /**
     * Processes a single resource (either jar file or directory) to find classes.
     *
     * @param resource the URL resource to process
     * @param path the package path as a string
     * @param packageName the original package name
     * @param classes the list to add found classes to
     * @throws IOException if there's an issue reading the resource
     * @throws ClassNotFoundException if a class cannot be loaded
     */
    private static void processResource(URL resource, String path, String packageName, List<Class<?>> classes)
            throws IOException, ClassNotFoundException {
        
        String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
        
        if (isJarResource(filePath)) {
            processJarResource(filePath, path, classes);
        } else {
            processDirectoryResource(resource, packageName, path, classes);
        }
    }
    
    /**
     * Determines if the resource is a JAR file resource.
     *
     * @param filePath the decoded file path
     * @return true if the resource is a JAR file, false otherwise
     */
    private static boolean isJarResource(String filePath) {
        return filePath.startsWith("file:") && filePath.contains("!");
    }
    
    /**
     * Processes a JAR file resource to find and load classes.
     *
     * @param filePath the file path of the JAR
     * @param path the package path in the JAR
     * @param classes the list to add found classes to
     * @throws IOException if there's an issue reading the JAR file
     * @throws ClassNotFoundException if a class cannot be loaded
     */
    private static void processJarResource(String filePath, String path, List<Class<?>> classes)
            throws IOException, ClassNotFoundException {
        
        String jarPath = filePath.substring(5, filePath.indexOf("!"));
        try (JarFile jar = new JarFile(jarPath)) {
            processJarEntries(jar, path, classes);
        }
    }
    
    /**
     * Processes all entries in a JAR file to find classes matching the package path.
     *
     * @param jar the JarFile to process
     * @param path the package path to match against
     * @param classes the list to add found classes to
     * @throws ClassNotFoundException if a class cannot be loaded
     */
    private static void processJarEntries(JarFile jar, String path, List<Class<?>> classes)
            throws ClassNotFoundException {
        
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            
            if (isClassEntry(entryName, path)) {
                String className = convertJarEntryToClassName(entryName);
                classes.add(Class.forName(className));
            }
        }
    }
    
    /**
     * Determines if a JAR entry represents a valid class file for the target package.
     *
     * @param entryName the name of the JAR entry
     * @param path the package path to match against
     * @return true if the entry is a class file in the target package, false otherwise
     */
    private static boolean isClassEntry(String entryName, String path) {
        return entryName.startsWith(path) && 
               entryName.endsWith(".class") && 
               !entryName.contains("$");
    }
    
    /**
     * Converts a JAR entry name to a fully qualified class name.
     *
     * @param entryName the JAR entry name
     * @return the fully qualified class name
     */
    private static String convertJarEntryToClassName(String entryName) {
        return entryName.replace('/', '.').replace(".class", "");
    }
    
    /**
     * Processes a directory resource to find and load classes.
     *
     * @param resource the URL resource pointing to the directory
     * @param packageName the original package name
     * @param path the package path as a string
     * @param classes the list to add found classes to
     * @throws IOException if there's an issue reading files
     * @throws ClassNotFoundException if a class cannot be loaded
     */
    private static void processDirectoryResource(URL resource, String packageName, String path, List<Class<?>> classes)
            throws IOException, ClassNotFoundException {
        
        File dir = new File(resource.getFile());
        if (dir.exists() && dir.isDirectory()) {
            findClassesInDirectory(packageName, dir, classes);
        }
    }
    
    /**
     * Recursively scans a directory for class files and loads them.
     *
     * @param packageName the package name to scan
     * @param dir the directory to scan for classes
     * @param classes the list to add found classes to
     * @throws ClassNotFoundException if a class cannot be loaded
     */
    private static void findClassesInDirectory(String packageName, File dir, List<Class<?>> classes)
            throws ClassNotFoundException {
        
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively process subdirectories
                findClassesInDirectory(packageName + "." + file.getName(), file, classes);
            } else if (isClassFile(file)) {
                // Load the class file
                String className = packageName + '.' + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
    }
    
    /**
     * Determines if a file is a valid class file (not an inner class).
     *
     * @param file the file to check
     * @return true if the file is a class file without inner class markers, false otherwise
     */
    private static boolean isClassFile(File file) {
        return file.getName().endsWith(".class") && !file.getName().contains("$");
    }
    
    /**
     * Gets all non-abstract, concrete classes from a package that implement or extend a specific interface or class.
     * 
     * @param packageName the package to scan
     * @param superClass the superclass or interface to filter by
     * @return a list of concrete classes that extend or implement the specified class/interface
     * @throws IOException if there's an issue reading resources or files
     * @throws ClassNotFoundException if a class cannot be loaded
     */
    public static List<Class<?>> getClassesForPackageWithSuperclass(String packageName, Class<?> superClass)
            throws IOException, ClassNotFoundException {
        
        List<Class<?>> allClasses = getClassesForPackage(packageName);
        List<Class<?>> filteredClasses = new ArrayList<>();
        
        for (Class<?> clazz : allClasses) {
            if (superClass.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                filteredClasses.add(clazz);
            }
        }
        
        return filteredClasses;
    }
}
