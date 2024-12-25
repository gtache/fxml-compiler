package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedConstant;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link ParsedConstant}
 *
 * @param className  The constant class
 * @param attributes The constant attributes
 */
public record ParsedConstantImpl(String className, Map<String, ParsedProperty> attributes) implements ParsedConstant {

    private static final String FX_CONSTANT = "fx:constant";

    /**
     * Instantiates the constant
     *
     * @param className  The constant class
     * @param attributes The constant attributes
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException If the attributes do not contain fx:constant
     */
    public ParsedConstantImpl {
        Objects.requireNonNull(className);
        if (!attributes.containsKey(FX_CONSTANT)) {
            throw new IllegalArgumentException("Missing " + FX_CONSTANT);
        }
        attributes = Map.copyOf(attributes);
    }

    /**
     * Instantiates the constant
     *
     * @param className The constant class
     * @param value     The constant value
     * @throws NullPointerException if any argument is null
     */
    public ParsedConstantImpl(final String className, final String value) {
        this(className, Map.of(FX_CONSTANT, new ParsedPropertyImpl(FX_CONSTANT, null, value)));
    }
}
