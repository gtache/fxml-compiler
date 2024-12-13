package com.github.gtache.fxml.compiler.maven.internal;

import java.util.Objects;

/**
 * Info about a field
 *
 * @param type The field type
 * @param name The field name
 */
record FieldInfo(String type, String name) {

    /**
     * Instantiates a new info
     *
     * @param type The field type
     * @param name The field name
     * @throws NullPointerException if any parameter is null
     */
    FieldInfo {
        Objects.requireNonNull(type);
        Objects.requireNonNull(name);
    }
}
