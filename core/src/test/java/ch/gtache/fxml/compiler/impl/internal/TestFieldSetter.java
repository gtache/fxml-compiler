package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.ControllerFieldInjectionType;
import ch.gtache.fxml.compiler.GenerationException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestFieldSetter {

    private final HelperProvider helperProvider;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final StringBuilder sb;
    private final SequencedCollection<String> controllerFactoryPostAction;
    private final ParsedProperty property;
    private final String propertyName;
    private final String propertyValue;
    private final String parentVariable;

    TestFieldSetter(@Mock final HelperProvider helperProvider, @Mock final GenerationCompatibilityHelper compatibilityHelper,
                    @Mock final ParsedProperty property) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.property = Objects.requireNonNull(property);
        this.propertyName = "propertyName";
        this.propertyValue = "$controller.value";
        this.parentVariable = "variable";
        this.sb = new StringBuilder();
        this.controllerFactoryPostAction = new ArrayList<>();
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(compatibilityHelper.getStartVar(anyString(), anyInt())).then(i -> i.getArgument(0));
        when(property.name()).thenReturn(propertyName);
        when(property.value()).thenReturn(propertyValue);
    }

    @Test
    void testSetEventHandlerAssign() throws GenerationException {
        final var setter = new FieldSetter(helperProvider, ControllerFieldInjectionType.ASSIGN, sb, controllerFactoryPostAction);
        setter.setEventHandler(property, parentVariable);
        final var expected = "        " + parentVariable + ".setPropertyName(controller.value);\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testSetFieldAssignException() {
        final var setter = new FieldSetter(helperProvider, ControllerFieldInjectionType.ASSIGN, sb, controllerFactoryPostAction);
        when(property.value()).thenReturn("x.value");
        assertThrows(GenerationException.class, () -> setter.setField(property, parentVariable, ""));
    }

    @Test
    void testSetFieldFactory() throws GenerationException {
        final var setter = new FieldSetter(helperProvider, ControllerFieldInjectionType.FACTORY, sb, controllerFactoryPostAction);
        setter.setField(property, parentVariable, "");
        final var expected = "        " + parentVariable + ".setPropertyName(controller.getValue());\n";
        assertEquals("", sb.toString());
        assertEquals(List.of(expected), controllerFactoryPostAction);
    }

    @Test
    void testSetFieldFactoryException() {
        final var setter = new FieldSetter(helperProvider, ControllerFieldInjectionType.FACTORY, sb, controllerFactoryPostAction);
        when(property.value()).thenReturn("x.value");
        assertThrows(GenerationException.class, () -> setter.setField(property, parentVariable, ""));
    }

    @Test
    void testSetReflection() throws GenerationException {
        final var setter = new FieldSetter(helperProvider, ControllerFieldInjectionType.REFLECTION, sb, controllerFactoryPostAction);
        setter.setField(property, parentVariable, "javafx.scene.control.Button");
        final var expected = """
                        try {
                            java.lang.reflect.Fieldfield = controller.getClass().getDeclaredField("value");
                            field.setAccessible(true);
                            final var value = (javafx.scene.control.Button) field.get(controller);
                            variable.setPropertyName(value);
                        } catch (final NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                """;
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testSetReflectionException() {
        final var setter = new FieldSetter(helperProvider, ControllerFieldInjectionType.REFLECTION, sb, controllerFactoryPostAction);
        when(property.value()).thenReturn("x.value");
        assertThrows(GenerationException.class, () -> setter.setField(property, parentVariable, ""));
    }

    @Test
    void testSetFieldSetters() throws GenerationException {
        final var setter = new FieldSetter(helperProvider, ControllerFieldInjectionType.SETTERS, sb, controllerFactoryPostAction);
        setter.setField(property, parentVariable, "");
        final var expected = "        " + parentVariable + ".setPropertyName(controller.getValue());\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testSetFieldSettersException() {
        final var setter = new FieldSetter(helperProvider, ControllerFieldInjectionType.SETTERS, sb, controllerFactoryPostAction);
        when(property.value()).thenReturn("x.value");
        assertThrows(GenerationException.class, () -> setter.setField(property, parentVariable, ""));
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new FieldSetter(null, ControllerFieldInjectionType.ASSIGN, sb, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new FieldSetter(helperProvider, null, sb, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new FieldSetter(helperProvider, ControllerFieldInjectionType.ASSIGN, null, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new FieldSetter(helperProvider, ControllerFieldInjectionType.ASSIGN, sb, null));
    }
}
