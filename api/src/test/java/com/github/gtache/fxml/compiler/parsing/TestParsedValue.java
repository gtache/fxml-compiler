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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestParsedValue {

    private final Map<String, ParsedProperty> attributes;
    private final ParsedProperty property;
    private final String string;
    private final ParsedValue value;

    TestParsedValue(@Mock final ParsedProperty property) {
        this.attributes = new HashMap<>();
        this.property = Objects.requireNonNull(property);
        this.string = "str/ing";
        this.value = spy(ParsedValue.class);
    }

    @BeforeEach
    void beforeEach() {
        when(value.attributes()).thenReturn(attributes);
        when(property.value()).thenReturn(string);
    }

    @Test
    void testConstantNull() {
        assertThrows(IllegalStateException.class, value::value);
    }

    @Test
    void testConstant() {
        attributes.put("fx:value", property);
        assertEquals(string, value.value());
    }

    @Test
    void testProperties() {
        assertEquals(new LinkedHashMap<>(), value.properties());
    }

    @Test
    void testChildren() {
        assertEquals(List.of(), value.children());
    }
}
