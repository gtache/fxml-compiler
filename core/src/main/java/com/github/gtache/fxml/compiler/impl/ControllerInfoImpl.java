package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerFieldInfo;
import com.github.gtache.fxml.compiler.ControllerInfo;

import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link ControllerInfo}
 *
 * @param className          The controller class name
 * @param handlerHasArgument The mapping of method name to true if the method has an argument
 * @param fieldInfo          The mapping of property name to controller field info
 * @param hasInitialize      True if the controller has an initialize method
 */
public record ControllerInfoImpl(String className, Map<String, Boolean> handlerHasArgument,
                                 Map<String, ControllerFieldInfo> fieldInfo,
                                 boolean hasInitialize) implements ControllerInfo {

    public ControllerInfoImpl {
        Objects.requireNonNull(className);
        handlerHasArgument = Map.copyOf(handlerHasArgument);
        fieldInfo = Map.copyOf(fieldInfo);
    }
}
