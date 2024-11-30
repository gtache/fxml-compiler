package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getControllerInjection;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getSetMethod;

/**
 * Various methods to help {@link GeneratorImpl} for injecting controllers
 */
final class ControllerInjector {
    private ControllerInjector() {

    }

    /**
     * Injects the given variable into the controller
     *
     * @param progress The generation progress
     * @param id       The object id
     * @param variable The object variable
     * @throws GenerationException if an error occurs
     */
    static void injectControllerField(final GenerationProgress progress, final String id, final String variable) throws GenerationException {
        final var controllerInjection = getControllerInjection(progress);
        final var controllerInjectionType = controllerInjection.fieldInjectionType();
        if (controllerInjectionType instanceof final ControllerFieldInjectionTypes types) {
            final var sb = progress.stringBuilder();
            switch (types) {
                case FACTORY ->
                        sb.append("    fieldMap.put(\"").append(id).append("\", ").append(variable).append(");\n");
                case ASSIGN -> sb.append("    controller.").append(id).append(" = ").append(variable).append(";\n");
                case SETTERS -> {
                    final var setMethod = getSetMethod(id);
                    sb.append("    controller.").append(setMethod).append("(").append(variable).append(");\n");
                }
                case REFLECTION ->
                        sb.append("    injectField(\"").append(id).append("\", ").append(variable).append(");\n");
            }
        } else {
            throw new GenerationException("Unknown controller injection type : " + controllerInjectionType);
        }
    }

    /**
     * Injects an event handler controller method
     *
     * @param progress       The generation progress
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @throws GenerationException if an error occurs
     */
    static void injectEventHandlerControllerMethod(final GenerationProgress progress, final ParsedProperty property, final String parentVariable) throws GenerationException {
        injectControllerMethod(progress, getEventHandlerMethodInjection(progress, property, parentVariable));
    }

    /**
     * Injects a callback controller method
     *
     * @param progress       The generation progress
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @param argumentClazz  The argument class
     * @throws GenerationException if an error occurs
     */
    static void injectCallbackControllerMethod(final GenerationProgress progress, final ParsedProperty property, final String parentVariable, final String argumentClazz) throws GenerationException {
        injectControllerMethod(progress, getCallbackMethodInjection(progress, property, parentVariable, argumentClazz));
    }

    /**
     * Injects a controller method
     *
     * @param progress        The generation progress
     * @param methodInjection The method injection
     * @throws GenerationException if an error occurs
     */
    private static void injectControllerMethod(final GenerationProgress progress, final String methodInjection) throws GenerationException {
        final var injection = getControllerInjection(progress);
        if (injection.fieldInjectionType() instanceof final ControllerFieldInjectionTypes fieldTypes) {
            switch (fieldTypes) {
                case FACTORY -> progress.controllerFactoryPostAction().add(methodInjection);
                case ASSIGN, SETTERS, REFLECTION -> progress.stringBuilder().append(methodInjection);
            }
        } else {
            throw getUnknownInjectionException(injection.fieldInjectionType());
        }
    }

    /**
     * Computes the method injection for event handler
     *
     * @param progress       The generation progress
     * @param property       The property
     * @param parentVariable The parent variable
     * @return The method injection
     * @throws GenerationException if an error occurs
     */
    private static String getEventHandlerMethodInjection(final GenerationProgress progress, final ParsedProperty property, final String parentVariable) throws GenerationException {
        final var setMethod = getSetMethod(property.name());
        final var injection = getControllerInjection(progress);
        final var controllerMethod = property.value().replace("#", "");
        if (injection.methodInjectionType() instanceof final ControllerMethodsInjectionType methodTypes) {
            return switch (methodTypes) {
                case REFERENCE -> {
                    final var hasArgument = progress.request().controllerInfo().handlerHasArgument(controllerMethod);
                    if (hasArgument) {
                        yield "    " + parentVariable + "." + setMethod + "(controller::" + controllerMethod + ");\n";
                    } else {
                        yield "    " + parentVariable + "." + setMethod + "(e -> controller." + controllerMethod + "());\n";
                    }
                }
                case REFLECTION ->
                        "    " + parentVariable + "." + setMethod + "(e -> callEventHandlerMethod(\"" + controllerMethod + "\", e));\n";
            };
        } else {
            throw getUnknownInjectionException(injection.methodInjectionType());
        }
    }

    /**
     * Computes the method injection for callback
     *
     * @param progress       The generation progress
     * @param property       The property
     * @param parentVariable The parent variable
     * @param argumentClazz  The argument class
     * @return The method injection
     * @throws GenerationException if an error occurs
     */
    private static String getCallbackMethodInjection(final GenerationProgress progress, final ParsedProperty property, final String parentVariable, final String argumentClazz) throws GenerationException {
        final var setMethod = getSetMethod(property.name());
        final var injection = getControllerInjection(progress);
        final var controllerMethod = property.value().replace("#", "");
        if (injection.methodInjectionType() instanceof final ControllerMethodsInjectionType methodTypes) {
            return switch (methodTypes) {
                case REFERENCE ->
                        "    " + parentVariable + "." + setMethod + "(controller::" + controllerMethod + ");\n";
                case REFLECTION ->
                        "    " + parentVariable + "." + setMethod + "(e -> callCallbackMethod(\"" + controllerMethod + "\", e, " + argumentClazz + "));\n";
            };
        } else {
            throw getUnknownInjectionException(injection.methodInjectionType());
        }
    }

    private static GenerationException getUnknownInjectionException(final InjectionType type) {
        return new GenerationException("Unknown injection type : " + type);
    }
}
