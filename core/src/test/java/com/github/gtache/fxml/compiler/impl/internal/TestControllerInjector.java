package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestControllerInjector {

    private final ControllerInfo controllerInfo;
    private final List<String> controllerFactoryPostAction;
    private final String id;
    private final String variable;
    private final ParsedProperty property;
    private final String propertyValue;
    private final StringBuilder sb;

    TestControllerInjector(@Mock final ControllerInfo controllerInfo,
                           @Mock final ParsedProperty property) {
        this.controllerInfo = Objects.requireNonNull(controllerInfo);
        this.controllerFactoryPostAction = new ArrayList<>();
        this.id = "id";
        this.variable = "variable";
        this.propertyValue = "#property";
        this.property = Objects.requireNonNull(property);
        this.sb = new StringBuilder();
    }

    @BeforeEach
    void beforeEach() {
        when(property.name()).thenReturn("name");
        when(property.value()).thenReturn(propertyValue);
    }

    @Test
    void testInjectControllerFieldFactory() throws GenerationException {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.FACTORY, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectControllerField(id, variable);
        final var expected = "        fieldMap.put(\"" + id + "\", " + variable + ");\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldAssign() throws GenerationException {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.ASSIGN, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectControllerField(id, variable);
        final var expected = "        controller." + id + " = " + variable + ";\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldSetters() throws GenerationException {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.SETTERS, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectControllerField(id, variable);
        final var expected = "        controller." + GenerationHelper.getSetMethod(id) + "(" + variable + ");\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldReflection() throws GenerationException {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.REFLECTION, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectControllerField(id, variable);
        final var expected = "        injectField(\"" + id + "\", " + variable + ");\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldUnknown() {
        final var injector = new ControllerInjector(controllerInfo, mock(InjectionType.class), ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        assertThrows(GenerationException.class, () -> injector.injectControllerField(id, variable));
    }

    @Test
    void testInjectEventHandlerReferenceFactoryNoArgument() throws GenerationException {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.FACTORY, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectEventHandlerControllerMethod(property, variable);
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(e -> controller." + property.value().replace("#", "") + "());\n";
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        assertEquals("", sb.toString());
    }

    @Test
    void testInjectEventHandlerReferenceFactoryWithArgument() throws GenerationException {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.FACTORY, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        when(controllerInfo.handlerHasArgument(propertyValue.replace("#", ""))).thenReturn(true);
        injector.injectEventHandlerControllerMethod(property, variable);
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(controller::" + propertyValue.replace("#", "") + ");\n";
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        assertEquals("", sb.toString());
    }

    @Test
    void testInjectEventHandlerReflectionAssign() throws GenerationException {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.ASSIGN, ControllerMethodsInjectionType.REFLECTION, sb, controllerFactoryPostAction);
        injector.injectEventHandlerControllerMethod(property, variable);
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(e -> callEventHandlerMethod(\"" + propertyValue.replace("#", "") + "\", e));\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectEventHandlerUnknownMethod() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.ASSIGN, mock(InjectionType.class), sb, controllerFactoryPostAction);
        assertThrows(GenerationException.class, () -> injector.injectEventHandlerControllerMethod(property, variable));
    }

    @Test
    void testInjectEventHandlerUnknownField() {
        final var injector = new ControllerInjector(controllerInfo, mock(InjectionType.class), ControllerMethodsInjectionType.REFLECTION, sb, controllerFactoryPostAction);
        assertThrows(GenerationException.class, () -> injector.injectEventHandlerControllerMethod(property, variable));
    }

    @Test
    void testInjectCallbackReflectionSetters() throws GenerationException {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.ASSIGN, ControllerMethodsInjectionType.REFLECTION, sb, controllerFactoryPostAction);
        injector.injectCallbackControllerMethod(property, variable, "clazz");
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(e -> callCallbackMethod(\"" + propertyValue.replace("#", "") + "\", e, clazz));\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectCallbackReferenceFactory() throws GenerationException {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionTypes.FACTORY, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectCallbackControllerMethod(property, variable, "clazz");
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(controller::" + propertyValue.replace("#", "") + ");\n";
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        assertEquals("", sb.toString());
    }
}
