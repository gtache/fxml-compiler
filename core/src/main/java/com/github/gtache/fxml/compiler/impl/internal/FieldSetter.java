package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.SequencedCollection;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.EXPRESSION_PREFIX;
import static java.util.Objects.requireNonNull;

/**
 * Helper methods for {@link GeneratorImpl} to set fields
 */
final class FieldSetter {

    private final HelperProvider helperProvider;
    private final InjectionType fieldInjectionType;
    private final StringBuilder sb;
    private final SequencedCollection<String> controllerFactoryPostAction;

    FieldSetter(final HelperProvider helperProvider, final InjectionType fieldInjectionType,
                final StringBuilder sb, final SequencedCollection<String> controllerFactoryPostAction) {
        this.helperProvider = requireNonNull(helperProvider);
        this.fieldInjectionType = requireNonNull(fieldInjectionType);
        this.sb = requireNonNull(sb);
        this.controllerFactoryPostAction = requireNonNull(controllerFactoryPostAction);
    }

    /**
     * Sets an event handler field
     *
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @throws GenerationException if an error occurs@
     */
    void setEventHandler(final ParsedProperty property, final String parentVariable) throws GenerationException {
        setField(property, parentVariable, "javafx.event.EventHandler");
    }


    /**
     * Sets a field
     *
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @param fieldType      The field type
     * @throws GenerationException if an error occurs
     */
    void setField(final ParsedProperty property, final String parentVariable, final String fieldType) throws GenerationException {
        if (fieldInjectionType instanceof final ControllerFieldInjectionTypes fieldTypes) {
            switch (fieldTypes) {
                case ASSIGN -> setAssign(property, parentVariable);
                case FACTORY -> setFactory(property, parentVariable);
                case SETTERS -> setSetter(property, parentVariable);
                case REFLECTION -> setReflection(property, parentVariable, fieldType);
            }
        } else {
            throw new GenerationException("Unknown injection type : " + fieldInjectionType);
        }
    }

    private void setAssign(final ParsedProperty property, final String parentVariable) {
        final var methodName = GenerationHelper.getSetMethod(property);
        final var value = property.value().replace(EXPRESSION_PREFIX, "");
        sb.append("        ").append(parentVariable).append(".").append(methodName).append("(").append(value).append(");\n");
    }

    private void setFactory(final ParsedProperty property, final String parentVariable) {
        controllerFactoryPostAction.add(getSetString(property, parentVariable));
    }

    private void setSetter(final ParsedProperty property, final String parentVariable) {
        sb.append(getSetString(property, parentVariable));
    }

    private static String getSetString(final ParsedProperty property, final String parentVariable) {
        final var methodName = GenerationHelper.getSetMethod(property);
        final var value = property.value().replace(EXPRESSION_PREFIX, "");
        final var split = value.split("\\.");
        final var getterName = GenerationHelper.getGetMethod(split[1]);
        return "        " + parentVariable + "." + methodName + "(" + split[0] + "." + getterName + ");\n";
    }

    private void setReflection(final ParsedProperty property, final String parentVariable, final String fieldType) {
        final var methodName = GenerationHelper.getSetMethod(property);
        final var value = property.value().replace(EXPRESSION_PREFIX, "");
        final var split = value.split("\\.");
        final var fieldName = split[1];
        sb.append("        try {\n");
        sb.append("            ").append(helperProvider.getCompatibilityHelper().getStartVar("java.lang.reflect.Field", 0))
                .append("field = controller.getClass().getDeclaredField(\"").append(fieldName).append("\");\n");
        sb.append("            field.setAccessible(true);\n");
        sb.append("            final var value = (").append(fieldType).append(") field.get(controller);\n");
        sb.append("            ").append(parentVariable).append(".").append(methodName).append("(value);\n");
        sb.append("        } catch (final NoSuchFieldException | IllegalAccessException e) {\n");
        sb.append("            throw new RuntimeException(e);\n");
        sb.append("        }\n");
    }

}
