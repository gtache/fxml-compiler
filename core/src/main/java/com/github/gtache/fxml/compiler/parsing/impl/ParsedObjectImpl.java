package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Implementation of {@link ParsedObject}
 *
 * @param clazz      The object class
 * @param properties The object properties
 * @param children   The object children (complex properties)
 */
public record ParsedObjectImpl(Class<?> clazz, SequencedMap<String, ParsedProperty> properties,
                               SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> children) implements ParsedObject {

    public ParsedObjectImpl {
        Objects.requireNonNull(clazz);
        properties = new LinkedHashMap<>(properties);
        children = new LinkedHashMap<>(children);
    }

    /**
     * Builder for {@link ParsedObjectImpl}
     */
    public static class Builder {

        private Class<?> clazz;
        private final SequencedMap<String, ParsedProperty> properties;
        private final SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> children;

        /**
         * Creates a new builder
         */
        public Builder() {
            this.properties = new LinkedHashMap<>();
            this.children = new LinkedHashMap<>();
        }

        /**
         * Sets the object class
         *
         * @param clazz The object class
         * @return The builder
         */
        public Builder clazz(final Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        /**
         * Adds a property
         *
         * @param property The property
         * @return The builder
         */
        public Builder addProperty(final ParsedProperty property) {
            properties.put(property.name(), property);
            return this;
        }

        /**
         * Adds a child
         *
         * @param property The property
         * @param child    The child
         * @return The builder
         */
        public Builder addChild(final ParsedProperty property, final ParsedObject child) {
            final var sequence = children.computeIfAbsent(property, k -> new ArrayList<>());
            sequence.add(child);
            children.put(property, sequence);
            return this;
        }

        /**
         * Builds the object
         *
         * @return The object
         */
        public ParsedObjectImpl build() {
            return new ParsedObjectImpl(clazz, properties, children);
        }
    }
}
