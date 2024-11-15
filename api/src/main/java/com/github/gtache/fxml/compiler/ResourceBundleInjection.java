package com.github.gtache.fxml.compiler;

/**
 * Represents a controller injection to use for generated code
 */
public interface ResourceBundleInjection {

    /**
     * @return The injection type
     */
    InjectionType injectionType();

    /**
     * @return The resource bundle path
     */
    String bundleName();
}
