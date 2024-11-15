package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInfo;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ControllerInfo}
 *
 * @param handlerHasArgument   The mapping of method name to true if the method has an argument
 * @param propertyGenericTypes The mapping of property name to generic types
 */
public record ControllerInfoImpl(Map<String, Boolean> handlerHasArgument,
                                 Map<String, List<String>> propertyGenericTypes) implements ControllerInfo {

    public ControllerInfoImpl {
        handlerHasArgument = Map.copyOf(handlerHasArgument);
        propertyGenericTypes = Map.copyOf(propertyGenericTypes);
    }
}
