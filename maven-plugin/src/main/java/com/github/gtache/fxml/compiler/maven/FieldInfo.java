package com.github.gtache.fxml.compiler.maven;

import java.util.Objects;

/**
 * Info about a field
 *
 * @param type The field type
 * @param name The field name
 */
record FieldInfo(String type, String name) {
    FieldInfo {
        Objects.requireNonNull(type);
        Objects.requireNonNull(name);
    }
}
