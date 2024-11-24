package com.github.gtache.fxml.compiler;

import java.util.Map;

/**
 * Factory for creating controllers
 *
 * @param <T> The type of the controller
 */
@FunctionalInterface
public interface ControllerFactory<T> {

    /**
     * Creates a controller
     *
     * @param fieldMap The assignment of field name to value
     * @return The created controller
     */
    T create(final Map<String, Object> fieldMap);
}
