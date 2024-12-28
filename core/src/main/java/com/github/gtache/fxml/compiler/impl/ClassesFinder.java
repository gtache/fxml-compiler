package com.github.gtache.fxml.compiler.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helper class to find classes in a package
 */
public final class ClassesFinder {
    private static final Pattern START_FILE_PATTERN = Pattern.compile("^(?:file:/)?/");

    private ClassesFinder() {

    }

    /**
     * Finds all classes in the given package
     *
     * @param packageName The package
     * @return The classes
     * @throws IOException If an error occurs
     */
    public static Set<String> getClasses(final String packageName) throws IOException {
        return doGetClasses(packageName);
    }

    private static Set<String> doGetClasses(final String packageName) throws IOException {
        final var classLoader = Thread.currentThread().getContextClassLoader();
        final var path = packageName.replace('.', '/');
        final var resources = classLoader.getResources(path);
        final var classes = new HashSet<String>();
        while (resources.hasMoreElements()) {
            final var resource = resources.nextElement();
            final var file = resource.getFile();
            if (file.contains(".jar!")) {
                final var jarFile = file.substring(0, file.indexOf(".jar!") + 4);
                try (final var fs = FileSystems.newFileSystem(Path.of(URI.create(jarFile)), classLoader)) {
                    classes.addAll(findClasses(fs.getPath(path), packageName));
                }
            } else {
                final var filepath = START_FILE_PATTERN.matcher(file).replaceAll("");
                classes.addAll(findClasses(Path.of(filepath), packageName));
            }
        }
        return classes;
    }

    private static List<String> findClasses(final Path directory, final String packageName) throws IOException {
        if (Files.isDirectory(directory)) {
            final var classes = new ArrayList<String>();
            try (final var stream = Files.list(directory)) {
                final var files = stream.toList();
                for (final var file : files) {
                    final var filename = file.getFileName().toString();
                    if (filename.endsWith(".class")) {
                        final var className = packageName + '.' + filename.substring(0, filename.length() - 6);
                        classes.add(className);
                    }
                }
            }
            return classes;
        } else {
            return List.of();
        }
    }
}
