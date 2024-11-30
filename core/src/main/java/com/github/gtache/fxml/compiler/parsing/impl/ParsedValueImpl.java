package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.ParsedValue;

import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link ParsedValue}
 *
 * @param className  The value class
 * @param attributes The value properties
 */
public record ParsedValueImpl(String className, Map<String, ParsedProperty> attributes) implements ParsedValue {

    public ParsedValueImpl {
        Objects.requireNonNull(className);
        attributes = Map.copyOf(attributes);
    }
}
