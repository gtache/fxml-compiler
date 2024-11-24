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
     * Returns the subcontroller id (if present)
     *
     * @return The id
     */
    default String controllerId() {
        final var property = attributes().get("fx:id");
        if (property == null) {
            return null;
        } else {
            return property.value() + "Controller";
        }
    }

    /**
     * Returns the subview resources path if present
     *
     * @return The resources
     */
    default String resources() {
        final var property = attributes().get("resources");
        if (property == null) {
            return null;
        } else {
            return property.value().replace("/", ".");
        }
    }

    /**
     * Returns the include source
     *
     * @return The source
     */
    default String source() {
        final var property = attributes().get("source");
        if (property == null) {
            throw new IllegalStateException("Missing source");
        } else {
            return property.value();
        }
    }

    @Override
    default String className() {
        return ParsedInclude.class.getName();
    }

    @Override
    default SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties() {
        return new LinkedHashMap<>();
    }
}
