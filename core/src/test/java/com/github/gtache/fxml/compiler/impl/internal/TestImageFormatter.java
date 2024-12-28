package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestImageFormatter {

    private final HelperProvider helperProvider;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final URLFormatter urlFormatter;
    private final VariableProvider variableProvider;
    private final ParsedObject parsedObject;
    private final Map<String, ParsedProperty> attributes;
    private final String variableName;
    private final StringBuilder sb;
    private final ImageFormatter imageFormatter;

    TestImageFormatter(@Mock final HelperProvider helperProvider, @Mock final GenerationCompatibilityHelper compatibilityHelper,
                       @Mock final URLFormatter urlFormatter, @Mock final VariableProvider variableProvider,
                       @Mock final ParsedObject parsedObject) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.urlFormatter = Objects.requireNonNull(urlFormatter);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.attributes = new HashMap<>();
        this.variableName = "variable";
        this.sb = new StringBuilder();
        this.imageFormatter = new ImageFormatter(helperProvider, sb, true);
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getURLFormatter()).thenReturn(urlFormatter);
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
        when(variableProvider.getNextVariableName(anyString())).then(i -> i.getArgument(0));
        when(parsedObject.children()).thenReturn(List.of());
        when(parsedObject.properties()).thenReturn(new LinkedHashMap<>());
        when(parsedObject.attributes()).thenReturn(attributes);
        when(urlFormatter.formatURL(anyString())).then(i -> i.getArgument(0) + "url");
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        when(compatibilityHelper.getStartVar(anyString(), anyInt())).then(i -> i.getArgument(0));
    }

    @Test
    void testHasChildren() {
        when(parsedObject.children()).thenReturn(List.of(parsedObject));
        assertThrows(GenerationException.class, () -> imageFormatter.formatImage(parsedObject, variableName));
    }

    @Test
    void testHasProperties() {
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(new ParsedPropertyImpl("str", null, null), List.of(parsedObject));
        when(parsedObject.properties()).thenReturn(properties);
        assertThrows(GenerationException.class, () -> imageFormatter.formatImage(parsedObject, variableName));
    }

    @Test
    void testUnknownAttribute() {
        attributes.put("unknown", new ParsedPropertyImpl("unknown", null, "value"));
        assertThrows(GenerationException.class, () -> imageFormatter.formatImage(parsedObject, variableName));
    }

    @Test
    void testMissingUrl() {
        attributes.put("requestedWidth", new ParsedPropertyImpl("requestedWidth", null, "50"));
        attributes.put("requestedHeight", new ParsedPropertyImpl("requestedHeight", null, "12.0"));
        attributes.put("preserveRatio", new ParsedPropertyImpl("preserveRatio", null, "true"));
        attributes.put("smooth", new ParsedPropertyImpl("smooth", null, "true"));
        attributes.put("backgroundLoading", new ParsedPropertyImpl("backgroundLoading", null, "true"));
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));

        assertThrows(GenerationException.class, () -> imageFormatter.formatImage(parsedObject, variableName));
    }

    @Test
    void testMinimumAttributesURL() throws GenerationException {
        final var urlImageFormatter = new ImageFormatter(helperProvider, sb, false);
        attributes.put("url", new ParsedPropertyImpl("url", null, "urlValue"));
        final var expected = """
                StringurlStr = urlValueurl.toString();
                javafx.scene.image.Imagevariable = new javafx.scene.image.Image(urlStr, 0.0, 0.0, false, false, false);
                """;
        urlImageFormatter.formatImage(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(urlFormatter).formatURL("urlValue");
    }

    @Test
    void testAllAttributesURL() throws GenerationException {
        final var urlImageFormatter = new ImageFormatter(helperProvider, sb, false);
        attributes.put("url", new ParsedPropertyImpl("url", null, "urlValue"));
        attributes.put("requestedWidth", new ParsedPropertyImpl("requestedWidth", null, "50"));
        attributes.put("requestedHeight", new ParsedPropertyImpl("requestedHeight", null, "12.0"));
        attributes.put("preserveRatio", new ParsedPropertyImpl("preserveRatio", null, "true"));
        attributes.put("smooth", new ParsedPropertyImpl("smooth", null, "true"));
        attributes.put("backgroundLoading", new ParsedPropertyImpl("backgroundLoading", null, "true"));
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));
        final var expected = """
                StringurlStr = urlValueurl.toString();
                javafx.scene.image.Imagevariable = new javafx.scene.image.Image(urlStr, 50.0, 12.0, true, true, true);
                """;
        urlImageFormatter.formatImage(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(urlFormatter).formatURL("urlValue");
    }

    @Test
    void testMinimumAttributesInputStream() throws GenerationException {
        attributes.put("url", new ParsedPropertyImpl("url", null, "urlValue"));
        final var expected = """
                        final javafx.scene.image.Image variable;
                        try (java.io.InputStreaminputStream = urlValueurl.openStream()) {
                            variable = new javafx.scene.image.Image(inputStream, 0.0, 0.0, false, false);
                        } catch (final java.io.IOException e) {
                            throw new RuntimeException(e);
                        }
                """;
        imageFormatter.formatImage(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(urlFormatter).formatURL("urlValue");
    }

    @Test
    void testAllAttributesInputStream() throws GenerationException {
        attributes.put("url", new ParsedPropertyImpl("url", null, "urlValue"));
        attributes.put("requestedWidth", new ParsedPropertyImpl("requestedWidth", null, "50"));
        attributes.put("requestedHeight", new ParsedPropertyImpl("requestedHeight", null, "12.0"));
        attributes.put("preserveRatio", new ParsedPropertyImpl("preserveRatio", null, "true"));
        attributes.put("smooth", new ParsedPropertyImpl("smooth", null, "true"));
        attributes.put("backgroundLoading", new ParsedPropertyImpl("backgroundLoading", null, "true"));
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));
        final var expected = """
                        final javafx.scene.image.Image variable;
                        try (java.io.InputStreaminputStream = urlValueurl.openStream()) {
                            variable = new javafx.scene.image.Image(inputStream, 50.0, 12.0, true, true);
                        } catch (final java.io.IOException e) {
                            throw new RuntimeException(e);
                        }
                """;
        imageFormatter.formatImage(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(urlFormatter).formatURL("urlValue");
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ImageFormatter(null, sb, true));
        assertThrows(NullPointerException.class, () -> new ImageFormatter(helperProvider, null, true));
    }
}
