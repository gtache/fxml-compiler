package com.github.gtache.fxml.compiler.impl.internal;

import static java.util.Objects.requireNonNull;

/**
 * Used by {@link ConstructorArgs} to store the constructor arguments
 *
 * @param name         The parameter name
 * @param type         The parameter type
 * @param defaultValue The parameter default value
 */
record Parameter(String name, Class<?> type, String defaultValue) {

    /**
     * Instantiates a new Parameter
     *
     * @param name         The parameter name
     * @param type         The parameter type
     * @param defaultValue The parameter default value
     * @throws NullPointerException if any parameter is null
     */
    public Parameter {
        requireNonNull(name);
        requireNonNull(type);
        requireNonNull(defaultValue);
    }
}
