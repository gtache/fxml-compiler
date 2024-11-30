package com.github.gtache.fxml.compiler.parsing;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestParsedInclude {

    private final Map<String, ParsedProperty> attributes;
    private final ParsedProperty property;
    private final String string;
    private final ParsedInclude include;

    TestParsedInclude(@Mock final ParsedProperty property) {
        this.attributes = new HashMap<>();
        this.property = Objects.requireNonNull(property);
        this.string = "str/ing";
        this.include = spy(ParsedInclude.class);
    }

    @BeforeEach
    void beforeEach() {
        when(include.attributes()).thenReturn(attributes);
        when(property.value()).thenReturn(string);
    }

    @Test
    void testControllerIdNull() {
        assertNull(include.controllerId());
    }

    @Test
    void testControllerId() {
        attributes.put("fx:id", property);
        assertEquals(string + "Controller", include.controllerId());
    }

    @Test
    void testResourcesNull() {
        assertNull(include.resources());
    }

    @Test
    void testResources() {
        attributes.put("resources", property);
        assertEquals(string.replace("/", "."), include.resources());
    }

    @Test
    void testSourceNull() {
        assertThrows(IllegalStateException.class, include::source);
    }

    @Test
    void testSource() {
        attributes.put("source", property);
        assertEquals(string, include.source());
    }

    @Test
    void testClassName() {
        assertEquals(ParsedInclude.class.getName(), include.className());
    }

    @Test
    void testProperties() {
        assertEquals(new LinkedHashMap<>(), include.properties());
    }

    @Test
    void testChildren() {
        assertEquals(List.of(), include.children());
    }
}
