package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.SequencedCollection;

import static java.util.Objects.requireNonNull;

/**
 * Various methods to help {@link GeneratorImpl} for injecting controllers
 */
final class ControllerInjector {

    private final ControllerInfo controllerInfo;
    private final InjectionType fieldInjectionType;
    private final InjectionType methodInjectionType;
    private final StringBuilder sb;
    private final SequencedCollection<String> controllerFactoryPostAction;

    ControllerInjector(final ControllerInfo controllerInfo, final InjectionType fieldInjectionType, final InjectionType methodInjectionType,
                       final StringBuilder sb, final SequencedCollection<String> controllerFactoryPostAction) {
        this.controllerInfo = controllerInfo;
        this.fieldInjectionType = requireNonNull(fieldInjectionType);
        this.methodInjectionType = requireNonNull(methodInjectionType);
        this.sb = requireNonNull(sb);
        this.controllerFactoryPostAction = requireNonNull(controllerFactoryPostAction);
    }

    /**
     * Injects the given variable into the controller
     *
     * @param id       The object id
     * @param variable The object variable
     * @throws GenerationException if an error occurs
     */
    void injectControllerField(final String id, final String variable) throws GenerationException {
        if (fieldInjectionType instanceof final ControllerFieldInjectionTypes types) {
            switch (types) {
                case FACTORY ->
                        sb.append("        fieldMap.put(\"").append(id).append("\", ").append(variable).append(");\n");
                case ASSIGN -> sb.append("        controller.").append(id).append(" = ").append(variable).append(";\n");
                case SETTERS -> {
                    final var setMethod = GenerationHelper.getSetMethod(id);
                    sb.append("        controller.").append(setMethod).append("(").append(variable).append(");\n");
                }
                case REFLECTION ->
                        sb.append("        injectField(\"").append(id).append("\", ").append(variable).append(");\n");
            }
        } else {
            throw new GenerationException("Unknown controller injection type : " + fieldInjectionType);
        }
    }

    /**
     * Injects an event handler controller method
     *
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @throws GenerationException if an error occurs
     */
    void injectEventHandlerControllerMethod(final ParsedProperty property, final String parentVariable) throws GenerationException {
        injectControllerMethod(getEventHandlerMethodInjection(property, parentVariable));
    }

    /**
     * Injects a callback controller method
     *
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @param argumentClazz  The argument class
     * @throws GenerationException if an error occurs
     */
    void injectCallbackControllerMethod(final ParsedProperty property, final String parentVariable, final String argumentClazz) throws GenerationException {
        injectControllerMethod(getCallbackMethodInjection(property, parentVariable, argumentClazz));
    }

    /**
     * Injects a controller method
     *
     * @param methodInjection The method injection
     * @throws GenerationException if an error occurs
     */
    private void injectControllerMethod(final String methodInjection) throws GenerationException {
        if (fieldInjectionType instanceof final ControllerFieldInjectionTypes fieldTypes) {
            switch (fieldTypes) {
                case FACTORY -> controllerFactoryPostAction.add(methodInjection);
                case ASSIGN, SETTERS, REFLECTION -> sb.append(methodInjection);
            }
        } else {
            throw getUnknownInjectionException(fieldInjectionType);
        }
    }

    /**
     * Computes the method injection for event handler
     *
     * @param property       The property
     * @param parentVariable The parent variable
     * @return The method injection
     * @throws GenerationException if an error occurs
     */
    private String getEventHandlerMethodInjection(final ParsedProperty property, final String parentVariable) throws GenerationException {
        final var setMethod = GenerationHelper.getSetMethod(property.name());
        final var controllerMethod = property.value().replace("#", "");
        if (methodInjectionType instanceof final ControllerMethodsInjectionType methodTypes) {
            return switch (methodTypes) {
                case REFERENCE -> {
                    final var hasArgument = controllerInfo.handlerHasArgument(controllerMethod);
                    if (hasArgument) {
                        yield "        " + parentVariable + "." + setMethod + "(controller::" + controllerMethod + ");\n";
                    } else {
                        yield "        " + parentVariable + "." + setMethod + "(e -> controller." + controllerMethod + "());\n";
                    }
                }
                case REFLECTION ->
                        "        " + parentVariable + "." + setMethod + "(e -> callEventHandlerMethod(\"" + controllerMethod + "\", e));\n";
            };
        } else {
            throw getUnknownInjectionException(methodInjectionType);
        }
    }

    /**
     * Computes the method injection for callback
     *
     * @param property       The property
     * @param parentVariable The parent variable
     * @param argumentClazz  The argument class
     * @return The method injection
     * @throws GenerationException if an error occurs
     */
    private String getCallbackMethodInjection(final ParsedProperty property, final String parentVariable, final String argumentClazz) throws GenerationException {
        final var setMethod = GenerationHelper.getSetMethod(property.name());
        final var controllerMethod = property.value().replace("#", "");
        if (methodInjectionType instanceof final ControllerMethodsInjectionType methodTypes) {
            return switch (methodTypes) {
                case REFERENCE ->
                        "        " + parentVariable + "." + setMethod + "(controller::" + controllerMethod + ");\n";
                case REFLECTION ->
                        "        " + parentVariable + "." + setMethod + "(e -> callCallbackMethod(\"" + controllerMethod + "\", e, " + argumentClazz + "));\n";
            };
        } else {
            throw getUnknownInjectionException(methodInjectionType);
        }
    }

    private static GenerationException getUnknownInjectionException(final InjectionType type) {
        return new GenerationException("Unknown injection type : " + type);
    }
}
