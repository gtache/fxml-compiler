package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedInclude;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.Map;

/**
 * Implementation of {@link ParsedInclude}
 *
 * @param attributes The object properties
 */
public record ParsedIncludeImpl(Map<String, ParsedProperty> attributes) implements ParsedInclude {

    public ParsedIncludeImpl {
        attributes = Map.copyOf(attributes);
    }
}
