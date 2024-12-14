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
import java.util.Objects;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestURLFormatter {

    private final HelperProvider helperProvider;
    private final GenerationHelper generationHelper;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final ParsedObject parsedObject;
    private final String variableName;
    private final StringBuilder sb;
    private final GenerationProgress progress;
    private final URLFormatter urlFormatter;

    TestURLFormatter(@Mock final HelperProvider helperProvider, @Mock final GenerationHelper generationHelper,
                     @Mock final GenerationCompatibilityHelper compatibilityHelper, @Mock final ParsedObject parsedObject,
                     @Mock final GenerationProgress progress) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.generationHelper = Objects.requireNonNull(generationHelper);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.progress = Objects.requireNonNull(progress);
        this.sb = new StringBuilder();
        this.variableName = "variable";
        this.urlFormatter = new URLFormatter(helperProvider, progress);
    }

    @BeforeEach
    void beforeEach() throws GenerationException {
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getGenerationHelper()).thenReturn(generationHelper);
        when(progress.stringBuilder()).thenReturn(sb);
        when(progress.getNextVariableName("url")).thenReturn("url1", "url2");
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        when(parsedObject.children()).thenReturn(List.of());
        when(parsedObject.properties()).thenReturn(new LinkedHashMap<>());
        doAnswer(i -> sb.append("handleId")).when(generationHelper).handleId(any(), anyString());
    }

    @Test
    void testFormatURLSheets() {
        final var styleSheets = List.of("style1.css", "@style2.css");
        final var expected = "        final java.net.URL url1;\n" +
                "        try {\n" +
                "            url1 = new java.net.URI(\"style1.css\").toURL();\n" +
                "        } catch (final java.net.MalformedURLException | java.net.URISyntaxException e) {\n" +
                "            throw new RuntimeException(\"Couldn't parse url : style1.css\", e);\n" +
                "        }\njava.net.URLurl2 = getClass().getResource(\"style2.css\");\n";
        assertEquals(List.of("url1", "url2"), urlFormatter.formatURL(styleSheets));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatURLObjectChildren() {
        when(parsedObject.children()).thenReturn(List.of(parsedObject));
        assertThrows(GenerationException.class, () -> urlFormatter.formatURL(parsedObject, variableName));
    }

    @Test
    void testFormatURLObjectProperties() {
        final var map = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        map.put(mock(ParsedProperty.class), List.of());
        when(parsedObject.properties()).thenReturn(map);
        assertThrows(GenerationException.class, () -> urlFormatter.formatURL(parsedObject, variableName));
    }

    @Test
    void testFormatURLObjectUnknownAttribute() {
        final var attributes = new HashMap<String, ParsedProperty>();
        attributes.put("unknown", new ParsedPropertyImpl("unknown", null, "value"));
        when(parsedObject.attributes()).thenReturn(attributes);
        assertThrows(GenerationException.class, () -> urlFormatter.formatURL(parsedObject, variableName));
    }

    @Test
    void testFormatURLObject() throws GenerationException {
        final var attributes = new HashMap<String, ParsedProperty>();
        attributes.put("value", new ParsedPropertyImpl("value", null, "key"));
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));
        when(parsedObject.attributes()).thenReturn(attributes);

        urlFormatter.formatURL(parsedObject, variableName);
        final var expected = "java.net.URL" + variableName + " = getClass().getResource(\"key\");\nhandleId";
        assertEquals(expected, sb.toString());
        verify(generationHelper).handleId(parsedObject, variableName);
    }
}
