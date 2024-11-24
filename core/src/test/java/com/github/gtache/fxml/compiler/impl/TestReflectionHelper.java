package com.github.gtache.fxml.compiler.impl;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class TestReflectionHelper {

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
}
