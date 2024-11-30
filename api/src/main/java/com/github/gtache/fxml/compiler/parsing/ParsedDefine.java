package com.github.gtache.fxml.compiler.parsing;

import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Special {@link ParsedObject} for fx:define
 */
@FunctionalInterface
public interface ParsedDefine extends ParsedObject {

    /**
     * Returns the object defined by this fx:define
     *
     * @return The object
     */
    ParsedObject object();

    @Override
    default String className() {
        return object().className();
    }

    @Override
    default Map<String, ParsedProperty> attributes() {
        return object().attributes();
    }

    @Override
    default SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties() {
        return object().properties();
    }

    @Override
    default SequencedCollection<ParsedObject> children() {
        return object().children();
    }
}
