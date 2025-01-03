package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.GenerationException;
import ch.gtache.fxml.compiler.parsing.ParsedObject;
import ch.gtache.fxml.compiler.parsing.ParsedProperty;
import ch.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestFontFormatter {

    private final HelperProvider helperProvider;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final URLFormatter urlFormatter;
    private final StringBuilder sb;
    private final ParsedObject parsedObject;
    private final Map<String, ParsedProperty> attributes;
    private final String variableName;
    private final FontFormatter fontFormatter;

    TestFontFormatter(@Mock final HelperProvider helperProvider,
                      @Mock final GenerationCompatibilityHelper compatibilityHelper, @Mock final URLFormatter urlFormatter,
                      @Mock final ParsedObject parsedObject) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.urlFormatter = Objects.requireNonNull(urlFormatter);
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.attributes = new HashMap<>();
        this.variableName = "variable";
        this.sb = new StringBuilder();
        this.fontFormatter = new FontFormatter(helperProvider, sb);
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getURLFormatter()).thenReturn(urlFormatter);
        when(parsedObject.attributes()).thenReturn(attributes);
        when(parsedObject.children()).thenReturn(List.of());
        when(parsedObject.properties()).thenReturn(new LinkedHashMap<>());
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        when(compatibilityHelper.getStartVar(anyString(), anyInt())).then(i -> i.getArgument(0));
        when(urlFormatter.formatURL(anyString())).then(i -> i.getArgument(0) + "url");
    }

    @Test
    void testHasChildren() {
        when(parsedObject.children()).thenReturn(List.of(parsedObject));
        assertThrows(GenerationException.class, () -> fontFormatter.formatFont(parsedObject, variableName));
    }

    @Test
    void testHasProperties() {
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(new ParsedPropertyImpl("str", null, null), List.of(parsedObject));
        when(parsedObject.properties()).thenReturn(properties);
        assertThrows(GenerationException.class, () -> fontFormatter.formatFont(parsedObject, variableName));
    }

    @Test
    void testUnknownAttribute() {
        attributes.put("unknown", new ParsedPropertyImpl("unknown", null, "value"));
        assertThrows(GenerationException.class, () -> fontFormatter.formatFont(parsedObject, variableName));
    }

    @Test
    void testNoNameNorURL() {
        attributes.put("size", new ParsedPropertyImpl("size", null, "14.0"));
        attributes.put("style", new ParsedPropertyImpl("style", null, "Bold italic"));
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));
        assertThrows(GenerationException.class, () -> fontFormatter.formatFont(parsedObject, variableName));
    }

    @Test
    void testNameDefault() throws GenerationException {
        attributes.put("name", new ParsedPropertyImpl("name", null, "Arial"));
        final var expected = """
                javafx.scene.text.Fontvariable = new javafx.scene.text.Font("Arial", 12.0);
                """;
        fontFormatter.formatFont(parsedObject, variableName);
        assertEquals(expected, sb.toString());
    }

    @Test
    void testName() throws GenerationException {
        attributes.put("name", new ParsedPropertyImpl("name", null, "Arial"));
        attributes.put("size", new ParsedPropertyImpl("size", null, "14.0"));
        final var expected = """
                javafx.scene.text.Fontvariable = new javafx.scene.text.Font("Arial", 14.0);
                """;
        fontFormatter.formatFont(parsedObject, variableName);
        assertEquals(expected, sb.toString());
    }

    @Test
    void testNameWeight() throws GenerationException {
        attributes.put("name", new ParsedPropertyImpl("name", null, "Arial"));
        attributes.put("size", new ParsedPropertyImpl("size", null, "14.0"));
        attributes.put("style", new ParsedPropertyImpl("style", null, "bold"));
        final var expected = """
                javafx.scene.text.Fontvariable = new javafx.scene.text.Font("Arial", javafx.scene.text.FontWeight.BOLD, javafx.scene.text.FontPosture.REGULAR, 14.0);
                """;
        fontFormatter.formatFont(parsedObject, variableName);
        assertEquals(expected, sb.toString());
    }

    @Test
    void testNamePosture() throws GenerationException {
        attributes.put("name", new ParsedPropertyImpl("name", null, "Arial"));
        attributes.put("size", new ParsedPropertyImpl("size", null, "14.0"));
        attributes.put("style", new ParsedPropertyImpl("style", null, "italic"));
        final var expected = """
                javafx.scene.text.Fontvariable = new javafx.scene.text.Font("Arial", javafx.scene.text.FontWeight.NORMAL, javafx.scene.text.FontPosture.ITALIC, 14.0);
                """;
        fontFormatter.formatFont(parsedObject, variableName);
        assertEquals(expected, sb.toString());
    }

    @Test
    void testNameStyle() throws GenerationException {
        attributes.put("name", new ParsedPropertyImpl("name", null, "Arial"));
        attributes.put("size", new ParsedPropertyImpl("size", null, "14.0"));
        attributes.put("style", new ParsedPropertyImpl("style", null, "bold italic"));
        final var expected = """
                javafx.scene.text.Fontvariable = new javafx.scene.text.Font("Arial", javafx.scene.text.FontWeight.BOLD, javafx.scene.text.FontPosture.ITALIC, 14.0);
                """;
        fontFormatter.formatFont(parsedObject, variableName);
        assertEquals(expected, sb.toString());
    }

    @Test
    void testURL() throws GenerationException {
        attributes.put("url", new ParsedPropertyImpl("url", null, "file:/urlValue"));
        final var expected = """
                        final javafx.scene.text.Font variable;
                        try (java.io.InputStreamin = file:/urlValueurl.openStream()) {
                            variable = javafx.scene.text.Font.loadFont(in, 12.0);
                        } catch (final java.io.IOException e) {
                            throw new RuntimeException(e);
                        }
                """;
        fontFormatter.formatFont(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(urlFormatter).formatURL("file:/urlValue");
    }

    @Test
    void testURLAllAttributes() throws GenerationException {
        attributes.put("url", new ParsedPropertyImpl("url", null, "file:/urlValue"));
        attributes.put("name", new ParsedPropertyImpl("name", null, "Arial"));
        attributes.put("size", new ParsedPropertyImpl("size", null, "14.0"));
        attributes.put("style", new ParsedPropertyImpl("style", null, "bold italic"));
        final var expected = """
                        final javafx.scene.text.Font variable;
                        try (java.io.InputStreamin = file:/urlValueurl.openStream()) {
                            variable = javafx.scene.text.Font.loadFont(in, 14.0);
                        } catch (final java.io.IOException e) {
                            throw new RuntimeException(e);
                        }
                """;
        fontFormatter.formatFont(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(urlFormatter).formatURL("file:/urlValue");
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new FontFormatter(null, sb));
        assertThrows(NullPointerException.class, () -> new FontFormatter(helperProvider, null));
    }
}
