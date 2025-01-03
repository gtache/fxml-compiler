package ch.gtache.fxml.compiler.impl;

import ch.gtache.fxml.compiler.ControllerFieldInjectionType;
import ch.gtache.fxml.compiler.ControllerInjectionType;
import ch.gtache.fxml.compiler.ControllerMethodsInjectionType;
import ch.gtache.fxml.compiler.GenerationParameters;
import ch.gtache.fxml.compiler.ResourceBundleInjectionType;
import ch.gtache.fxml.compiler.compatibility.GenerationCompatibility;

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
                                       ControllerInjectionType controllerInjectionType,
                                       ControllerFieldInjectionType fieldInjectionType,
                                       ControllerMethodsInjectionType methodInjectionType,
                                       ResourceBundleInjectionType resourceInjectionType) implements GenerationParameters {

    /**
     * Instantiates new parameters
     *
     * @param compatibility                  The compatibility info
     * @param useImageInputStreamConstructor True if the InputStream constructor should be used
     * @param bundleMap                      The mapping of controller class to resource bundle path
     * @param controllerInjectionType        The controller injection type
     * @param fieldInjectionType             The field injection type
     * @param methodInjectionType            The method injection type
     * @param resourceInjectionType          The resource injection type
     * @throws NullPointerException if any parameter is null
     */
    public GenerationParametersImpl {
        requireNonNull(compatibility);
        bundleMap = Map.copyOf(bundleMap);
        requireNonNull(controllerInjectionType);
        requireNonNull(fieldInjectionType);
        requireNonNull(methodInjectionType);
        requireNonNull(resourceInjectionType);
    }
}
