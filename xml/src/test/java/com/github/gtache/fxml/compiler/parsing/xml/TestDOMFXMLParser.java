package com.github.gtache.fxml.compiler.parsing.xml;

import com.github.gtache.fxml.compiler.parsing.ParseException;
import com.github.gtache.fxml.compiler.parsing.impl.*;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.*;

class TestDOMFXMLParser {

    private final DOMFXMLParser parser;

    TestDOMFXMLParser() {
        this.parser = new DOMFXMLParser();
    }

    @Test
    void testRealCase() throws Exception {
        final var expected = new ParsedObjectImpl(BorderPane.class.getName(),
                newLinkedHashMap("fx:controller", new ParsedPropertyImpl("fx:controller", null, "com.github.gtache.fxml.compiler.parsing.xml.LoadController")),
                newLinkedHashMap(new ParsedPropertyImpl("bottom", null, null),
                        List.of(new ParsedDefineImpl(List.of(new ParsedObjectImpl(HBox.class.getName(), Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, "define1")), newLinkedHashMap(), List.of()))),
                                new ParsedObjectImpl(VBox.class.getName(),
                                        Map.of("alignment", new ParsedPropertyImpl("alignment", BorderPane.class.getName(), "CENTER")),
                                        newLinkedHashMap(new ParsedPropertyImpl("children", null, null), List.of(
                                                new ParsedObjectImpl(Slider.class.getName(),
                                                        Map.of("hgrow", new ParsedPropertyImpl("hgrow", HBox.class.getName(), "ALWAYS"), "fx:id", new ParsedPropertyImpl("fx:id", null, "playSlider")),
                                                        newLinkedHashMap(new ParsedPropertyImpl("padding", null, null),
                                                                List.of(new ParsedObjectImpl(Insets.class.getName(),
                                                                        Map.of("left", new ParsedPropertyImpl("left", null, "$define7")),
                                                                        newLinkedHashMap(), List.of()))), List.of()),
                                                new ParsedObjectImpl(Label.class.getName(),
                                                        Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, "playLabel"), "onMouseClicked", new ParsedPropertyImpl("onMouseClicked", EventHandler.class.getName(), "#mouseClicked"), "text", new ParsedPropertyImpl("text", null, "Label")),
                                                        newLinkedHashMap(new ParsedPropertyImpl("padding", null, null),
                                                                List.of(new ParsedObjectImpl(Insets.class.getName(),
                                                                        Map.of(),
                                                                        newLinkedHashMap(new ParsedPropertyImpl("right", null, null),
                                                                                List.of(new ParsedReferenceImpl(Map.of("source", new ParsedPropertyImpl("source", null, "define7"))))), List.of()))), List.of()),
                                                new ParsedIncludeImpl(
                                                        Map.of("source", new ParsedPropertyImpl("source", null, "includedView.fxml"), "resources", new ParsedPropertyImpl("resources", null, "com/github/gtache/fxml/compiler/parsing/xml/IncludedBundle"), "fx:id", new ParsedPropertyImpl("fx:id", null, "id")))
                                        )), List.of(new ParsedDefineImpl(List.of(
                                        new ParsedConstantImpl(Cursor.class.getName(), Map.of("fx:constant", new ParsedPropertyImpl("fx:constant", null, "CLOSED_HAND"))),
                                        new ParsedObjectImpl(String.class.getName(), Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, "define2")), newLinkedHashMap(), List.of(new ParsedTextImpl("text"))),
                                        new ParsedObjectImpl(String.class.getName(), Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, "define3"), "value", new ParsedPropertyImpl("value", null, "text")), newLinkedHashMap(), List.of()),
                                        new ParsedValueImpl(String.class.getName(), Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, "define4"), "fx:value", new ParsedPropertyImpl("fx:value", null, "text"))),
                                        new ParsedObjectImpl(Integer.class.getName(), Map.of("value", new ParsedPropertyImpl("value", null, "1")), newLinkedHashMap(), List.of()),
                                        new ParsedValueImpl(Integer.class.getName(), Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, "define7"), "fx:value", new ParsedPropertyImpl("fx:value", null, "2"))),
                                        new ParsedObjectImpl(Double.class.getName(), Map.of("value", new ParsedPropertyImpl("value", null, "Infinity")), newLinkedHashMap(), List.of()),
                                        new ParsedValueImpl(Long.class.getName(), Map.of("fx:value", new ParsedPropertyImpl("fx:value", null, "3"))),
                                        new ParsedObjectImpl(Float.class.getName(), Map.of(), newLinkedHashMap(), List.of(new ParsedTextImpl("-Infinity"))),
                                        new ParsedObjectImpl(Float.class.getName(), Map.of(), newLinkedHashMap(), List.of()),
                                        new ParsedFactoryImpl(FXCollections.class.getName(), Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, "define5"), "fx:factory", new ParsedPropertyImpl("fx:factory", null, "observableArrayList")), List.of(
                                                new ParsedObjectImpl(String.class.getName(), Map.of(), newLinkedHashMap(), List.of(new ParsedTextImpl("text1"))),
                                                new ParsedValueImpl(String.class.getName(), Map.of("fx:value", new ParsedPropertyImpl("fx:value", null, "text2"))),
                                                new ParsedObjectImpl(String.class.getName(), Map.of("value", new ParsedPropertyImpl("value", null, "text3")), newLinkedHashMap(), List.of()),
                                                new ParsedCopyImpl(Map.of("source", new ParsedPropertyImpl("source", null, "define2")))
                                        ), List.of(new ParsedDefineImpl(
                                                List.of(new ParsedObjectImpl(Byte.class.getName(), Map.of(), newLinkedHashMap(), List.of(new ParsedTextImpl("3")))))
                                        )),
                                        new ParsedFactoryImpl(FXCollections.class.getName(), Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, "define6"), "fx:factory", new ParsedPropertyImpl("fx:factory", null, "emptyObservableMap")), List.of(), List.of())))))),
                        new ParsedPropertyImpl("center", null, null),
                        List.of(new ParsedObjectImpl(VBox.class.getName(), newLinkedHashMap("fx:id", new ParsedPropertyImpl("fx:id", null, "vbox"), "alignment", new ParsedPropertyImpl("alignment", null, "TOP_RIGHT")),
                                newLinkedHashMap(
                                        new ParsedPropertyImpl("accessibleText", null, null), List.of(
                                                new ParsedDefineImpl(List.of(
                                                        new ParsedObjectImpl(String.class.getName(), Map.of("value",
                                                                new ParsedPropertyImpl("value", null, "3")), newLinkedHashMap(), List.of())
                                                )), new ParsedTextImpl("text"))), List.of()))
                ), List.of());
        try (final var in = getClass().getResourceAsStream("loadView.fxml")) {
            assertNotNull(in);
            final var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            final var actual = parser.parse(content);
            assertEquals(expected, actual);
        }
    }

    @Test
    void testInvalidDefine() throws IOException {
        try (final var in = getClass().getResourceAsStream("invalidDefine.fxml")) {
            assertNotNull(in);
            final var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThrows(ParseException.class, () -> parser.parse(content));
        }
    }

    @Test
    void testInvalidFactory() throws IOException {
        try (final var in = getClass().getResourceAsStream("invalidFactory.fxml")) {
            assertNotNull(in);
            final var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThrows(ParseException.class, () -> parser.parse(content));
        }
    }

    @Test
    void testLoadRoot() throws IOException {
        try (final var in = getClass().getResourceAsStream("loadRoot.fxml")) {
            assertNotNull(in);
            final var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThrows(ParseException.class, () -> parser.parse(content));
        }
    }

    @Test
    void testLoadScript() throws IOException {
        try (final var in = getClass().getResourceAsStream("loadScript.fxml")) {
            assertNotNull(in);
            final var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThrows(ParseException.class, () -> parser.parse(content));
        }
    }

    @Test
    void testUnknownClass() throws IOException {
        try (final var in = getClass().getResourceAsStream("unknownClass.fxml")) {
            assertNotNull(in);
            final var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThrows(ParseException.class, () -> parser.parse(content));
        }
    }

    private static <K, V> SequencedMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    private static <K, V> SequencedMap<K, V> newLinkedHashMap(final K k1, final V v1) {
        final var map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        return map;
    }

    private static <K, V> SequencedMap<K, V> newLinkedHashMap(final K k1, final V v1, final K k2, final V v2) {
        final var map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
}
