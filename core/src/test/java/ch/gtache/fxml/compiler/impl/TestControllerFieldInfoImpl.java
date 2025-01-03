package ch.gtache.fxml.compiler.impl;

import ch.gtache.fxml.compiler.ControllerFieldInfo;
import ch.gtache.fxml.compiler.GenericTypes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class TestControllerFieldInfoImpl {

    private final String name;
    private final List<GenericTypes> genericTypes;
    private final ControllerFieldInfo info;

    TestControllerFieldInfoImpl() {
        this.name = "name";
        this.genericTypes = new ArrayList<>(List.of(mock(GenericTypes.class)));
        this.info = new ControllerFieldInfoImpl(name, genericTypes);
    }

    @Test
    void testGetters() {
        assertEquals(name, info.name());
        assertEquals(genericTypes, info.genericTypes());
    }

    @Test
    void testCopyList() {
        final var originalGenericTypes = info.genericTypes();
        genericTypes.clear();
        assertEquals(originalGenericTypes, info.genericTypes());
    }

    @Test
    void testUnmodifiable() {
        final var infoList = info.genericTypes();
        assertThrows(UnsupportedOperationException.class, infoList::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ControllerFieldInfoImpl(null, genericTypes));
        assertThrows(NullPointerException.class, () -> new ControllerFieldInfoImpl(name, null));
    }
}
