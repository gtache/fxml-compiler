package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.ParsedReference;

import java.util.Map;

/**
 * Implementation of {@link ParsedReference}
 *
 * @param attributes The reference properties
 */
public record ParsedReferenceImpl(Map<String, ParsedProperty> attributes) implements ParsedReference {

    public ParsedReferenceImpl {
        attributes = Map.copyOf(attributes);
    }
}
