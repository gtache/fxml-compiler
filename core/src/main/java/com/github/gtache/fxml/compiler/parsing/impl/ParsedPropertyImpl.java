package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.Objects;

/**
 * Implementation of {@link ParsedProperty}
 *
 * @param name       The property name
 * @param sourceType The property source type
 * @param value      The property value
 * @param defines    The property defines
 */
public record ParsedPropertyImpl(String name, String sourceType, String value) implements ParsedProperty {

    public ParsedPropertyImpl {
        Objects.requireNonNull(name);
    }
}
