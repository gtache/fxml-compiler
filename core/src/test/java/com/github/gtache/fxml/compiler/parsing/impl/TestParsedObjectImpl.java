package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestParsedObjectImpl {

    private final String clazz;
    private final SequencedMap<String, ParsedProperty> properties;
    private final SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> children;
    private final ParsedObject parsedObject;

    TestParsedObjectImpl(@Mock final ParsedProperty property, @Mock final ParsedObject object) {
        this.clazz = Object.class.getName();
        this.properties = new LinkedHashMap<>();
        this.properties.put("name", property);
        this.children = new LinkedHashMap<>();
        this.children.put(property, List.of(object));
        this.parsedObject = new ParsedObjectImpl(clazz, properties, children);
    }

    @Test
    void testGetters() {
        assertEquals(clazz, parsedObject.className());
        assertEquals(properties, parsedObject.attributes());
        assertEquals(children, parsedObject.properties());
    }

    @Test
    void testCopyMap() {
        final var originalProperties = parsedObject.attributes();
        final var originalChildren = parsedObject.properties();
        properties.clear();
        children.clear();
        assertEquals(originalProperties, parsedObject.attributes());
        assertEquals(originalChildren, parsedObject.properties());
    }

    @Test
    void testUnmodifiable() {
        final var objectProperties = parsedObject.attributes();
        final var objectChildren = parsedObject.properties();
        assertThrows(UnsupportedOperationException.class, objectProperties::clear);
        assertThrows(UnsupportedOperationException.class, objectChildren::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedObjectImpl(null, properties, children));
        assertThrows(NullPointerException.class, () -> new ParsedObjectImpl(clazz, null, children));
        assertThrows(NullPointerException.class, () -> new ParsedObjectImpl(clazz, properties, null));
    }
}
