package com.github.gtache.fxml.compiler.parsing;

import java.util.LinkedHashMap;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Special {@link ParsedObject} for fx:include
 */
@FunctionalInterface
public interface ParsedInclude extends ParsedObject {

    /**
     * @return The controller id if present
     */
    default String controllerId() {
        final var property = properties().get("fx:id");
        if (property == null) {
            return null;
        } else {
            return property.value() + "Controller";
        }
    }

    /**
     * @return The resources if present
     */
    default String resources() {
        final var property = properties().get("resources");
        if (property == null) {
            return null;
        } else {
            return property.value().replace("/", ".");
        }
    }

    /**
     * @return The source
     */
    default String source() {
        final var property = properties().get("source");
        if (property == null) {
            throw new IllegalStateException("Missing source");
        } else {
            return property.value();
        }
    }

    @Override
    default Class<?> clazz() {
        return ParsedInclude.class;
    }

    @Override
    default SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> children() {
        return new LinkedHashMap<>();
    }
}
