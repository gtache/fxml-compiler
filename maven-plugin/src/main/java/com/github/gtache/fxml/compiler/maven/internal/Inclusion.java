package com.github.gtache.fxml.compiler.maven.internal;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents an fx:include info
 *
 * @param path  The path to the included file
 * @param count The number of times the file is included
 */
record Inclusion(Path path, int count) {

    /**
     * Instantiates a new Inclusion
     *
     * @param path  The path to the included file
     * @param count The number of times the file is included
     * @throws NullPointerException     if path is null
     * @throws IllegalArgumentException if count < 1
     */
    Inclusion {
        Objects.requireNonNull(path);
        if (count < 1) {
            throw new IllegalArgumentException("count must be >= 1");
        }
    }
}
