package ch.gtache.fxml.compiler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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
        when(info.genericTypes()).thenReturn(List.of(mock(GenericTypes.class)));
        assertTrue(info.isGeneric());
    }
}
