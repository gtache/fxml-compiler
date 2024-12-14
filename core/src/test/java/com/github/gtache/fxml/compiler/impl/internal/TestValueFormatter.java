package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;
import javafx.geometry.Pos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestValueFormatter {

    private final Map<String, VariableInfo> idToVariableInfo;
    private final InjectionType resourceInjectionType;
    private final ValueFormatter formatter;

    TestValueFormatter(@Mock final InjectionType resourceInjectionType) {
        this.resourceInjectionType = requireNonNull(resourceInjectionType);
        this.idToVariableInfo = new HashMap<>();
        this.formatter = new ValueFormatter(resourceInjectionType, idToVariableInfo);
    }

    @Test
    void testGetArgStringResourceUnknown() {
        assertThrows(GenerationException.class, () -> formatter.getArg("%value", String.class));
    }

    @Test
    void testGetArgStringResourceSimpleGet() throws GenerationException {
        final var types = List.of(ResourceBundleInjectionTypes.CONSTRUCTOR, ResourceBundleInjectionTypes.CONSTRUCTOR_NAME, ResourceBundleInjectionTypes.GET_BUNDLE);
        for (final var type : types) {
            final var resourceFormatter = new ValueFormatter(type, idToVariableInfo);
            assertEquals("resourceBundle.getString(\"value\")", resourceFormatter.getArg("%value", String.class));
        }
    }

    @Test
    void testGetArgStringResourceController() throws GenerationException {
        final var resourceFormatter = new ValueFormatter(ResourceBundleInjectionTypes.GETTER, idToVariableInfo);
        assertEquals("controller.resources().getString(\"value\")", resourceFormatter.getArg("%value", String.class));
    }

    @Test
    void testGetArgStringResourceFunction() throws GenerationException {
        final var resourceFormatter = new ValueFormatter(ResourceBundleInjectionTypes.CONSTRUCTOR_FUNCTION, idToVariableInfo);
        assertEquals("resourceBundleFunction.apply(\"value\")", resourceFormatter.getArg("%value", String.class));
    }

    @Test
    void testGetArgRelativePath() throws GenerationException {
        assertEquals("getClass().getResource(\"value\").toString()", formatter.getArg("@value", String.class));
        assertEquals("getClass().getResource(\"value\").toString()", formatter.getArg("@value", URL.class));
    }

    @Test
    void testGetArgBinding() {
        assertThrows(GenerationException.class, () -> formatter.getArg("${value}", String.class));
    }

    @Test
    void testGetArgExpressionNull() {
        assertThrows(GenerationException.class, () -> formatter.getArg("$value", String.class));
    }

    @Test
    void testGetArgExpression() throws GenerationException {
        final var info = mock(VariableInfo.class);
        when(info.variableName()).thenReturn("variable");
        idToVariableInfo.put("value", info);
        assertEquals("variable", formatter.getArg("$value", String.class));
    }

    @Test
    void testGetArgOther() throws GenerationException {
        assertEquals("\"value\"", formatter.getArg("value", String.class));
    }

    @Test
    void testToStringString() {
        assertEquals("\"value\"", formatter.toString("value", String.class));
    }

    @Test
    void testToStringEscape() {
        assertEquals("\"val\\\\u\\\"e\"", formatter.toString("\\val\\u\"e", String.class));
    }

    @Test
    void testToStringChar() {
        assertEquals("'v'", formatter.toString("v", char.class));
        assertEquals("'v'", formatter.toString("v", Character.class));
    }

    @Test
    void testToStringBoolean() {
        assertEquals("true", formatter.toString("true", boolean.class));
        assertEquals("true", formatter.toString("true", Boolean.class));
    }

    @Test
    void testToStringInteger() {
        final var types = List.of(byte.class, Byte.class, short.class, Short.class, long.class, Long.class, int.class, Integer.class);
        for (final var type : types) {
            assertEquals("1", formatter.toString("1", type));
            assertEquals(ReflectionHelper.getWrapperClass(type) + ".valueOf(\"value\")", formatter.toString("value", type));
        }
    }

    @Test
    void testToStringDecimal() {
        final var types = List.of(float.class, Float.class, double.class, Double.class);
        for (final var type : types) {
            assertEquals("1.0", formatter.toString("1.0", type));
            assertEquals(ReflectionHelper.getWrapperClass(type) + ".valueOf(\"value\")", formatter.toString("value", type));
        }
    }

    @Test
    void testToStringValueOfEnum() {
        assertEquals("javafx.geometry.Pos.value", formatter.toString("value", Pos.class));
    }

    @Test
    void testToStringValueOfColor() {
        assertEquals("javafx.scene.paint.Color.valueOf(\"value\")", formatter.toString("value", javafx.scene.paint.Color.class));
    }

    @Test
    void testOther() {
        assertEquals("value", formatter.toString("value", Object.class));
    }
}
