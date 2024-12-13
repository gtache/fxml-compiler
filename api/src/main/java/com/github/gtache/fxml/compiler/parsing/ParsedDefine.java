package com.github.gtache.fxml.compiler.parsing;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Special {@link ParsedObject} for fx:define
 */
@FunctionalInterface
public interface ParsedDefine extends ParsedObject {

    @Override
    default String className() {
        return ParsedDefine.class.getName();
    }

    @Override
    default Map<String, ParsedProperty> attributes() {
        return Map.of();
    }

    @Override
    default SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties() {
        return new LinkedHashMap<>();
    }
}
