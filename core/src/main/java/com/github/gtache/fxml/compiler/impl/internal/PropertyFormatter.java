package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.ResourceBundleInjectionType;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.ParsedText;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.Objects;
import java.util.SequencedCollection;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;
import static java.util.Objects.requireNonNull;

/**
 * Helper methods for {@link GeneratorImpl} to format properties
 */
final class PropertyFormatter {


    private final HelperProvider helperProvider;
    private final GenerationProgress progress;

    PropertyFormatter(final HelperProvider helperProvider, final GenerationProgress progress) {
        this.helperProvider = requireNonNull(helperProvider);
        this.progress = requireNonNull(progress);
    }

    /**
     * Formats a property
     *
     * @param property       The property to format
     * @param parent         The property's parent object
     * @param parentVariable The parent variable
     * @throws GenerationException if an error occurs
     */
    void formatProperty(final ParsedProperty property, final ParsedObject parent, final String parentVariable) throws GenerationException {
        final var propertyName = property.name();
        if (propertyName.equals(FX_ID)) {
            //Do nothing
        } else if (propertyName.equals("fx:controller")) {
            checkDuplicateController(parent);
        } else if (Objects.equals(property.sourceType(), EventHandler.class.getName())) {
            handleEventHandler(property, parentVariable);
        } else if (property.sourceType() != null) {
            handleStaticProperty(property, parentVariable, propertyName);
        } else {
            handleProperty(property, parent, parentVariable);
        }
    }

    /**
     * Formats a complex property (containing a list of objects).
     * The values should not contain any ParsedDefine
     *
     * @param property       The property to format
     * @param  values         The property's values
     * @param parent         The property's parent object
     * @param parentVariable The parent variable
     * @throws GenerationException if an error occurs or if the values contain a ParsedDefine
     */
    void formatProperty(final ParsedProperty property, final SequencedCollection<? extends ParsedObject> values, final ParsedObject parent, final String parentVariable) throws GenerationException {
        if (values.stream().anyMatch(ParsedDefine.class::isInstance)) {
            throw new GenerationException("Values should not contain any ParsedDefine");
        } else if (values.size() == 1 && values.getFirst() instanceof final ParsedText text) {
            final var newProperty = new ParsedPropertyImpl(property.name(), property.sourceType(), text.text());
            formatProperty(newProperty, parent, parentVariable);
        } else {
            formatChild(parent, property, values, parentVariable);
        }
    }

    private void checkDuplicateController(final ParsedObject parent) throws GenerationException {
        if (parent != progress.request().rootObject()) {
            throw new GenerationException("Invalid nested controller");
        }
    }

    private void handleEventHandler(final ParsedProperty property, final String parentVariable) throws GenerationException {
        if (property.value().startsWith("#")) {
            helperProvider.getControllerInjector().injectEventHandlerControllerMethod(property, parentVariable);
        } else {
            helperProvider.getFieldSetter().setEventHandler(property, parentVariable);
        }
    }

    private void handleStaticProperty(final ParsedProperty property, final String parentVariable, final String propertyName) throws GenerationException {
        final var setMethod = getSetMethod(propertyName);
        final var propertySourceTypeClass = ReflectionHelper.getClass(property.sourceType());
        if (ReflectionHelper.hasStaticMethod(propertySourceTypeClass, setMethod)) {
            final var method = ReflectionHelper.getStaticMethod(propertySourceTypeClass, setMethod);
            final var parameterType = method.getParameterTypes()[1];
            final var arg = helperProvider.getValueFormatter().getArg(property.value(), parameterType);
            setLaterIfNeeded(property, parameterType, "        " + property.sourceType() + "." + setMethod + "(" + parentVariable + ", " + arg + ");\n");
        } else {
            throw new GenerationException("Cannot set " + propertyName + " on " + property.sourceType());
        }
    }

    private void handleProperty(final ParsedProperty property, final ParsedObject parent, final String parentVariable) throws GenerationException {
        final var propertyName = property.name();
        final var setMethod = getSetMethod(propertyName);
        final var getMethod = getGetMethod(propertyName);
        final var parentClass = ReflectionHelper.getClass(parent.className());
        if (ReflectionHelper.hasMethod(parentClass, setMethod)) {
            handleSetProperty(property, parentClass, parentVariable);
        } else if (ReflectionHelper.hasMethod(parentClass, getMethod)) {
            handleGetProperty(property, parentClass, parentVariable);
        } else {
            throw new GenerationException("Cannot set " + propertyName + " on " + parent.className());
        }
    }

    private void handleSetProperty(final ParsedProperty property, final Class<?> parentClass, final String parentVariable) throws GenerationException {
        final var setMethod = getSetMethod(property.name());
        final var method = ReflectionHelper.getMethod(parentClass, setMethod);
        final var parameterType = method.getParameterTypes()[0];
        final var arg = helperProvider.getValueFormatter().getArg(property.value(), parameterType);
        setLaterIfNeeded(property, parameterType, "        " + parentVariable + "." + setMethod + "(" + arg + ");\n");
    }

