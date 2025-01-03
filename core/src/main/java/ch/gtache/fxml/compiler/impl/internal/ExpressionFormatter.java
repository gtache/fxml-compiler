package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.ControllerFieldInjectionType;
import ch.gtache.fxml.compiler.GenerationException;
import javafx.beans.property.ReadOnlyProperty;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * Formats binding expressions
 */
class ExpressionFormatter {
    private static final String PROPERTY_METHOD = "Property()";

    private final HelperProvider helperProvider;
    private final ControllerFieldInjectionType fieldInjectionType;
    private final StringBuilder sb;

    /**
     * Instantiates a new Expression formatter
     *
     * @param helperProvider     The helper provider
     * @param fieldInjectionType The field injection type
     * @param sb                 The string builder
     */
    ExpressionFormatter(final HelperProvider helperProvider, final ControllerFieldInjectionType fieldInjectionType, final StringBuilder sb) {
        this.helperProvider = requireNonNull(helperProvider);
        this.fieldInjectionType = requireNonNull(fieldInjectionType);
        this.sb = requireNonNull(sb);
    }

    /**
     * Formats a binding expression
     *
     * @param expression The expression
     * @param returnType The return type
     * @return The argument to pass to the bind method (e.g. a variable, a method call...)
     * @throws GenerationException If an error occurs
     */
    String format(final String expression, final Class<?> returnType) throws GenerationException {
        final var cleaned = expression.substring(2, expression.length() - 1).trim();
        if (cleaned.contains(".")) {
            //Reference to the property of an object
            return getDotExpression(expression, returnType);
        } else {
            //Simple reference to an id
            return getNonDotExpression(expression);
        }
    }

    private String getDotExpression(final String expression, final Class<?> returnType) throws GenerationException {
        final var cleaned = expression.substring(2, expression.length() - 1).trim();
        final var split = Arrays.stream(cleaned.split("\\.")).filter(s -> !s.isEmpty()).toList();
        if (split.size() == 2) {
            final var referenced = split.get(0);
            final var value = split.get(1);
            //Checks if it is a reference to the controller
            if (referenced.equals("controller")) {
                return getControllerExpression(value, returnType);
            } else {
                return getNonControllerExpression(referenced, value);
            }
        } else {
            //Only supports one level
            throw new GenerationException("Unsupported binding : " + expression);
        }
    }

    private String getNonDotExpression(final String expression) throws GenerationException {
        final var cleaned = expression.substring(2, expression.length() - 1).trim();
        final var info = helperProvider.getVariableProvider().getVariableInfo(cleaned);
        if (info == null) {
            throw new GenerationException("Unknown variable : " + cleaned);
        } else {
            return info.variableName();
        }
    }

    private String getControllerExpression(final String value, final Class<?> returnType) {
        return switch (fieldInjectionType) {
            case REFLECTION -> getControllerReflectionExpression(value, returnType);
            case SETTERS, FACTORY -> "controller." + value + PROPERTY_METHOD;
            case ASSIGN -> "controller." + value;
        };
    }

    private String getControllerReflectionExpression(final String value, final Class<?> returnType) {
        final var startVar = helperProvider.getCompatibilityHelper().getStartVar(returnType.getName());
        final var variable = helperProvider.getVariableProvider().getNextVariableName("binding");
        sb.append(startVar).append(variable).append(";\n");
        sb.append("        try {\n");
        sb.append("            ").append(helperProvider.getCompatibilityHelper().getStartVar("java.lang.reflect.Field", 0))
                .append("field = controller.getClass().getDeclaredField(\"").append(value).append("\");\n");
        sb.append("            field.setAccessible(true);\n");
        sb.append("            ").append(variable).append(" = (").append(returnType.getName()).append(") field.get(controller);\n");
        sb.append("        } catch (final NoSuchFieldException | IllegalAccessException e) {\n");
        sb.append("            throw new RuntimeException(e);\n");
        sb.append("        }\n");
        return variable;
    }

    private String getNonControllerExpression(final String referenced, final String value) throws GenerationException {
        final var info = helperProvider.getVariableProvider().getVariableInfo(referenced);
        if (info == null) {
            throw new GenerationException("Unknown variable : " + referenced);
        } else {
            final var hasReadProperty = ReadOnlyProperty.class.isAssignableFrom(ReflectionHelper.getReturnType(info.className(), value + "Property"));
            if (hasReadProperty) {
                return info.variableName() + "." + value + PROPERTY_METHOD;
            } else {
                throw new GenerationException("Cannot read " + value + " on " + info.className());
            }
        }
    }

}
