package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerFieldInfo;
import com.github.gtache.fxml.compiler.ControllerInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TestControllerInfoImpl {

    private final Map<String, Boolean> handlerHasArgument;
    private final ControllerFieldInfo fieldInfo;
    private final Map<String, ControllerFieldInfo> fieldInfoMap;
    private final ControllerInfo info;

    TestControllerInfoImpl(@Mock final ControllerFieldInfo fieldInfo) {
        this.handlerHasArgument = new HashMap<>(Map.of("one", true, "two", false));
        this.fieldInfo = Objects.requireNonNull(fieldInfo);
        this.fieldInfoMap = new HashMap<>(Map.of("one", fieldInfo));
        this.info = new ControllerInfoImpl(handlerHasArgument, fieldInfoMap);
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
        assertEquals(fieldInfoMap, info.fieldInfo());
        assertEquals(fieldInfo, info.fieldInfo("one"));
    }

    @Test
    void testMapsCopied() {
        final var originalHandler = Map.copyOf(handlerHasArgument);
        final var originalFieldInfo = Map.copyOf(fieldInfoMap);
        assertEquals(originalHandler, info.handlerHasArgument());
        assertEquals(originalFieldInfo, info.fieldInfo());

        handlerHasArgument.clear();
        fieldInfoMap.clear();
        assertEquals(originalHandler, info.handlerHasArgument());
        assertEquals(originalFieldInfo, info.fieldInfo());
    }

    @Test
    void testUnmodifiable() {
        final var infoHandler = info.handlerHasArgument();
        final var infoProperty = info.fieldInfo();
        assertThrows(UnsupportedOperationException.class, infoHandler::clear);
        assertThrows(UnsupportedOperationException.class, infoProperty::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ControllerInfoImpl(null, fieldInfoMap));
        assertThrows(NullPointerException.class, () -> new ControllerInfoImpl(handlerHasArgument, null));
    }
}
