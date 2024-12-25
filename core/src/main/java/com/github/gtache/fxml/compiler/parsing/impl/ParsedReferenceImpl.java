package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.ParsedReference;

import java.util.Map;

/**
 * Implementation of {@link ParsedReference}
 *
 * @param attributes The reference attributes
 */
public record ParsedReferenceImpl(Map<String, ParsedProperty> attributes) implements ParsedReference {

    private static final String SOURCE = "source";

    /**
     * Instantiates a new reference
     *
     * @param attributes The reference attributes
     * @throws NullPointerException     If the attributes are null
     * @throws IllegalArgumentException If the attributes do not contain source
     */
    public ParsedReferenceImpl {
        if (!attributes.containsKey(SOURCE)) {
            throw new IllegalArgumentException("Missing " + SOURCE);
        }
        attributes = Map.copyOf(attributes);
    }

    /**
     * Instantiates a new reference
     *
     * @param source The reference source
     * @throws NullPointerException If the source is null
     */
    public ParsedReferenceImpl(final String source) {
        this(Map.of(SOURCE, new ParsedPropertyImpl(SOURCE, null, source)));
    }
}
