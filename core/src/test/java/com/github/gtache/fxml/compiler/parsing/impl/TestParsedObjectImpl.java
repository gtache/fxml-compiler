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

    private final Class<?> clazz;
    private final SequencedMap<String, ParsedProperty> properties;
    private final SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> children;
    private final ParsedObject parsedObject;

    TestParsedObjectImpl(@Mock final ParsedProperty property, @Mock final ParsedObject object) {
        this.clazz = Object.class;
        this.properties = new LinkedHashMap<>();
        this.properties.put("name", property);
        this.children = new LinkedHashMap<>();
        this.children.put(property, List.of(object));
        this.parsedObject = new ParsedObjectImpl(clazz, properties, children);
    }

    @Test
    void testGetters() {
        assertEquals(clazz, parsedObject.clazz());
        assertEquals(properties, parsedObject.properties());
        assertEquals(children, parsedObject.children());
    }

    @Test
    void testCopyMap() {
        final var originalProperties = parsedObject.properties();
        final var originalChildren = parsedObject.children();
        properties.clear();
        children.clear();
        assertEquals(originalProperties, parsedObject.properties());
        assertEquals(originalChildren, parsedObject.children());
    }

    @Test
    void testUnmodifiable() {
        final var objectProperties = parsedObject.properties();
        final var objectChildren = parsedObject.children();
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
