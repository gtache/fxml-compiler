package ch.gtache.fxml.compiler;

import ch.gtache.fxml.compiler.parsing.ParsedObject;

/**
 * Represents a request for a code generation
 */
public interface GenerationRequest {

    /**
     * Returns the info about the main controller for code generation
     *
     * @return The info
     */
    ControllerInfo controllerInfo();

    /**
     * Returns the generation parameters
     *
     * @return The parameters
     */
    GenerationParameters parameters();

    /**
     * Returns the info about the main source file
     *
     * @return The info
     */
    SourceInfo sourceInfo();

    /**
     * Returns the object to generate code for
     *
     * @return The object
     */
    ParsedObject rootObject();

    /**
     * Returns the output class name
     *
     * @return The class name
     */
    String outputClassName();
}
