package ch.gtache.fxml.compiler.maven.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestFieldInfo {

    private final String type;
    private final String name;
    private final FieldInfo fieldInfo;

    TestFieldInfo() {
        this.type = "type";
        this.name = "name";
        this.fieldInfo = new FieldInfo(type, name);
    }

    @Test
    void testGetters() {
        assertEquals(type, fieldInfo.type());
        assertEquals(name, fieldInfo.name());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new FieldInfo(null, name));
        assertThrows(NullPointerException.class, () -> new FieldInfo(type, null));
    }
}
