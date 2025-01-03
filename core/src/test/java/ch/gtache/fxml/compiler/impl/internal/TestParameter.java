package ch.gtache.fxml.compiler.impl.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestParameter {

    private final String name;
    private final Class<?> type;
    private final String defaultValue;
    private final Parameter parameter;

    TestParameter() {
        this.name = "name";
        this.type = Object.class;
        this.defaultValue = "default";
        this.parameter = new Parameter(name, type, defaultValue);
    }

    @Test
    void testGetters() {
        assertEquals(name, parameter.name());
        assertEquals(type, parameter.type());
        assertEquals(defaultValue, parameter.defaultValue());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new Parameter(null, type, defaultValue));
        assertThrows(NullPointerException.class, () -> new Parameter(name, null, defaultValue));
        assertThrows(NullPointerException.class, () -> new Parameter(name, type, null));
    }
}
