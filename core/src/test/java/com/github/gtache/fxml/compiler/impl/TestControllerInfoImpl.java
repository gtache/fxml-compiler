package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInfo;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestControllerInfoImpl {

    private final Map<String, Boolean> handlerHasArgument;
    private final Map<String, List<String>> propertyGenericTypes;
    private final ControllerInfo info;

    TestControllerInfoImpl() {
        this.handlerHasArgument = new HashMap<>(Map.of("one", true, "two", false));
        this.propertyGenericTypes = new HashMap<>(Map.of("one", List.of("a", "b"), "two", List.of()));
        this.info = new ControllerInfoImpl(handlerHasArgument, propertyGenericTypes);
    }

    @Test
    void testHandlerHasArgument() {
        assertEquals(handlerHasArgument, info.handlerHasArgument());
        assertTrue(info.handlerHasArgument("one"));
        assertFalse(info.handlerHasArgument("two"));
        assertTrue(info.handlerHasArgument("three"));
    }

    @Test
    void testPropertyGenericTypes() {
        assertEquals(propertyGenericTypes, info.propertyGenericTypes());
        assertEquals(List.of("a", "b"), info.propertyGenericTypes("one"));
        assertEquals(List.of(), info.propertyGenericTypes("two"));
    }

    @Test
    void testMapsCopied() {
        final var originalHandler = Map.copyOf(handlerHasArgument);
        final var originalPropertyTypes = Map.copyOf(propertyGenericTypes);
        assertEquals(originalHandler, info.handlerHasArgument());
        assertEquals(originalPropertyTypes, info.propertyGenericTypes());

        handlerHasArgument.clear();
        propertyGenericTypes.clear();
        assertEquals(originalHandler, info.handlerHasArgument());
        assertEquals(originalPropertyTypes, info.propertyGenericTypes());
    }

    @Test
    void testUnmodifiable() {
        final var infoHandler = info.handlerHasArgument();
        final var infoProperty = info.propertyGenericTypes();
        assertThrows(UnsupportedOperationException.class, infoHandler::clear);
        assertThrows(UnsupportedOperationException.class, infoProperty::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ControllerInfoImpl(null, propertyGenericTypes));
        assertThrows(NullPointerException.class, () -> new ControllerInfoImpl(handlerHasArgument, null));
    }
}
