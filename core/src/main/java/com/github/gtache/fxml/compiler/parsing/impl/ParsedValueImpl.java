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

    private static final String FX_VALUE = "fx:value";

    /**
     * Instantiates a new value
     *
     * @param className  The value class
     * @param attributes The value properties
     * @throws NullPointerException     If any parameter is null
     * @throws IllegalArgumentException If the attributes don't contain fx:value
     */
    public ParsedValueImpl {
        Objects.requireNonNull(className);
        if (!attributes.containsKey(FX_VALUE)) {
            throw new IllegalArgumentException("Missing " + FX_VALUE);
        }
        attributes = Map.copyOf(attributes);
    }

    /**
     * Instantiates a new value
     *
     * @param className The value class
     * @param value     The value
     * @throws NullPointerException If any parameter is null
     */
    public ParsedValueImpl(final String className, final String value) {
        this(className, Map.of(FX_VALUE, new ParsedPropertyImpl(FX_VALUE, null, value)));
    }
}
