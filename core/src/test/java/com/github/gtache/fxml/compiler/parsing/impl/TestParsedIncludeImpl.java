package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedInclude;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TestParsedIncludeImpl {

    private final SequencedMap<String, ParsedProperty> properties;
    private final ParsedInclude include;

    TestParsedIncludeImpl(@Mock final ParsedProperty property) {
        this.properties = new LinkedHashMap<>();
        this.properties.put("name", property);
        this.include = new ParsedIncludeImpl(properties);
    }

    @Test
    void testGetters() {
        assertEquals(properties, include.attributes());
        assertEquals(ParsedInclude.class.getName(), include.className());
        assertEquals(new LinkedHashMap<>(), include.properties());
    }

    @Test
    void testCopyMap() {
        final var originalProperties = include.attributes();
        properties.clear();
        assertEquals(originalProperties, include.attributes());
        assertNotEquals(properties, include.attributes());
    }

    @Test
    void testUnmodifiable() {
        final var objectProperties = include.attributes();
        assertThrows(UnsupportedOperationException.class, objectProperties::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedIncludeImpl(null));
    }
}
