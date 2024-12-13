package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInfo;
import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.impl.GenericTypesImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestReflectionHelper {

    private final GenerationProgress progress;
    private final GenerationRequest request;
    private final Map<String, ParsedProperty> attributes;
    private final ControllerInfo controllerInfo;
    private final ControllerFieldInfo fieldInfo;
    private final ParsedObject parsedObject;

    TestReflectionHelper(@Mock final GenerationProgress progress, @Mock final GenerationRequest request,
                         @Mock final ControllerInfo controllerInfo, @Mock final ControllerFieldInfo fieldInfo,
                         @Mock final ParsedObject parsedObject) {
        this.progress = Objects.requireNonNull(progress);
        this.request = Objects.requireNonNull(request);
        this.controllerInfo = Objects.requireNonNull(controllerInfo);
        this.fieldInfo = Objects.requireNonNull(fieldInfo);
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.attributes = new HashMap<>();
    }

    @BeforeEach
    void beforeEach() {
        when(progress.request()).thenReturn(request);
        when(request.controllerInfo()).thenReturn(controllerInfo);
        when(controllerInfo.fieldInfo("id")).thenReturn(fieldInfo);
        when(parsedObject.attributes()).thenReturn(attributes);
        when(parsedObject.className()).thenReturn("javafx.scene.control.ComboBox");
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));
        when(fieldInfo.isGeneric()).thenReturn(true);
    }

    @Test
    void testIsGeneric() {
        assertFalse(ReflectionHelper.isGeneric(String.class));
        assertTrue(ReflectionHelper.isGeneric(ComboBox.class));
        assertTrue(ReflectionHelper.isGeneric(TableCell.class));
    }

    @Test
    void testHasMethod() {
        assertFalse(ReflectionHelper.hasMethod(String.class, "bla"));
        assertTrue(ReflectionHelper.hasMethod(String.class, "charAt"));
        assertTrue(ReflectionHelper.hasMethod(StackPane.class, "getChildren"));
    }

    @Test
    void testGetMethod() throws NoSuchMethodException {
        assertEquals(String.class.getMethod("charAt", int.class), ReflectionHelper.getMethod(String.class, "charAt"));
    }

    @Test
    void testHasStaticMethod() {
        assertTrue(ReflectionHelper.hasStaticMethod(HBox.class, "setHgrow"));
    }

    @Test
    void testGetStaticMethod() throws NoSuchMethodException {
        assertEquals(HBox.class.getMethod("setHgrow", Node.class, Priority.class), ReflectionHelper.getStaticMethod(HBox.class, "setHgrow"));
    }

    @Test
    void testHasValueOf() {
        assertTrue(ReflectionHelper.hasValueOf(Integer.class));
        assertTrue(ReflectionHelper.hasValueOf(Pos.class));
        assertFalse(ReflectionHelper.hasValueOf(HBox.class));
    }

    @Test
    void testGetConstructorArgsNamedArgsDefault() {
        final var parameters = new LinkedHashMap<String, Parameter>();
        parameters.put("p1", new Parameter("p1", int.class, "0"));
        parameters.put("p2", new Parameter("p2", Integer.class, "0"));
        parameters.put("p3", new Parameter("p3", char.class, "\u0000"));
        parameters.put("p4", new Parameter("p4", Character.class, "\u0000"));
        parameters.put("p5", new Parameter("p5", boolean.class, "false"));
        parameters.put("p6", new Parameter("p6", Boolean.class, "false"));
        parameters.put("p7", new Parameter("p7", byte.class, "0"));
        parameters.put("p8", new Parameter("p8", Byte.class, "0"));
        parameters.put("p9", new Parameter("p9", short.class, "0"));
        parameters.put("p10", new Parameter("p10", Short.class, "0"));
        parameters.put("p11", new Parameter("p11", long.class, "0"));
        parameters.put("p12", new Parameter("p12", Long.class, "0"));
        parameters.put("p13", new Parameter("p13", float.class, "0"));
        parameters.put("p14", new Parameter("p14", Float.class, "0"));
        parameters.put("p15", new Parameter("p15", double.class, "0"));
        parameters.put("p16", new Parameter("p16", Double.class, "0"));
        parameters.put("p17", new Parameter("p17", String.class, "null"));
        parameters.put("p18", new Parameter("p18", Object.class, "null"));
        final var defaultConstructor = Arrays.stream(WholeConstructorArgs.class.getConstructors()).filter(c -> c.getParameterCount() == 18).findFirst().orElseThrow();
        final var expected = new ConstructorArgs(defaultConstructor, parameters);
        final var actual = ReflectionHelper.getConstructorArgs(defaultConstructor);
        assertEquals(expected, actual);
    }

    @Test
    void testGetConstructorArgsNamedArgs() {
        final var parameters = new LinkedHashMap<String, Parameter>();
        parameters.put("p1", new Parameter("p1", int.class, "1"));
        parameters.put("p3", new Parameter("p3", char.class, "a"));
        parameters.put("p5", new Parameter("p5", boolean.class, "true"));
        parameters.put("p7", new Parameter("p7", byte.class, "2"));
        parameters.put("p9", new Parameter("p9", short.class, "3"));
        parameters.put("p11", new Parameter("p11", long.class, "4"));
        parameters.put("p13", new Parameter("p13", float.class, "5.5"));
        parameters.put("p15", new Parameter("p15", double.class, "6.6"));
        parameters.put("p17", new Parameter("p17", String.class, "str"));
        final var constructor = Arrays.stream(WholeConstructorArgs.class.getConstructors()).filter(c -> c.getParameterCount() == 9).findFirst().orElseThrow();
        final var expected = new ConstructorArgs(constructor, parameters);
        final var actual = ReflectionHelper.getConstructorArgs(constructor);
        assertEquals(expected, actual);
    }

    @Test
    void testGetConstructorArgsNoNamedArgs() {
        final var constructor = Arrays.stream(WholeConstructorArgs.class.getConstructors()).filter(c -> c.getParameterCount() == 2).findFirst().orElseThrow();
        final var expected = new ConstructorArgs(constructor, new LinkedHashMap<>());
        final var actual = ReflectionHelper.getConstructorArgs(constructor);
        assertEquals(expected, actual);
    }

    @Test
    void testGetConstructorArgsMixed() {
        final var constructor = Arrays.stream(WholeConstructorArgs.class.getConstructors()).filter(c -> c.getParameterCount() == 3).findFirst().orElseThrow();
        assertThrows(IllegalStateException.class, () -> ReflectionHelper.getConstructorArgs(constructor));
    }

    @Test
    void testGetDefaultPropertyClassNotFound() {
        assertThrows(GenerationException.class, () -> ReflectionHelper.getDefaultProperty("bla.bla"));
    }

    @Test
    void testGetDefaultPropertyNoDefaultProperty() throws GenerationException {
        assertNull(ReflectionHelper.getDefaultProperty("java.lang.String"));
    }

    @Test
    void testGetDefaultProperty() throws GenerationException {
        assertEquals("items", ReflectionHelper.getDefaultProperty("javafx.scene.control.ListView"));
    }

    @Test
    void testGetWrapperClass() {
        assertEquals(Integer.class.getName(), ReflectionHelper.getWrapperClass(int.class));
        assertEquals(String.class.getName(), ReflectionHelper.getWrapperClass(String.class));
        assertEquals(Object.class.getName(), ReflectionHelper.getWrapperClass(Object.class));
        assertEquals(Double.class.getName(), ReflectionHelper.getWrapperClass(double.class));
    }

    @Test
    void testGetClass() throws GenerationException {
        assertEquals(String.class, ReflectionHelper.getClass("java.lang.String"));
        assertEquals(Object.class, ReflectionHelper.getClass("java.lang.Object"));
        assertEquals(int.class, ReflectionHelper.getClass("int"));
    }

    @Test
    void testGetGenericTypesNotGeneric() throws GenerationException {
        when(parsedObject.className()).thenReturn("java.lang.String");
        assertEquals("", ReflectionHelper.getGenericTypes(progress, parsedObject));
    }

    @Test
    void testGetGenericTypesNullProperty() throws GenerationException {
        attributes.clear();
        assertEquals("", ReflectionHelper.getGenericTypes(progress, parsedObject));
    }

    @Test
    void testGetGenericTypesFieldNotFound() throws GenerationException {
        when(controllerInfo.fieldInfo("id")).thenReturn(null);
        assertEquals("", ReflectionHelper.getGenericTypes(progress, parsedObject));
    }

    @Test
    void testGetGenericTypesFieldNotGeneric() throws GenerationException {
        when(fieldInfo.isGeneric()).thenReturn(false);
        when(fieldInfo.genericTypes()).thenReturn(List.of(new GenericTypesImpl("java.lang.String", List.of()), new GenericTypesImpl("java.lang.Integer", List.of())));
        assertEquals("", ReflectionHelper.getGenericTypes(progress, parsedObject));
    }

    @Test
    void testGetGenericTypes() throws GenerationException {
        when(fieldInfo.genericTypes()).thenReturn(List.of(new GenericTypesImpl("java.lang.String", List.of()), new GenericTypesImpl("java.lang.Integer", List.of())));
        assertEquals("<java.lang.String, java.lang.Integer>", ReflectionHelper.getGenericTypes(progress, parsedObject));
    }
}
