package com.github.gtache.fxml.compiler;

import java.util.Map;

/**
 * Info about a controller for code generation
 */
public interface ControllerInfo {

    /**
     * Returns a mapping of event handler method name -> boolean
     *
     * @return A mapping of method name to true if the method has an argument
     */
    Map<String, Boolean> handlerHasArgument();

    /**
     * Returns whether the given event handler method has an argument
     *
     * @param methodName The method name
     * @return A mapping of method name to true if the method has an event
     */
    default boolean handlerHasArgument(final String methodName) {
        return handlerHasArgument().getOrDefault(methodName, true);
    }

    /**
     * Returns a mapping of property name -> field info
     *
     * @return the mapping
     */
    Map<String, ControllerFieldInfo> fieldInfo();

    /**
     * Returns the field information for the given property
     *
     * @param property The property
     * @return The info, or null if not found
     */
    default ControllerFieldInfo fieldInfo(final String property) {
        return fieldInfo().get(property);
    }
}
