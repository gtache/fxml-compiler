package com.github.gtache.fxml.compiler.parsing;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestParsedInclude {

    private final SequencedMap<String, ParsedProperty> properties;
    private final ParsedProperty property;
    private final String string;
    private final ParsedInclude include;

    TestParsedInclude(@Mock final ParsedProperty property) {
        this.properties = new LinkedHashMap<>();
        this.property = Objects.requireNonNull(property);
        this.string = "str/ing";
        this.include = spy(ParsedInclude.class);
    }

    @BeforeEach
    void beforeEach() {
        when(include.properties()).thenReturn(properties);
        when(property.value()).thenReturn(string);
    }

    @Test
    void testControllerIdNull() {
        assertNull(include.controllerId());
    }

    @Test
    void testControllerId() {
        properties.put("fx:id", property);
        assertEquals(string + "Controller", include.controllerId());
    }

    @Test
    void testResourcesNull() {
        assertNull(include.resources());
    }

    @Test
    void testResources() {
        properties.put("resources", property);
        assertEquals(string.replace("/", "."), include.resources());
    }

    @Test
    void testSourceNull() {
        assertThrows(IllegalStateException.class, include::source);
    }

    @Test
    void testSource() {
        properties.put("source", property);
        assertEquals(string, include.source());
    }

    @Test
    void testClazz() {
        assertEquals(ParsedInclude.class, include.clazz());
    }

    @Test
    void testChildren() {
        assertEquals(new LinkedHashMap<>(), include.children());
    }
}
