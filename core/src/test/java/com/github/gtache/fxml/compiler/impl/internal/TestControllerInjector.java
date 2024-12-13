package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestControllerInjector {

    private final GenerationProgress progress;
    private final GenerationRequest request;
    private final GenerationParameters parameters;
    private final ControllerInfo controllerInfo;
    private final List<String> controllerFactoryPostAction;
    private final String id;
    private final String variable;
    private final ParsedProperty property;
    private final String propertyValue;
    private final StringBuilder sb;

    TestControllerInjector(@Mock final GenerationProgress progress, @Mock final GenerationRequest request,
                           @Mock final GenerationParameters parameters, @Mock final ControllerInfo controllerInfo,
                           @Mock final ParsedProperty property) {
        this.progress = Objects.requireNonNull(progress);
        this.request = Objects.requireNonNull(request);
        this.parameters = Objects.requireNonNull(parameters);
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
        when(progress.request()).thenReturn(request);
        when(request.parameters()).thenReturn(parameters);
        when(request.controllerInfo()).thenReturn(controllerInfo);
        when(progress.controllerFactoryPostAction()).thenReturn(controllerFactoryPostAction);
        when(progress.stringBuilder()).thenReturn(sb);
        when(property.name()).thenReturn("name");
        when(property.value()).thenReturn(propertyValue);
    }

    @Test
    void testInjectControllerFieldFactory() throws GenerationException {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.FACTORY);
        ControllerInjector.injectControllerField(progress, id, variable);
        final var expected = "        fieldMap.put(\"" + id + "\", " + variable + ");\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldAssign() throws GenerationException {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.ASSIGN);
        ControllerInjector.injectControllerField(progress, id, variable);
        final var expected = "        controller." + id + " = " + variable + ";\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldSetters() throws GenerationException {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.SETTERS);
        ControllerInjector.injectControllerField(progress, id, variable);
        final var expected = "        controller." + GenerationHelper.getSetMethod(id) + "(" + variable + ");\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldReflection() throws GenerationException {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.REFLECTION);
        ControllerInjector.injectControllerField(progress, id, variable);
        final var expected = "        injectField(\"" + id + "\", " + variable + ");\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectControllerFieldUnknown() {
        when(parameters.fieldInjectionType()).thenReturn(null);
        assertThrows(GenerationException.class, () -> ControllerInjector.injectControllerField(progress, id, variable));
    }

    @Test
    void testInjectEventHandlerReferenceFactoryNoArgument() throws GenerationException {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.FACTORY);
        when(parameters.methodInjectionType()).thenReturn(ControllerMethodsInjectionType.REFERENCE);
        ControllerInjector.injectEventHandlerControllerMethod(progress, property, variable);
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(e -> controller." + property.value().replace("#", "") + "());\n";
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        assertEquals("", sb.toString());
    }

    @Test
    void testInjectEventHandlerReferenceFactoryWithArgument() throws GenerationException {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.FACTORY);
        when(parameters.methodInjectionType()).thenReturn(ControllerMethodsInjectionType.REFERENCE);
        when(controllerInfo.handlerHasArgument(propertyValue.replace("#", ""))).thenReturn(true);
        ControllerInjector.injectEventHandlerControllerMethod(progress, property, variable);
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(controller::" + propertyValue.replace("#", "") + ");\n";
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        assertEquals("", sb.toString());
    }

    @Test
    void testInjectEventHandlerReflectionAssign() throws GenerationException {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.ASSIGN);
        when(parameters.methodInjectionType()).thenReturn(ControllerMethodsInjectionType.REFLECTION);
        ControllerInjector.injectEventHandlerControllerMethod(progress, property, variable);
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(e -> callEventHandlerMethod(\"" + propertyValue.replace("#", "") + "\", e));\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectEventHandlerUnknownMethod() {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.ASSIGN);
        when(parameters.methodInjectionType()).thenReturn(null);
        assertThrows(GenerationException.class, () -> ControllerInjector.injectEventHandlerControllerMethod(progress, property, variable));
    }

    @Test
    void testInjectEventHandlerUnknownField() {
        when(parameters.fieldInjectionType()).thenReturn(null);
        when(parameters.methodInjectionType()).thenReturn(ControllerMethodsInjectionType.REFLECTION);
        assertThrows(GenerationException.class, () -> ControllerInjector.injectEventHandlerControllerMethod(progress, property, variable));
    }

    @Test
    void testInjectCallbackReflectionSetters() throws GenerationException {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.ASSIGN);
        when(parameters.methodInjectionType()).thenReturn(ControllerMethodsInjectionType.REFLECTION);
        ControllerInjector.injectCallbackControllerMethod(progress, property, variable, "clazz");
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(e -> callCallbackMethod(\"" + propertyValue.replace("#", "") + "\", e, clazz));\n";
        assertEquals(expected, sb.toString());
        assertTrue(controllerFactoryPostAction.isEmpty());
    }

    @Test
    void testInjectCallbackReferenceFactory() throws GenerationException {
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionTypes.FACTORY);
        when(parameters.methodInjectionType()).thenReturn(ControllerMethodsInjectionType.REFERENCE);
        ControllerInjector.injectCallbackControllerMethod(progress, property, variable, "clazz");
        final var expected = "        " + variable + "." + GenerationHelper.getSetMethod(property.name()) + "(controller::" + propertyValue.replace("#", "") + ");\n";
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        assertEquals("", sb.toString());
    }
}
