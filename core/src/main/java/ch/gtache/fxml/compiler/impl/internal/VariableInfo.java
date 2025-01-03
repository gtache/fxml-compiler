package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.Objects;

/**
 * Info about a variable
 *
 * @param id           The fx:id of the variable
 * @param parsedObject The parsed object of the variable
 * @param variableName The variable name
 * @param className    The class name of the variable
 */
record VariableInfo(String id, ParsedObject parsedObject, String variableName, String className) {

    /**
     * Instantiates a new variable info
     *
     * @param id           The fx:id of the variable
     * @param parsedObject The parsed object of the variable
     * @param variableName The variable name
     * @param className    The class name of the variable
     * @throws NullPointerException if any parameter is null
     */
    VariableInfo {
        Objects.requireNonNull(id);
        Objects.requireNonNull(parsedObject);
        Objects.requireNonNull(variableName);
        Objects.requireNonNull(className);
    }
}
