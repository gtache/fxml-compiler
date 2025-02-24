package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.ControllerFieldInjectionType;
import ch.gtache.fxml.compiler.GenerationException;
import ch.gtache.fxml.compiler.impl.GeneratorImpl;
import ch.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.Objects;
import java.util.SequencedCollection;

import static ch.gtache.fxml.compiler.impl.internal.GenerationHelper.EXPRESSION_PREFIX;
import static ch.gtache.fxml.compiler.impl.internal.GenerationHelper.INDENT_8;
import static java.util.Objects.requireNonNull;

/**
 * Helper methods for {@link GeneratorImpl} to set nodes properties using controller's fields
 */
final class FieldSetter {

    private static final String CONTROLLER = "controller";
    private final HelperProvider helperProvider;
    private final ControllerFieldInjectionType fieldInjectionType;
    private final StringBuilder sb;
    private final SequencedCollection<String> controllerFactoryPostAction;

    FieldSetter(final HelperProvider helperProvider, final ControllerFieldInjectionType fieldInjectionType,
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
     * @throws GenerationException if an error occurs
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
        switch (fieldInjectionType) {
            case ASSIGN -> setAssign(property, parentVariable);
            case FACTORY -> setFactory(property, parentVariable);
            case REFLECTION -> setReflection(property, parentVariable, fieldType);
            case SETTERS -> setSetter(property, parentVariable);
        }
    }

    private void setAssign(final ParsedProperty property, final String parentVariable) throws GenerationException {
        final var methodName = GenerationHelper.getSetMethod(property);
        final var value = property.value().replace(EXPRESSION_PREFIX, "");
        final var split = value.split("\\.");
        final var holderName = split[0];
        if (Objects.equals(holderName, CONTROLLER)) {
            sb.append(INDENT_8).append(parentVariable).append(".").append(methodName).append("(").append(value).append(");\n");
        } else {
            throw unexpectedHolderException(holderName);
        }
    }

    private void setFactory(final ParsedProperty property, final String parentVariable) throws GenerationException {
        controllerFactoryPostAction.add(getSetString(property, parentVariable));
    }

    private void setSetter(final ParsedProperty property, final String parentVariable) throws GenerationException {
        sb.append(getSetString(property, parentVariable));
    }

    private static String getSetString(final ParsedProperty property, final String parentVariable) throws GenerationException {
        //If field injection is setter or factory, assume controller has getters
        final var methodName = GenerationHelper.getSetMethod(property);
        final var value = property.value().replace(EXPRESSION_PREFIX, "");
        final var split = value.split("\\.");
        final var holderName = split[0];
        if (Objects.equals(holderName, CONTROLLER)) {
            final var getterName = GenerationHelper.getGetMethod(split[1]);
            return INDENT_8 + parentVariable + "." + methodName + "(" + CONTROLLER + "." + getterName + "());\n";
        } else {
            throw unexpectedHolderException(holderName);
        }
    }

    private void setReflection(final ParsedProperty property, final String parentVariable, final String fieldType) throws GenerationException {
        final var methodName = GenerationHelper.getSetMethod(property);
        final var value = property.value().replace(EXPRESSION_PREFIX, "");
        final var split = value.split("\\.");
        final var holderName = split[0];
        if (Objects.equals(holderName, CONTROLLER)) {
            final var fieldName = split[1];
            sb.append("        try {\n");
            sb.append("            ").append(helperProvider.getCompatibilityHelper().getStartVar("java.lang.reflect.Field", 0))
                    .append("field = ").append(CONTROLLER).append(".getClass().getDeclaredField(\"").append(fieldName).append("\");\n");
            sb.append("            field.setAccessible(true);\n");
            sb.append("            final var value = (").append(fieldType).append(") field.get(").append(CONTROLLER).append(");\n");
            sb.append("            ").append(parentVariable).append(".").append(methodName).append("(value);\n");
            sb.append("        } catch (final NoSuchFieldException | IllegalAccessException e) {\n");
            sb.append("            throw new RuntimeException(e);\n");
            sb.append("        }\n");
        } else {
            throw unexpectedHolderException(holderName);
        }
    }

    private static GenerationException unexpectedHolderException(final String holderName) {
        return new GenerationException("Unexpected variable holder : " + holderName + " ; expected : " + CONTROLLER);
    }

}
