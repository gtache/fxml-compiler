package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.ControllerFieldInjectionType;
import ch.gtache.fxml.compiler.GenerationException;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestExpressionFormatter {

    private final HelperProvider helperProvider;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final VariableProvider variableProvider;
    private final VariableInfo variableInfo;
    private final ControllerFieldInjectionType fieldInjectionType;
    private final StringBuilder sb;
    private final ExpressionFormatter formatter;

    TestExpressionFormatter(@Mock final HelperProvider helperProvider, @Mock final GenerationCompatibilityHelper compatibilityHelper,
                            @Mock final VariableProvider variableProvider, @Mock final ControllerFieldInjectionType fieldInjectionType,
                            @Mock final VariableInfo variableInfo) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.fieldInjectionType = Objects.requireNonNull(fieldInjectionType);
        this.variableInfo = Objects.requireNonNull(variableInfo);
        this.sb = new StringBuilder();
        this.formatter = new ExpressionFormatter(helperProvider, fieldInjectionType, sb);
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
        when(variableProvider.getVariableInfo(anyString())).thenReturn(variableInfo);
        when(variableProvider.getNextVariableName(anyString())).then(i -> i.getArgument(0));
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        when(compatibilityHelper.getStartVar(anyString(), anyInt())).then(i -> i.getArgument(0));
    }

    @Test
    void testFormatNonDotNoInfo() {
        when(variableProvider.getVariableInfo(anyString())).thenReturn(null);
        assertThrows(GenerationException.class, () -> formatter.format("${value}", StringProperty.class));
    }

    @Test
    void testFormatNonDot() throws GenerationException {
        when(variableInfo.variableName()).thenReturn("variableName");
        assertEquals("variableName", formatter.format("${value}", StringProperty.class));
        assertEquals("", sb.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"${controller.}", "${.value}", "${controller.value.value}"})
    void testGetDotExpressionBadSplit(final String expression) {
        assertThrows(GenerationException.class, () -> formatter.format(expression, StringProperty.class));
    }

    @Test
    void testFormatNonControllerNoInfo() {
        when(variableProvider.getVariableInfo(anyString())).thenReturn(null);
        assertThrows(GenerationException.class, () -> formatter.format("${other.text}", StringProperty.class));
    }

    @Test
    void testFormatNonControllerCantRead() {
        when(variableInfo.className()).thenReturn("javafx.scene.control.ComboBox");
        assertThrows(GenerationException.class, () -> formatter.format("${other.text}", StringProperty.class));
    }

    @Test
    void testFormatNonController() throws GenerationException {
        when(variableInfo.variableName()).thenReturn("variableName");
        when(variableInfo.className()).thenReturn("javafx.scene.control.TextField");
        formatter.format("${other.text}", StringProperty.class);
        assertEquals("variableName.textProperty()", formatter.format("${other.text}", StringProperty.class));
        assertEquals("", sb.toString());
    }

    @Test
    void testFormatControllerReflection() throws GenerationException {
        final var reflectionFormatter = new ExpressionFormatter(helperProvider, ControllerFieldInjectionType.REFLECTION, sb);
        final var expected = """
                javafx.beans.property.StringPropertybinding;
                        try {
                            java.lang.reflect.Fieldfield = controller.getClass().getDeclaredField("text");
                            field.setAccessible(true);
                            binding = (javafx.beans.property.StringProperty) field.get(controller);
                        } catch (final NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                """;
        assertEquals("binding", reflectionFormatter.format("${controller.text}", StringProperty.class));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatControllerSetters() throws GenerationException {
        final var settersFormatter = new ExpressionFormatter(helperProvider, ControllerFieldInjectionType.SETTERS, sb);
        assertEquals("controller.textProperty()", settersFormatter.format("${controller.text}", StringProperty.class));
    }

    @Test
    void testFormatControllerFactory() throws GenerationException {
        final var factoryFormatter = new ExpressionFormatter(helperProvider, ControllerFieldInjectionType.FACTORY, sb);
        assertEquals("controller.textProperty()", factoryFormatter.format("${controller.text}", StringProperty.class));
    }

    @Test
    void testFormatControllerAssign() throws GenerationException {
        final var assignFormatter = new ExpressionFormatter(helperProvider, ControllerFieldInjectionType.ASSIGN, sb);
        assertEquals("controller.text", assignFormatter.format("${controller.text}", StringProperty.class));
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ExpressionFormatter(null, fieldInjectionType, sb));
        assertThrows(NullPointerException.class, () -> new ExpressionFormatter(helperProvider, null, sb));
        assertThrows(NullPointerException.class, () -> new ExpressionFormatter(helperProvider, fieldInjectionType, null));
    }
}
