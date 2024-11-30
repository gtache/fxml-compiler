package com.github.gtache.fxml.compiler.parsing.xml;

import com.github.gtache.fxml.compiler.parsing.impl.ParsedIncludeImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedObjectImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                        List.of(new ParsedObjectImpl(VBox.class.getName(),
                                newLinkedHashMap("alignment", new ParsedPropertyImpl("alignment", BorderPane.class.getName(), "CENTER")),
                                newLinkedHashMap(new ParsedPropertyImpl("children", null, null), List.of(
                                        new ParsedObjectImpl(Slider.class.getName(),
                                                newLinkedHashMap("hgrow", new ParsedPropertyImpl("hgrow", HBox.class.getName(), "ALWAYS"), "fx:id", new ParsedPropertyImpl("fx:id", null, "playSlider")),
                                                newLinkedHashMap(new ParsedPropertyImpl("padding", null, null),
                                                        List.of(new ParsedObjectImpl(Insets.class.getName(),
                                                                newLinkedHashMap("left", new ParsedPropertyImpl("left", null, "10.0")),
                                                                newLinkedHashMap(), List.of()))), List.of()),
                                        new ParsedObjectImpl(Label.class.getName(),
                                                newLinkedHashMap("fx:id", new ParsedPropertyImpl("fx:id", null, "playLabel"), "onMouseClicked", new ParsedPropertyImpl("onMouseClicked", EventHandler.class.getName(), "#mouseClicked"), "text", new ParsedPropertyImpl("text", null, "Label")),
                                                newLinkedHashMap(new ParsedPropertyImpl("padding", null, null),
                                                        List.of(new ParsedObjectImpl(Insets.class.getName(),
                                                                newLinkedHashMap("right", new ParsedPropertyImpl("right", null, "10.0")),
                                                                newLinkedHashMap(), List.of()))), List.of()),
                                        new ParsedIncludeImpl(
                                                newLinkedHashMap("source", new ParsedPropertyImpl("source", null, "includedView.fxml"), "resources", new ParsedPropertyImpl("resources", null, "com/github/gtache/fxml/compiler/parsing/xml/IncludedBundle"), "fx:id", new ParsedPropertyImpl("fx:id", null, "id")))
                                )), List.of())),
                        new ParsedPropertyImpl("center", null, null),
                        List.of(new ParsedObjectImpl(VBox.class.getName(), newLinkedHashMap("fx:id", new ParsedPropertyImpl("fx:id", null, "vbox")), newLinkedHashMap(), List.of()))
                ), List.of());
        try (final var in = getClass().getResourceAsStream("loadView.fxml")) {
            final var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            final var actual = parser.parse(content);
            assertEquals(expected, actual);
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

    private static <K, V> SequencedMap<K, V> newLinkedHashMap(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3) {
        final var map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }
}
