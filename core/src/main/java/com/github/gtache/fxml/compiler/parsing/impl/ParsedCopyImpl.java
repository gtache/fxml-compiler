package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedCopy;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.Map;

/**
 * Implementation of {@link ParsedCopy}
 *
 * @param attributes The reference properties
 */
public record ParsedCopyImpl(Map<String, ParsedProperty> attributes) implements ParsedCopy {

    public ParsedCopyImpl {
        attributes = Map.copyOf(attributes);
    }
}
