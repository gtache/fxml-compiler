package com.github.gtache.fxml.compiler.parsing;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestParsedConstant {

    private final Map<String, ParsedProperty> attributes;
    private final ParsedProperty property;
    private final String string;
    private final ParsedConstant constant;

    TestParsedConstant(@Mock final ParsedProperty property) {
        this.attributes = new HashMap<>();
        this.property = Objects.requireNonNull(property);
        this.string = "str/ing";
        this.constant = spy(ParsedConstant.class);
    }

    @BeforeEach
    void beforeEach() {
        when(constant.attributes()).thenReturn(attributes);
        when(property.value()).thenReturn(string);
    }

    @Test
    void testConstantNull() {
        assertThrows(IllegalStateException.class, constant::constant);
    }

    @Test
    void testConstant() {
        attributes.put("fx:constant", property);
        assertEquals(string, constant.constant());
    }

    @Test
    void testProperties() {
        assertEquals(new LinkedHashMap<>(), constant.properties());
    }
}
