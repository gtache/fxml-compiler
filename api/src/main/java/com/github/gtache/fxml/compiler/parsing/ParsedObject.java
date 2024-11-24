package com.github.gtache.fxml.compiler.parsing;

import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Parsed object from FXML
 */
public interface ParsedObject {

    /**
     * The type of the object
     *
     * @return The class name
     */
    String className();

    /**
     * Returns the object's attributes (simple properties)
     *
     * @return The attributes
     */
    Map<String, ParsedProperty> attributes();

    /**
     * Returns the object's complex properties
     *
     * @return The properties
     */
    SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties();
}
