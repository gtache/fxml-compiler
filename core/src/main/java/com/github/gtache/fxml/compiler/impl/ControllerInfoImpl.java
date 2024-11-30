package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerFieldInfo;
import com.github.gtache.fxml.compiler.ControllerInfo;

import java.util.Map;

/**
 * Implementation of {@link ControllerInfo}
 *
 * @param handlerHasArgument The mapping of method name to true if the method has an argument
 * @param fieldInfo          The mapping of property name to controller field info
 */
public record ControllerInfoImpl(Map<String, Boolean> handlerHasArgument,
                                 Map<String, ControllerFieldInfo> fieldInfo) implements ControllerInfo {

    public ControllerInfoImpl {
        handlerHasArgument = Map.copyOf(handlerHasArgument);
        fieldInfo = Map.copyOf(fieldInfo);
    }
}
