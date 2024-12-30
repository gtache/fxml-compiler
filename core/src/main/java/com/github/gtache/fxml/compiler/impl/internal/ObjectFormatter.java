package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedConstant;
import com.github.gtache.fxml.compiler.parsing.ParsedCopy;
import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedFactory;
import com.github.gtache.fxml.compiler.parsing.ParsedInclude;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedReference;
import com.github.gtache.fxml.compiler.parsing.ParsedText;
import com.github.gtache.fxml.compiler.parsing.ParsedValue;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SequencedCollection;
import java.util.Set;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;
import static java.util.Objects.requireNonNull;

/**
 * Helper methods for {@link GeneratorImpl} to format properties
 */
final class ObjectFormatter {

    private static final Logger logger = LogManager.getLogger(ObjectFormatter.class);

    private static final String NEW_ASSIGN = " = new ";

    private static final Set<String> BUILDER_CLASSES = Set.of(
            "javafx.scene.Scene",
            "javafx.scene.text.Font",
            "javafx.scene.image.Image",
            "java.net.URL",
            "javafx.scene.shape.TriangleMesh",
            "javafx.scene.web.WebView"
    );

    private static final Set<String> SIMPLE_CLASSES = Set.of(
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double"
    );

    private final HelperProvider helperProvider;
    private final GenerationRequest request;
    private final StringBuilder sb;

    ObjectFormatter(final HelperProvider helperProvider, final GenerationRequest request, final StringBuilder sb) {
        this.helperProvider = requireNonNull(helperProvider);
        this.request = requireNonNull(request);
        this.sb = requireNonNull(sb);
    }

    /**
     * Formats an object
     *
     * @param parsedObject The parsed object to format
     * @param variableName The variable name for the object
     * @throws GenerationException if an error occurs
     */
    public void format(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        switch (parsedObject) {
            case final ParsedConstant constant -> formatConstant(constant, variableName);
            case final ParsedCopy copy -> formatCopy(copy, variableName);
            case final ParsedDefine define -> formatDefine(define);
            case final ParsedFactory factory -> formatFactory(factory, variableName);
            case final ParsedInclude include -> formatInclude(include, variableName);
            case final ParsedReference reference -> formatReference(reference, variableName);
            case final ParsedValue value -> formatValue(value, variableName);
            case final ParsedText text -> formatText(text, variableName);
            default -> formatObject(parsedObject, variableName);
        }
        handleId(parsedObject, variableName);
    }

    /**
     * Handles the fx:id attribute of an object
     *
     * @param parsedObject The parsed object
     * @param variableName The variable name
     * @throws GenerationException if an error occurs
     */
    private void handleId(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        final var id = parsedObject.attributes().get(FX_ID);
        if (id == null) {
            handleIdProperty(parsedObject, variableName);
        } else {
            final var idValue = id.value();
            handleId(parsedObject, variableName, idValue);
        }
    }

    private void handleIdProperty(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        final var property = parsedObject.properties().entrySet().stream().filter(
                e -> e.getKey().name().equals(FX_ID) && e.getKey().sourceType() == null).findFirst().orElse(null);
        if (property != null) {
            final var values = property.getValue();
            formatDefines(values);
            final var notDefinedChildren = getNotDefines(values);
            if (notDefinedChildren.size() == 1) {
                final var object = notDefinedChildren.getFirst();
                if (object instanceof final ParsedText text) {
                    final var idValue = text.text();
                    handleId(parsedObject, variableName, idValue);
                } else {
                    throw new GenerationException("Malformed fx:id property : " + parsedObject);
                }
            } else {
                throw new GenerationException("Malformed fx:id property : " + parsedObject);
            }
        }
    }

    private static SequencedCollection<ParsedDefine> getDefines(final SequencedCollection<ParsedObject> objects) {
        return objects.stream().filter(ParsedDefine.class::isInstance).map(ParsedDefine.class::cast).toList();
    }

    private static SequencedCollection<ParsedObject> getNotDefines(final SequencedCollection<ParsedObject> objects) {
        return objects.stream().filter(c -> !(c instanceof ParsedDefine)).toList();
    }

    private void handleId(final ParsedObject parsedObject, final String variableName, final String value) throws GenerationException {
        final String className;
        if (request.controllerInfo().fieldInfo(value) == null) {
            className = parsedObject.className();
            logger.debug("Not injecting {} because it is not found in controller", value);
        } else {
            final var reflectionHelper = helperProvider.getReflectionHelper();
            if (ReflectionHelper.isGeneric(ReflectionHelper.getClass(parsedObject.className()))) {
                className = parsedObject.className() + reflectionHelper.getGenericTypes(parsedObject);
            } else {
                className = parsedObject.className();
            }
            helperProvider.getControllerInjector().injectControllerField(value, variableName);
        }
        helperProvider.getVariableProvider().addVariableInfo(value, new VariableInfo(value, parsedObject, variableName, className));
    }

