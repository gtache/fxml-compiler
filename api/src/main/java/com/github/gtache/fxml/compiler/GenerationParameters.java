package com.github.gtache.fxml.compiler;

import java.util.Map;

/**
 * Parameters for FXML generation
 */
public interface GenerationParameters {

    /**
     * Returns the mapping of controller class name to controller injection
     *
     * @return The mapping
     */
    Map<String, ControllerInjection> controllerInjections();

    /**
     * Returns the mapping of fx:include source to generated class name
     *
     * @return The mapping
     */
    Map<String, String> sourceToGeneratedClassName();


    /**
     * Returns the mapping of fx:include source to controller class name
     *
     * @return The mapping
     */
    Map<String, String> sourceToControllerName();

    /**
     * Returns the resource bundle injection to use
     *
     * @return The injection
     */
    ResourceBundleInjection resourceBundleInjection();
}
