package com.github.gtache.fxml.compiler;

import java.util.List;

/**
 * Info about a controller field
 */
public interface ControllerFieldInfo {

    /**
     * Returns the field name
     *
     * @return The name
     */
    String name();

    /**
     * Returns whether the field is generic
     *
     * @return True if the field is generic, false if not (or raw)
     */
    default boolean isGeneric() {
        return !genericTypes().isEmpty();
    }

    /**
     * Returns the generic types for the field
     *
     * @return The generic types as a list, empty if not generic or raw
     */
    List<GenericTypes> genericTypes();
}
