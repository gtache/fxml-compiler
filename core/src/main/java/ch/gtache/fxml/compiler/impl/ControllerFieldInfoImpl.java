package ch.gtache.fxml.compiler.impl;

import ch.gtache.fxml.compiler.ControllerFieldInfo;
import ch.gtache.fxml.compiler.GenericTypes;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ControllerFieldInfo}
 *
 * @param name         The field name
 * @param genericTypes The generic types
 */
public record ControllerFieldInfoImpl(String name, List<GenericTypes> genericTypes) implements ControllerFieldInfo {

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
