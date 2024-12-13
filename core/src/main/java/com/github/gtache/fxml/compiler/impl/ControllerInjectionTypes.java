package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.InjectionType;

/**
 * Base controller {@link InjectionType}s
 */
public enum ControllerInjectionTypes implements InjectionType {
    /**
     * Inject the controller instance
     */
    INSTANCE,
    /**
     * Inject a controller factory
     */
    FACTORY
}
