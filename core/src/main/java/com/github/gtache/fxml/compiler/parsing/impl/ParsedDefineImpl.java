package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.Objects;

/**
 * Implementation of {@link ParsedObject}
 *
 * @param className  The object class
 * @param attributes The object properties
 * @param properties The object children (complex properties)
 */
public record ParsedDefineImpl(ParsedObject object) implements ParsedDefine {

    public ParsedDefineImpl {
        Objects.requireNonNull(object);
    }
}
