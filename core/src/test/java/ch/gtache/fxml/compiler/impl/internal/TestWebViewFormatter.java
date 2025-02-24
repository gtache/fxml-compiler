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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestWebViewFormatter {

    private final HelperProvider helperProvider;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final ControllerInjector controllerInjector;
    private final FieldSetter fieldSetter;
    private final PropertyFormatter propertyFormatter;
    private final VariableProvider variableProvider;
    private final ParsedObject parsedObject;
    private final String variableName;
    private final String engineVariable;
    private final StringBuilder sb;
    private final Map<String, ParsedProperty> attributes;
    private final WebViewFormatter webViewFormatter;

    TestWebViewFormatter(@Mock final HelperProvider helperProvider, @Mock final GenerationCompatibilityHelper compatibilityHelper,
                         @Mock final ControllerInjector controllerInjector, @Mock final FieldSetter fieldSetter,
                         @Mock final PropertyFormatter propertyFormatter, @Mock final VariableProvider variableProvider,
                         @Mock final ParsedObject parsedObject) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.controllerInjector = Objects.requireNonNull(controllerInjector);
        this.fieldSetter = Objects.requireNonNull(fieldSetter);
        this.propertyFormatter = Objects.requireNonNull(propertyFormatter);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.variableName = "variable";
        this.engineVariable = "engine";
        this.sb = new StringBuilder();
        this.attributes = new HashMap<>();
        this.webViewFormatter = new WebViewFormatter(helperProvider, sb);
    }

    @BeforeEach
    void beforeEach() throws GenerationException {
        when(parsedObject.children()).thenReturn(List.of());
        when(parsedObject.properties()).thenReturn(new LinkedHashMap<>());
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getControllerInjector()).thenReturn(controllerInjector);
        when(helperProvider.getFieldSetter()).thenReturn(fieldSetter);
        when(helperProvider.getPropertyFormatter()).thenReturn(propertyFormatter);
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        doAnswer(i -> sb.append(((ParsedProperty) i.getArgument(0)).value()).append((String) i.getArgument(2))).when(controllerInjector).injectCallbackControllerMethod(any(), eq(engineVariable), any());
        doAnswer(i -> sb.append(((ParsedProperty) i.getArgument(0)).value())).when(controllerInjector).injectEventHandlerControllerMethod(any(), eq(engineVariable));
        doAnswer(i -> sb.append(((ParsedProperty) i.getArgument(0)).value())).when(fieldSetter).setEventHandler(any(), eq(engineVariable));
        doAnswer(i -> sb.append(((ParsedProperty) i.getArgument(0)).value()).append((String) i.getArgument(2))).when(fieldSetter).setField(any(), eq(engineVariable), any());
        doAnswer(i -> sb.append(((ParsedProperty) i.getArgument(0)).value())).when(propertyFormatter).formatProperty(any(), any(), any());
        when(parsedObject.attributes()).thenReturn(attributes);

        when(variableProvider.getNextVariableName("engine")).thenReturn(engineVariable);
    }

    @Test
    void testFormatHasChildren() {
        when(parsedObject.children()).thenReturn(List.of(parsedObject));
        assertThrows(GenerationException.class, () -> webViewFormatter.formatWebView(parsedObject, variableName));
    }

    @Test
    void testFormatHasProperty() {
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(new ParsedPropertyImpl("str", null, null), List.of(parsedObject));
        when(parsedObject.properties()).thenReturn(properties);
        assertThrows(GenerationException.class, () -> webViewFormatter.formatWebView(parsedObject, variableName));
    }

    @Test
    void testFormatAllMethods() throws GenerationException {
        attributes.put("confirmHandler", new ParsedPropertyImpl("confirmHandler", null, "#confirmHandler"));
        attributes.put("createPopupHandler", new ParsedPropertyImpl("createPopupHandler", null, "#createPopupHandler"));
        attributes.put("onAlert", new ParsedPropertyImpl("onAlert", null, "#onAlert"));
        attributes.put("onResized", new ParsedPropertyImpl("onResized", null, "#onResized"));
        attributes.put("onStatusChanged", new ParsedPropertyImpl("onStatusChanged", null, "#onStatusChanged"));
        attributes.put("onVisibilityChanged", new ParsedPropertyImpl("onVisibilityChanged", null, "#onVisibilityChanged"));
        attributes.put("promptHandler", new ParsedPropertyImpl("promptHandler", null, "#promptHandler"));
        attributes.put("location", new ParsedPropertyImpl("location", null, "location"));
        attributes.put("property", new ParsedPropertyImpl("property", null, "property"));
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));

        webViewFormatter.formatWebView(parsedObject, variableName);
        final var expected = """
                javafx.scene.web.WebViewvariable = new javafx.scene.web.WebView();
                javafx.scene.web.WebEngineengine = variable.getEngine();
                #confirmHandlerString.class#createPopupHandlerjavafx.scene.web.PopupFeatures.class\
                        engine.load("location");
                #onAlert#onResized#onStatusChanged#onVisibilityChanged#promptHandlerjavafx.scene.web.PromptData.class\
                property""";
        assertEquals(expected, sb.toString());
        verify(propertyFormatter).formatProperty(attributes.get("property"), parsedObject, variableName);
        verify(controllerInjector).injectCallbackControllerMethod(attributes.get("confirmHandler"), engineVariable, "String.class");
        verify(controllerInjector).injectCallbackControllerMethod(attributes.get("createPopupHandler"), engineVariable, "javafx.scene.web.PopupFeatures.class");
        verify(controllerInjector).injectEventHandlerControllerMethod(attributes.get("onAlert"), engineVariable);
        verify(controllerInjector).injectEventHandlerControllerMethod(attributes.get("onResized"), engineVariable);
        verify(controllerInjector).injectEventHandlerControllerMethod(attributes.get("onStatusChanged"), engineVariable);
        verify(controllerInjector).injectEventHandlerControllerMethod(attributes.get("onVisibilityChanged"), engineVariable);
        verify(controllerInjector).injectCallbackControllerMethod(attributes.get("promptHandler"), engineVariable, "javafx.scene.web.PromptData.class");
    }

    @Test
    void testFormatAllVariables() throws GenerationException {
        attributes.put("confirmHandler", new ParsedPropertyImpl("confirmHandler", null, "$controller.confirmHandler"));
        attributes.put("createPopupHandler", new ParsedPropertyImpl("createPopupHandler", null, "$controller.createPopupHandler"));
        attributes.put("onAlert", new ParsedPropertyImpl("onAlert", null, "$controller.onAlert"));
        attributes.put("onResized", new ParsedPropertyImpl("onResized", null, "$controller.onResized"));
        attributes.put("onStatusChanged", new ParsedPropertyImpl("onStatusChanged", null, "$controller.onStatusChanged"));
        attributes.put("onVisibilityChanged", new ParsedPropertyImpl("onVisibilityChanged", null, "$controller.onVisibilityChanged"));
        attributes.put("promptHandler", new ParsedPropertyImpl("promptHandler", null, "$controller.promptHandler"));
        attributes.put("location", new ParsedPropertyImpl("location", null, "location"));
        attributes.put("property", new ParsedPropertyImpl("property", null, "property"));
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));

        webViewFormatter.formatWebView(parsedObject, variableName);
        final var expected = """
                javafx.scene.web.WebViewvariable = new javafx.scene.web.WebView();
                javafx.scene.web.WebEngineengine = variable.getEngine();
                $controller.confirmHandlerjavafx.util.Callback$controller.createPopupHandlerjavafx.util.Callback\
                        engine.load("location");
                $controller.onAlert$controller.onResized$controller.onStatusChanged$controller.onVisibilityChanged\
                $controller.promptHandlerjavafx.util.Callback\
                property""";
        assertEquals(expected, sb.toString());

        verify(fieldSetter).setEventHandler(attributes.get("onAlert"), engineVariable);
        verify(fieldSetter).setEventHandler(attributes.get("onResized"), engineVariable);
        verify(fieldSetter).setEventHandler(attributes.get("onStatusChanged"), engineVariable);
        verify(fieldSetter).setEventHandler(attributes.get("onVisibilityChanged"), engineVariable);
        verify(propertyFormatter).formatProperty(attributes.get("property"), parsedObject, variableName);
        verify(fieldSetter).setField(attributes.get("confirmHandler"), engineVariable, "javafx.util.Callback");
        verify(fieldSetter).setField(attributes.get("createPopupHandler"), engineVariable, "javafx.util.Callback");
        verify(fieldSetter).setField(attributes.get("promptHandler"), engineVariable, "javafx.util.Callback");
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new WebViewFormatter(null, sb));
        assertThrows(NullPointerException.class, () -> new WebViewFormatter(helperProvider, null));
    }
}
