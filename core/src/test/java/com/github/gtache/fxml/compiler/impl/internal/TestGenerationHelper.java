package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInfo;
import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationRequest;
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

    private final GenerationProgress progress;
    private final GenerationRequest request;
    private final ControllerInfo controllerInfo;
    private final ControllerFieldInfo fieldInfo;
    private final Map<String, VariableInfo> idToVariableInfo;
    private final String variableName;
    private final ParsedObject object;
    private final Map<String, ParsedProperty> attributes;
    private final String className;
    private final ParsedProperty property;
    private final String propertyName;

    TestGenerationHelper(@Mock final GenerationProgress progress, @Mock final GenerationRequest request,
                         @Mock final ControllerInfo controllerInfo, @Mock final ControllerFieldInfo fieldInfo,
                         @Mock final ParsedObject object, @Mock final ParsedProperty property) {
        this.progress = Objects.requireNonNull(progress);
        this.request = Objects.requireNonNull(request);
        this.controllerInfo = Objects.requireNonNull(controllerInfo);
        this.fieldInfo = Objects.requireNonNull(fieldInfo);
        this.object = Objects.requireNonNull(object);
        this.property = Objects.requireNonNull(property);
        this.idToVariableInfo = new HashMap<>();
        this.variableName = "variable";
        this.attributes = new HashMap<>();
        this.className = "java.lang.String";
        this.propertyName = "property";
    }

    @BeforeEach
    void beforeEach() {
        when(progress.request()).thenReturn(request);
        when(request.controllerInfo()).thenReturn(controllerInfo);
        when(object.attributes()).thenReturn(attributes);
        when(object.className()).thenReturn(className);
        when(property.name()).thenReturn(propertyName);
        when(progress.idToVariableInfo()).thenReturn(idToVariableInfo);
    }

    @Test
    void testGetVariablePrefixObject() {
        assertEquals("string", GenerationHelper.getVariablePrefix(object));
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
        assertEquals("getSomething", GenerationHelper.getGetMethod("Something"));
    }

    @Test
    void testGetSetMethodProperty() {
        assertEquals("setProperty", GenerationHelper.getSetMethod(property));
    }

    @Test
    void testGetSetMethod() {
        assertEquals("setSomething", GenerationHelper.getSetMethod("Something"));
    }

    @Test
    void testGetSortedAttributes() {
        attributes.put("a", new ParsedPropertyImpl("a", null, "valueA"));
        attributes.put("b", new ParsedPropertyImpl("b", null, "valueB"));
        attributes.put("c", new ParsedPropertyImpl("c", null, "valueC"));
        final var expected = List.of(attributes.get("a"), attributes.get("b"), attributes.get("c"));
        assertEquals(expected, GenerationHelper.getSortedAttributes(object));
    }
}
