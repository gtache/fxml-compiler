package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedConstant;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link ParsedConstant}
 *
 * @param className  The constant class
 * @param attributes The constant properties
 */
public record ParsedConstantImpl(String className, Map<String, ParsedProperty> attributes) implements ParsedConstant {

    public ParsedConstantImpl {
        Objects.requireNonNull(className);
        attributes = Map.copyOf(attributes);
    }
}
