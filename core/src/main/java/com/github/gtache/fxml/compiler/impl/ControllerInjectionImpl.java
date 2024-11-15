package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInjection;
import com.github.gtache.fxml.compiler.InjectionType;

import java.util.Objects;

/**
 * Implementation of {@link ControllerInjection}
 *
 * @param fieldInjectionType  The field injection type
 * @param methodInjectionType The method injection type
 * @param injectionClass      The injection class name
 */
public record ControllerInjectionImpl(InjectionType fieldInjectionType, InjectionType methodInjectionType,
                                      String injectionClass) implements ControllerInjection {
    public ControllerInjectionImpl {
        Objects.requireNonNull(fieldInjectionType);
        Objects.requireNonNull(methodInjectionType);
        Objects.requireNonNull(injectionClass);
    }
}
