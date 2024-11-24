package com.github.gtache.fxml.compiler.impl;

import static java.util.Objects.requireNonNull;

/**
 * Used by {@link ConstructorArgs} to store the constructor arguments
 *
 * @param name         The parameter name
 * @param type         The parameter type
 * @param defaultValue The parameter default value
 */
record Parameter(String name, Class<?> type, String defaultValue) {

    Parameter {
        requireNonNull(name);
        requireNonNull(type);
        requireNonNull(defaultValue);
    }
}
