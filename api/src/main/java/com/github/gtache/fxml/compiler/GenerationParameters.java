package com.github.gtache.fxml.compiler;

import com.github.gtache.fxml.compiler.compatibility.GenerationCompatibility;

import java.util.Map;

/**
 * Parameters for FXML generation
 */
public interface GenerationParameters {

    /**
     * Returns the compatibility information
     *
     * @return The compatibility
     */
    GenerationCompatibility compatibility();

    /**
     * Returns whether to use Image InputStream constructor instead of the String (url) one.
     * This allows avoiding opening some packages with JPMS
     *
     * @return True if the constructor should be used
     */
    boolean useImageInputStreamConstructor();

    /**
     * Returns the mapping of controller class to resource bundle path (in case of GET-BUNDLE injection)
     *
     * @return The map
     */
    Map<String, String> bundleMap();

    /**
     * Returns the controller injection to use
     *
     * @return The injection
     */
    ControllerInjectionType controllerInjectionType();

    /**
     * Returns the field injection to use
     *
     * @return The injection
     */
    ControllerFieldInjectionType fieldInjectionType();

    /**
     * Returns the method injection to use
     *
     * @return The injection
     */
    ControllerMethodsInjectionType methodInjectionType();

    /**
     * Returns the resource injection to use
     *
     * @return The injection
     */
    ResourceBundleInjectionType resourceInjectionType();
}