    private void handleGetProperty(final ParsedProperty property, final Class<?> parentClass, final String parentVariable) throws GenerationException {
        final var getMethod = getGetMethod(property.name());
        final var method = ReflectionHelper.getMethod(parentClass, getMethod);
        final var returnType = method.getReturnType();
        if (ReflectionHelper.hasMethod(returnType, "addAll")) {
            final var arg = helperProvider.getValueFormatter().getArg(property.value(), String.class);
            setLaterIfNeeded(property, String.class, "        " + parentVariable + "." + getMethod + "().addAll(" +
                    helperProvider.getCompatibilityHelper().getListOf() + arg + "));\n");
        } else {
            throw new GenerationException("Cannot set " + property.name() + " on " + parentClass);
        }
    }

    /**
     * Saves the text to set after constructor creation if factory injection is used
     *
     * @param property The property
     * @param type     The type
     * @param arg      The argument
     */
    private void setLaterIfNeeded(final ParsedProperty property, final Class<?> type, final String arg) {
        final var parameters = progress.request().parameters();
        if (type == String.class && property.value().startsWith(RESOURCE_KEY_PREFIX) && parameters.resourceInjectionType() == ResourceBundleInjectionType.GETTER
                && parameters.fieldInjectionType() == ControllerFieldInjectionType.FACTORY) {
            progress.controllerFactoryPostAction().add(arg);
        } else {
            progress.stringBuilder().append(arg);
        }
    }


    /**
     * Formats the children objects of a property
     *
     * @param parent         The parent object
     * @param property       The parent property
     * @param objects        The child objects
     * @param parentVariable The parent object variable
     */
    private void formatChild(final ParsedObject parent, final ParsedProperty property,
                             final Iterable<? extends ParsedObject> objects, final String parentVariable) throws GenerationException {
        final var propertyName = property.name();
        final var variables = new ArrayList<String>();
        for (final var object : objects) {
            final var vn = helperProvider.getVariableProvider().getNextVariableName(getVariablePrefix(object));
            helperProvider.getObjectFormatter().format(object, vn);
            variables.add(vn);
        }
        if (variables.size() > 1) {
            formatMultipleChildren(variables, propertyName, parent, parentVariable);
        } else if (variables.size() == 1) {
            final var vn = variables.getFirst();
            formatSingleChild(vn, property, parent, parentVariable);
        }
    }

    /**
     * Formats children objects given that they are more than one
     *
     * @param variables      The children variables
     * @param propertyName   The property name
     * @param parent         The parent object
     * @param parentVariable The parent object variable
     */
    private void formatMultipleChildren(final Iterable<String> variables, final String propertyName, final ParsedObject parent,
                                        final String parentVariable) throws GenerationException {
        final var getMethod = getGetMethod(propertyName);
        if (ReflectionHelper.hasMethod(ReflectionHelper.getClass(parent.className()), getMethod)) {
            progress.stringBuilder().append("        ").append(parentVariable).append(".").append(getMethod).append("().addAll(").append(helperProvider.getCompatibilityHelper().getListOf()).append(String.join(", ", variables)).append("));\n");
        } else {
            throw getCannotSetException(propertyName, parent.className());
        }
    }

    /**
     * Formats a single child object
     *
     * @param variableName   The child's variable name
     * @param property       The parent property
     * @param parent         The parent object
     * @param parentVariable The parent object variable
     */
    private void formatSingleChild(final String variableName, final ParsedProperty property, final ParsedObject parent,
                                   final String parentVariable) throws GenerationException {
        if (property.sourceType() == null) {
            formatSingleChildInstance(variableName, property, parent, parentVariable);
        } else {
            formatSingleChildStatic(variableName, property, parentVariable);
        }
    }

    /**
     * Formats a single child object using an instance method on the parent object
     *
     * @param variableName   The child's variable name
     * @param property       The parent property
     * @param parent         The parent object
     * @param parentVariable The parent object variable
     */
    private void formatSingleChildInstance(final String variableName,
                                           final ParsedProperty property, final ParsedObject parent,
                                           final String parentVariable) throws GenerationException {
        final var setMethod = getSetMethod(property);
        final var getMethod = getGetMethod(property);
        final var parentClass = ReflectionHelper.getClass(parent.className());
        final var sb = progress.stringBuilder();
        if (ReflectionHelper.hasMethod(parentClass, setMethod)) {
            sb.append("        ").append(parentVariable).append(".").append(setMethod).append("(").append(variableName).append(");\n");
        } else if (ReflectionHelper.hasMethod(parentClass, getMethod)) {
            //Probably a list method that has only one element
            sb.append("        ").append(parentVariable).append(".").append(getMethod).append("().addAll(").append(helperProvider.getCompatibilityHelper().getListOf()).append(variableName).append("));\n");
        } else {
            throw getCannotSetException(property.name(), parent.className());
        }
    }

    /**
     * Formats a child object using a static method
     *
     * @param variableName   The child's variable name
     * @param property       The parent property
     * @param parentVariable The parent variable
     */
    private void formatSingleChildStatic(final String variableName,
                                         final ParsedProperty property, final String parentVariable) throws GenerationException {
        final var setMethod = getSetMethod(property);
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
