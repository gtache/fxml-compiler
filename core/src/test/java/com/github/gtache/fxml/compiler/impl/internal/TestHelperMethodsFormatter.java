package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerMethodsInjectionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestHelperMethodsFormatter {

    private final HelperProvider helperProvider;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final StringBuilder sb;

    TestHelperMethodsFormatter(@Mock final HelperProvider helperProvider, @Mock final GenerationCompatibilityHelper compatibilityHelper) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.sb = new StringBuilder();
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        when(compatibilityHelper.getStartVar(anyString(), anyInt())).then(i -> i.getArgument(0));
        when(compatibilityHelper.getListOf()).thenReturn("listof(");
        when(compatibilityHelper.getGetFirst()).thenReturn(".getFirst()");
        when(compatibilityHelper.getToList()).thenReturn(".toList()");
    }

    @Test
    void testMethodReflection() {
        final var helperMethodsFormatter = new HelperMethodsFormatter(helperProvider, ControllerFieldInjectionType.ASSIGN, ControllerMethodsInjectionType.REFLECTION, sb);
        final var expected = """
                    private <T extends javafx.event.Event> void callEventHandlerMethod(final String methodName, final T event) {
                        try {
                            final java.lang.reflect.Method method;
                            java.util.List<java.lang.reflect.Method>methods = java.util.Arrays.stream(controller.getClass().getDeclaredMethods())
                                    .filter(m -> m.getName().equals(methodName)).toList();
                            if (methods.size() > 1) {
                                java.util.List<java.lang.reflect.Method>eventMethods = methods.stream().filter(m ->
                                        m.getParameterCount() == 1 && javafx.event.Event.class.isAssignableFrom(m.getParameterTypes()[0])).toList();
                                if (eventMethods.size() == 1) {
                                    method = eventMethods.getFirst();
                                } else {
                                    java.util.List<java.lang.reflect.Method>emptyMethods = methods.stream().filter(m -> m.getParameterCount() == 0).toList();
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
                            java.util.List<java.lang.reflect.Method>methods = java.util.Arrays.stream(controller.getClass().getDeclaredMethods())
                                    .filter(m -> m.getName().equals(methodName)).toList();
                            if (methods.size() > 1) {
                                java.util.List<java.lang.reflect.Method>eventMethods = methods.stream().filter(m ->
                                        m.getParameterCount() == 2 && clazz.isAssignableFrom(m.getParameterTypes()[1])).toList();
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
                """;
        helperMethodsFormatter.formatHelperMethods();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFieldReflection() {
        final var helperMethodsFormatter = new HelperMethodsFormatter(helperProvider, ControllerFieldInjectionType.REFLECTION, ControllerMethodsInjectionType.REFERENCE, sb);
        final var expected = """
                    private <T> void injectField(final String fieldName, final T object) {
                        try {
                            java.lang.reflect.Fieldfield = controller.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(controller, object);
                        } catch (final NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException("Error using reflection on " + fieldName, e);
                        }
                    }
                """;
        helperMethodsFormatter.formatHelperMethods();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testNoReflection() {
        final var helperMethodsFormatter = new HelperMethodsFormatter(helperProvider, ControllerFieldInjectionType.FACTORY, ControllerMethodsInjectionType.REFERENCE, sb);
        helperMethodsFormatter.formatHelperMethods();
        assertEquals("", sb.toString());
    }

    @Test
    void testBothReflection() {
        final var helperMethodsFormatter = new HelperMethodsFormatter(helperProvider, ControllerFieldInjectionType.REFLECTION, ControllerMethodsInjectionType.REFLECTION, sb);
        final var expected = """
                    private <T extends javafx.event.Event> void callEventHandlerMethod(final String methodName, final T event) {
                        try {
                            final java.lang.reflect.Method method;
                            java.util.List<java.lang.reflect.Method>methods = java.util.Arrays.stream(controller.getClass().getDeclaredMethods())
                                    .filter(m -> m.getName().equals(methodName)).toList();
                            if (methods.size() > 1) {
                                java.util.List<java.lang.reflect.Method>eventMethods = methods.stream().filter(m ->
                                        m.getParameterCount() == 1 && javafx.event.Event.class.isAssignableFrom(m.getParameterTypes()[0])).toList();
                                if (eventMethods.size() == 1) {
                                    method = eventMethods.getFirst();
                                } else {
                                    java.util.List<java.lang.reflect.Method>emptyMethods = methods.stream().filter(m -> m.getParameterCount() == 0).toList();
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
                            java.util.List<java.lang.reflect.Method>methods = java.util.Arrays.stream(controller.getClass().getDeclaredMethods())
                                    .filter(m -> m.getName().equals(methodName)).toList();
                            if (methods.size() > 1) {
                                java.util.List<java.lang.reflect.Method>eventMethods = methods.stream().filter(m ->
                                        m.getParameterCount() == 2 && clazz.isAssignableFrom(m.getParameterTypes()[1])).toList();
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
                    private <T> void injectField(final String fieldName, final T object) {
                        try {
                            java.lang.reflect.Fieldfield = controller.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(controller, object);
                        } catch (final NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException("Error using reflection on " + fieldName, e);
                        }
                    }
                """;
        helperMethodsFormatter.formatHelperMethods();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new HelperMethodsFormatter(null, ControllerFieldInjectionType.FACTORY, ControllerMethodsInjectionType.REFERENCE, sb));
        assertThrows(NullPointerException.class, () -> new HelperMethodsFormatter(helperProvider, null, ControllerMethodsInjectionType.REFERENCE, sb));
        assertThrows(NullPointerException.class, () -> new HelperMethodsFormatter(helperProvider, ControllerFieldInjectionType.FACTORY, null, sb));
        assertThrows(NullPointerException.class, () -> new HelperMethodsFormatter(helperProvider, ControllerFieldInjectionType.FACTORY, ControllerMethodsInjectionType.REFERENCE, null));
    }
}
