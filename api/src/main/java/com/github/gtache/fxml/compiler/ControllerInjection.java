package com.github.gtache.fxml.compiler;

/**
 * Represents a controller injection to use for generated code
 */
public interface ControllerInjection {

    /**
     * Returns the injection type of class fields
     *
     * @return The injection type for fields
     */
    InjectionType fieldInjectionType();

    /**
     * Returns the injection type for event handlers methods
     *
     * @return The injection type for event handlers
     */
    InjectionType methodInjectionType();

    /**
     * The name of the controller class
     *
     * @return The class
     */
    String injectionClass();
}
