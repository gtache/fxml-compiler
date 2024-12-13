package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.SourceInfo;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

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
    public GenerationRequestImpl {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(controllerInfo);
        Objects.requireNonNull(sourceInfo);
        Objects.requireNonNull(rootObject);
        Objects.requireNonNull(outputClassName);
    }
}
