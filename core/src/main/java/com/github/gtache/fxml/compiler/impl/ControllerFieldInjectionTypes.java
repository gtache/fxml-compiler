package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInjection;
import com.github.gtache.fxml.compiler.InjectionType;

/**
 * Base field {@link InjectionType}s for {@link ControllerInjection}
 */
public enum ControllerFieldInjectionTypes implements InjectionType {
    /**
     * Inject using variable assignment
     */
    ASSIGN,
    /**
     * Inject using a factory
     */
    FACTORY,
    /**
     * Inject using reflection
     */
    REFLECTION,
    /**
     * Inject using setters
     */
    SETTERS
}
