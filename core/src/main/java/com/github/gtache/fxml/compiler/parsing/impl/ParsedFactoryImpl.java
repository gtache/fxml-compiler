package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedFactory;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;

/**
 * Implementation of {@link ParsedFactory}
 *
 * @param className  The factory class
 * @param attributes The factory properties
 * @param arguments  The factory arguments
 */
public record ParsedFactoryImpl(String className, Map<String, ParsedProperty> attributes,
                                SequencedCollection<ParsedObject> arguments,
                                SequencedCollection<ParsedObject> children) implements ParsedFactory {

    public ParsedFactoryImpl {
        Objects.requireNonNull(className);
        attributes = Map.copyOf(attributes);
        arguments = List.copyOf(arguments);
        children = List.copyOf(children);
    }
}