    /**
     * Formats a simple text
     *
     * @param text         The parsed text
     * @param variableName The variable name
     */
    private void formatText(final ParsedText text, final String variableName) {
        sb.append(helperProvider.getCompatibilityHelper().getStartVar("String"))
                .append(variableName).append(" = \"").append(text.text()).append("\";\n");
    }

    /**
     * Formats a basic object
     *
     * @param parsedObject The parsed object to format
     * @param variableName The variable name for the object
     */
    private void formatObject(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (BUILDER_CLASSES.contains(parsedObject.className())) {
            formatBuilderObject(parsedObject, variableName);
        } else {
            formatNotBuilder(parsedObject, variableName);
        }
    }

    /**
     * Formats a builder object
     *
     * @param parsedObject The parsed object
     * @param variableName The variable name
     */
    private void formatBuilderObject(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        final var className = parsedObject.className();
        switch (className) {
            case "javafx.scene.Scene" -> helperProvider.getSceneFormatter().formatScene(parsedObject, variableName);
            case "javafx.scene.text.Font" -> helperProvider.getFontFormatter().formatFont(parsedObject, variableName);
            case "javafx.scene.image.Image" ->
                    helperProvider.getImageFormatter().formatImage(parsedObject, variableName);
            case "javafx.scene.shape.TriangleMesh" ->
                    helperProvider.getTriangleMeshFormatter().formatTriangleMesh(parsedObject, variableName);
            case "java.net.URL" -> helperProvider.getURLFormatter().formatURL(parsedObject, variableName);
            case "javafx.scene.web.WebView" ->
                    helperProvider.getWebViewFormatter().formatWebView(parsedObject, variableName);
            default -> throw new IllegalArgumentException("Unknown builder class : " + className);
        }
    }

    private void formatNotBuilder(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (isSimpleClass(parsedObject)) {
            formatSimpleClass(parsedObject, variableName);
        } else {
            formatComplexClass(parsedObject, variableName);
        }
    }

    private void formatSimpleClass(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (!parsedObject.properties().isEmpty()) {
            throw new GenerationException("Simple class cannot have properties : " + parsedObject);
        }
        if (parsedObject.attributes().keySet().stream().anyMatch(k -> !k.equals(FX_ID) && !k.equals(VALUE) && !k.equals(FX_VALUE))) {
            throw new GenerationException("Invalid attributes for simple class : " + parsedObject);
        }
        final var value = getSimpleValue(parsedObject);
        final var valueStr = ValueFormatter.toString(value, ReflectionHelper.getClass(parsedObject.className()));
        sb.append(helperProvider.getCompatibilityHelper().getStartVar(parsedObject)).append(variableName).append(" = ").append(valueStr).append(";\n");
    }

    private String getSimpleValue(final ParsedObject parsedObject) throws GenerationException {
        formatDefines(parsedObject.children());
        final var notDefinedChildren = getNotDefines(parsedObject.children());
        if (parsedObject.attributes().containsKey(FX_VALUE)) {
            return getSimpleFXValue(parsedObject, notDefinedChildren);
        } else if (parsedObject.attributes().containsKey(VALUE)) {
            return getSimpleValue(parsedObject, notDefinedChildren);
        } else {
            return getSimpleChild(parsedObject, notDefinedChildren);
        }
    }

    private void formatDefines(final SequencedCollection<ParsedObject> values) throws GenerationException {
        final var defines = getDefines(values);
        for (final var definedChild : defines) {
            format(definedChild, helperProvider.getVariableProvider().getNextVariableName("define"));
        }
    }

    private static String getSimpleFXValue(final ParsedObject parsedObject, final Collection<ParsedObject> notDefinedChildren) throws GenerationException {
        if (notDefinedChildren.isEmpty() && !parsedObject.attributes().containsKey(VALUE)) {
            return parsedObject.attributes().get(FX_VALUE).value();
        } else {
            throw new GenerationException("Malformed simple class : " + parsedObject);
        }
    }

    private static String getSimpleValue(final ParsedObject parsedObject, final Collection<ParsedObject> notDefinedChildren) throws GenerationException {
        if (notDefinedChildren.isEmpty()) {
            return parsedObject.attributes().get(VALUE).value();
        } else {
            throw new GenerationException("Malformed simple class : " + parsedObject);
        }
    }

    private static String getSimpleChild(final ParsedObject parsedObject, final SequencedCollection<ParsedObject> notDefinedChildren) throws GenerationException {
        if (notDefinedChildren.size() == 1) {
            final var child = notDefinedChildren.getFirst();
            if (child instanceof final ParsedText text) {
                return text.text();
            } else {
                throw new GenerationException("Invalid value for : " + parsedObject);
            }
        } else {
            throw new GenerationException("Value not found for : " + parsedObject);
        }
    }

