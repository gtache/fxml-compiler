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
 * @param attributes The factory attributes
 * @param arguments  The factory arguments
 * @param children   The factory children
 */
public record ParsedFactoryImpl(String className, Map<String, ParsedProperty> attributes,
                                SequencedCollection<ParsedObject> arguments,
                                SequencedCollection<ParsedObject> children) implements ParsedFactory {

    /**
     * Instantiates the factory
     *
     * @param className  The factory class
     * @param attributes The factory attributes
     * @param arguments  The factory arguments
     * @param children   The factory children
     * @throws NullPointerException if any argument is null
     */
    public ParsedFactoryImpl {
        Objects.requireNonNull(className);
        attributes = Map.copyOf(attributes);
        arguments = List.copyOf(arguments);
        children = List.copyOf(children);
    }
}
