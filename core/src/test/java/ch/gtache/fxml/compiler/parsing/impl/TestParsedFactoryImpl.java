package ch.gtache.fxml.compiler.parsing.impl;

import ch.gtache.fxml.compiler.parsing.ParsedDefine;
import ch.gtache.fxml.compiler.parsing.ParsedFactory;
import ch.gtache.fxml.compiler.parsing.ParsedObject;
import ch.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TestParsedFactoryImpl {

    private final String className;
    private final Map<String, ParsedProperty> attributes;
    private final SequencedCollection<ParsedObject> arguments;
    private final SequencedCollection<ParsedObject> children;
    private final ParsedFactory factory;

    TestParsedFactoryImpl(@Mock final ParsedObject object1, @Mock final ParsedObject object2, @Mock final ParsedDefine define) {
        this.className = "test";
        this.attributes = new HashMap<>(Map.of("fx:factory", new ParsedPropertyImpl("fx:factory", String.class.getName(), "value")));
        this.arguments = new ArrayList<>(List.of(object1, object2));
        this.children = new ArrayList<>(List.of(define));
        this.factory = new ParsedFactoryImpl(className, attributes, arguments, children);
    }

    @Test
    void testGetters() {
        assertEquals(className, factory.className());
        assertEquals(attributes, factory.attributes());
        assertEquals(attributes.get("fx:factory").value(), factory.factory());
        assertEquals(arguments, factory.arguments());
        assertEquals(children, factory.children());
        assertEquals(Map.of(), factory.properties());
    }

    @Test
    void testCopyMap() {
        final var originalAttributes = factory.attributes();
        attributes.clear();
        assertEquals(originalAttributes, factory.attributes());
        assertNotEquals(attributes, factory.attributes());
    }

    @Test
    void testCopyArguments() {
        final var originalArguments = factory.arguments();
        arguments.clear();
        assertEquals(originalArguments, factory.arguments());
        assertNotEquals(arguments, factory.arguments());
    }

    @Test
    void testCopyDefines() {
        final var originalDefines = factory.children();
        children.clear();
        assertEquals(originalDefines, factory.children());
        assertNotEquals(children, factory.children());
    }

    @Test
    void testUnmodifiable() {
        final var objectProperties = factory.attributes();
        assertThrows(UnsupportedOperationException.class, objectProperties::clear);

        final var objectArguments = factory.arguments();
        assertThrows(UnsupportedOperationException.class, objectArguments::clear);

        final var objectDefines = factory.children();
        assertThrows(UnsupportedOperationException.class, objectDefines::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedFactoryImpl(null, attributes, arguments, children));
        assertThrows(NullPointerException.class, () -> new ParsedFactoryImpl(className, null, arguments, children));
        assertThrows(NullPointerException.class, () -> new ParsedFactoryImpl(className, attributes, null, children));
        assertThrows(NullPointerException.class, () -> new ParsedFactoryImpl(className, attributes, arguments, null));
    }
}
