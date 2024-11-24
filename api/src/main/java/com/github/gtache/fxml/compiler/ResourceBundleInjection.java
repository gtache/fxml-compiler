package com.github.gtache.fxml.compiler;

/**
 * Represents a controller injection to use for generated code
 */
public interface ResourceBundleInjection {

    /**
     * Returns the injection type for the resource bundle
     *
     * @return The injection type
     */
    InjectionType injectionType();

    /**
     * Returns the resource bundle name
     *
     * @return The path
     */
    String bundleName();
}
