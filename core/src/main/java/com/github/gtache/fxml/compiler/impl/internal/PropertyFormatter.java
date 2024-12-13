package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import javafx.event.EventHandler;

import java.util.Objects;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.FX_ID;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.RESOURCE_KEY_PREFIX;

/**
 * Helper methods for {@link GeneratorImpl} to format properties
 */
final class PropertyFormatter {

    private PropertyFormatter() {
    }

    /**
     * Formats a property
     *
     * @param progress       The generation progress
     * @param property       The property to format
     * @param parent         The property's parent object
     * @param parentVariable The parent variable
     * @throws GenerationException if an error occurs
     */
    static void formatProperty(final GenerationProgress progress, final ParsedProperty property, final ParsedObject parent, final String parentVariable) throws GenerationException {
        final var propertyName = property.name();
        if (propertyName.equals(FX_ID)) {
            GenerationHelper.handleId(progress, parent, parentVariable);
        } else if (propertyName.equals("fx:controller")) {
            checkDuplicateController(progress, parent);
        } else if (Objects.equals(property.sourceType(), EventHandler.class.getName())) {
            handleEventHandler(progress, property, parentVariable);
        } else if (property.sourceType() != null) {
            handleStaticProperty(progress, property, parentVariable, propertyName);
        } else {
            handleProperty(progress, property, parent, parentVariable);
        }
    }

    private static void checkDuplicateController(final GenerationProgress progress, final ParsedObject parent) throws GenerationException {
        if (parent != progress.request().rootObject()) {
            throw new GenerationException("Invalid nested controller");
        }
    }

    private static void handleEventHandler(final GenerationProgress progress, final ParsedProperty property, final String parentVariable) throws GenerationException {
        if (property.value().startsWith("#")) {
            ControllerInjector.injectEventHandlerControllerMethod(progress, property, parentVariable);
        } else {
            FieldSetter.setEventHandler(progress, property, parentVariable);
        }
    }

    private static void handleStaticProperty(final GenerationProgress progress, final ParsedProperty property, final String parentVariable, final String propertyName) throws GenerationException {
        final var setMethod = GenerationHelper.getSetMethod(propertyName);
        final var propertySourceTypeClass = ReflectionHelper.getClass(property.sourceType());
        if (ReflectionHelper.hasStaticMethod(propertySourceTypeClass, setMethod)) {
            final var method = ReflectionHelper.getStaticMethod(propertySourceTypeClass, setMethod);
            final var parameterType = method.getParameterTypes()[1];
            final var arg = ValueFormatter.getArg(progress, property.value(), parameterType);
            setLaterIfNeeded(progress, property, parameterType, "        " + property.sourceType() + "." + setMethod + "(" + parentVariable + ", " + arg + ");\n");
        } else {
            throw new GenerationException("Cannot set " + propertyName + " on " + property.sourceType());
        }
    }

    private static void handleProperty(final GenerationProgress progress, final ParsedProperty property, final ParsedObject parent, final String parentVariable) throws GenerationException {
        final var propertyName = property.name();
        final var setMethod = GenerationHelper.getSetMethod(propertyName);
        final var getMethod = GenerationHelper.getGetMethod(propertyName);
        final var parentClass = ReflectionHelper.getClass(parent.className());
        if (ReflectionHelper.hasMethod(parentClass, setMethod)) {
            handleSetProperty(progress, property, parentClass, parentVariable);
        } else if (ReflectionHelper.hasMethod(parentClass, getMethod)) {
            handleGetProperty(progress, property, parentClass, parentVariable);
        } else {
            throw new GenerationException("Cannot set " + propertyName + " on " + parent.className());
        }
    }

    private static void handleSetProperty(final GenerationProgress progress, final ParsedProperty property, final Class<?> parentClass, final String parentVariable) throws GenerationException {
        final var setMethod = GenerationHelper.getSetMethod(property.name());
        final var method = ReflectionHelper.getMethod(parentClass, setMethod);
        final var parameterType = method.getParameterTypes()[0];
        final var arg = ValueFormatter.getArg(progress, property.value(), parameterType);
        setLaterIfNeeded(progress, property, parameterType, "        " + parentVariable + "." + setMethod + "(" + arg + ");\n");
    }

    private static void handleGetProperty(final GenerationProgress progress, final ParsedProperty property, final Class<?> parentClass, final String parentVariable) throws GenerationException {
        final var getMethod = GenerationHelper.getGetMethod(property.name());
        final var method = ReflectionHelper.getMethod(parentClass, getMethod);
        final var returnType = method.getReturnType();
        if (ReflectionHelper.hasMethod(returnType, "addAll")) {
            final var arg = ValueFormatter.getArg(progress, property.value(), String.class);
            setLaterIfNeeded(progress, property, String.class, "        " + parentVariable + "." + getMethod + "().addAll(" + GenerationCompatibilityHelper.getListOf(progress) + arg + "));\n");
        }
    }

    /**
     * Saves the text to set after constructor creation if factory injection is used
     *
     * @param progress The generation progress
     * @param property The property
     * @param type     The type
     * @param arg      The argument
     */
    private static void setLaterIfNeeded(final GenerationProgress progress, final ParsedProperty property, final Class<?> type, final String arg) {
        final var parameters = progress.request().parameters();
        if (type == String.class && property.value().startsWith(RESOURCE_KEY_PREFIX) && parameters.resourceInjectionType() == ResourceBundleInjectionTypes.GETTER
                && parameters.fieldInjectionType() == ControllerFieldInjectionTypes.FACTORY) {
            progress.controllerFactoryPostAction().add(arg);
        } else {
            progress.stringBuilder().append(arg);
        }
    }
}
