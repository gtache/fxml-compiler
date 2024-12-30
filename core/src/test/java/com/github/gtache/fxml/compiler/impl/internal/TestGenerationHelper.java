package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestGenerationHelper {

    private final ParsedObject parsedObject;
    private final Map<String, ParsedProperty> attributes;
    private final String className;
    private final ParsedProperty property;
    private final String propertyName;

    TestGenerationHelper(@Mock final ParsedObject parsedObject, @Mock final ParsedProperty property) {
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.property = Objects.requireNonNull(property);
        this.attributes = new HashMap<>();
        this.className = "java.lang.String";
        this.propertyName = "property";
    }

    @BeforeEach
    void beforeEach() {
        when(parsedObject.attributes()).thenReturn(attributes);
        when(parsedObject.className()).thenReturn(className);
        when(property.name()).thenReturn(propertyName);
    }

    @Test
    void testGetVariablePrefixObject() {
        assertEquals("string", GenerationHelper.getVariablePrefix(parsedObject));
    }

    @Test
    void testGetVariablePrefix() {
        assertEquals("int", GenerationHelper.getVariablePrefix("int"));
    }

    @Test
    void testGetGetMethodProperty() {
        assertEquals("getProperty", GenerationHelper.getGetMethod(property));
    }

    @Test
    void testGetGetMethod() {
        assertEquals("getSomething", GenerationHelper.getGetMethod("something"));
    }

    @Test
    void testGetIsMethodProperty() {
        assertEquals("isProperty", GenerationHelper.getIsMethod(property));
    }

    @Test
    void testGetIsMethod() {
        assertEquals("isSomething", GenerationHelper.getIsMethod("something"));
    }

    @Test
    void testGetSetMethodProperty() {
        assertEquals("setProperty", GenerationHelper.getSetMethod(property));
    }

    @Test
    void testGetSetMethod() {
        assertEquals("setSomething", GenerationHelper.getSetMethod("something"));
    }

    @Test
    void testGetSortedAttributes() {
        attributes.put("a", new ParsedPropertyImpl("a", null, "valueA"));
        attributes.put("b", new ParsedPropertyImpl("b", null, "valueB"));
        attributes.put("c", new ParsedPropertyImpl("c", null, "valueC"));
        final var expected = List.of(attributes.get("a"), attributes.get("b"), attributes.get("c"));
        assertEquals(expected, GenerationHelper.getSortedAttributes(parsedObject));
    }
}
