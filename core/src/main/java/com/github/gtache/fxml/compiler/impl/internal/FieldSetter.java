package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.EXPRESSION_PREFIX;

/**
 * Helper methods for {@link GeneratorImpl} to set fields
 */
final class FieldSetter {

    private FieldSetter() {
    }

    /**
     * Sets an event handler field
     *
     * @param progress       The generation progress
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @throws GenerationException if an error occurs@
     */
    static void setEventHandler(final GenerationProgress progress, final ParsedProperty property, final String parentVariable) throws GenerationException {
        setField(progress, property, parentVariable, "javafx.event.EventHandler");
    }


    /**
     * Sets a field
     *
     * @param progress       The generation progress
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @param fieldType      The field type
     * @throws GenerationException if an error occurs
     */
    static void setField(final GenerationProgress progress, final ParsedProperty property, final String parentVariable, final String fieldType) throws GenerationException {
        final var fieldInjectionType = progress.request().parameters().fieldInjectionType();
        if (fieldInjectionType instanceof final ControllerFieldInjectionTypes fieldTypes) {
            switch (fieldTypes) {
                case ASSIGN -> setAssign(progress, property, parentVariable);
                case FACTORY -> setFactory(progress, property, parentVariable);
                case SETTERS -> setSetter(progress, property, parentVariable);
                case REFLECTION -> setReflection(progress, property, parentVariable, fieldType);
            }
        } else {
            throw new GenerationException("Unknown injection type : " + fieldInjectionType);
        }
    }

    private static void setAssign(final GenerationProgress progress, final ParsedProperty property, final String parentVariable) {
        final var methodName = GenerationHelper.getSetMethod(property);
        final var value = property.value().replace(EXPRESSION_PREFIX, "");
        progress.stringBuilder().append("        ").append(parentVariable).append(".").append(methodName).append("(").append(value).append(");\n");
    }

    private static void setFactory(final GenerationProgress progress, final ParsedProperty property, final String parentVariable) {
        progress.controllerFactoryPostAction().add(getSetString(property, parentVariable));
    }

    private static void setSetter(final GenerationProgress progress, final ParsedProperty property, final String parentVariable) {
        progress.stringBuilder().append(getSetString(property, parentVariable));
    }

    private static String getSetString(final ParsedProperty property, final String parentVariable) {
        final var methodName = GenerationHelper.getSetMethod(property);
        final var value = property.value().replace(EXPRESSION_PREFIX, "");
        final var split = value.split("\\.");
        final var getterName = GenerationHelper.getGetMethod(split[1]);
        return "        " + parentVariable + "." + methodName + "(" + split[0] + "." + getterName + ");\n";
    }

    private static void setReflection(final GenerationProgress progress, final ParsedProperty property, final String parentVariable, final String fieldType) {
        final var methodName = GenerationHelper.getSetMethod(property);
        final var value = property.value().replace(EXPRESSION_PREFIX, "");
        final var split = value.split("\\.");
        final var fieldName = split[1];
        final var sb = progress.stringBuilder();
        sb.append("        try {\n");
        sb.append("            ").append(GenerationCompatibilityHelper.getStartVar(progress, "java.lang.reflect.Field", 0)).append("field = controller.getClass().getDeclaredField(\"").append(fieldName).append("\");\n");
        sb.append("            field.setAccessible(true);\n");
        sb.append("            final var value = (").append(fieldType).append(") field.get(controller);\n");
        sb.append("            ").append(parentVariable).append(".").append(methodName).append("(value);\n");
        sb.append("        } catch (final NoSuchFieldException | IllegalAccessException e) {\n");
        sb.append("            throw new RuntimeException(e);\n");
        sb.append("        }\n");
    }

}
