package com.github.gtache.fxml.compiler.parsing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Special {@link ParsedObject} for fx:value
 */
public interface ParsedValue extends ParsedObject {

    /**
     * Returns the value from fx:value
     *
     * @return The value
     */
    default String value() {
        final var attribute = attributes().get("fx:value");
        if (attribute == null) {
            throw new IllegalStateException("Missing fx:value");
        } else {
            return attribute.value();
        }
    }

    @Override
    default SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties() {
        return new LinkedHashMap<>();
    }

    @Override
    default SequencedCollection<ParsedObject> children() {
        return List.of();
    }
}
