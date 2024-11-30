package com.github.gtache.fxml.compiler.parsing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Special {@link ParsedObject} for fx:copy
 */
@FunctionalInterface
public interface ParsedCopy extends ParsedObject {

    /**
     * Returns the source from fx:copy
     *
     * @return The source
     */
    default String source() {
        final var attribute = attributes().get("source");
        if (attribute == null) {
            throw new IllegalStateException("Missing source");
        } else {
            return attribute.value();
        }
    }

    @Override
    default String className() {
        return ParsedCopy.class.getName();
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
