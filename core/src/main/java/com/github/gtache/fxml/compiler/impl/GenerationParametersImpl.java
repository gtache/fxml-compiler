package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.compatibility.GenerationCompatibility;

import java.util.Map;

import static java.util.Objects.requireNonNull;


/**
 * Implementation of {@link GenerationParameters}
 *
 * @param compatibility                  The compatibility info
 * @param useImageInputStreamConstructor True if the InputStream constructor should be used
 * @param bundleMap                      The mapping of controller class to resource bundle path
 * @param controllerInjectionType        The controller injection type
 * @param fieldInjectionType             The field injection type
 * @param methodInjectionType            The method injection type
 * @param resourceInjectionType          The resource injection type
 */
public record GenerationParametersImpl(GenerationCompatibility compatibility, boolean useImageInputStreamConstructor,
                                       Map<String, String> bundleMap,
                                       InjectionType controllerInjectionType,
                                       InjectionType fieldInjectionType,
                                       InjectionType methodInjectionType,
                                       InjectionType resourceInjectionType) implements GenerationParameters {

    public GenerationParametersImpl {
        requireNonNull(compatibility);
        bundleMap = Map.copyOf(bundleMap);
        requireNonNull(controllerInjectionType);
        requireNonNull(fieldInjectionType);
        requireNonNull(methodInjectionType);
        requireNonNull(resourceInjectionType);
    }
}
