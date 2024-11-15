package com.github.gtache.fxml.compiler;

import com.github.gtache.fxml.compiler.parsing.ParsedObject;

/**
 * Represents a request for a code generation
 */
public interface GenerationRequest {

    /**
     * @return The main controller info
     */
    ControllerInfo controllerInfo();

    /**
     * @return The request parameters
     */
    GenerationParameters parameters();

    /**
     * @return The object to generate code for
     */
    ParsedObject rootObject();

    /**
     * @return The output class name
     */
    String outputClassName();
}
