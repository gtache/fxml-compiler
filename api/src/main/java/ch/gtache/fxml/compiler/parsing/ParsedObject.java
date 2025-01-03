package ch.gtache.fxml.compiler.parsing;

import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Parsed object from FXML
 */
public interface ParsedObject {

    /**
     * Returns the type of the object
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

    /**
     * Returns the children (fx:define, fx:copy, etc.) contained in this object
     *
     * @return The children
     */
    SequencedCollection<ParsedObject> children();
}
