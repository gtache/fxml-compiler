package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.ResourceBundleInjection;

/**
 * Base {@link InjectionType}s for {@link ResourceBundleInjection}
 */
public enum ResourceBundleInjectionTypes implements InjectionType {
    /**
     * Resource bundle is injected in the constructor
     */
    CONSTRUCTOR,
    /**
     * Resource bundle is loaded using getBundle
     */
    GET_BUNDLE,
    /**
     * Resource bundle is retrieved from controller using getter
     */
    GETTER
}
