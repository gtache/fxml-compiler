package com.github.gtache.fxml.compiler.compatibility.impl;

import com.github.gtache.fxml.compiler.compatibility.GenerationCompatibility;

/**
 * Implementation of {@link GenerationCompatibility}
 *
 * @param javaVersion The minimum supported Java version
 */
public record GenerationCompatibilityImpl(int javaVersion) implements GenerationCompatibility {

    /**
     * Instantiates a new compatibility
     *
     * @param javaVersion The minimum supported Java version
     */
    public GenerationCompatibilityImpl {
        if (javaVersion < 8) {
            throw new IllegalArgumentException("Java version must be at least 8");
        }
    }
}
