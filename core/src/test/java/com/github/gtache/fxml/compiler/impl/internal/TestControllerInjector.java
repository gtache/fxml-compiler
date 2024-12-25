package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.ControllerMethodsInjectionType;
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
    void testInjectControllerFieldFactory() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionType.FACTORY, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectControllerField(id, variable);
        final var expected = "        fieldMap.put(\"" + id + "\", " + variable + ");\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldAssign() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionType.ASSIGN, com.github.gtache.fxml.compiler.ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectControllerField(id, variable);
        final var expected = "        controller." + id + " = " + variable + ";\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldSetters() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionType.SETTERS, com.github.gtache.fxml.compiler.ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectControllerField(id, variable);
        final var expected = "        controller." + GenerationHelper.getSetMethod(id) + "(" + variable + ");\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldReflection() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionType.REFLECTION, com.github.gtache.fxml.compiler.ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectControllerField(id, variable);
        final var expected = "        injectField(\"" + id + "\", " + variable + ");\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectEventHandlerReferenceFactoryNoArgument() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionType.FACTORY, com.github.gtache.fxml.compiler.ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectEventHandlerControllerMethod(property, variable);
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(e -> controller." + property.value().replace("#", "") + "());\n";
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        assertEquals("", sb.toString());
    }

    @Test
    void testInjectEventHandlerReferenceFactoryWithArgument() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionType.FACTORY, com.github.gtache.fxml.compiler.ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        when(controllerInfo.handlerHasArgument(propertyValue.replace("#", ""))).thenReturn(true);
        injector.injectEventHandlerControllerMethod(property, variable);
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(controller::" + propertyValue.replace("#", "") + ");\n";
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        assertEquals("", sb.toString());
    }

    @Test
    void testInjectEventHandlerReflectionAssign() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionType.ASSIGN, com.github.gtache.fxml.compiler.ControllerMethodsInjectionType.REFLECTION, sb, controllerFactoryPostAction);
        injector.injectEventHandlerControllerMethod(property, variable);
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(e -> callEventHandlerMethod(\"" + propertyValue.replace("#", "") + "\", e));\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectCallbackReflectionSetters() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionType.ASSIGN, com.github.gtache.fxml.compiler.ControllerMethodsInjectionType.REFLECTION, sb, controllerFactoryPostAction);
        injector.injectCallbackControllerMethod(property, variable, "clazz");
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(e -> callCallbackMethod(\"" + propertyValue.replace("#", "") + "\", e, clazz));\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectCallbackReferenceFactory() {
        final var injector = new ControllerInjector(controllerInfo, ControllerFieldInjectionType.FACTORY, com.github.gtache.fxml.compiler.ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction);
        injector.injectCallbackControllerMethod(property, variable, "clazz");
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(controller::" + propertyValue.replace("#", "") + ");\n";
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        assertEquals("", sb.toString());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ControllerInjector(null, ControllerFieldInjectionType.ASSIGN, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new ControllerInjector(controllerInfo, null, ControllerMethodsInjectionType.REFERENCE, sb, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new ControllerInjector(controllerInfo, ControllerFieldInjectionType.ASSIGN, null, sb, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new ControllerInjector(controllerInfo, ControllerFieldInjectionType.ASSIGN, ControllerMethodsInjectionType.REFERENCE, null, controllerFactoryPostAction));
        assertThrows(NullPointerException.class, () -> new ControllerInjector(controllerInfo, ControllerFieldInjectionType.ASSIGN, ControllerMethodsInjectionType.REFERENCE, sb, null));
    }
}