    private static boolean isSimpleClass(final ParsedObject object) throws GenerationException {
        final var className = object.className();
        if (SIMPLE_CLASSES.contains(className)) {
            return true;
        } else {
            final var clazz = ReflectionHelper.getClass(className);
            return clazz.isEnum();
        }
    }

    private void formatComplexClass(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        final var clazz = ReflectionHelper.getClass(parsedObject.className());
        final var children = parsedObject.children();
        final var notDefinedChildren = getNotDefines(children);
        final var allProperties = new HashMap<String, List<Class<?>>>();
        for (final var entry : parsedObject.attributes().entrySet()) {
            final var possibleTypes = helperProvider.getValueClassGuesser().guess(entry.getValue().value());
            allProperties.put(entry.getKey(), possibleTypes);
        }
        for (final var entry : parsedObject.properties().entrySet()) {
            allProperties.put(entry.getKey().name(), List.of(Node.class, Node[].class));
        }
        formatDefines(children);
        if (!notDefinedChildren.isEmpty()) {
            final var defaultProperty = ReflectionHelper.getDefaultProperty(parsedObject.className());
            if (defaultProperty != null) {
                allProperties.put(defaultProperty, List.of(Node.class, Node[].class));
            }
        }
        final var constructors = clazz.getConstructors();
        final var constructorArgs = ConstructorHelper.getMatchingConstructorArgs(constructors, allProperties);
        if (constructorArgs == null) {
            logger.debug("No constructor found for {} with attributes {}", clazz.getCanonicalName(), allProperties);
            formatNoConstructor(parsedObject, variableName, allProperties.keySet());
        } else {
            formatConstructor(parsedObject, variableName, constructorArgs);
        }
    }

    private void formatNoConstructor(final ParsedObject parsedObject, final String variableName, final Collection<String> allAttributesNames) throws GenerationException {
        final var clazz = ReflectionHelper.getClass(parsedObject.className());
        if (allAttributesNames.contains("fx:constant") && (allAttributesNames.size() == 1 ||
                (allAttributesNames.size() == 2 && allAttributesNames.contains("fx:id")))) {
            final var property = parsedObject.attributes().get("fx:constant");
            sb.append(helperProvider.getCompatibilityHelper().getStartVar(parsedObject)).append(variableName).append(" = ").append(clazz.getCanonicalName()).append(".").append(property.value()).append(";\n");
        } else {
            throw new GenerationException("Cannot find empty constructor for " + clazz.getCanonicalName());
        }
    }

    private void formatConstructor(final ParsedObject parsedObject, final String variableName, final ConstructorArgs constructorArgs) throws GenerationException {
        final var reflectionHelper = helperProvider.getReflectionHelper();
        final var args = ConstructorHelper.getListConstructorArgs(constructorArgs, parsedObject);
        final var genericTypes = reflectionHelper.getGenericTypes(parsedObject);
        sb.append(helperProvider.getCompatibilityHelper().getStartVar(parsedObject)).append(variableName)
                .append(NEW_ASSIGN).append(parsedObject.className()).append(genericTypes).append("(")
                .append(String.join(", ", args)).append(");\n");
        final var sortedAttributes = getSortedAttributes(parsedObject);
        for (final var value : sortedAttributes) {
            if (!constructorArgs.namedArgs().containsKey(value.name())) {
                helperProvider.getPropertyFormatter().formatProperty(value, parsedObject, variableName);
            }
        }
        final var sortedProperties = parsedObject.properties().entrySet().stream().sorted(Comparator.comparing(p -> p.getKey().name())).toList();
        for (final var e : sortedProperties) {
            if (!constructorArgs.namedArgs().containsKey(e.getKey().name())) {
                final var p = e.getKey();
                final var o = e.getValue();
                helperProvider.getPropertyFormatter().formatProperty(p, o, parsedObject, variableName);
            }
        }
        final var notDefinedChildren = parsedObject.children().stream().filter(c -> !(c instanceof ParsedDefine)).toList();
        if (!notDefinedChildren.isEmpty()) {
            final var defaultProperty = ReflectionHelper.getDefaultProperty(parsedObject.className());
            if (!constructorArgs.namedArgs().containsKey(defaultProperty)) {
                final var property = new ParsedPropertyImpl(defaultProperty, null, null);
                helperProvider.getPropertyFormatter().formatProperty(property, notDefinedChildren, parsedObject, variableName);
            }
        }
    }

    /**
     * Formats an include object
     *
     * @param include     The include object
     * @param subNodeName The sub node name
     */
    private void formatInclude(final ParsedInclude include, final String subNodeName) throws GenerationException {
        final var viewVariable = helperProvider.getInitializationFormatter().formatSubViewConstructorCall(include);
        sb.append("        final javafx.scene.Parent ").append(subNodeName).append(" = ").append(viewVariable).append(".load();\n");
        injectSubController(include, viewVariable);
    }

