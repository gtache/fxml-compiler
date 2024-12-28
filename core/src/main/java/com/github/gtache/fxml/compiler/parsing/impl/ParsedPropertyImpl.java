package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link ParsedProperty}
 *
 * @param name       The property name
 * @param sourceType The property source type
 * @param value      The property value
 */
public record ParsedPropertyImpl(String name, String sourceType, String value) implements ParsedProperty {

    /**
     * Instantiates a property
     *
     * @param name       The property name
     * @param sourceType The property source type
     * @param value      The property value
     * @throws NullPointerException If the name is null
     */
    public ParsedPropertyImpl {
        requireNonNull(name);
    }
}
