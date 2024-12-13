package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.List;

/**
 * Implementation of {@link ParsedObject}
 *
 * @param className  The object class
 * @param attributes The object properties
 * @param properties The object children (complex properties)
 */
public record ParsedDefineImpl(List<ParsedObject> children) implements ParsedDefine {

    public ParsedDefineImpl {
        children = List.copyOf(children);
    }
}
