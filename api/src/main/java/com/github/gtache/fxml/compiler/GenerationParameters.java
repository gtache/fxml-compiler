package com.github.gtache.fxml.compiler;

import java.util.Map;

/**
 * Parameters for FXML generation
 */
public interface GenerationParameters {

    /**
     * @return The mapping of controller class name to controller injection
     */
    Map<String, ControllerInjection> controllerInjections();

    /**
     * @return The mapping of fx:include source to generated class name
     */
    Map<String, String> sourceToGeneratedClassName();

    /**
     * @return The mapping of fx:include source to controller class name
     */
    Map<String, String> sourceToControllerName();

    /**
     * @return The resource bundle injection to use
     */
    ResourceBundleInjection resourceBundleInjection();
}
