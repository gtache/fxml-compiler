package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerMethodsInjectionType;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getControllerInjection;

/**
 * Provides the helper methods for the generated code
 */
public final class HelperMethodsProvider {

    private HelperMethodsProvider() {

    }

    /**
     * Gets helper methods string for the given generation progress
     *
     * @param progress The generation progress
     * @return The helper methods
     * @throws GenerationException if an error occurs
     */
    public static String getHelperMethods(final GenerationProgress progress) throws GenerationException {
        final var injection = getControllerInjection(progress);
        final var methodInjectionType = injection.methodInjectionType();
        final var sb = new StringBuilder();
        if (methodInjectionType == ControllerMethodsInjectionType.REFLECTION) {
            sb.append("""
                    private <T extends javafx.event.Event> void callEventHandlerMethod(final String methodName, final T event) {
                        try {
                            final java.lang.reflect.Method method;
                            final var methods = java.util.Arrays.stream(controller.getClass().getDeclaredMethods())
                                    .filter(m -> m.getName().equals(methodName)).toList();
                            if (methods.size() > 1) {
                                final var eventMethods = methods.stream().filter(m ->
                                        m.getParameterCount() == 1 && javafx.event.Event.class.isAssignableFrom(m.getParameterTypes()[0])).toList();
                                if (eventMethods.size() == 1) {
                                    method = eventMethods.getFirst();
                                } else {
                                    final var emptyMethods = methods.stream().filter(m -> m.getParameterCount() == 0).toList();
                                    if (emptyMethods.size() == 1) {
                                        method = emptyMethods.getFirst();
                                    } else {
                                        throw new IllegalArgumentException("Multiple matching methods for " + methodName);
                                    }
                                }
                            } else if (methods.size() == 1) {
                                method = methods.getFirst();
                            } else {
                                throw new IllegalArgumentException("No matching method for " + methodName);
                            }
                            method.setAccessible(true);
                            if (method.getParameterCount() == 0) {
                                method.invoke(controller);
                            } else {
                                method.invoke(controller, event);
                            }
                        } catch (final IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
                            throw new RuntimeException("Error using reflection on " + methodName, ex);
                        }
                    }
                    
                    private <T, U> U callCallbackMethod(final String methodName, final T value, final Class<T> clazz) {
                        try {
                            final java.lang.reflect.Method method;
                            final var methods = java.util.Arrays.stream(controller.getClass().getDeclaredMethods())
                                    .filter(m -> m.getName().equals(methodName)).toList();
                            if (methods.size() > 1) {
                                final var eventMethods = methods.stream().filter(m ->
                                        m.getParameterCount() == 1 && clazz.isAssignableFrom(m.getParameterTypes()[0])).toList();
                                if (eventMethods.size() == 1) {
                                    method = eventMethods.getFirst();
                                } else {
                                    throw new IllegalArgumentException("Multiple matching methods for " + methodName);
                                }
                            } else if (methods.size() == 1) {
                                method = methods.getFirst();
                            } else {
                                throw new IllegalArgumentException("No matching method for " + methodName);
                            }
                            method.setAccessible(true);
                            return (U) method.invoke(controller, value);
                        } catch (final IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
                            throw new RuntimeException("Error using reflection on " + methodName, ex);
                        }
                    }
                    """);
        }
        if (injection.fieldInjectionType() == ControllerFieldInjectionTypes.REFLECTION) {
            sb.append("""
                    private <T> void injectField(final String fieldName, final T object) {
                        try {
                            final var field = controller.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(controller, object);
                        } catch (final NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException("Error using reflection on " + fieldName, e);
                        }
                    }
                    """);
        }
        return sb.toString();
    }
}
