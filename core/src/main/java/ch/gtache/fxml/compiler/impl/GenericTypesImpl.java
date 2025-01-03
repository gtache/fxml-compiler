package ch.gtache.fxml.compiler.impl;

import ch.gtache.fxml.compiler.GenericTypes;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link GenericTypes}
 *
 * @param name     The name
 * @param subTypes The subtypes
 */
public record GenericTypesImpl(String name, List<GenericTypes> subTypes) implements GenericTypes {

    /**
     * Instantiates a new generic types
     *
     * @param name     The name
     * @param subTypes The subtypes
     * @throws NullPointerException if any parameter is null
     */
    public GenericTypesImpl {
        Objects.requireNonNull(name);
        subTypes = List.copyOf(subTypes);
    }
}
