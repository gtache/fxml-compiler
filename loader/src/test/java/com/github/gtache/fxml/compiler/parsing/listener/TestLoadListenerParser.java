package com.github.gtache.fxml.compiler.parsing.listener;

import com.github.gtache.fxml.compiler.parsing.impl.ParsedIncludeImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedObjectImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class TestLoadListenerParser {

    private final LoadListenerParser listener;

    TestLoadListenerParser() {
        this.listener = new LoadListenerParser();
    }

    @BeforeAll
    static void beforeAll() {
        try {
            Platform.startup(() -> {

            });
        } catch (final IllegalStateException ignored) {

        }
    }

    @Test
    void testRealCase() {
        final var expected = new ParsedObjectImpl(BorderPane.class.getName(),
                newLinkedHashMap("fx:controller", new ParsedPropertyImpl("fx:controller", null, "com.github.gtache.fxml.compiler.parsing.listener.LoadController")),
                newLinkedHashMap(new ParsedPropertyImpl("bottom", null, null),
                        List.of(new ParsedObjectImpl(VBox.class.getName(),
                                newLinkedHashMap("alignment", new ParsedPropertyImpl("alignment", BorderPane.class.getName(), "CENTER")),
                                newLinkedHashMap(new ParsedPropertyImpl("children", null, null), List.of(
                                        new ParsedObjectImpl(Slider.class.getName(),
                                                newLinkedHashMap("fx:id", new ParsedPropertyImpl("fx:id", null, "playSlider"), "hgrow", new ParsedPropertyImpl("hgrow", HBox.class.getName(), "ALWAYS")),
                                                newLinkedHashMap(new ParsedPropertyImpl("padding", null, null),
                                                        List.of(new ParsedObjectImpl(Insets.class.getName(),
                                                                newLinkedHashMap("left", new ParsedPropertyImpl("left", null, "10.0")),
                                                                newLinkedHashMap())))),
                                        new ParsedObjectImpl(Label.class.getName(),
                                                newLinkedHashMap("fx:id", new ParsedPropertyImpl("fx:id", null, "playLabel"), "text", new ParsedPropertyImpl("text", null, "Label"), "onMouseClicked", new ParsedPropertyImpl("onMouseClicked", EventHandler.class.getName(), "#mouseClicked")),
                                                newLinkedHashMap(new ParsedPropertyImpl("padding", null, null),
                                                        List.of(new ParsedObjectImpl(Insets.class.getName(),
                                                                newLinkedHashMap("right", new ParsedPropertyImpl("right", null, "10.0")),
                                                                newLinkedHashMap())))),
                                        new ParsedIncludeImpl(
                                                newLinkedHashMap("source", new ParsedPropertyImpl("source", null, "includedView.fxml"), "resources", new ParsedPropertyImpl("resources", null, "com/github/gtache/fxml/compiler/parsing/listener/IncludedBundle"), "fx:id", new ParsedPropertyImpl("fx:id", null, "id")))
                                )))),
                        new ParsedPropertyImpl("center", null, null),
                        List.of(new ParsedObjectImpl(VBox.class.getName(), newLinkedHashMap("fx:id", new ParsedPropertyImpl("fx:id", null, "vbox")), newLinkedHashMap()))
                ));
        final var actual = CompletableFuture.supplyAsync(() -> {
            final var loader = new FXMLLoader(TestLoadListenerParser.class.getResource("loadView.fxml"));
            loader.setLoadListener(listener);
            try {
                loader.load();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return listener.root();
        }, Platform::runLater).join();
        assertEquals(expected, actual);
    }

    private static <K, V> SequencedMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<K, V>();
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

    @Test
    void testRootNullObjects() {
        assertThrows(IllegalStateException.class, listener::root);
    }

    @Test
    void testRootOneObject() {
        listener.beginInstanceDeclarationElement(String.class);
        listener.endElement("");
        final var expected = new ParsedObjectImpl(String.class.getName(), new LinkedHashMap<>(), new LinkedHashMap<>());
        assertEquals(expected, listener.root());
    }

    @Test
    void testRootTwoObjects() {
        listener.beginInstanceDeclarationElement(String.class);
        listener.endElement("");
        listener.beginInstanceDeclarationElement(String.class);
        listener.endElement("");
        assertThrows(IllegalStateException.class, listener::root);
    }

    @Test
    void testReadImportProcessingInstruction() {
        listener.beginInstanceDeclarationElement(String.class);
        listener.beginPropertyElement("name", null);
        listener.beginInstanceDeclarationElement(String.class);
        final var element = "element";
        listener.endElement(element);
        listener.readImportProcessingInstruction("test");
        listener.endElement(element);
        assertThrows(IllegalStateException.class, () -> listener.endElement("other"));
    }

    @Test
    void testReadLanguageProcessingInstruction() {
        listener.beginInstanceDeclarationElement(String.class);
        listener.beginPropertyElement("name", null);
        listener.beginInstanceDeclarationElement(String.class);
        final var element = "element";
        listener.endElement(element);
        listener.readLanguageProcessingInstruction("test");
        listener.endElement(element);
        assertThrows(IllegalStateException.class, () -> listener.endElement("other"));
    }

    @Test
    void testReadComment() {
        listener.beginInstanceDeclarationElement(String.class);
        listener.beginPropertyElement("name", null);
        listener.beginInstanceDeclarationElement(String.class);
        final var element = "element";
        listener.endElement(element);
        listener.readComment("test");
        listener.endElement(element);
        assertDoesNotThrow(() -> listener.endElement("other"));
        assertDoesNotThrow(listener::root);
    }

    @Test
    void testBeginUnknownTypeElement() {
        assertThrows(IllegalArgumentException.class, () -> listener.beginUnknownTypeElement(""));
    }

    @Test
    void testInclude() {
        listener.beginInstanceDeclarationElement(Object.class);
        listener.beginIncludeElement();
        listener.readInternalAttribute("source", "s");
        listener.readInternalAttribute("p", "v");
        listener.endElement("String");
        final var root = listener.root();
        final var expected = new ParsedIncludeImpl(new LinkedHashMap<>(Map.of("source",
                new ParsedPropertyImpl("source", null, "s"), "p", new ParsedPropertyImpl("p", null, "v"))));
        assertEquals(expected, root);
    }

    @Test
    void testBeginNestedIncludeElement() {
        listener.beginIncludeElement();
        assertThrows(IllegalStateException.class, listener::beginIncludeElement);
    }

    @Test
    void testBeginReferenceElement() {
        assertThrows(UnsupportedOperationException.class, listener::beginReferenceElement);
    }

    @Test
    void testBeginCopyElement() {
        assertThrows(UnsupportedOperationException.class, listener::beginCopyElement);
    }

    @Test
    void testBeginRootElement() {
        assertThrows(UnsupportedOperationException.class, listener::beginRootElement);
    }

    @Test
    void testBeginPropertyElementInclude() {
        listener.beginIncludeElement();
        assertThrows(IllegalStateException.class, () -> listener.beginPropertyElement("", null));
    }

    @Test
    void testBeginUnknownStaticPropertyElement() {
        assertThrows(IllegalArgumentException.class, () -> listener.beginUnknownStaticPropertyElement(""));
    }

    @Test
    void testBeginScriptElement() {
        assertThrows(UnsupportedOperationException.class, listener::beginScriptElement);
    }

    @Test
    void testBeginDefineElement() {
        assertThrows(UnsupportedOperationException.class, listener::beginDefineElement);
    }

    @Test
    void testReadInternalAttributeNull() {
        assertThrows(IllegalStateException.class, () -> listener.readInternalAttribute("name", "value"));
    }

    @Test
    void testReadPropertyAttributeNull() {
        assertThrows(IllegalStateException.class, () -> listener.readPropertyAttribute("name", null, "value"));
    }

    @Test
    void testReadPropertyAttributeIsInclude() {
        listener.beginIncludeElement();
        assertThrows(IllegalStateException.class, () -> listener.readPropertyAttribute("name", null, "value"));
    }

    @Test
    void testReadUnknownStaticPropertyAttribute() {
        assertThrows(IllegalArgumentException.class, () -> listener.readUnknownStaticPropertyAttribute("", ""));
    }

    @Test
    void testReadEventHandlerAttributeNull() {
        assertThrows(IllegalStateException.class, () -> listener.readEventHandlerAttribute("name", "value"));
    }

    @Test
    void testReadEventHandlerAttributeInclude() {
        listener.beginIncludeElement();
        assertThrows(IllegalStateException.class, () -> listener.readEventHandlerAttribute("name", "value"));
    }

    @Test
    void testEndPropertyNull() {
        listener.beginInstanceDeclarationElement(String.class);
        listener.beginInstanceDeclarationElement(String.class);
        listener.endElement("");
        assertThrows(IllegalStateException.class, () -> listener.endElement(""));
    }

    @Test
    void testEndElementNull() {
        assertThrows(IllegalStateException.class, () -> listener.endElement(""));
    }
}
