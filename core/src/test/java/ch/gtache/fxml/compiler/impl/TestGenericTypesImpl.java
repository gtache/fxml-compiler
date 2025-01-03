package ch.gtache.fxml.compiler.impl;

import ch.gtache.fxml.compiler.GenericTypes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class TestGenericTypesImpl {

    private final String name;
    private final List<GenericTypes> subTypes;
    private final GenericTypes types;

    TestGenericTypesImpl() {
        this.name = "name";
        this.subTypes = new ArrayList<>(List.of(mock(GenericTypes.class)));
        this.types = new GenericTypesImpl(name, subTypes);
    }

    @Test
    void testGetters() {
        assertEquals(name, types.name());
        assertEquals(subTypes, types.subTypes());
    }

    @Test
    void testCopyList() {
        final var originalGenericTypes = types.subTypes();
        subTypes.clear();
        assertEquals(originalGenericTypes, types.subTypes());
    }

    @Test
    void testUnmodifiable() {
        final var infoList = types.subTypes();
        assertThrows(UnsupportedOperationException.class, infoList::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ControllerFieldInfoImpl(null, subTypes));
        assertThrows(NullPointerException.class, () -> new ControllerFieldInfoImpl(name, null));
    }
}
