package com.github.gtache.fxml.compiler;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Info about a source file
 */
public interface SourceInfo {

    /**
     * Returns the generated view class name
     *
     * @return The class name
     */
    String generatedClassName();

    /**
     * Returns the controller class name
     *
     * @return The class name
     */
    String controllerClassName();

    /**
     * Returns the source file
     *
     * @return The file
     */
    Path sourceFile();

    /**
     * Returns the included sources.
     * Note that there can be multiple times the same source.
     *
     * @return The sources
     */
    List<SourceInfo> includedSources();

    /**
     * Returns the mapping of source value to source info
     *
     * @return The mapping
     */
    Map<String, SourceInfo> sourceToSourceInfo();

    /**
     * Returns whether the source or its children requires a resource bundle
     *
     * @return True if the subtree requires a resource bundle
     */
    boolean requiresResourceBundle();
}
