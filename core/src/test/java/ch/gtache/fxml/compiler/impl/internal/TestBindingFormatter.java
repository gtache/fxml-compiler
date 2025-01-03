package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.ControllerFieldInjectionType;
import ch.gtache.fxml.compiler.GenerationException;
import ch.gtache.fxml.compiler.parsing.ParsedObject;
import ch.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestBindingFormatter {

    private final HelperProvider helperProvider;
    private final ExpressionFormatter expressionFormatter;
    private final ControllerFieldInjectionType fieldInjectionType;
    private final StringBuilder sb;
    private final SequencedCollection<String> controllerFactoryPostAction;
    private final ParsedProperty property;
    private final ParsedObject parent;
    private final String parentVariable;
    private final BindingFormatter bindingFormatter;

    TestBindingFormatter(@Mock final HelperProvider helperProvider, @Mock final ExpressionFormatter expressionFormatter,
                         @Mock final ControllerFieldInjectionType fieldInjectionType, @Mock final ParsedProperty property, @Mock final ParsedObject parent) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.expressionFormatter = Objects.requireNonNull(expressionFormatter);
        this.fieldInjectionType = Objects.requireNonNull(fieldInjectionType);
        this.property = Objects.requireNonNull(property);
        this.parent = Objects.requireNonNull(parent);
        this.parentVariable = "parentVariable";
        this.sb = new StringBuilder();
        this.controllerFactoryPostAction = new ArrayList<>();
        this.bindingFormatter = new BindingFormatter(helperProvider, fieldInjectionType, sb, controllerFactoryPostAction);
    }

    @BeforeEach
    void beforeEach() throws GenerationException {
        when(helperProvider.getExpressionFormatter()).thenReturn(expressionFormatter);
        when(expressionFormatter.format(anyString(), any())).then(i -> i.getArgument(0) + "-" + i.getArgument(1));
    }

    @Test
    void testFormatDoesntEndValid() {
        when(property.value()).thenReturn("${value");
        assertThrows(GenerationException.class, () -> bindingFormatter.formatBinding(property, parent, parentVariable));
    }

    @Test
    void testFormatDoesntStartValid() {
        when(property.value()).thenReturn("value}");
        assertThrows(GenerationException.class, () -> bindingFormatter.formatBinding(property, parent, parentVariable));
    }

    @Test
    void testFormatSimpleNoReadProperty() {
        when(property.name()).thenReturn("abc");
        when(property.value()).thenReturn("${value}");
        when(parent.className()).thenReturn("javafx.scene.control.Label");
        assertThrows(GenerationException.class, () -> bindingFormatter.formatBinding(property, parent, parentVariable));
    }

    @Test
    void testFormatSimple() throws GenerationException {
        when(property.name()).thenReturn("text");
        when(property.value()).thenReturn("${value}");
        when(parent.className()).thenReturn("javafx.scene.control.Label");
        bindingFormatter.formatBinding(property, parent, parentVariable);
        final var expected = """
                        parentVariable.textProperty().bind(${value}-class javafx.beans.property.StringProperty);
                """;
        assertEquals(expected, sb.toString());
        assertEquals(List.of(), controllerFactoryPostAction);
    }

    @Test
    void testFormatBidirectional() throws GenerationException {
        when(property.name()).thenReturn("text");
        when(property.value()).thenReturn("#{value}");
        when(parent.className()).thenReturn("javafx.scene.control.Label");
        bindingFormatter.formatBinding(property, parent, parentVariable);
        final var expected = """
                        parentVariable.textProperty().bindBidirectional(#{value}-class javafx.beans.property.StringProperty);
                """;
        assertEquals(expected, sb.toString());
        assertEquals(List.of(), controllerFactoryPostAction);
    }

    @Test
    void testFormatSimpleControllerFactory() throws GenerationException {
        final var factoryFormatter = new BindingFormatter(helperProvider, ControllerFieldInjectionType.FACTORY, sb, controllerFactoryPostAction);
        when(property.name()).thenReturn("text");
        when(property.value()).thenReturn("${controller.value}");
        when(parent.className()).thenReturn("javafx.scene.control.Label");
        factoryFormatter.formatBinding(property, parent, parentVariable);
        final var expected = """
                        parentVariable.textProperty().bind(${controller.value}-class javafx.beans.property.StringProperty);
                """;
        assertEquals("", sb.toString());
        assertEquals(List.of(expected), controllerFactoryPostAction);
    }

    @Test
    void testFormatBidirectionalNoWriteProperty() {
        when(property.name()).thenReturn("labelPadding");
        when(property.value()).thenReturn("#{value}");
        when(parent.className()).thenReturn("javafx.scene.control.Label");
        assertThrows(GenerationException.class, () -> bindingFormatter.formatBinding(property, parent, parentVariable));
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new BindingFormatter(null, fieldInjectionType, sb, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new BindingFormatter(helperProvider, null, sb, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new BindingFormatter(helperProvider, fieldInjectionType, null, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new BindingFormatter(helperProvider, fieldInjectionType, sb, null));
    }
}
