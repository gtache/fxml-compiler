package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedInclude;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

/**
 * Implementation of {@link ParsedInclude}
 *
 * @param properties The object properties
 */
public record ParsedIncludeImpl(SequencedMap<String, ParsedProperty> properties) implements ParsedInclude {

    public ParsedIncludeImpl {
        properties = new LinkedHashMap<>(properties);
    }
}
