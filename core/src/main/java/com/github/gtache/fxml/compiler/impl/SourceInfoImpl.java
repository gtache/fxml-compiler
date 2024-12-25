package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.SourceInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link SourceInfo}
 *
 * @param generatedClassName     The generated class name
 * @param controllerClassName    The controller class name
 * @param sourceFile             The source file
 * @param includedSources        The included sources
 * @param sourceToSourceInfo     The mapping of source value to source info
 * @param requiresResourceBundle True if the subtree requires a resource bundle
 */
public record SourceInfoImpl(String generatedClassName, String controllerClassName, Path sourceFile,
                             List<SourceInfo> includedSources,
                             Map<String, SourceInfo> sourceToSourceInfo,
                             boolean requiresResourceBundle) implements SourceInfo {

    /**
     * Instantiates a new source info
     * @param generatedClassName     The generated class name
     * @param controllerClassName    The controller class name
     * @param sourceFile             The source file
     * @param includedSources        The included sources
     * @param sourceToSourceInfo     The mapping of source value to source info
     * @param requiresResourceBundle True if the subtree requires a resource bundle
     * @throws NullPointerException If any parameter is null
     */
    public SourceInfoImpl {
        Objects.requireNonNull(generatedClassName);
        Objects.requireNonNull(controllerClassName);
        Objects.requireNonNull(sourceFile);
        includedSources = List.copyOf(includedSources);
        sourceToSourceInfo = Map.copyOf(sourceToSourceInfo);
    }
}
