package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.GenerationException;
import ch.gtache.fxml.compiler.ResourceBundleInjectionType;
import ch.gtache.fxml.compiler.impl.GeneratorImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import static ch.gtache.fxml.compiler.impl.internal.GenerationHelper.*;
import static java.util.Objects.requireNonNull;

/**
 * Helper methods for {@link GeneratorImpl} to format values
 */
final class ValueFormatter {

    private static final Pattern INT_PATTERN = Pattern.compile("\\d+");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?");
    private static final Pattern START_BACKSLASH_PATTERN = Pattern.compile("^\\\\");

    private final HelperProvider helperProvider;
    private final ResourceBundleInjectionType resourceInjectionType;

    ValueFormatter(final HelperProvider helperProvider, final ResourceBundleInjectionType resourceInjectionType) {
        this.helperProvider = requireNonNull(helperProvider);
        this.resourceInjectionType = requireNonNull(resourceInjectionType);
    }

    /**
     * Formats an argument to a method
     *
     * @param value         The value
     * @param parameterType The parameter type
     * @return The formatted value
     * @throws GenerationException if an error occurs
     */
    String getArg(final String value, final Class<?> parameterType) throws GenerationException {
        if (parameterType == String.class && value.startsWith(RESOURCE_KEY_PREFIX)) {
            return getBundleValue(value.substring(1));
        } else if (value.startsWith(RELATIVE_PATH_PREFIX)) {
            final var subpath = value.substring(1);
            return getResourceValue(subpath);
        } else if (value.startsWith(BINDING_EXPRESSION_PREFIX)) {
            throw new GenerationException("Should be handled by BindingFormatter");
        } else if (value.startsWith(BIDIRECTIONAL_BINDING_PREFIX)) {
            throw new GenerationException("Should be handled by BindingFormatter");
        } else if (value.startsWith(EXPRESSION_PREFIX)) {
            final var variable = helperProvider.getVariableProvider().getVariableInfo(value.substring(1));
            if (variable == null) {
                throw new GenerationException("Unknown variable : " + value.substring(1));
            }
            return variable.variableName();
        } else {
            return toString(value, parameterType);
        }
    }

    private static String getResourceValue(final String subpath) {
        return "getClass().getResource(\"" + subpath + "\").toString()";
    }

    /**
     * Gets the resource bundle value for the given value
     *
     * @param value The value
     * @return The resource bundle value
     */
    private String getBundleValue(final String value) {
        return switch (resourceInjectionType) {
            case CONSTRUCTOR, GET_BUNDLE, CONSTRUCTOR_NAME -> "resourceBundle.getString(\"" + value + "\")";
            case GETTER -> "controller.resources().getString(\"" + value + "\")";
            case CONSTRUCTOR_FUNCTION -> "resourceBundleFunction.apply(\"" + value + "\")";
        };
    }


    /**
     * Computes the string value to use in the generated code
     *
     * @param value The value
     * @param clazz The value class
     * @return The computed string value
     */
    static String toString(final String value, final Class<?> clazz) {
        if (clazz == String.class) {
            return "\"" + START_BACKSLASH_PATTERN.matcher(value).replaceAll("").replace("\\", "\\\\")
                    .replace("\"", "\\\"") + "\"";
        } else if (clazz == char.class || clazz == Character.class) {
            return "'" + value + "'";
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return value;
        } else if (clazz == byte.class || clazz == Byte.class || clazz == short.class || clazz == Short.class ||
                clazz == int.class || clazz == Integer.class || clazz == long.class || clazz == Long.class) {
            return intToString(value, clazz);
        } else if (clazz == float.class || clazz == Float.class || clazz == double.class || clazz == Double.class) {
            return decimalToString(value, clazz);
        } else if (clazz == LocalDate.class) {
            return "LocalDate.parse(\"" + value + "\")";
        } else if (clazz == LocalDateTime.class) {
            return "LocalDateTime.parse(\"" + value + "\")";
        } else if (ReflectionHelper.hasValueOf(clazz)) {
            return valueOfToString(value, clazz);
        } else {
            return value;
        }
    }

    private static String intToString(final String value, final Class<?> clazz) {
        if (INT_PATTERN.matcher(value).matches()) {
            return value;
        } else {
            return getValueOf(ReflectionHelper.getWrapperClass(clazz), value);
        }
    }

    private static String decimalToString(final String value, final Class<?> clazz) {
        if (DECIMAL_PATTERN.matcher(value).matches()) {
            return value;
        } else {
            return getValueOf(ReflectionHelper.getWrapperClass(clazz), value);
        }
    }

    private static String valueOfToString(final String value, final Class<?> clazz) {
        if (clazz.isEnum()) {
            return clazz.getCanonicalName() + "." + value;
        } else {
            return getValueOf(clazz.getCanonicalName(), value);
        }
    }

    private static String getValueOf(final String clazz, final String value) {
        return clazz + ".valueOf(\"" + value + "\")";
    }
}
