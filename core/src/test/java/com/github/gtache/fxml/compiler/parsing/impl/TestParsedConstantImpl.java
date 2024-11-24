package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedConstant;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestParsedConstantImpl {

    private final String className;
    private final Map<String, ParsedProperty> attributes;
    private final ParsedConstant constant;

    TestParsedConstantImpl() {
        this.className = "test";
        this.attributes = new HashMap<>(Map.of("fx:constant", new ParsedPropertyImpl("fx:constant", String.class.getName(), "value")));
        this.constant = new ParsedConstantImpl(className, attributes);
    }

    @Test
    void testGetters() {
        assertEquals(className, constant.className());
        assertEquals(attributes, constant.attributes());
        assertEquals(attributes.get("fx:constant").value(), constant.constant());
        assertEquals(Map.of(), constant.properties());
    }

    @Test
    void testCopyMap() {
        final var originalAttributes = constant.attributes();
        attributes.clear();
        assertEquals(originalAttributes, constant.attributes());
        assertNotEquals(attributes, constant.attributes());
    }

    @Test
    void testUnmodifiable() {
        final var objectProperties = constant.attributes();
        assertThrows(UnsupportedOperationException.class, objectProperties::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedConstantImpl(null, attributes));
        assertThrows(NullPointerException.class, () -> new ParsedConstantImpl(className, null));
    }
}
