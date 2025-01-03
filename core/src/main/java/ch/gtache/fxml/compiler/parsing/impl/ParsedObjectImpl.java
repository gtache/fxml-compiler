package ch.gtache.fxml.compiler.parsing.impl;

import ch.gtache.fxml.compiler.parsing.ParsedObject;
import ch.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Implementation of {@link ParsedObject}
 *
 * @param className  The object class
 * @param attributes The object attributes
 * @param properties The object properties
 * @param children   The object children
 */
public record ParsedObjectImpl(String className, Map<String, ParsedProperty> attributes,
                               SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties,
                               SequencedCollection<ParsedObject> children) implements ParsedObject {

    /**
     * Instantiates a new object
     *
     * @param className  The object class
     * @param attributes The object attributes
     * @param properties The object properties
     * @param children   The object children
     * @throws NullPointerException if any parameter is null
     */
    public ParsedObjectImpl {
        Objects.requireNonNull(className);
        attributes = Map.copyOf(attributes);
        properties = Collections.unmodifiableSequencedMap(new LinkedHashMap<>(properties));
        children = List.copyOf(children);
    }
}
