package ch.gtache.fxml.compiler.parsing.impl;

import ch.gtache.fxml.compiler.parsing.ParsedDefine;
import ch.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.List;

/**
 * Implementation of {@link ParsedObject}
 *
 * @param children The objects in this define
 */
public record ParsedDefineImpl(List<ParsedObject> children) implements ParsedDefine {

    /**
     * Instantiates the define
     *
     * @param children The children
     * @throws NullPointerException If the children are null
     */
    public ParsedDefineImpl {
        children = List.copyOf(children);
    }
}
