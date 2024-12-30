package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.ResourceBundleInjectionType;
import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedObjectImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedTextImpl;
import javafx.event.EventHandler;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestPropertyFormatter {

    private final HelperProvider helperProvider;
    private final BindingFormatter bindingFormatter;
    private final VariableProvider variableProvider;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final ControllerInjector controllerInjector;
    private final FieldSetter fieldSetter;
    private final ValueFormatter valueFormatter;
    private final GenerationProgress progress;
    private final GenerationRequest request;
    private final GenerationParameters parameters;
    private final ParsedObject rootObject;
    private final ParsedProperty property;
    private final String variableName;
    private final StringBuilder sb;
    private final List<String> controllerFactoryPostAction;
    private final PropertyFormatter propertyFormatter;

    TestPropertyFormatter(@Mock final HelperProvider helperProvider, @Mock final BindingFormatter bindingFormatter, @Mock final VariableProvider variableProvider,
                          @Mock final GenerationCompatibilityHelper compatibilityHelper, @Mock final ControllerInjector controllerInjector,
                          @Mock final FieldSetter fieldSetter, @Mock final ValueFormatter valueFormatter, @Mock final GenerationProgress progress,
                          @Mock final GenerationRequest request, @Mock final GenerationParameters parameters,
                          @Mock final ParsedObject rootObject, @Mock final ParsedProperty property) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.bindingFormatter = Objects.requireNonNull(bindingFormatter);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.controllerInjector = Objects.requireNonNull(controllerInjector);
        this.fieldSetter = Objects.requireNonNull(fieldSetter);
        this.valueFormatter = Objects.requireNonNull(valueFormatter);
        this.progress = Objects.requireNonNull(progress);
        this.request = Objects.requireNonNull(request);
        this.parameters = Objects.requireNonNull(parameters);
        this.rootObject = Objects.requireNonNull(rootObject);
        this.property = Objects.requireNonNull(property);
        this.variableName = "variable";
        this.sb = new StringBuilder();
        this.controllerFactoryPostAction = new ArrayList<>();
        this.propertyFormatter = spy(new PropertyFormatter(helperProvider, progress));
    }

    @BeforeEach
    void beforeEach() throws GenerationException {
        when(helperProvider.getBindingFormatter()).thenReturn(bindingFormatter);
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getControllerInjector()).thenReturn(controllerInjector);
        when(helperProvider.getFieldSetter()).thenReturn(fieldSetter);
        when(helperProvider.getValueFormatter()).thenReturn(valueFormatter);
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
        when(variableProvider.getNextVariableName(anyString())).then(i -> i.getArgument(0));
        when(progress.request()).thenReturn(request);
        when(request.parameters()).thenReturn(parameters);
        when(request.rootObject()).thenReturn(rootObject);
        when(progress.stringBuilder()).thenReturn(sb);
        when(progress.controllerFactoryPostAction()).thenReturn(controllerFactoryPostAction);
        when(compatibilityHelper.getListOf()).thenReturn("listof(");
        when(valueFormatter.getArg(anyString(), any())).then(i -> i.getArgument(0) + "-" + i.getArgument(1));
        doAnswer(i -> sb.append(i.getArgument(0) + "-" + i.getArgument(2))).when(bindingFormatter).formatBinding(any(), any(), anyString());
        doAnswer(i -> sb.append(i.getArgument(0) + "-" + i.getArgument(1))).when(controllerInjector).injectEventHandlerControllerMethod(any(), anyString());
        doAnswer(i -> sb.append(i.getArgument(0) + "-" + i.getArgument(1))).when(fieldSetter).setEventHandler(any(), anyString());
    }

    @Test
    void testFormatSimpleBinding() throws GenerationException {
        when(property.name()).thenReturn("text");
        when(property.value()).thenReturn("${value}");
        propertyFormatter.formatProperty(property, rootObject, variableName);
        final var expected = property + "-" + variableName;
        assertEquals(expected, sb.toString());
        verify(bindingFormatter).formatBinding(property, rootObject, variableName);
    }

    @Test
    void testFormatBidirectionalBinding() throws GenerationException {
        when(property.name()).thenReturn("text");
        when(property.value()).thenReturn("#{value}");
        propertyFormatter.formatProperty(property, rootObject, variableName);
        final var expected = property + "-" + variableName;
        assertEquals(expected, sb.toString());
        verify(bindingFormatter).formatBinding(property, rootObject, variableName);
    }

    @Test
    void testFormatPropertyId() throws GenerationException {
        when(property.name()).thenReturn("fx:id");
        when(property.value()).thenReturn("value");
        propertyFormatter.formatProperty(property, rootObject, variableName);
        assertEquals("", sb.toString());
    }

    @Test
    void testFormatControllerSame() {
        when(property.name()).thenReturn("fx:controller");
        when(property.value()).thenReturn(variableName);
        assertDoesNotThrow(() -> propertyFormatter.formatProperty(property, rootObject, variableName));
    }

    @Test
    void testFormatControllerDifferent() {
        when(property.name()).thenReturn("fx:controller");
        when(property.value()).thenReturn("value");
        assertThrows(GenerationException.class, () -> propertyFormatter.formatProperty(property, mock(ParsedObject.class), variableName));
    }

    @Test
    void testFormatEventHandlerMethod() throws GenerationException {
        when(property.name()).thenReturn("onAction");
        when(property.sourceType()).thenReturn(EventHandler.class.getName());
        when(property.value()).thenReturn("#method");
        propertyFormatter.formatProperty(property, rootObject, variableName);
        final var expected = property + "-" + variableName;
        assertEquals(expected, sb.toString());
        verify(controllerInjector).injectEventHandlerControllerMethod(property, variableName);
    }

    @Test
    void testFormatEventHandlerField() throws GenerationException {
        when(property.name()).thenReturn("onAction");
        when(property.sourceType()).thenReturn(EventHandler.class.getName());
        when(property.value()).thenReturn("field");
        propertyFormatter.formatProperty(property, rootObject, variableName);
        final var expected = property + "-" + variableName;
        assertEquals(expected, sb.toString());
        verify(fieldSetter).setEventHandler(property, variableName);
    }

    @Test
    void testFormatStaticProperty() throws GenerationException {
        when(property.sourceType()).thenReturn(HBox.class.getName());
        when(property.name()).thenReturn("hgrow");
        when(property.value()).thenReturn("value");
        propertyFormatter.formatProperty(property, rootObject, variableName);
        final var arg = "value-" + Priority.class;
        final var expected = "        javafx.scene.layout.HBox.setHgrow(" + variableName + ", " + arg + ");\n";
        assertEquals(expected, sb.toString());
        verify(valueFormatter).getArg("value", Priority.class);
    }

    @Test
    void testFormatStaticNotFound() {
        when(property.sourceType()).thenReturn(HBox.class.getName());
        when(property.name()).thenReturn("vvvvvvv");
        when(property.value()).thenReturn("value");
        assertThrows(GenerationException.class, () -> propertyFormatter.formatProperty(property, rootObject, variableName));
    }

    @Test
    void testFormatSetProperty() throws GenerationException {
        when(rootObject.className()).thenReturn("javafx.scene.control.Label");
        when(property.name()).thenReturn("text");
        when(property.value()).thenReturn("value");
        propertyFormatter.formatProperty(property, rootObject, variableName);
        final var arg = "value-" + String.class;
        final var expected = "        " + variableName + ".setText(" + arg + ");\n";
        assertEquals(expected, sb.toString());
        verify(valueFormatter).getArg("value", String.class);
    }

    @Test
    void testFormatSetPropertyLater() throws GenerationException {
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.GETTER);
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionType.FACTORY);
        when(rootObject.className()).thenReturn("javafx.scene.control.Label");
        when(property.name()).thenReturn("text");
        when(property.value()).thenReturn("%value");
        propertyFormatter.formatProperty(property, rootObject, variableName);
        final var arg = "%value-" + String.class;
        final var expected = "        " + variableName + ".setText(" + arg + ");\n";
        assertEquals("", sb.toString());
        assertEquals(1, controllerFactoryPostAction.size());
        assertEquals(expected, controllerFactoryPostAction.getFirst());
        verify(valueFormatter).getArg("%value", String.class);
    }

    @Test
    void testFormatGetProperty() throws GenerationException {
        when(rootObject.className()).thenReturn("javafx.scene.layout.HBox");
        when(property.name()).thenReturn("children");
        when(property.value()).thenReturn("value");
        propertyFormatter.formatProperty(property, rootObject, variableName);
        final var arg = "value-" + String.class;
        final var expected = "        " + variableName + ".getChildren().addAll(listof(" + arg + "));\n";
        assertEquals(expected, sb.toString());
        verify(valueFormatter).getArg("value", String.class);
    }

    @Test
    void testFormatNoProperty() {
        when(rootObject.className()).thenReturn("javafx.scene.layout.HBox");
        when(property.name()).thenReturn("whatever");
        when(property.value()).thenReturn("value");
        assertThrows(GenerationException.class, () -> propertyFormatter.formatProperty(property, rootObject, variableName));
    }

    @Test
    void testFormatListParsedDefine() {
        final var values = List.of(mock(ParsedDefine.class));
        assertThrows(GenerationException.class, () -> propertyFormatter.formatProperty(property, values, rootObject, variableName));
    }

    @Test
    void testFormatListSingleValue() throws GenerationException {
        final var text = new ParsedTextImpl("text");
        final var values = List.of(text);
        final var emptyProperty = new ParsedPropertyImpl("name", null, null);
        final var newProperty = new ParsedPropertyImpl("name", null, text.text());
        doNothing().when(propertyFormatter).formatProperty(newProperty, rootObject, variableName);
        propertyFormatter.formatProperty(emptyProperty, values, rootObject, variableName);
        verify(propertyFormatter).formatProperty(newProperty, rootObject, variableName);
    }

    @Test
    void testFormatListSingleValueStatic() throws GenerationException {
        final var text = new ParsedTextImpl("text");
        final var values = List.of(text);
        final var emptyProperty = new ParsedPropertyImpl("name", String.class.getName(), null);
        final var newProperty = new ParsedPropertyImpl("name", String.class.getName(), text.text());
        doNothing().when(propertyFormatter).formatProperty(newProperty, rootObject, variableName);
        propertyFormatter.formatProperty(emptyProperty, values, rootObject, variableName);
        verify(propertyFormatter).formatProperty(newProperty, rootObject, variableName);
    }

    @Test
    void testFormatSingleChildSet(@Mock final ObjectFormatter objectFormatter) throws GenerationException {
        when(helperProvider.getObjectFormatter()).thenReturn(objectFormatter);
        final var className = "javafx.scene.layout.BorderPane";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(property.name()).thenReturn("center");
        final var child = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var objects = List.of(child);
        doAnswer(i -> sb.append("object")).when(objectFormatter).format(child, "label");
        propertyFormatter.formatProperty(property, objects, parsedObject, variableName);
        final var expected = "object        variable.setCenter(label);\n";
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(child, "label");
    }

    @Test
    void testFormatSingleChildGet(@Mock final ObjectFormatter objectFormatter) throws GenerationException {
        when(helperProvider.getObjectFormatter()).thenReturn(objectFormatter);
        final var className = "javafx.scene.layout.HBox";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(property.name()).thenReturn("children");
        final var child = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var objects = List.of(child);
        doAnswer(i -> sb.append("object")).when(objectFormatter).format(child, "label");
        propertyFormatter.formatProperty(property, objects, parsedObject, variableName);
        final var expected = "object        variable.getChildren().addAll(listof(label));\n";
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(child, "label");
    }

    @Test
    void testFormatSingleChildNoSetter(@Mock final ObjectFormatter objectFormatter) throws GenerationException {
        when(helperProvider.getObjectFormatter()).thenReturn(objectFormatter);
        final var className = "javafx.scene.layout.BorderPane";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(property.name()).thenReturn("abc");
        final var child = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var objects = List.of(child);
        assertThrows(GenerationException.class, () -> propertyFormatter.formatProperty(property, objects, parsedObject, variableName));
        verify(objectFormatter).format(child, "label");
    }

    @Test
    void testFormatChildrenGet(@Mock final ObjectFormatter objectFormatter) throws GenerationException {
        when(helperProvider.getObjectFormatter()).thenReturn(objectFormatter);
        final var className = "javafx.scene.layout.HBox";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(property.name()).thenReturn("children");
        final var child = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var objects = List.of(child, child);
        doAnswer(i -> sb.append("object")).when(objectFormatter).format(child, "label");
        propertyFormatter.formatProperty(property, objects, parsedObject, variableName);
        final var expected = "objectobject        variable.getChildren().addAll(listof(label, label));\n";
        assertEquals(expected, sb.toString());
        verify(objectFormatter, times(2)).format(child, "label");
    }

    @Test
    void testFormatChildrenNoGetter(@Mock final ObjectFormatter objectFormatter) throws GenerationException {
        when(helperProvider.getObjectFormatter()).thenReturn(objectFormatter);
        final var className = "javafx.scene.layout.HBox";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(property.name()).thenReturn("abc");
        final var child = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var objects = List.of(child, child);
        assertThrows(GenerationException.class, () -> propertyFormatter.formatProperty(property, objects, parsedObject, variableName));
        verify(objectFormatter, times(2)).format(child, "label");
    }

    @Test
    void testFormatSingleStaticChild(@Mock final ObjectFormatter objectFormatter) throws GenerationException {
        when(helperProvider.getObjectFormatter()).thenReturn(objectFormatter);
        final var className = "javafx.scene.layout.BorderPane";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(property.name()).thenReturn("fillHeight");
        when(property.sourceType()).thenReturn("javafx.scene.layout.GridPane");
        final var child = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var objects = List.of(child);
        doAnswer(i -> sb.append("object")).when(objectFormatter).format(child, "label");
        propertyFormatter.formatProperty(property, objects, parsedObject, variableName);
        final var expected = "object        javafx.scene.layout.GridPane.setFillHeight(variable, label);\n";
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(child, "label");
    }

    @Test
    void testFormatSingleStaticChildNoSetter(@Mock final ObjectFormatter objectFormatter) throws GenerationException {
        when(helperProvider.getObjectFormatter()).thenReturn(objectFormatter);
        final var className = "javafx.scene.layout.BorderPane";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(property.name()).thenReturn("abc");
        when(property.sourceType()).thenReturn("javafx.scene.layout.GridPane");
        final var child = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var objects = List.of(child);
        assertThrows(GenerationException.class, () -> propertyFormatter.formatProperty(property, objects, parsedObject, variableName));
        verify(objectFormatter).format(child, "label");
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new PropertyFormatter(null, progress));
        assertThrows(NullPointerException.class, () -> new PropertyFormatter(helperProvider, null));
    }
}
