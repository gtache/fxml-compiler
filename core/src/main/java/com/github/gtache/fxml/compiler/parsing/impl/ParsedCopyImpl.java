package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedCopy;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.Map;

/**
 * Implementation of {@link ParsedCopy}
 *
 * @param attributes The copy attributes
 */
public record ParsedCopyImpl(Map<String, ParsedProperty> attributes) implements ParsedCopy {

    private static final String SOURCE = "source";

    /**
     * Instantiates the copy
     *
     * @param attributes The copy attributes
     * @throws NullPointerException     If the attributes are null
     * @throws IllegalArgumentException If the attributes don't contain source
     */
    public ParsedCopyImpl {
        attributes = Map.copyOf(attributes);
        if (!attributes.containsKey(SOURCE)) {
            throw new IllegalArgumentException("Missing " + SOURCE);
        }
    }

    /**
     * Instantiates the copy
     *
     * @param source The source
     * @throws NullPointerException If the source is null
     */
    public ParsedCopyImpl(final String source) {
        this(Map.of(SOURCE, new ParsedPropertyImpl(SOURCE, null, source)));
    }
}
