package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInjection;
import com.github.gtache.fxml.compiler.InjectionType;

/**
 * Base methods {@link InjectionType}s for {@link ControllerInjection}
 */
public enum ControllerMethodsInjectionType implements InjectionType {
    /**
     * Inject using visible methods
     */
    REFERENCE,
    /**
     * Inject using reflection
     */
    REFLECTION,
}
