package ch.gtache.fxml.compiler.parsing.impl;

import ch.gtache.fxml.compiler.parsing.ParsedCopy;
import ch.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TestParsedCopyImpl {

    private final SequencedMap<String, ParsedProperty> properties;
    private final ParsedCopy copy;

    TestParsedCopyImpl(@Mock final ParsedProperty property) {
        this.properties = new LinkedHashMap<>();
        this.properties.put("source", property);
        this.copy = new ParsedCopyImpl(properties);
    }

    @Test
    void testGetters() {
        assertEquals(properties, copy.attributes());
        assertEquals(ParsedCopy.class.getName(), copy.className());
        assertEquals(new LinkedHashMap<>(), copy.properties());
    }

    @Test
    void testCopyMap() {
        final var originalProperties = copy.attributes();
        properties.clear();
        assertEquals(originalProperties, copy.attributes());
        assertNotEquals(properties, copy.attributes());
    }

    @Test
    void testUnmodifiable() {
        final var objectProperties = copy.attributes();
        assertThrows(UnsupportedOperationException.class, objectProperties::clear);
    }

    @Test
    void testOtherConstructor() {
        final var otherCopy = new ParsedCopyImpl("source");
        assertEquals("source", otherCopy.source());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedCopyImpl((Map<String, ParsedProperty>) null));
        final var emptyMap = Map.<String, ParsedProperty>of();
        assertThrows(IllegalArgumentException.class, () -> new ParsedCopyImpl(emptyMap));
    }
}
