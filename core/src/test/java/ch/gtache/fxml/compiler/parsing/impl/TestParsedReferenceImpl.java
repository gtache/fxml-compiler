package ch.gtache.fxml.compiler.parsing.impl;

import ch.gtache.fxml.compiler.parsing.ParsedProperty;
import ch.gtache.fxml.compiler.parsing.ParsedReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TestParsedReferenceImpl {

    private final SequencedMap<String, ParsedProperty> properties;
    private final ParsedReference reference;

    TestParsedReferenceImpl(@Mock final ParsedProperty property) {
        this.properties = new LinkedHashMap<>();
        this.properties.put("source", property);
        this.reference = new ParsedReferenceImpl(properties);
    }

    @Test
    void testGetters() {
        assertEquals(properties, reference.attributes());
        assertEquals(ParsedReference.class.getName(), reference.className());
        assertEquals(new LinkedHashMap<>(), reference.properties());
    }

    @Test
    void testCopyMap() {
        final var originalProperties = reference.attributes();
        properties.clear();
        assertEquals(originalProperties, reference.attributes());
        assertNotEquals(properties, reference.attributes());
    }

    @Test
    void testUnmodifiable() {
        final var objectProperties = reference.attributes();
        assertThrows(UnsupportedOperationException.class, objectProperties::clear);
    }

    @Test
    void testOtherConstructor() {
        final var otherReference = new ParsedReferenceImpl("name");
        assertEquals("name", otherReference.source());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedReferenceImpl((Map<String, ParsedProperty>) null));
        final var emptyMap = Map.<String, ParsedProperty>of();
        assertThrows(IllegalArgumentException.class, () -> new ParsedReferenceImpl(emptyMap));
    }
}
