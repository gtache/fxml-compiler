package com.github.gtache.fxml.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class TestControllerInfo {

    private final String string;
    private final Map<String, Boolean> handlerHasArgument;
    private final Map<String, List<String>> propertyGenericTypes;
    private final List<String> genericTypes;
    private final ControllerInfo controllerInfo;

    TestControllerInfo() {
        this.string = "string";
        this.handlerHasArgument = new HashMap<>();
        this.propertyGenericTypes = new HashMap<>();
        this.genericTypes = List.of("a", "b");
        this.controllerInfo = spy(ControllerInfo.class);
    }

    @BeforeEach
    void beforeEach() {
        when(controllerInfo.handlerHasArgument()).thenReturn(handlerHasArgument);
        when(controllerInfo.propertyGenericTypes()).thenReturn(propertyGenericTypes);
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
    void testPropertyGenericTypesNull() {
        assertNull(controllerInfo.propertyGenericTypes(string));
    }

    @Test
    void testPropertyGenericTypes() {
        propertyGenericTypes.put(string, genericTypes);
        assertEquals(genericTypes, controllerInfo.propertyGenericTypes(string));
    }
}
