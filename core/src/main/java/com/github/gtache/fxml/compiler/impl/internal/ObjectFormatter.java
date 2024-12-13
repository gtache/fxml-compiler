package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.*;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;

/**
 * Helper methods for {@link GeneratorImpl} to format properties
 */
public final class ObjectFormatter {

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

    private ObjectFormatter() {
    }

    /**
     * Formats an object
     *
     * @param progress     The generation progress
     * @param parsedObject The parsed object to format
     * @param variableName The variable name for the object
     * @throws GenerationException if an error occurs
     */
    public static void format(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        switch (parsedObject) {
            case final ParsedConstant constant -> formatConstant(progress, constant, variableName);
            case final ParsedCopy copy -> formatCopy(progress, copy, variableName);
            case final ParsedDefine define -> formatDefine(progress, define);
            case final ParsedFactory factory -> formatFactory(progress, factory, variableName);
            case final ParsedInclude include -> formatInclude(progress, include, variableName);
            case final ParsedReference reference -> formatReference(progress, reference, variableName);
            case final ParsedValue value -> formatValue(progress, value, variableName);
            case final ParsedText text -> formatText(progress, text, variableName);
            default -> formatObject(progress, parsedObject, variableName);
        }
    }

    /**
     * Formats a simple text
     *
     * @param progress     The generation progress
     * @param text         The parsed text
     * @param variableName The variable name
     */
    private static void formatText(final GenerationProgress progress, final ParsedText text, final String variableName) {
        progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, "String")).append(variableName).append(" = \"").append(text.text()).append("\";\n");
    }

    /**
     * Formats a basic object
     *
     * @param progress     The generation progress
     * @param parsedObject The parsed object to format
     * @param variableName The variable name for the object
     */
    private static void formatObject(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (BUILDER_CLASSES.contains(parsedObject.className())) {
            formatBuilderObject(progress, parsedObject, variableName);
        } else {
            formatNotBuilder(progress, parsedObject, variableName);
        }
    }

    /**
     * Formats a builder object
     *
     * @param progress     The generation progress
     * @param parsedObject The parsed object
     * @param variableName The variable name
     */
    private static void formatBuilderObject(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        final var className = parsedObject.className();
        switch (className) {
            case "javafx.scene.Scene" -> SceneFormatter.formatScene(progress, parsedObject, variableName);
            case "javafx.scene.text.Font" -> FontFormatter.formatFont(progress, parsedObject, variableName);
            case "javafx.scene.image.Image" -> ImageFormatter.formatImage(progress, parsedObject, variableName);
            case "java.net.URL" -> URLFormatter.formatURL(progress, parsedObject, variableName);
            case "javafx.scene.shape.TriangleMesh" ->
                    TriangleMeshFormatter.formatTriangleMesh(progress, parsedObject, variableName);
            case "javafx.scene.web.WebView" -> WebViewFormatter.formatWebView(progress, parsedObject, variableName);
            default -> throw new IllegalArgumentException("Unknown builder class : " + className);
        }
    }

    private static void formatNotBuilder(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (isSimpleClass(parsedObject)) {
            formatSimpleClass(progress, parsedObject, variableName);
        } else {
            formatComplexClass(progress, parsedObject, variableName);
        }
    }

    private static void formatSimpleClass(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (!parsedObject.properties().isEmpty()) {
            throw new GenerationException("Simple class cannot have properties : " + parsedObject);
        }
        if (parsedObject.attributes().keySet().stream().anyMatch(k -> !k.equals(FX_ID) && !k.equals(VALUE) && !k.equals(FX_VALUE))) {
            throw new GenerationException("Invalid attributes for simple class : " + parsedObject);
        }
        final var value = getSimpleValue(progress, parsedObject);
        final var valueStr = ValueFormatter.toString(value, ReflectionHelper.getClass(parsedObject.className()));
        progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, parsedObject)).append(variableName).append(" = ").append(valueStr).append(";\n");
        GenerationHelper.handleId(progress, parsedObject, variableName);
    }

    private static String getSimpleValue(final GenerationProgress progress, final ParsedObject parsedObject) throws GenerationException {
        final var definedChildren = parsedObject.children().stream().filter(ParsedDefine.class::isInstance).toList();
        for (final var definedChild : definedChildren) {
            formatObject(progress, definedChild, progress.getNextVariableName("define"));
        }
        final var notDefinedChildren = parsedObject.children().stream().filter(c -> !(c instanceof ParsedDefine)).toList();
        if (parsedObject.attributes().containsKey(FX_VALUE)) {
            return getSimpleFXValue(parsedObject, notDefinedChildren);
        } else if (parsedObject.attributes().containsKey(VALUE)) {
            return getSimpleValue(parsedObject, notDefinedChildren);
        } else {
            return getSimpleChild(parsedObject, notDefinedChildren);
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

    private static void formatComplexClass(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        final var clazz = ReflectionHelper.getClass(parsedObject.className());
        final var children = parsedObject.children();
        final var definedChildren = children.stream().filter(ParsedDefine.class::isInstance).toList();
        final var notDefinedChildren = children.stream().filter(c -> !(c instanceof ParsedDefine)).toList();
        final var constructors = clazz.getConstructors();
        final var allPropertyNames = new HashSet<>(parsedObject.attributes().keySet());
        allPropertyNames.addAll(parsedObject.properties().keySet().stream().map(ParsedProperty::name).collect(Collectors.toSet()));
        if (!definedChildren.isEmpty()) {
            for (final var definedChild : definedChildren) {
                format(progress, definedChild, progress.getNextVariableName("define"));
            }
        }
        if (!notDefinedChildren.isEmpty()) {
            final var defaultProperty = ReflectionHelper.getDefaultProperty(parsedObject.className());
            if (defaultProperty != null) {
                allPropertyNames.add(defaultProperty);
            }
        }
        final var constructorArgs = ConstructorHelper.getMatchingConstructorArgs(constructors, allPropertyNames);
        if (constructorArgs == null) {
            formatNoConstructor(progress, parsedObject, variableName, allPropertyNames);
        } else {
            formatConstructor(progress, parsedObject, variableName, constructorArgs);
        }
    }

    private static void formatNoConstructor(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName, final Collection<String> allPropertyNames) throws GenerationException {
        final var clazz = ReflectionHelper.getClass(parsedObject.className());
        if (allPropertyNames.size() == 1 && allPropertyNames.iterator().next().equals("fx:constant")) {
            final var property = parsedObject.attributes().get("fx:constant");
            progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, parsedObject)).append(variableName).append(" = ").append(clazz.getCanonicalName()).append(".").append(property.value()).append(";\n");
        } else {
            throw new GenerationException("Cannot find constructor for " + clazz.getCanonicalName());
        }
    }

    private static void formatConstructor(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName, final ConstructorArgs constructorArgs) throws GenerationException {
        final var args = ConstructorHelper.getListConstructorArgs(constructorArgs, parsedObject);
        final var genericTypes = ReflectionHelper.getGenericTypes(progress, parsedObject);
        progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, parsedObject)).append(variableName).append(NEW_ASSIGN).append(parsedObject.className())
                .append(genericTypes).append("(").append(String.join(", ", args)).append(");\n");
        final var sortedAttributes = getSortedAttributes(parsedObject);
        for (final var value : sortedAttributes) {
            if (!constructorArgs.namedArgs().containsKey(value.name())) {
                PropertyFormatter.formatProperty(progress, value, parsedObject, variableName);
            }
        }
        final var sortedProperties = parsedObject.properties().entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().name())).toList();
        for (final var e : sortedProperties) {
            if (!constructorArgs.namedArgs().containsKey(e.getKey().name())) {
                final var p = e.getKey();
                final var o = e.getValue();
                formatChild(progress, parsedObject, p, o, variableName);
            }
        }
        final var notDefinedChildren = parsedObject.children().stream().filter(c -> !(c instanceof ParsedDefine)).toList();
        if (!notDefinedChildren.isEmpty()) {
            final var defaultProperty = ReflectionHelper.getDefaultProperty(parsedObject.className());
            if (!constructorArgs.namedArgs().containsKey(defaultProperty)) {
                final var property = new ParsedPropertyImpl(defaultProperty, null, null);
                formatChild(progress, parsedObject, property, notDefinedChildren, variableName);
            }
        }
    }

    /**
     * Formats an include object
     *
     * @param progress    The generation progress
     * @param include     The include object
     * @param subNodeName The sub node name
     */
    private static void formatInclude(final GenerationProgress progress, final ParsedInclude include, final String subNodeName) throws GenerationException {
        final var subViewVariable = progress.getNextVariableName("view");
        final var viewVariable = ConstructorFormatter.formatSubViewConstructorCall(progress, include);
        progress.stringBuilder().append("    final javafx.scene.Parent ").append(subNodeName).append(" = ").append(viewVariable).append(".load();\n");
        injectSubController(progress, include, subViewVariable);
    }

    private static void injectSubController(final GenerationProgress progress, final ParsedInclude include, final String subViewVariable) throws GenerationException {
        final var id = include.controllerId();
        if (id != null) {
            final var subControllerVariable = progress.getNextVariableName("controller");
            final var controllerClass = progress.request().sourceInfo().sourceToSourceInfo().get(include.source()).controllerClassName();
            progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, controllerClass)).append(subControllerVariable).append(" = ").append(subViewVariable).append(".controller();\n");
            progress.idToVariableInfo().put(id, new VariableInfo(id, include, subControllerVariable, controllerClass));
            if (progress.request().controllerInfo().fieldInfo(id) == null) {
                logger.debug("Not injecting {} because it is not found in controller", id);
            } else {
                ControllerInjector.injectControllerField(progress, id, subControllerVariable);
            }
        }
    }

    /**
     * Formats a fx:define
     *
     * @param progress The generation progress
     * @param define   The parsed define
     * @throws GenerationException if an error occurs
     */
    private static void formatDefine(final GenerationProgress progress, final ParsedObject define) throws GenerationException {
        for (final var child : define.children()) {
            format(progress, child, progress.getNextVariableName("definedObject"));
        }
    }

    /**
     * Formats a fx:reference
     *
     * @param progress  The generation progress
     * @param reference The parsed reference
     * @throws GenerationException if an error occurs
     */
    private static void formatReference(final GenerationProgress progress, final ParsedReference reference, final String variableName) throws GenerationException {
        final var id = reference.source();
        final var variableInfo = progress.idToVariableInfo().get(id);
        if (variableInfo == null) {
            throw new GenerationException("Unknown id : " + id);
        }
        final var referenceName = variableInfo.variableName();
        progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, variableInfo.className())).append(variableName).append(" = ").append(referenceName).append(";\n");
    }

    /**
     * Formats a fx:copy
     *
     * @param progress     The generation progress
     * @param copy         The parsed copy
     * @param variableName The variable name
     * @throws GenerationException if an error occurs
     */
    private static void formatCopy(final GenerationProgress progress, final ParsedCopy copy, final String variableName) throws GenerationException {
        final var id = copy.source();
        final var variableInfo = progress.idToVariableInfo().get(id);
        if (variableInfo == null) {
            throw new GenerationException("Unknown id : " + id);
        }
        final var copyVariable = variableInfo.variableName();
        progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, variableInfo.className())).append(variableName).append(NEW_ASSIGN).append(variableInfo.className()).append("(").append(copyVariable).append(");\n");
    }

    /**
     * Formats a constant object
     *
     * @param progress     The generation progress
     * @param constant     The constant
     * @param variableName The variable name
     */
    private static void formatConstant(final GenerationProgress progress, final ParsedConstant constant, final String variableName) {
        progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, constant.className())).append(variableName).append(" = ").append(constant.className()).append(".").append(constant.constant()).append(";\n");
    }

    /**
     * Formats a value object
     *
     * @param progress     The generation progress
     * @param value        The value
     * @param variableName The variable name
     */
    private static void formatValue(final GenerationProgress progress, final ParsedValue value, final String variableName) {
        progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, value.className())).append(variableName).append(" = ").append(value.className()).append(".valueOf(\"").append(value.value()).append("\");\n");
    }

    /**
     * Formats a factory object
     *
     * @param progress     The generation progress
     * @param factory      The factory
     * @param variableName The variable name
     */
    private static void formatFactory(final GenerationProgress progress, final ParsedFactory factory, final String variableName) throws GenerationException {
        final var variables = new ArrayList<String>();
        for (final var argument : factory.arguments()) {
            final var argumentVariable = progress.getNextVariableName("arg");
            variables.add(argumentVariable);
            format(progress, argument, argumentVariable);
        }
        if (progress.request().parameters().compatibility().useVar()) {
            progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, factory.className())).append(variableName).append(" = ").append(factory.className())
                    .append(".").append(factory.factory()).append("(").append(String.join(", ", variables)).append(");\n");
        } else {
            final var returnType = ReflectionHelper.getReturnType(factory.className(), factory.factory());
            progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, returnType)).append(variableName).append(" = ").append(factory.className())
                    .append(".").append(factory.factory()).append("(").append(String.join(", ", variables)).append(");\n");
        }
    }

    /**
     * Formats the children objects of a property
     *
     * @param progress       The generation progress
     * @param parent         The parent object
     * @param property       The parent property
     * @param objects        The child objects
     * @param parentVariable The parent object variable
     */
    private static void formatChild(final GenerationProgress progress, final ParsedObject parent, final ParsedProperty property,
                                    final Iterable<? extends ParsedObject> objects, final String parentVariable) throws GenerationException {
        final var propertyName = property.name();
        final var variables = new ArrayList<String>();
        for (final var object : objects) {
            final var vn = progress.getNextVariableName(GenerationHelper.getVariablePrefix(object));
            format(progress, object, vn);
            if (!(object instanceof ParsedDefine)) {
                variables.add(vn);
            }
        }
        if (variables.size() > 1) {
            formatMultipleChildren(progress, variables, propertyName, parent, parentVariable);
        } else if (variables.size() == 1) {
            final var vn = variables.getFirst();
            formatSingleChild(progress, vn, property, parent, parentVariable);
        }
    }

    /**
     * Formats children objects given that they are more than one
     *
     * @param progress       The generation progress
     * @param variables      The children variables
     * @param propertyName   The property name
     * @param parent         The parent object
     * @param parentVariable The parent object variable
     */
    private static void formatMultipleChildren(final GenerationProgress progress, final Iterable<String> variables, final String propertyName, final ParsedObject parent,
                                               final String parentVariable) throws GenerationException {
        final var getMethod = GenerationHelper.getGetMethod(propertyName);
        if (ReflectionHelper.hasMethod(ReflectionHelper.getClass(parent.className()), getMethod)) {
            progress.stringBuilder().append("        ").append(parentVariable).append(".").append(getMethod).append("().addAll(").append(GenerationCompatibilityHelper.getListOf(progress)).append(String.join(", ", variables)).append("));\n");
        } else {
            throw getCannotSetException(propertyName, parent.className());
        }
    }

    /**
     * Formats a single child object
     *
     * @param progress       The generation progress
     * @param variableName   The child's variable name
     * @param property       The parent property
     * @param parent         The parent object
     * @param parentVariable The parent object variable
     */
    private static void formatSingleChild(final GenerationProgress progress, final String variableName, final ParsedProperty property, final ParsedObject parent,
                                          final String parentVariable) throws GenerationException {
        if (property.sourceType() == null) {
            formatSingleChildInstance(progress, variableName, property, parent, parentVariable);
        } else {
            formatSingleChildStatic(progress, variableName, property, parentVariable);
        }
    }

    /**
     * Formats a single child object using an instance method on the parent object
     *
     * @param progress       The generation progress
     * @param variableName   The child's variable name
     * @param property       The parent property
     * @param parent         The parent object
     * @param parentVariable The parent object variable
     */
    private static void formatSingleChildInstance(final GenerationProgress progress, final String variableName,
                                                  final ParsedProperty property, final ParsedObject parent,
                                                  final String parentVariable) throws GenerationException {
        final var setMethod = GenerationHelper.getSetMethod(property);
        final var getMethod = GenerationHelper.getGetMethod(property);
        final var parentClass = ReflectionHelper.getClass(parent.className());
        final var sb = progress.stringBuilder();
        if (ReflectionHelper.hasMethod(parentClass, setMethod)) {
            sb.append("        ").append(parentVariable).append(".").append(setMethod).append("(").append(variableName).append(");\n");
        } else if (ReflectionHelper.hasMethod(parentClass, getMethod)) {
            //Probably a list method that has only one element
            sb.append("        ").append(parentVariable).append(".").append(getMethod).append("().addAll(").append(GenerationCompatibilityHelper.getListOf(progress)).append(variableName).append("));\n");
        } else {
            throw getCannotSetException(property.name(), parent.className());
        }
    }

    /**
     * Formats a child object using a static method
     *
     * @param progress       The generation progress
     * @param variableName   The child's variable name
     * @param property       The parent property
     * @param parentVariable The parent variable
     */
    private static void formatSingleChildStatic(final GenerationProgress progress, final String variableName,
                                                final ParsedProperty property, final String parentVariable) throws GenerationException {
        final var setMethod = GenerationHelper.getSetMethod(property);
        if (ReflectionHelper.hasStaticMethod(ReflectionHelper.getClass(property.sourceType()), setMethod)) {
            progress.stringBuilder().append("        ").append(property.sourceType()).append(".").append(setMethod)
                    .append("(").append(parentVariable).append(", ").append(variableName).append(");\n");
        } else {
            throw getCannotSetException(property.name(), property.sourceType());
        }
    }

    private static GenerationException getCannotSetException(final String propertyName, final String className) {
        return new GenerationException("Cannot set " + propertyName + " on " + className);
    }
}