    private void injectSubController(final ParsedInclude include, final String subViewVariable) {
        final var id = include.controllerId();
        if (id != null) {
            final var variableProvider = helperProvider.getVariableProvider();
            final var subControllerVariable = variableProvider.getNextVariableName("controller");
            final var controllerClass = request.sourceInfo().sourceToSourceInfo().get(include.source()).controllerClassName();
            sb.append(helperProvider.getCompatibilityHelper().getStartVar(controllerClass)).append(subControllerVariable).append(" = ").append(subViewVariable).append(".controller();\n");
            variableProvider.addVariableInfo(id, new VariableInfo(id, include, subControllerVariable, controllerClass));
            if (request.controllerInfo().fieldInfo(id) == null) {
                logger.debug("Not injecting {} because it is not found in controller", id);
            } else {
                helperProvider.getControllerInjector().injectControllerField(id, subControllerVariable);
            }
        }
    }

    /**
     * Formats a fx:define
     *
     * @param define The parsed define
     * @throws GenerationException if an error occurs
     */
    private void formatDefine(final ParsedObject define) throws GenerationException {
        for (final var child : define.children()) {
            format(child, helperProvider.getVariableProvider().getNextVariableName("definedObject"));
        }
    }

    /**
     * Formats a fx:reference
     *
     * @param reference The parsed reference
     * @throws GenerationException if an error occurs
     */
    private void formatReference(final ParsedReference reference, final String variableName) throws GenerationException {
        final var id = reference.source();
        final var variableInfo = helperProvider.getVariableProvider().getVariableInfo(id);
        if (variableInfo == null) {
            throw new GenerationException("Unknown id : " + id);
        }
        final var referenceName = variableInfo.variableName();
        sb.append(helperProvider.getCompatibilityHelper().getStartVar(variableInfo.className())).append(variableName).append(" = ").append(referenceName).append(";\n");
    }

    /**
     * Formats a fx:copy
     *
     * @param copy         The parsed copy
     * @param variableName The variable name
     * @throws GenerationException if an error occurs
     */
    private void formatCopy(final ParsedCopy copy, final String variableName) throws GenerationException {
        final var id = copy.source();
        final var variableInfo = helperProvider.getVariableProvider().getVariableInfo(id);
        if (variableInfo == null) {
            throw new GenerationException("Unknown id : " + id);
        }
        final var copyVariable = variableInfo.variableName();
        sb.append(helperProvider.getCompatibilityHelper().getStartVar(variableInfo.className())).append(variableName)
                .append(NEW_ASSIGN).append(variableInfo.className()).append("(").append(copyVariable).append(");\n");
    }

    /**
     * Formats a constant object
     *
     * @param constant     The constant
     * @param variableName The variable name
     */
    private void formatConstant(final ParsedConstant constant, final String variableName) {
        sb.append(helperProvider.getCompatibilityHelper().getStartVar(constant.className()))
                .append(variableName).append(" = ").append(constant.className()).append(".").append(constant.constant()).append(";\n");
    }

    /**
     * Formats a value object
     *
     * @param value        The value
     * @param variableName The variable name
     */
    private void formatValue(final ParsedValue value, final String variableName) {
        sb.append(helperProvider.getCompatibilityHelper().getStartVar(value.className())).append(variableName).append(" = ").append(value.className()).append(".valueOf(\"").append(value.value()).append("\");\n");
    }

    /**
     * Formats a factory object
     *
     * @param factory      The factory
     * @param variableName The variable name
     */
    private void formatFactory(final ParsedFactory factory, final String variableName) throws GenerationException {
        final var variables = new ArrayList<String>();
        for (final var child : factory.children()) {
            final var vn = helperProvider.getVariableProvider().getNextVariableName(getVariablePrefix(child));
            format(child, vn);
        }
        for (final var argument : factory.arguments()) {
            final var argumentVariable = helperProvider.getVariableProvider().getNextVariableName("arg");
            variables.add(argumentVariable);
            format(argument, argumentVariable);
        }
        final var compatibilityHelper = helperProvider.getCompatibilityHelper();
        if (request.parameters().compatibility().useVar()) {
            sb.append(compatibilityHelper.getStartVar(factory.className())).append(variableName).append(" = ").append(factory.className())
                    .append(".").append(factory.factory()).append("(").append(String.join(", ", variables)).append(");\n");
        } else {
            final var returnType = ReflectionHelper.getStaticReturnType(factory.className(), factory.factory()).getName();
            sb.append(compatibilityHelper.getStartVar(returnType)).append(variableName).append(" = ").append(factory.className())
                    .append(".").append(factory.factory()).append("(").append(String.join(", ", variables)).append(");\n");
        }
    }
}
