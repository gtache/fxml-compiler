package com.github.gtache.fxml.compiler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class TestControllerFieldInfo {

    private final ControllerFieldInfo info;

    TestControllerFieldInfo() {
        this.info = spy(ControllerFieldInfo.class);
    }

    @Test
    void testIsGenericFalse() {
        when(info.genericTypes()).thenReturn(List.of());
        assertFalse(info.isGeneric());
    }

    @Test
    void testIsGenericTrue() {
        when(info.genericTypes()).thenReturn(List.of("A", "B", "C"));
        assertTrue(info.isGeneric());
    }
}
