package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.Objects;

/**
 * Implementation of {@link GenerationRequest}
 *
 * @param parameters      The generation parameters
 * @param rootObject      The root object
 * @param outputClassName The output class name
 */
public record GenerationRequestImpl(GenerationParameters parameters, ControllerInfo controllerInfo,
                                    ParsedObject rootObject,
                                    String outputClassName) implements GenerationRequest {
    public GenerationRequestImpl {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(controllerInfo);
        Objects.requireNonNull(rootObject);
        Objects.requireNonNull(outputClassName);
    }
}
