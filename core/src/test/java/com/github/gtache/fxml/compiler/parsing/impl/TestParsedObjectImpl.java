package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestParsedObjectImpl {

    private final String clazz;
    private final SequencedMap<String, ParsedProperty> attributes;
    private final SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties;
    private final SequencedCollection<ParsedObject> objects;
    private final ParsedObject parsedObject;

    TestParsedObjectImpl(@Mock final ParsedProperty property, @Mock final ParsedObject object, @Mock final ParsedDefine define) {
        this.clazz = Object.class.getName();
        this.attributes = new LinkedHashMap<>();
        this.attributes.put("name", property);
        this.properties = new LinkedHashMap<>();
        this.properties.put(new ParsedPropertyImpl("property", null, null), List.of(object));
        this.objects = new ArrayList<>(List.of(define));
        this.parsedObject = new ParsedObjectImpl(clazz, attributes, properties, objects);
    }

    @Test
    void testGetters() {
        assertEquals(clazz, parsedObject.className());
        assertEquals(attributes, parsedObject.attributes());
        assertEquals(properties, parsedObject.properties());
        assertEquals(objects, parsedObject.children());
    }

    @Test
    void testCopyMap() {
        final var originalAttributes = parsedObject.attributes();
        final var originalProperties = parsedObject.properties();
        final var originalChildren = parsedObject.children();
        attributes.clear();
        properties.clear();
        objects.clear();
        assertEquals(originalAttributes, parsedObject.attributes());
        assertEquals(originalProperties, parsedObject.properties());
        assertEquals(originalChildren, parsedObject.children());
    }

    @Test
    void testUnmodifiable() {
        final var objectAttributes = parsedObject.attributes();
        final var objectProperties = parsedObject.properties();
        final var objectChildren = parsedObject.children();
        assertThrows(UnsupportedOperationException.class, objectAttributes::clear);
        assertThrows(UnsupportedOperationException.class, objectProperties::clear);
        assertThrows(UnsupportedOperationException.class, objectChildren::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedObjectImpl(null, attributes, properties, objects));
        assertThrows(NullPointerException.class, () -> new ParsedObjectImpl(clazz, null, properties, objects));
        assertThrows(NullPointerException.class, () -> new ParsedObjectImpl(clazz, attributes, null, objects));
        assertThrows(NullPointerException.class, () -> new ParsedObjectImpl(clazz, attributes, properties, null));
    }
}
