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
            helperProvider.getGenerationHelper().handleId(parent, parentVariable);
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
        final var setMethod = GenerationHelper.getSetMethod(propertyName);
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
        final var setMethod = GenerationHelper.getSetMethod(propertyName);
        final var getMethod = GenerationHelper.getGetMethod(propertyName);
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
        final var setMethod = GenerationHelper.getSetMethod(property.name());
        final var method = ReflectionHelper.getMethod(parentClass, setMethod);
        final var parameterType = method.getParameterTypes()[0];
        final var arg = helperProvider.getValueFormatter().getArg(property.value(), parameterType);
        setLaterIfNeeded(property, parameterType, "        " + parentVariable + "." + setMethod + "(" + arg + ");\n");
    }

    private void handleGetProperty(final ParsedProperty property, final Class<?> parentClass, final String parentVariable) throws GenerationException {
        final var getMethod = GenerationHelper.getGetMethod(property.name());
        final var method = ReflectionHelper.getMethod(parentClass, getMethod);
        final var returnType = method.getReturnType();
        if (ReflectionHelper.hasMethod(returnType, "addAll")) {
            final var arg = helperProvider.getValueFormatter().getArg(property.value(), String.class);
            setLaterIfNeeded(property, String.class, "        " + parentVariable + "." + getMethod + "().addAll(" + helperProvider.getCompatibilityHelper().getListOf() + arg + "));\n");
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
        if (type == String.class && property.value().startsWith(RESOURCE_KEY_PREFIX) && parameters.resourceInjectionType() == ResourceBundleInjectionTypes.GETTER
                && parameters.fieldInjectionType() == ControllerFieldInjectionTypes.FACTORY) {
            progress.controllerFactoryPostAction().add(arg);
        } else {
            progress.stringBuilder().append(arg);
        }
    }
}
