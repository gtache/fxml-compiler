package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.GenerationException;
import ch.gtache.fxml.compiler.ResourceBundleInjectionType;
import javafx.geometry.Pos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestValueFormatter {

    private final HelperProvider helperProvider;
    private final VariableProvider variableProvider;
    private final ValueFormatter formatter;

    TestValueFormatter(@Mock final HelperProvider helperProvider, @Mock final VariableProvider variableProvider,
                       @Mock final ResourceBundleInjectionType resourceInjectionType) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.formatter = new ValueFormatter(helperProvider, resourceInjectionType);
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
    }

    @Test
    void testGetArgStringResourceSimpleGet() throws GenerationException {
        final var types = List.of(ResourceBundleInjectionType.CONSTRUCTOR, ResourceBundleInjectionType.CONSTRUCTOR_NAME, ResourceBundleInjectionType.GET_BUNDLE);
        for (final var type : types) {
            final var resourceFormatter = new ValueFormatter(helperProvider, type);
            assertEquals("resourceBundle.getString(\"value\")", resourceFormatter.getArg("%value", String.class));
        }
    }

    @Test
    void testGetArgStringResourceController() throws GenerationException {
        final var resourceFormatter = new ValueFormatter(helperProvider, ResourceBundleInjectionType.GETTER);
        assertEquals("controller.resources().getString(\"value\")", resourceFormatter.getArg("%value", String.class));
    }

    @Test
    void testGetArgStringResourceFunction() throws GenerationException {
        final var resourceFormatter = new ValueFormatter(helperProvider, ResourceBundleInjectionType.CONSTRUCTOR_FUNCTION);
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
        when(variableProvider.getVariableInfo("value")).thenReturn(info);
        when(info.variableName()).thenReturn("variable");
        assertEquals("variable", formatter.getArg("$value", String.class));
    }

    @Test
    void testGetArgOther() throws GenerationException {
        assertEquals("\"value\"", formatter.getArg("value", String.class));
    }

    @Test
    void testToStringString() {
        assertEquals("\"value\"", ValueFormatter.toString("value", String.class));
    }

    @Test
    void testToStringEscape() {
        assertEquals("\"val\\\\u\\\"e\"", ValueFormatter.toString("\\val\\u\"e", String.class));
    }

    @Test
    void testToStringChar() {
        assertEquals("'v'", ValueFormatter.toString("v", char.class));
        assertEquals("'v'", ValueFormatter.toString("v", Character.class));
    }

    @Test
    void testToStringBoolean() {
        assertEquals("true", ValueFormatter.toString("true", boolean.class));
        assertEquals("true", ValueFormatter.toString("true", Boolean.class));
    }

    @Test
    void testToStringInteger() {
        final var types = List.of(byte.class, Byte.class, short.class, Short.class, long.class, Long.class, int.class, Integer.class);
        for (final var type : types) {
            assertEquals("1", ValueFormatter.toString("1", type));
            assertEquals(ReflectionHelper.getWrapperClass(type) + ".valueOf(\"value\")", ValueFormatter.toString("value", type));
        }
    }

    @Test
    void testToStringDecimal() {
        final var types = List.of(float.class, Float.class, double.class, Double.class);
        for (final var type : types) {
            assertEquals("1.0", ValueFormatter.toString("1.0", type));
            assertEquals(ReflectionHelper.getWrapperClass(type) + ".valueOf(\"value\")", ValueFormatter.toString("value", type));
        }
    }

    @Test
    void testToStringValueOfEnum() {
        assertEquals("javafx.geometry.Pos.value", ValueFormatter.toString("value", Pos.class));
    }

    @Test
    void testToStringValueOfColor() {
        assertEquals("javafx.scene.paint.Color.valueOf(\"value\")", ValueFormatter.toString("value", javafx.scene.paint.Color.class));
    }

    @Test
    void testOther() {
        assertEquals("value", ValueFormatter.toString("value", Object.class));
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ValueFormatter(null, ResourceBundleInjectionType.CONSTRUCTOR));
        assertThrows(NullPointerException.class, () -> new ValueFormatter(helperProvider, null));
    }
}
