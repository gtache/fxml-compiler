package ch.gtache.fxml.compiler;

import java.util.List;

/**
 * Represents generic types for a field
 */
public interface GenericTypes {

    /**
     * Returns the name of the type
     *
     * @return The name
     */
    String name();

    /**
     * Returns the possible subtypes of the type
     *
     * @return The list of subtypes, empty if no subtypes
     */
    List<GenericTypes> subTypes();
}
