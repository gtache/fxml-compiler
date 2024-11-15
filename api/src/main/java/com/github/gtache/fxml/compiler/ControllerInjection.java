package com.github.gtache.fxml.compiler;

/**
 * Represents a controller injection to use for generated code
 */
public interface ControllerInjection {

    /**
     * @return The injection type for fields
     */
    InjectionType fieldInjectionType();

    /**
     * @return The injection type for event handlers
     */
    InjectionType methodInjectionType();

    /**
     * @return The injection class
     */
    String injectionClass();
}
