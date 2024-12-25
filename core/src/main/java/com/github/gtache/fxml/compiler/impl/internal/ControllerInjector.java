package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.SequencedCollection;

import static java.util.Objects.requireNonNull;

/**
 * Various methods to help {@link GeneratorImpl} for injecting controllers
 */
final class ControllerInjector {

    private final ControllerInfo controllerInfo;
    private final ControllerFieldInjectionType fieldInjectionType;
    private final ControllerMethodsInjectionType methodInjectionType;
    private final StringBuilder sb;
    private final SequencedCollection<String> controllerFactoryPostAction;

    ControllerInjector(final ControllerInfo controllerInfo, final ControllerFieldInjectionType fieldInjectionType,
                       final ControllerMethodsInjectionType methodInjectionType, final StringBuilder sb,
                       final SequencedCollection<String> controllerFactoryPostAction) {
        this.controllerInfo = requireNonNull(controllerInfo);
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
     */
    void injectControllerField(final String id, final String variable) {
        switch (fieldInjectionType) {
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
    }

    /**
     * Injects an event handler controller method
     *
     * @param property       The property to inject
     * @param parentVariable The parent variable
     */
    void injectEventHandlerControllerMethod(final ParsedProperty property, final String parentVariable) {
        injectControllerMethod(getEventHandlerMethodInjection(property, parentVariable));
    }

    /**
     * Injects a callback controller method
     *
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @param argumentClazz  The argument class
     */
    void injectCallbackControllerMethod(final ParsedProperty property, final String parentVariable, final String argumentClazz) {
        injectControllerMethod(getCallbackMethodInjection(property, parentVariable, argumentClazz));
    }

    /**
     * Injects a controller method
     *
     * @param methodInjection The method injection
     */
    private void injectControllerMethod(final String methodInjection) {
        switch (fieldInjectionType) {
            case FACTORY -> controllerFactoryPostAction.add(methodInjection);
            case ASSIGN, SETTERS, REFLECTION -> sb.append(methodInjection);
        }
    }

    /**
     * Computes the method injection for event handler
     *
     * @param property       The property
     * @param parentVariable The parent variable
     * @return The method injection
     */
    private String getEventHandlerMethodInjection(final ParsedProperty property, final String parentVariable) {
        final var setMethod = GenerationHelper.getSetMethod(property.name());
        final var controllerMethod = property.value().replace("#", "");
        return switch (methodInjectionType) {
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
    }

    /**
     * Computes the method injection for callback
     *
     * @param property       The property
     * @param parentVariable The parent variable
     * @param argumentClazz  The argument class
     * @return The method injection
     */
    private String getCallbackMethodInjection(final ParsedProperty property, final String parentVariable, final String argumentClazz) {
        final var setMethod = GenerationHelper.getSetMethod(property.name());
        final var controllerMethod = property.value().replace("#", "");
        return switch (methodInjectionType) {
            case REFERENCE ->
                    "        " + parentVariable + "." + setMethod + "(controller::" + controllerMethod + ");\n";
            case REFLECTION ->
                    "        " + parentVariable + "." + setMethod + "(e -> callCallbackMethod(\"" + controllerMethod + "\", e, " + argumentClazz + "));\n";
        };
    }
}
