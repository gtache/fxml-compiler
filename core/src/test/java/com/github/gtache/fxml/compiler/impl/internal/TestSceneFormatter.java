package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestSceneFormatter {

    private final HelperProvider helperProvider;
    private final ObjectFormatter objectFormatter;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final URLFormatter urlFormatter;
    private final VariableProvider variableProvider;
    private final StringBuilder sb;
    private final ParsedObject parsedObject;
    private final SequencedCollection<ParsedObject> children;
    private final SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties;
    private final Map<String, ParsedProperty> attributes;
    private final String variableName;
    private final SceneFormatter sceneFormatter;

    TestSceneFormatter(@Mock final HelperProvider helperProvider, @Mock final ObjectFormatter objectFormatter,
                       @Mock final GenerationCompatibilityHelper compatibilityHelper, @Mock final VariableProvider variableProvider,
                       @Mock final URLFormatter urlFormatter, @Mock final ParsedObject parsedObject) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.objectFormatter = Objects.requireNonNull(objectFormatter);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.urlFormatter = Objects.requireNonNull(urlFormatter);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.children = new ArrayList<>();
        this.properties = new LinkedHashMap<>();
        this.attributes = new HashMap<>();
        this.variableName = "variable";
        this.sb = new StringBuilder();
        this.sceneFormatter = new SceneFormatter(helperProvider, sb);
    }

    @BeforeEach
    void beforeEach() throws GenerationException {
        when(helperProvider.getObjectFormatter()).thenReturn(objectFormatter);
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getURLFormatter()).thenReturn(urlFormatter);
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
        when(parsedObject.children()).thenReturn(children);
        when(parsedObject.properties()).thenReturn(properties);
        when(parsedObject.attributes()).thenReturn(attributes);
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        when(compatibilityHelper.getListOf()).thenReturn("listof(");
        when(variableProvider.getNextVariableName(anyString())).then(i -> i.getArgument(0));
        doAnswer(i -> sb.append((String) i.getArgument(1))).when(objectFormatter).format(any(), any());
        doAnswer(i -> {
            sb.append(((List<String>) i.getArgument(0)).getFirst());
            return List.of("1", "2");
        }).when(urlFormatter).formatURL(anyIterable());
    }


    @Test
    void testUnknownAttribute() {
        properties.put(new ParsedPropertyImpl("root", null, ""), List.of(parsedObject));
        attributes.put("unknown", new ParsedPropertyImpl("unknown", null, "value"));
        assertThrows(GenerationException.class, () -> sceneFormatter.formatScene(parsedObject, variableName));
    }

    @Test
    void testNonRootProperty() {
        properties.put(new ParsedPropertyImpl("property", null, ""), List.of(parsedObject));
        assertThrows(GenerationException.class, () -> sceneFormatter.formatScene(parsedObject, variableName));
    }

    @Test
    void testRootPropertyNonEmptyChildren() {
        properties.put(new ParsedPropertyImpl("root", null, ""), List.of(parsedObject));
        children.add(parsedObject);
        assertThrows(GenerationException.class, () -> sceneFormatter.formatScene(parsedObject, variableName));
    }

    @Test
    void testRootPropertyEmptyChild() {
        properties.put(new ParsedPropertyImpl("root", null, ""), List.of());
        assertThrows(GenerationException.class, () -> sceneFormatter.formatScene(parsedObject, variableName));
    }

    @Test
    void testRootPropertyNonOneChild() {
        properties.put(new ParsedPropertyImpl("root", null, ""), List.of(parsedObject, parsedObject));
        assertThrows(GenerationException.class, () -> sceneFormatter.formatScene(parsedObject, variableName));
    }

    @Test
    void testNonRootPropertyEmptyChild() {
        assertThrows(GenerationException.class, () -> sceneFormatter.formatScene(parsedObject, variableName));
    }

    @Test
    void testNonRootPropertyMultipleChildren() {
        children.add(parsedObject);
        children.add(parsedObject);
        assertThrows(GenerationException.class, () -> sceneFormatter.formatScene(parsedObject, variableName));
    }

    @Test
    void testDefaultAttributesProperty() throws GenerationException {
        final var rootObject = mock(ParsedObject.class);
        final var define = mock(ParsedDefine.class);
        properties.put(new ParsedPropertyImpl("root", null, ""), List.of(define, rootObject));
        sceneFormatter.formatScene(parsedObject, variableName);
        final var expected = "definerootjavafx.scene.Scenevariable = new javafx.scene.Scene(root, -1.0, -1.0, javafx.scene.paint.Color.valueOf(\"0xffffffff\"));\n";
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(define, "define");
        verify(objectFormatter).format(rootObject, "root");
    }

    @Test
    void testDefaultAttributesChild() throws GenerationException {
        final var rootObject = mock(ParsedObject.class);
        final var define = mock(ParsedDefine.class);
        children.add(define);
        children.add(rootObject);
        sceneFormatter.formatScene(parsedObject, variableName);
        final var expected = "definerootjavafx.scene.Scenevariable = new javafx.scene.Scene(root, -1.0, -1.0, javafx.scene.paint.Color.valueOf(\"0xffffffff\"));\n";
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(define, "define");
        verify(objectFormatter).format(rootObject, "root");
    }

    @Test
    void testAllAttributesProperty() throws GenerationException {
        final var rootObject = mock(ParsedObject.class);
        properties.put(new ParsedPropertyImpl("root", null, ""), List.of(rootObject));
        attributes.put("width", new ParsedPropertyImpl("width", null, "100"));
        attributes.put("height", new ParsedPropertyImpl("height", null, "200"));
        attributes.put("fill", new ParsedPropertyImpl("fill", null, "#FF0000"));
        attributes.put("stylesheets", new ParsedPropertyImpl("stylesheets", null, "style.css"));
        sceneFormatter.formatScene(parsedObject, variableName);
        final var expected = "rootjavafx.scene.Scenevariable = new javafx.scene.Scene(root, 100.0, 200.0, javafx.scene.paint.Color.valueOf(\"#FF0000\"));\n" +
                "style.cssjava.util.List<String>stylesheets = variable.getStyleSheets();\n" +
                "        stylesheets.addAll(listof(1, 2));\n";
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(rootObject, "root");
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new SceneFormatter(null, sb));
        assertThrows(NullPointerException.class, () -> new SceneFormatter(helperProvider, null));
    }
}
