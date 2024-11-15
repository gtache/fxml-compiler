package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.ResourceBundleInjection;

import java.util.Objects;

/**
 * Implementation of {@link ResourceBundleInjection}
 *
 * @param injectionType The injection type
 * @param bundleName    The bundle name
 */
public record ResourceBundleInjectionImpl(InjectionType injectionType,
                                          String bundleName) implements ResourceBundleInjection {

    public ResourceBundleInjectionImpl {
        Objects.requireNonNull(injectionType);
        Objects.requireNonNull(bundleName);
    }
}
