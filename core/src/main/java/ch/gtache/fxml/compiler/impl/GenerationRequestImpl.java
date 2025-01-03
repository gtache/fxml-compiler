package ch.gtache.fxml.compiler.impl;

import ch.gtache.fxml.compiler.ControllerInfo;
import ch.gtache.fxml.compiler.GenerationParameters;
import ch.gtache.fxml.compiler.GenerationRequest;
import ch.gtache.fxml.compiler.SourceInfo;
import ch.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.Objects;

/**
 * Implementation of {@link GenerationRequest}
 *
 * @param parameters      The generation parameters
 * @param controllerInfo  The controller info
 * @param sourceInfo      The source info
 * @param rootObject      The root object
 * @param outputClassName The output class name
 */
public record GenerationRequestImpl(GenerationParameters parameters, ControllerInfo controllerInfo,
                                    SourceInfo sourceInfo, ParsedObject rootObject,
                                    String outputClassName) implements GenerationRequest {
    /**
     * Instantiates a new request
     *
     * @param parameters      The generation parameters
     * @param controllerInfo  The controller info
     * @param sourceInfo      The source info
     * @param rootObject      The root object
     * @param outputClassName The output class name
     * @throws NullPointerException If any parameter is null
     */
    public GenerationRequestImpl {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(controllerInfo);
        Objects.requireNonNull(sourceInfo);
        Objects.requireNonNull(rootObject);
        Objects.requireNonNull(outputClassName);
    }
}
