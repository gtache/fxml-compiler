package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInfo;
import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.SourceInfo;
import com.github.gtache.fxml.compiler.compatibility.GenerationCompatibility;
import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedInclude;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.ParsedReference;
import com.github.gtache.fxml.compiler.parsing.impl.*;
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
class TestObjectFormatter {

    private final HelperProvider helperProvider;
    private final ControllerInjector controllerInjector;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final InitializationFormatter initializationFormatter;
    private final ReflectionHelper reflectionHelper;
    private final VariableProvider variableProvider;
    private final GenerationRequest request;
    private final ControllerInfo controllerInfo;
    private final SourceInfo sourceInfo;
    private final StringBuilder sb;
    private final String variableName;
    private final ObjectFormatter objectFormatter;


    TestObjectFormatter(@Mock final HelperProvider helperProvider, @Mock final GenerationCompatibilityHelper compatibilityHelper,
                        @Mock final InitializationFormatter initializationFormatter, @Mock final ReflectionHelper reflectionHelper,
                        @Mock final VariableProvider variableProvider, @Mock final GenerationRequest request,
                        @Mock final ControllerInfo controllerInfo, @Mock final ControllerInjector controllerInjector,
                        @Mock final SourceInfo sourceInfo) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.controllerInjector = Objects.requireNonNull(controllerInjector);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.initializationFormatter = Objects.requireNonNull(initializationFormatter);
        this.reflectionHelper = Objects.requireNonNull(reflectionHelper);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.request = Objects.requireNonNull(request);
        this.controllerInfo = Objects.requireNonNull(controllerInfo);
        this.sourceInfo = Objects.requireNonNull(sourceInfo);
        this.sb = new StringBuilder();
        this.variableName = "variable";
        this.objectFormatter = spy(new ObjectFormatter(helperProvider, request, sb));
    }

    @BeforeEach
    void beforeEach() throws GenerationException {
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getControllerInjector()).thenReturn(controllerInjector);
        when(helperProvider.getInitializationFormatter()).thenReturn(initializationFormatter);
        when(helperProvider.getReflectionHelper()).thenReturn(reflectionHelper);
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        when(compatibilityHelper.getStartVar(anyString(), anyInt())).then(i -> i.getArgument(0));
        when(variableProvider.getNextVariableName(anyString())).then(i -> i.getArgument(0));
        when(request.controllerInfo()).thenReturn(controllerInfo);
        when(request.sourceInfo()).thenReturn(sourceInfo);
        doAnswer(i -> {
            final var value = (ParsedInclude) i.getArgument(0);
            sb.append("include(").append(value.source()).append(", ").append(value.resources()).append(")");
            return "view";
        }).when(initializationFormatter).formatSubViewConstructorCall(any());
        doAnswer(i -> {
            final var id = (String) i.getArgument(0);
            final var variable = (String) i.getArgument(1);
            sb.append("inject(").append(id).append(", ").append(variable).append(")");
            return null;
        }).when(controllerInjector).injectControllerField(anyString(), anyString());
        when(compatibilityHelper.getStartVar(any(ParsedObject.class))).thenReturn("startVar");
    }

    @Test
    void testHandleIdNull() throws GenerationException {
        final var parsedObject = new ParsedValueImpl("className", Map.of("fx:value", new ParsedPropertyImpl("fx:value", null, "value")));
        objectFormatter.format(parsedObject, variableName);
        verifyNoInteractions(controllerInjector, reflectionHelper, variableProvider);
    }

    @Test
    void testHandleIdNotFound() throws GenerationException {
        final var className = "className";
        final var value = "id";
        final var parsedObject = new ParsedValueImpl(className, Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, value),
                "fx:value", new ParsedPropertyImpl("fx:value", null, "value")));
        objectFormatter.format(parsedObject, variableName);
        final var variableInfo = new VariableInfo(value, parsedObject, variableName, className);
        verify(variableProvider).addVariableInfo(value, variableInfo);
        verifyNoInteractions(controllerInjector, reflectionHelper);
    }

    @Test
    void testHandleIdGeneric(@Mock final ControllerFieldInfo fieldInfo) throws GenerationException {
        final var value = "id";
        final var genericClassName = "javafx.scene.control.ComboBox";
        final var parsedObject = new ParsedValueImpl(genericClassName, Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, value),
                "fx:value", new ParsedPropertyImpl("fx:value", null, "value")));
        final var genericTypes = "<java.lang.String, java.lang.Integer>";
        when(reflectionHelper.getGenericTypes(parsedObject)).thenReturn("<java.lang.String, java.lang.Integer>");
        when(controllerInfo.fieldInfo(value)).thenReturn(fieldInfo);
        objectFormatter.format(parsedObject, variableName);
        final var variableInfo = new VariableInfo(value, parsedObject, variableName, genericClassName + genericTypes);
        verify(variableProvider).addVariableInfo(value, variableInfo);
        verify(reflectionHelper).getGenericTypes(parsedObject);
        verify(controllerInfo).fieldInfo(value);
        verify(controllerInjector).injectControllerField(value, variableName);
    }

    @Test
    void testHandleId(@Mock final ControllerFieldInfo fieldInfo) throws GenerationException {
        final var className = "java.lang.String";
        final var value = "id";
        final var parsedObject = new ParsedValueImpl(className, Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, value),
                "fx:value", new ParsedPropertyImpl("fx:value", null, "value")));
        when(controllerInfo.fieldInfo(value)).thenReturn(fieldInfo);
        objectFormatter.format(parsedObject, variableName);
        final var variableInfo = new VariableInfo(value, parsedObject, variableName, className);
        verify(variableProvider).addVariableInfo(value, variableInfo);
        verify(controllerInfo).fieldInfo(value);
        verify(controllerInjector).injectControllerField(value, variableName);
        verifyNoInteractions(reflectionHelper);
    }

    @Test
    void testHandleIdPropertyTooManyChildren(@Mock final ControllerFieldInfo fieldInfo, @Mock final PropertyFormatter propertyFormatter) {
        when(helperProvider.getPropertyFormatter()).thenReturn(propertyFormatter);
        final var className = "javafx.scene.control.Label";
        final var value = "id";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(new ParsedPropertyImpl("fx:id", null, null), List.of(new ParsedTextImpl("value"), new ParsedTextImpl("value2")));
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(controllerInfo.fieldInfo(value)).thenReturn(fieldInfo);
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testHandleIdPropertyNoChildren(@Mock final ControllerFieldInfo fieldInfo, @Mock final PropertyFormatter propertyFormatter) {
        when(helperProvider.getPropertyFormatter()).thenReturn(propertyFormatter);
        final var className = "javafx.scene.control.Label";
        final var value = "id";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(new ParsedPropertyImpl("fx:id", null, null), List.of());
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(controllerInfo.fieldInfo(value)).thenReturn(fieldInfo);
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testHandleIdPropertyNotText(@Mock final ControllerFieldInfo fieldInfo, @Mock final PropertyFormatter propertyFormatter) {
        when(helperProvider.getPropertyFormatter()).thenReturn(propertyFormatter);
        final var className = "javafx.scene.control.Label";
        final var value = "id";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(new ParsedPropertyImpl("fx:id", null, null), List.of(mock(ParsedObject.class)));
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(controllerInfo.fieldInfo(value)).thenReturn(fieldInfo);
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testHandleIdProperty(@Mock final ControllerFieldInfo fieldInfo, @Mock final PropertyFormatter propertyFormatter) throws GenerationException {
        when(helperProvider.getPropertyFormatter()).thenReturn(propertyFormatter);
        final var className = "javafx.scene.control.Label";
        final var value = "id";
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        final var define = mock(ParsedDefine.class);
        properties.put(new ParsedPropertyImpl("fx:id", null, null), List.of(define, new ParsedTextImpl(value)));
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), properties, List.of());
        when(controllerInfo.fieldInfo(value)).thenReturn(fieldInfo);
        doNothing().when(objectFormatter).format(define, "define");
        objectFormatter.format(parsedObject, variableName);
        final var variableInfo = new VariableInfo(value, parsedObject, variableName, className);
        verify(variableProvider).addVariableInfo(value, variableInfo);
        verify(controllerInfo).fieldInfo(value);
        verify(controllerInjector).injectControllerField(value, variableName);
        verify(objectFormatter).format(define, "define");
    }

    @Test
    void testFormatConstant() throws GenerationException {
        final var className = "java.lang.String";
        final var attributes = new HashMap<String, ParsedProperty>();
        attributes.put("fx:constant", new ParsedPropertyImpl("fx:constant", null, "value"));
        final var constant = new ParsedConstantImpl(className, attributes);
        objectFormatter.format(constant, variableName);
        final var expected = "java.lang.String" + variableName + " = " + className + "." + constant.constant() + ";\n";
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatCopyNoInfo() {
        final var copy = new ParsedCopyImpl("source");
        assertThrows(GenerationException.class, () -> objectFormatter.format(copy, variableName));
    }

    @Test
    void testFormatCopy(@Mock final VariableInfo variableInfo) throws GenerationException {
        when(variableProvider.getVariableInfo("source")).thenReturn(variableInfo);
        final var infoVariableName = "vn";
        when(variableInfo.variableName()).thenReturn(infoVariableName);
        final var className = "className";
        when(variableInfo.className()).thenReturn(className);
        final var copy = new ParsedCopyImpl("source");
        objectFormatter.format(copy, variableName);
        final var expected = "classNamevariable = new className(vn);\n";
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatDefine(@Mock final ParsedObject inner, @Mock final ParsedReference inner2) throws GenerationException {
        final var define = new ParsedDefineImpl(List.of(inner, inner2));
        doAnswer(i -> sb.append("object")).when(objectFormatter).format(inner, "definedObject");
        doAnswer(i -> sb.append("reference")).when(objectFormatter).format(inner2, "definedObject");
        objectFormatter.format(define, variableName);
        assertEquals("objectreference", sb.toString());
        verify(objectFormatter).format(inner, "definedObject");
        verify(objectFormatter).format(inner2, "definedObject");
    }

    @Test
    void testFormatFactoryVar(@Mock final GenerationParameters parameters, @Mock final GenerationCompatibility compatibility) throws GenerationException {
        when(parameters.compatibility()).thenReturn(compatibility);
        when(request.parameters()).thenReturn(parameters);
        when(compatibility.useVar()).thenReturn(true);

        final var children = List.<ParsedObject>of(new ParsedDefineImpl(List.of()));
        final var arguments = List.of(mock(ParsedObject.class), mock(ParsedObject.class));
        for (final var c : children) {
            doAnswer(i -> {
                final var object = (ParsedObject) i.getArgument(0);
                sb.append("define");
                return object;
            }).when(objectFormatter).format(c, "parseddefine");
        }
        for (final var a : arguments) {
            doAnswer(i -> {
                final var object = (ParsedObject) i.getArgument(0);
                sb.append("argument");
                return object;
            }).when(objectFormatter).format(a, "arg");
        }
        final var factory = new ParsedFactoryImpl("javafx.collections.FXCollections",
                Map.of("fx:factory", new ParsedPropertyImpl("fx:factory", null, "observableArrayList")),
                arguments, children);
        objectFormatter.format(factory, variableName);
        assertEquals("defineargumentargumentjavafx.collections.FXCollectionsvariable = javafx.collections.FXCollections.observableArrayList(arg, arg);\n", sb.toString());
        for (final var child : children) {
            verify(objectFormatter).format(child, "parseddefine");
        }
        for (final var argument : arguments) {
            verify(objectFormatter).format(argument, "arg");
        }
    }

    @Test
    void testFormatFactory(@Mock final GenerationParameters parameters, @Mock final GenerationCompatibility compatibility) throws GenerationException {
        when(parameters.compatibility()).thenReturn(compatibility);
        when(request.parameters()).thenReturn(parameters);

        final var children = List.<ParsedObject>of(new ParsedDefineImpl(List.of()));
        for (final var c : children) {
            doAnswer(i -> {
                final var object = (ParsedObject) i.getArgument(0);
                sb.append("define");
                return object;
            }).when(objectFormatter).format(c, "parseddefine");
        }
        final var factory = new ParsedFactoryImpl("javafx.collections.FXCollections",
                Map.of("fx:factory", new ParsedPropertyImpl("fx:factory", null, "emptyObservableList")),
                List.of(), children);
        objectFormatter.format(factory, variableName);
        assertEquals("definejavafx.collections.ObservableListvariable = javafx.collections.FXCollections.emptyObservableList();\n", sb.toString());
        for (final var child : children) {
            verify(objectFormatter).format(child, "parseddefine");
        }
    }

    @Test
    void testFormatIncludeOnlySource() throws GenerationException {
        final var include = new ParsedIncludeImpl("source", null, null);
        objectFormatter.format(include, variableName);
        final var expected = "include(source, null)    final javafx.scene.Parent variable = view.load();\n";
        assertEquals(expected, sb.toString());
        verify(initializationFormatter).formatSubViewConstructorCall(include);
    }

    @Test
    void testFormatIncludeIDNotInController(@Mock final SourceInfo innerSourceInfo) throws GenerationException {
        final var controllerClassName = "controllerClassName";
        final var source = "source";
        when(innerSourceInfo.controllerClassName()).thenReturn(controllerClassName);
        final var sourceToSourceInfo = Map.of(source, innerSourceInfo);
        when(sourceInfo.sourceToSourceInfo()).thenReturn(sourceToSourceInfo);
        final var include = new ParsedIncludeImpl("source", "resources", "id");
        objectFormatter.format(include, variableName);
        final var expected = """
                include(source, resources)    final javafx.scene.Parent variable = view.load();
                controllerClassNamecontroller = view.controller();
                """;
        assertEquals(expected, sb.toString());
        verify(initializationFormatter).formatSubViewConstructorCall(include);
    }

    @Test
    void testFormatIncludeID(@Mock final ControllerFieldInfo fieldInfo, @Mock final SourceInfo innerSourceInfo) throws GenerationException {
        when(controllerInfo.fieldInfo("id")).thenReturn(fieldInfo);
        when(controllerInfo.fieldInfo("idController")).thenReturn(fieldInfo);
        final var controllerClassName = "controllerClassName";
        final var source = "source";
        when(innerSourceInfo.controllerClassName()).thenReturn(controllerClassName);
        final var sourceToSourceInfo = Map.of(source, innerSourceInfo);
        when(sourceInfo.sourceToSourceInfo()).thenReturn(sourceToSourceInfo);
        final var include = new ParsedIncludeImpl(source, "resources", "id");
        objectFormatter.format(include, variableName);
        final var expected = """
                include(source, resources)    final javafx.scene.Parent variable = view.load();
                controllerClassNamecontroller = view.controller();
                inject(idController, controller)inject(id, variable)""";
        assertEquals(expected, sb.toString());
        verify(initializationFormatter).formatSubViewConstructorCall(include);
        verify(controllerInjector).injectControllerField("id", "variable");
        verify(controllerInjector).injectControllerField("idController", "controller");
    }

    @Test
    void testFormatReferenceNullVariable() {
        final var reference = new ParsedReferenceImpl("source");
        assertThrows(GenerationException.class, () -> objectFormatter.format(reference, null));
    }

    @Test
    void testFormatReference(@Mock final VariableInfo variableInfo) throws GenerationException {
        when(variableProvider.getVariableInfo("source")).thenReturn(variableInfo);
        final var infoVariableName = "vn";
        when(variableInfo.variableName()).thenReturn(infoVariableName);
        final var className = "className";
        when(variableInfo.className()).thenReturn(className);
        final var reference = new ParsedReferenceImpl("source");
        objectFormatter.format(reference, variableName);
        final var expected = "classNamevariable = vn;\n";
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatValue() throws GenerationException {
        final var value = new ParsedValueImpl("className", "value");
        objectFormatter.format(value, variableName);
        final var expected = "classNamevariable = className.valueOf(\"value\");\n";
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatText() throws GenerationException {
        final var text = new ParsedTextImpl("text");
        objectFormatter.format(text, variableName);
        final var expected = "Stringvariable = \"text\";\n";
        assertEquals(expected, sb.toString());
        verifyNoInteractions(variableProvider);
    }

    @Test
    void testFormatScene(@Mock final SceneFormatter sceneFormatter) throws GenerationException {
        when(helperProvider.getSceneFormatter()).thenReturn(sceneFormatter);
        final var object = new ParsedObjectImpl("javafx.scene.Scene", Map.of(), new LinkedHashMap<>(), List.of());
        doAnswer(i -> sb.append("scene")).when(sceneFormatter).formatScene(object, variableName);
        objectFormatter.format(object, variableName);
        assertEquals("scene", sb.toString());
        verify(sceneFormatter).formatScene(object, variableName);
    }

    @Test
    void testFormatFont(@Mock final FontFormatter fontFormatter) throws GenerationException {
        when(helperProvider.getFontFormatter()).thenReturn(fontFormatter);
        final var object = new ParsedObjectImpl("javafx.scene.text.Font", Map.of(), new LinkedHashMap<>(), List.of());
        doAnswer(i -> sb.append("font")).when(fontFormatter).formatFont(object, variableName);
        objectFormatter.format(object, variableName);
        assertEquals("font", sb.toString());
        verify(fontFormatter).formatFont(object, variableName);
    }

    @Test
    void testFormatImage(@Mock final ImageFormatter imageFormatter) throws GenerationException {
        when(helperProvider.getImageFormatter()).thenReturn(imageFormatter);
        final var object = new ParsedObjectImpl("javafx.scene.image.Image", Map.of(), new LinkedHashMap<>(), List.of());
        doAnswer(i -> sb.append("image")).when(imageFormatter).formatImage(object, variableName);
        objectFormatter.format(object, variableName);
        assertEquals("image", sb.toString());
        verify(imageFormatter).formatImage(object, variableName);
    }

    @Test
    void testFormatTriangleMesh(@Mock final TriangleMeshFormatter triangleMeshFormatter) throws GenerationException {
        when(helperProvider.getTriangleMeshFormatter()).thenReturn(triangleMeshFormatter);
        final var object = new ParsedObjectImpl("javafx.scene.shape.TriangleMesh", Map.of(), new LinkedHashMap<>(), List.of());
        doAnswer(i -> sb.append("triangleMesh")).when(triangleMeshFormatter).formatTriangleMesh(object, variableName);
        objectFormatter.format(object, variableName);
        assertEquals("triangleMesh", sb.toString());
        verify(triangleMeshFormatter).formatTriangleMesh(object, variableName);
    }

    @Test
    void testFormatURL(@Mock final URLFormatter urlFormatter) throws GenerationException {
        when(helperProvider.getURLFormatter()).thenReturn(urlFormatter);
        final var object = new ParsedObjectImpl("java.net.URL", Map.of(), new LinkedHashMap<>(), List.of());
        doAnswer(i -> sb.append("url")).when(urlFormatter).formatURL(object, variableName);
        objectFormatter.format(object, variableName);
        assertEquals("url", sb.toString());
        verify(urlFormatter).formatURL(object, variableName);
    }

    @Test
    void testFormatWebView(@Mock final WebViewFormatter webViewFormatter) throws GenerationException {
        when(helperProvider.getWebViewFormatter()).thenReturn(webViewFormatter);
        final var object = new ParsedObjectImpl("javafx.scene.web.WebView", Map.of(), new LinkedHashMap<>(), List.of());
        doAnswer(i -> sb.append("webView")).when(webViewFormatter).formatWebView(object, variableName);
        objectFormatter.format(object, variableName);
        assertEquals("webView", sb.toString());
        verify(webViewFormatter).formatWebView(object, variableName);
    }

    @Test
    void testFormatSimpleClassProperties() {
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(new ParsedPropertyImpl("str", null, null), List.of(mock(ParsedObject.class)));
        final var parsedObject = new ParsedObjectImpl("java.lang.String", Map.of(), properties, List.of());
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatSimpleClassInvalidAttribute() {
        final var parsedObject = new ParsedObjectImpl("java.lang.Integer", Map.of("invalid", new ParsedPropertyImpl("invalid", null, "4")), new LinkedHashMap<>(), List.of());
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatSimpleClassFXValueHasChildren() {
        final var parsedObject = new ParsedObjectImpl("java.lang.Byte", Map.of("fx:value", new ParsedPropertyImpl("fx:value", null, "1")), new LinkedHashMap<>(), List.of(mock(ParsedObject.class)));
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatSimpleClassFXValueHasValue() {
        final var parsedObject = new ParsedObjectImpl("java.lang.Short", Map.of("fx:value", new ParsedPropertyImpl("fx:value", null, "2"), "value", new ParsedPropertyImpl("value", null, "value")), new LinkedHashMap<>(), List.of());
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatSimpleClassFXValue() throws GenerationException {
        final var parsedObject = new ParsedObjectImpl("java.lang.Long", Map.of("fx:value", new ParsedPropertyImpl("fx:value", null, "3")), new LinkedHashMap<>(), List.of());
        final var expected = "startVarvariable = 3;\n";
        objectFormatter.format(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(compatibilityHelper).getStartVar(parsedObject);
    }

    @Test
    void testFormatSimpleClassValueHasChildren() {
        final var parsedObject = new ParsedObjectImpl("java.lang.Float", Map.of("value", new ParsedPropertyImpl("value", null, "4")), new LinkedHashMap<>(), List.of(mock(ParsedObject.class)));
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatSimpleClassValue() throws GenerationException {
        final var parsedObject = new ParsedObjectImpl("java.lang.Double", Map.of("value", new ParsedPropertyImpl("value", null, "5")), new LinkedHashMap<>(), List.of());
        final var expected = "startVarvariable = 5;\n";
        objectFormatter.format(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(compatibilityHelper).getStartVar(parsedObject);
    }

    @Test
    void testFormatSimpleClassNoChildren() {
        final var parsedObject = new ParsedObjectImpl("javafx.geometry.Pos", Map.of(), new LinkedHashMap<>(), List.of());
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatSimpleClassChildren() {
        final var parsedObject = new ParsedObjectImpl("javafx.geometry.Pos", Map.of(), new LinkedHashMap<>(), List.of(mock(ParsedObject.class), mock(ParsedObject.class)));
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatSimpleClassChildNotText() {
        final var parsedObject = new ParsedObjectImpl("javafx.geometry.Pos", Map.of(), new LinkedHashMap<>(), List.of(mock(ParsedObject.class)));
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatSimpleClassChild() throws GenerationException {
        final var define = mock(ParsedDefine.class);
        doAnswer(i -> sb.append("define")).when(objectFormatter).format(define, "define");
        final var text = new ParsedTextImpl("TOP_LEFT");
        final var parsedObject = new ParsedObjectImpl("javafx.geometry.Pos", Map.of(), new LinkedHashMap<>(), List.of(define, text));
        final var expected = "definestartVarvariable = javafx.geometry.Pos.TOP_LEFT;\n";
        objectFormatter.format(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(define, "define");
        verify(compatibilityHelper).getStartVar(parsedObject);
    }

    @Test
    void testFormatNoConstructorNoProperties() {
        final var className = "javafx.scene.Cursor";
        final var parsedObject = new ParsedObjectImpl(className, Map.of(), new LinkedHashMap<>(), List.of());
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatNoConstructorTooManyProperties() {
        final var className = "javafx.scene.Cursor";
        final var attributes = new HashMap<String, ParsedProperty>();
        attributes.put("fx:constant", new ParsedPropertyImpl("fx:constant", null, "value"));
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));
        attributes.put("value", new ParsedPropertyImpl("value", null, "value"));
        final var parsedObject = new ParsedObjectImpl(className, attributes, new LinkedHashMap<>(), List.of());
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatNoConstructorNoFXConstant() {
        final var className = "javafx.scene.Cursor";
        final var attributes = new HashMap<String, ParsedProperty>();
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));
        final var parsedObject = new ParsedObjectImpl(className, attributes, new LinkedHashMap<>(), List.of());
        assertThrows(GenerationException.class, () -> objectFormatter.format(parsedObject, variableName));
    }

    @Test
    void testFormatNoConstructor() throws GenerationException {
        final var className = "javafx.scene.Cursor";
        final var attributes = new HashMap<String, ParsedProperty>();
        attributes.put("fx:constant", new ParsedPropertyImpl("fx:constant", null, "TEXT"));
        final var parsedObject = new ParsedObjectImpl(className, attributes, new LinkedHashMap<>(), List.of());
        final var expected = "startVarvariable = javafx.scene.Cursor.TEXT;\n";
        objectFormatter.format(parsedObject, variableName);
        assertEquals(expected, sb.toString());
        verify(compatibilityHelper).getStartVar(parsedObject);
    }

    @Test
    void testFormatConstructorNamedArgs(@Mock final PropertyFormatter propertyFormatter) throws GenerationException {
        when(helperProvider.getPropertyFormatter()).thenReturn(propertyFormatter);
        doAnswer(i -> sb.append("property")).when(propertyFormatter).formatProperty(any(ParsedProperty.class), any(), any());
        final var className = "javafx.scene.control.Spinner";
        final var attributes = Map.<String, ParsedProperty>of("min", new ParsedPropertyImpl("min", null, "1"),
                "max", new ParsedPropertyImpl("max", null, "2"), "editable", new ParsedPropertyImpl("editable", null, "false"),
                "initialValue", new ParsedPropertyImpl("initialValue", null, "3"));
        final var label = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(new ParsedPropertyImpl("childrenUnmodifiable", null, null), List.of(label));
        final var define = mock(ParsedDefine.class);
        doAnswer(i -> sb.append("define")).when(objectFormatter).format(define, "define");
        final var parsedObject = new ParsedObjectImpl(className, attributes, properties, List.of(define));
        when(reflectionHelper.getGenericTypes(parsedObject)).thenReturn("<bla>");
        objectFormatter.format(parsedObject, variableName);
        final var expected = "definestartVarvariable = new javafx.scene.control.Spinner<bla>(1, 2, 3);\nproperty";
        assertEquals(expected, sb.toString());
        verify(propertyFormatter).formatProperty(new ParsedPropertyImpl("editable", null, "false"), parsedObject, variableName);
        verify(propertyFormatter).formatProperty(new ParsedPropertyImpl("childrenUnmodifiable", null, null), List.of(label), parsedObject, variableName);
        verify(objectFormatter).format(define, "define");
        verify(compatibilityHelper).getStartVar(parsedObject);
        verify(reflectionHelper).getGenericTypes(parsedObject);
    }


    @Test
    void testFormatConstructorDefault(@Mock final PropertyFormatter propertyFormatter) throws GenerationException {
        when(helperProvider.getPropertyFormatter()).thenReturn(propertyFormatter);
        doAnswer(i -> sb.append("property")).when(propertyFormatter).formatProperty(any(ParsedProperty.class), anyList(), any(), any());
        final var className = "javafx.scene.control.Spinner";
        final var attributes = Map.<String, ParsedProperty>of();
        final var label = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var property = new ParsedPropertyImpl("childrenUnmodifiable", null, null);
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(property, List.of(label, label));
        final var parsedObject = new ParsedObjectImpl(className, attributes, properties, List.of());
        when(reflectionHelper.getGenericTypes(parsedObject)).thenReturn("");
        objectFormatter.format(parsedObject, variableName);
        final var expected = "startVarvariable = new javafx.scene.control.Spinner();\nproperty";
        assertEquals(expected, sb.toString());
        verify(propertyFormatter).formatProperty(property, List.of(label, label), parsedObject, variableName);
        verify(compatibilityHelper).getStartVar(parsedObject);
        verify(reflectionHelper).getGenericTypes(parsedObject);
    }

    @Test
    void testFormatConstructorDefaultProperty(@Mock final PropertyFormatter propertyFormatter) throws GenerationException {
        when(helperProvider.getPropertyFormatter()).thenReturn(propertyFormatter);
        doAnswer(i -> sb.append("property")).when(propertyFormatter).formatProperty(any(ParsedProperty.class), anyList(), any(), any());
        final var className = "javafx.scene.layout.StackPane";
        final var attributes = Map.<String, ParsedProperty>of();
        final var label = new ParsedObjectImpl("javafx.scene.control.Label", Map.of(), new LinkedHashMap<>(), List.of());
        final var children = List.<ParsedObject>of(label, label);
        final var parsedObject = new ParsedObjectImpl(className, attributes, new LinkedHashMap<>(), children);
        when(reflectionHelper.getGenericTypes(parsedObject)).thenReturn("");
        objectFormatter.format(parsedObject, variableName);
        final var expected = "startVarvariable = new javafx.scene.layout.StackPane();\nproperty";
        assertEquals(expected, sb.toString());
        verify(propertyFormatter).formatProperty(new ParsedPropertyImpl("children", null, null), children, parsedObject, variableName);
        verify(compatibilityHelper).getStartVar(parsedObject);
        verify(reflectionHelper).getGenericTypes(parsedObject);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ObjectFormatter(null, request, sb));
        assertThrows(NullPointerException.class, () -> new ObjectFormatter(helperProvider, null, sb));
        assertThrows(NullPointerException.class, () -> new ObjectFormatter(helperProvider, request, null));
    }
}