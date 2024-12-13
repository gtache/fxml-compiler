package com.github.gtache.fxml.compiler.maven.internal;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Info about FXML file compilation
 *
 * @param inputFile              The input file
 * @param outputFile             The output file
 * @param outputClass            The output class name
 * @param controllerFile         The controller file
 * @param controllerClass        The controller class name
 * @param injectedFields         The injected fields
 * @param injectedMethods        The injected methods
 * @param includes               The FXML inclusions
 * @param requiresResourceBundle True if the file requires a resource bundle
 */
public record CompilationInfo(Path inputFile, Path outputFile, String outputClass, Path controllerFile,
                              String controllerClass, Set<FieldInfo> injectedFields, Set<String> injectedMethods,
                              Map<String, Path> includes, boolean requiresResourceBundle) {

    public CompilationInfo {
        Objects.requireNonNull(inputFile);
        Objects.requireNonNull(outputFile);
        Objects.requireNonNull(outputClass);
        Objects.requireNonNull(controllerFile);
        injectedFields = Set.copyOf(injectedFields);
        injectedMethods = Set.copyOf(injectedMethods);
        includes = Map.copyOf(includes);
    }

    /**
     * Builder for {@link CompilationInfo}
     */
    static class Builder {

        private Path inputFile;
        private Path outputFile;
        private String outputClass;
        private Path controllerFile;
        private String controllerClass;
        private boolean requiresResourceBundle;
        private final Set<FieldInfo> injectedFields;
        private final Set<String> injectedMethods;
        private final Map<String, Path> includes;

        Builder() {
            this.injectedFields = new HashSet<>();
            this.injectedMethods = new HashSet<>();
            this.includes = new HashMap<>();
        }

        Path inputFile() {
            return inputFile;
        }

        Builder inputFile(final Path inputFile) {
            this.inputFile = inputFile;
            return this;
        }

        Builder outputFile(final Path outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        Builder outputClass(final String outputClassName) {
            this.outputClass = outputClassName;
            return this;
        }

        Builder controllerFile(final Path controllerFile) {
            this.controllerFile = controllerFile;
            return this;
        }

        Builder controllerClass(final String controllerClass) {
            this.controllerClass = controllerClass;
            return this;
        }

        Builder addInjectedField(final String field, final String type) {
            injectedFields.add(new FieldInfo(type, field));
            return this;
        }

        Builder addInjectedMethod(final String method) {
            injectedMethods.add(method);
            return this;
        }

        Builder addInclude(final String key, final Path value) {
            this.includes.put(key, value);
            return this;
        }

        Builder requiresResourceBundle() {
            this.requiresResourceBundle = true;
            return this;
        }

        CompilationInfo build() {
            return new CompilationInfo(inputFile, outputFile, outputClass, controllerFile, controllerClass, injectedFields, injectedMethods, includes, requiresResourceBundle);
        }
    }
}
