package com.github.gtache.fxml.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestControllerInfo {

    private final String string;
    private final Map<String, Boolean> handlerHasArgument;
    private final ControllerFieldInfo fieldInfo;
    private final Map<String, ControllerFieldInfo> fieldInfoMap;
    private final ControllerInfo controllerInfo;

    TestControllerInfo(@Mock final ControllerFieldInfo fieldInfo) {
        this.string = "string";
        this.handlerHasArgument = new HashMap<>();
        this.fieldInfoMap = new HashMap<>();
        this.fieldInfo = Objects.requireNonNull(fieldInfo);
        this.controllerInfo = spy(ControllerInfo.class);
    }

    @BeforeEach
    void beforeEach() {
        when(controllerInfo.handlerHasArgument()).thenReturn(handlerHasArgument);
        when(controllerInfo.fieldInfo()).thenReturn(fieldInfoMap);
    }

    @Test
    void testHandlerHasArgumentNull() {
        assertTrue(controllerInfo.handlerHasArgument(string));
    }

    @Test
    void testHandlerHasArgumentFalse() {
        handlerHasArgument.put(string, false);
        assertFalse(controllerInfo.handlerHasArgument(string));
    }

    @Test
    void testHandlerHasArgumentTrue() {
        handlerHasArgument.put(string, true);
        assertTrue(controllerInfo.handlerHasArgument(string));
    }

    @Test
    void testFieldInfoNull() {
        assertNull(controllerInfo.fieldInfo(string));
    }

    @Test
    void testFieldInfo() {
        fieldInfoMap.put(string, fieldInfo);
        assertEquals(fieldInfo, controllerInfo.fieldInfo(string));
    }
}
