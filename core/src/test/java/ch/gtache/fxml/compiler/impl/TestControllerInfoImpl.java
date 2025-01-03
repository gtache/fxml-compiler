package ch.gtache.fxml.compiler.impl;

import ch.gtache.fxml.compiler.ControllerFieldInfo;
import ch.gtache.fxml.compiler.ControllerInfo;
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

    private final String className;
    private final Map<String, Boolean> handlerHasArgument;
    private final ControllerFieldInfo fieldInfo;
    private final Map<String, ControllerFieldInfo> fieldInfoMap;
    private final boolean hasInitialize;
    private final ControllerInfo info;

    TestControllerInfoImpl(@Mock final ControllerFieldInfo fieldInfo) {
        this.className = "controllerName";
        this.handlerHasArgument = new HashMap<>(Map.of("one", true, "two", false));
        this.fieldInfo = Objects.requireNonNull(fieldInfo);
        this.fieldInfoMap = new HashMap<>(Map.of("one", fieldInfo));
        this.hasInitialize = true;
        this.info = new ControllerInfoImpl(className, handlerHasArgument, fieldInfoMap, hasInitialize);
    }

    @Test
    void testGetters() {
        assertEquals(className, info.className());
        assertEquals(handlerHasArgument, info.handlerHasArgument());
        assertEquals(fieldInfoMap, info.fieldInfo());
        assertEquals(hasInitialize, info.hasInitialize());
    }

    @Test
    void testHandlerHasArgument() {
        assertTrue(info.handlerHasArgument("one"));
        assertFalse(info.handlerHasArgument("two"));
        assertTrue(info.handlerHasArgument("three"));
    }

    @Test
    void testFieldInfo() {
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
        assertThrows(NullPointerException.class, () -> new ControllerInfoImpl(null, handlerHasArgument, fieldInfoMap, hasInitialize));
        assertThrows(NullPointerException.class, () -> new ControllerInfoImpl(className, null, fieldInfoMap, hasInitialize));
        assertThrows(NullPointerException.class, () -> new ControllerInfoImpl(className, handlerHasArgument, null, hasInitialize));
    }
}
