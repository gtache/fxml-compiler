package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerMethodsInjectionType;

import java.util.Objects;

/**
 * Formats the helper methods for the generated code
 */
public final class HelperMethodsFormatter {

    private final HelperProvider helperProvider;
    private final ControllerFieldInjectionType fieldInjectionType;
    private final ControllerMethodsInjectionType methodInjectionType;
    private final StringBuilder sb;

    HelperMethodsFormatter(final HelperProvider helperProvider, final ControllerFieldInjectionType fieldInjectionType,
                           final ControllerMethodsInjectionType methodInjectionType, final StringBuilder sb) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.fieldInjectionType = Objects.requireNonNull(fieldInjectionType);
        this.methodInjectionType = Objects.requireNonNull(methodInjectionType);
        this.sb = Objects.requireNonNull(sb);
    }

    /**
     * Formats the helper methods
     */
    public void formatHelperMethods() {
        final var compatibilityHelper = helperProvider.getCompatibilityHelper();
        if (methodInjectionType == ControllerMethodsInjectionType.REFLECTION) {
            final var toList = compatibilityHelper.getToList();
            final var getFirst = compatibilityHelper.getGetFirst();
            final var startVariableMethodList = compatibilityHelper.getStartVar("java.util.List<java.lang.reflect.Method>", 0);
            sb.append("    private <T extends javafx.event.Event> void callEventHandlerMethod(final String methodName, final T event) {\n");
            sb.append("        try {\n");
            sb.append("            final java.lang.reflect.Method method;\n");
            sb.append("            ").append(startVariableMethodList).append("methods = java.util.Arrays.stream(controller.getClass().getDeclaredMethods())\n");
            sb.append("                    .filter(m -> m.getName().equals(methodName))").append(toList).append(";\n");
            sb.append("            if (methods.size() > 1) {\n");
            sb.append("                ").append(startVariableMethodList).append("eventMethods = methods.stream().filter(m ->\n");
            sb.append("                        m.getParameterCount() == 1 && javafx.event.Event.class.isAssignableFrom(m.getParameterTypes()[0]))").append(toList).append(";\n");
            sb.append("                if (eventMethods.size() == 1) {\n");
            sb.append("                    method = eventMethods").append(getFirst).append(";\n");
            sb.append("                } else {\n");
            sb.append("                    ").append(startVariableMethodList).append("emptyMethods = methods.stream().filter(m -> m.getParameterCount() == 0)").append(toList).append(";\n");
            sb.append("                    if (emptyMethods.size() == 1) {\n");
            sb.append("                        method = emptyMethods").append(getFirst).append(";\n");
            sb.append("                    } else {\n");
            sb.append("                        throw new IllegalArgumentException(\"Multiple matching methods for \" + methodName);\n");
            sb.append("                    }\n");
            sb.append("                }\n");
            sb.append("            } else if (methods.size() == 1) {\n");
            sb.append("                method = methods").append(getFirst).append(";\n");
            sb.append("            } else {\n");
            sb.append("                throw new IllegalArgumentException(\"No matching method for \" + methodName);\n");
            sb.append("            }\n");
            sb.append("            method.setAccessible(true);\n");
            sb.append("            if (method.getParameterCount() == 0) {\n");
            sb.append("                method.invoke(controller);\n");
            sb.append("            } else {\n");
            sb.append("                method.invoke(controller, event);\n");
            sb.append("            }\n");
            sb.append("        } catch (final IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {\n");
            sb.append("            throw new RuntimeException(\"Error using reflection on \" + methodName, ex);\n");
            sb.append("        }\n");
            sb.append("    }\n");
            sb.append("\n");
            sb.append("    private <T, U> U callCallbackMethod(final String methodName, final T value, final Class<T> clazz) {\n");
            sb.append("        try {\n");
            sb.append("            final java.lang.reflect.Method method;\n");
            sb.append("            ").append(startVariableMethodList).append("methods = java.util.Arrays.stream(controller.getClass().getDeclaredMethods())\n");
            sb.append("                    .filter(m -> m.getName().equals(methodName))").append(toList).append(";\n");
            sb.append("            if (methods.size() > 1) {\n");
            sb.append("                ").append(startVariableMethodList).append("eventMethods = methods.stream().filter(m ->\n");
            sb.append("                        m.getParameterCount() == 2 && clazz.isAssignableFrom(m.getParameterTypes()[1]))").append(toList).append(";\n");
            sb.append("                if (eventMethods.size() == 1) {\n");
            sb.append("                    method = eventMethods").append(getFirst).append(";\n");
            sb.append("                } else {\n");
            sb.append("                    throw new IllegalArgumentException(\"Multiple matching methods for \" + methodName);\n");
            sb.append("                }\n");
            sb.append("            } else if (methods.size() == 1) {\n");
            sb.append("                method = methods").append(getFirst).append(";\n");
            sb.append("            } else {\n");
            sb.append("                throw new IllegalArgumentException(\"No matching method for \" + methodName);\n");
            sb.append("            }\n");
            sb.append("            method.setAccessible(true);\n");
            sb.append("            return (U) method.invoke(controller, value);\n");
            sb.append("        } catch (final IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {\n");
            sb.append("            throw new RuntimeException(\"Error using reflection on \" + methodName, ex);\n");
            sb.append("        }\n");
            sb.append("    }\n");
        }
        if (fieldInjectionType == ControllerFieldInjectionType.REFLECTION) {
            sb.append("    private <T> void injectField(final String fieldName, final T object) {\n");
            sb.append("        try {\n");
            sb.append("            ").append(compatibilityHelper.getStartVar("java.lang.reflect.Field", 0)).append("field = controller.getClass().getDeclaredField(fieldName);\n");
            sb.append("            field.setAccessible(true);\n");
            sb.append("            field.set(controller, object);\n");
            sb.append("        } catch (final NoSuchFieldException | IllegalAccessException e) {\n");
            sb.append("            throw new RuntimeException(\"Error using reflection on \" + fieldName, e);\n");
            sb.append("        }\n");
            sb.append("    }\n");
        }
    }
}
