package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Implementation of {@link ParsedObject}
 *
 * @param className  The object class
 * @param attributes The object properties
 * @param properties The object children (complex properties)
 */
public record ParsedObjectImpl(String className, Map<String, ParsedProperty> attributes,
                               SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties) implements ParsedObject {

    public ParsedObjectImpl {
        Objects.requireNonNull(className);
        attributes = Map.copyOf(attributes);
        properties = Collections.unmodifiableSequencedMap(new LinkedHashMap<>(properties));
    }

    /**
     * Builder for {@link ParsedObjectImpl}
     */
    public static class Builder {

        private String className;
        private final Map<String, ParsedProperty> attributes;
        private final SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties;

        /**
         * Creates a new builder
         */
        public Builder() {
            this.attributes = new HashMap<>();
            this.properties = new LinkedHashMap<>();
        }

        /**
         * Sets the object class
         *
         * @param className The object class
         * @return The builder
         */
        public Builder className(final String className) {
            this.className = className;
            return this;
        }

        /**
         * Adds an attribute
         *
         * @param attribute The attribute
         * @return The builder
         */
        public Builder addAttribute(final ParsedProperty attribute) {
            attributes.put(attribute.name(), attribute);
            return this;
        }

        /**
         * Adds a property
         *
         * @param property The property
         * @param child    The property element
         * @return The builder
         */
        public Builder addProperty(final ParsedProperty property, final ParsedObject child) {
            final var sequence = properties.computeIfAbsent(property, k -> new ArrayList<>());
            sequence.add(child);
            properties.put(property, sequence);
            return this;
        }

        /**
         * Builds the object
         *
         * @return The object
         */
        public ParsedObjectImpl build() {
            return new ParsedObjectImpl(className, attributes, properties);
        }
    }
}
