package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.ParsedValue;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestParsedValueImpl {

    private final String className;
    private final Map<String, ParsedProperty> attributes;
    private final ParsedValue value;

    TestParsedValueImpl() {
        this.className = "test";
        this.attributes = new HashMap<>(Map.of("fx:value", new ParsedPropertyImpl("fx:value", String.class.getName(), "value")));
        this.value = new ParsedValueImpl(className, attributes);
    }

    @Test
    void testGetters() {
        assertEquals(className, value.className());
        assertEquals(attributes, value.attributes());
        assertEquals(attributes.get("fx:value").value(), value.value());
        assertEquals(Map.of(), value.properties());
    }

    @Test
    void testCopyMap() {
        final var originalAttributes = value.attributes();
        attributes.clear();
        assertEquals(originalAttributes, value.attributes());
        assertNotEquals(attributes, value.attributes());
    }

    @Test
    void testUnmodifiable() {
        final var objectProperties = value.attributes();
        assertThrows(UnsupportedOperationException.class, objectProperties::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedValueImpl(null, attributes));
        assertThrows(NullPointerException.class, () -> new ParsedValueImpl(className, null));
    }
}
