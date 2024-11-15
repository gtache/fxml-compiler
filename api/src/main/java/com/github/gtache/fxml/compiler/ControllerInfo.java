package com.github.gtache.fxml.compiler;

import java.util.List;
import java.util.Map;

/**
 * Info about a controller for code generation
 */
public interface ControllerInfo {

    /**
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
     * @return A mapping of property name to generic types
     */
    Map<String, List<String>> propertyGenericTypes();

    /**
     * Returns the generic types for the given property (null if not generic)
     *
     * @param property The property
     * @return The generic types
     */
    default List<String> propertyGenericTypes(final String property) {
        return propertyGenericTypes().get(property);
    }
}
