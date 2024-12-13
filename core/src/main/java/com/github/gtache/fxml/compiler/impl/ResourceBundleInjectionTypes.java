package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.InjectionType;

/**
 * Base {@link InjectionType}s for resource bundles
 */
public enum ResourceBundleInjectionTypes implements InjectionType {
    /**
     * Resource bundle is injected in the constructor
     */
    CONSTRUCTOR,
    /**
     * Resource bundle is injected as a function in the constructor
     */
    CONSTRUCTOR_FUNCTION,
    /**
     * Resource bundle name is injected in the constructor
     */
    CONSTRUCTOR_NAME,
    /**
     * Resource bundle is loaded using getBundle
     */
    GET_BUNDLE,
    /**
     * Resource bundle is retrieved from controller using getter
     */
    GETTER
}
