package ch.gtache.fxml.compiler.parsing.impl;

import ch.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestParsedPropertyImpl {

    private final String name;
    private final String sourceType;
    private final String value;
    private final ParsedProperty property;

    TestParsedPropertyImpl() {
        this.name = "name";
        this.sourceType = Object.class.getName();
        this.value = "value";
        this.property = new ParsedPropertyImpl(name, sourceType, value);
    }

    @Test
    void testGetters() {
        assertEquals(name, property.name());
        assertEquals(sourceType, property.sourceType());
        assertEquals(value, property.value());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedPropertyImpl(null, sourceType, value));
        assertDoesNotThrow(() -> new ParsedPropertyImpl(name, null, value));
        assertDoesNotThrow(() -> new ParsedPropertyImpl(name, sourceType, null));
    }
}
