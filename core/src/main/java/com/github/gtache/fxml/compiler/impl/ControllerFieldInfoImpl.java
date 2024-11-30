package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerFieldInfo;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ControllerFieldInfo}
 *
 * @param name         The field name
 * @param genericTypes The generic types
 */
public record ControllerFieldInfoImpl(String name, List<String> genericTypes) implements ControllerFieldInfo {

    /**
     * Instantiates a new info
     *
     * @param name         The field name
     * @param genericTypes The generic types (will be copied)
     * @throws NullPointerException if any parameter is null
     */
    public ControllerFieldInfoImpl {
        Objects.requireNonNull(name);
        genericTypes = List.copyOf(genericTypes);
    }
}
