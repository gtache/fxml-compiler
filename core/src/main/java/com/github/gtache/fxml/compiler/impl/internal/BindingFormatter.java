package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

import java.util.Arrays;
import java.util.SequencedCollection;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;
import static java.util.Objects.requireNonNull;

/**
 * Formatter for property bindings
 */
class BindingFormatter {

    private static final String PROPERTY = "Property";

    private final HelperProvider helperProvider;
    private final ControllerFieldInjectionType fieldInjectionType;
    private final StringBuilder sb;
    private final SequencedCollection<String> controllerFactoryPostAction;

    BindingFormatter(final HelperProvider helperProvider, final ControllerFieldInjectionType fieldInjectionType,
                     final StringBuilder sb, final SequencedCollection<String> controllerFactoryPostAction) {
        this.helperProvider = requireNonNull(helperProvider);
        this.fieldInjectionType = requireNonNull(fieldInjectionType);
        this.sb = requireNonNull(sb);
        this.controllerFactoryPostAction = requireNonNull(controllerFactoryPostAction);
    }

    /**
     * Formats a binding
     *
     * @param property       The property
     * @param parent         The parent object
     * @param parentVariable The parent variable
     */
    void formatBinding(final ParsedProperty property, final ParsedObject parent, final String parentVariable) throws GenerationException {
        final var value = property.value();
        if (value.endsWith("}")) {
            if (value.startsWith(BINDING_EXPRESSION_PREFIX)) {
                formatSimpleBinding(property, parent, parentVariable);
            } else if (value.startsWith(BIDIRECTIONAL_BINDING_PREFIX)) {
                formatBidirectionalBinding(property, parent, parentVariable);
            } else {
                throw new GenerationException("Unknown binding : " + value);
            }
        } else {
            throw new GenerationException("Invalid binding : " + value);
        }
    }

    private void formatSimpleBinding(final ParsedProperty property, final ParsedObject parent, final String parentVariable) throws GenerationException {
        formatBinding(property, parent, parentVariable, false);
    }

    private void formatBidirectionalBinding(final ParsedProperty property, final ParsedObject parent, final String parentVariable) throws GenerationException {
        formatBinding(property, parent, parentVariable, true);
    }

    private void formatBinding(final ParsedProperty property, final ParsedObject parent, final String parentVariable, final boolean bidirectional) throws GenerationException {
        final var name = property.name();
        final var value = property.value();
        final var className = parent.className();
        final var methodName = name + PROPERTY;
        final var bindMethod = bidirectional ? "bindBidirectional" : "bind";
        if (bidirectional ? hasWriteProperty(className, methodName) : hasReadProperty(className, methodName)) {
            final var returnType = ReflectionHelper.getReturnType(className, methodName);
            final var expression = helperProvider.getExpressionFormatter().format(value, returnType);
            if (isControllerWithFactory(value)) {
                controllerFactoryPostAction.add(INDENT_8 + parentVariable + "." + methodName + "()." + bindMethod + "(" + expression + ");\n");
            } else {
                sb.append(INDENT_8).append(parentVariable).append(".").append(methodName).append("().").append(bindMethod).append("(").append(expression).append(");\n");
            }
        } else {
            throw new GenerationException("Cannot bind " + name + " on " + className);
        }
    }

    private boolean isControllerWithFactory(final String expression) {
        final var cleaned = expression.substring(2, expression.length() - 1).trim();
        final var split = Arrays.stream(cleaned.split("\\.")).filter(s -> !s.isEmpty()).toList();
        if (split.size() == 2) {
            final var referenced = split.getFirst();
            if (referenced.equals("controller")) {
                return fieldInjectionType == ControllerFieldInjectionType.FACTORY;
            }
        }
        return false;
    }


    private static boolean hasReadProperty(final String className, final String methodName) throws GenerationException {
        return isPropertyReturnType(className, methodName, ReadOnlyProperty.class);
    }

    private static boolean hasWriteProperty(final String className, final String methodName) throws GenerationException {
        return isPropertyReturnType(className, methodName, Property.class);
    }

    private static boolean isPropertyReturnType(final String className, final String methodName, final Class<?> expectedReturnType) throws GenerationException {
        final var returnType = ReflectionHelper.getReturnType(className, methodName);
        return expectedReturnType.isAssignableFrom(returnType);
    }
}
