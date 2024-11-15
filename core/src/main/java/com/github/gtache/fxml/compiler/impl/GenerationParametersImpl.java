package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInjection;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.ResourceBundleInjection;

import java.util.Map;
import java.util.Objects;


/**
 * Implementation of {@link GenerationParameters}
 *
 * @param controllerInjections       The mapping of controller class name to controller injection
 * @param sourceToGeneratedClassName The mapping of fx:include source to generated class name
 * @param sourceToControllerName     The mapping of fx:include source to controller class name
 * @param resourceBundleInjection    The resource bundle injection
 */
public record GenerationParametersImpl(Map<String, ControllerInjection> controllerInjections,
                                       Map<String, String> sourceToGeneratedClassName,
                                       Map<String, String> sourceToControllerName,
                                       ResourceBundleInjection resourceBundleInjection) implements GenerationParameters {

    public GenerationParametersImpl {
        controllerInjections = Map.copyOf(controllerInjections);
        sourceToGeneratedClassName = Map.copyOf(sourceToGeneratedClassName);
        sourceToControllerName = Map.copyOf(sourceToControllerName);
        Objects.requireNonNull(resourceBundleInjection);
    }
}
