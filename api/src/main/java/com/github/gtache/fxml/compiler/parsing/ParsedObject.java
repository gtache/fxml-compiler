package com.github.gtache.fxml.compiler.parsing;

import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Parsed object from FXML
 */
public interface ParsedObject {

    /**
     * @return The object class
     */
    Class<?> clazz();

    /**
     * @return The object properties
     */
    SequencedMap<String, ParsedProperty> properties();

    /**
     * @return The object children (complex properties)
     */
    SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> children();
}
