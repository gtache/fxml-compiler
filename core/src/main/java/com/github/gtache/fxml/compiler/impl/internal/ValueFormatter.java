package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;

import java.util.regex.Pattern;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;

/**
 * Helper methods for {@link GeneratorImpl} to format values
 */
final class ValueFormatter {

    private static final Pattern INT_PATTERN = Pattern.compile("\\d+");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?");
    private static final Pattern START_BACKSLASH_PATTERN = Pattern.compile("^\\\\");

    private ValueFormatter() {
    }

    /**
     * Formats an argument to a method
     *
     * @param progress      The generation progress
     * @param value         The value
     * @param parameterType The parameter type
     * @return The formatted value
     * @throws GenerationException if an error occurs
     */
    static String getArg(final GenerationProgress progress, final String value, final Class<?> parameterType) throws GenerationException {
        if (parameterType == String.class && value.startsWith(RESOURCE_KEY_PREFIX)) {
            return getBundleValue(progress, value.substring(1));
        } else if (value.startsWith(RELATIVE_PATH_PREFIX)) {
            final var subpath = value.substring(1);
            return getResourceValue(subpath);
        } else if (value.startsWith(BINDING_EXPRESSION_PREFIX)) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else if (value.startsWith(EXPRESSION_PREFIX)) {
            final var variable = progress.idToVariableInfo().get(value.substring(1));
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
     * @param progress The generation progress
     * @param value    The value
     * @return The resource bundle value
     * @throws GenerationException if an error occurs
     */
    private static String getBundleValue(final GenerationProgress progress, final String value) throws GenerationException {
        final var resourceBundleInjectionType = progress.request().parameters().resourceInjectionType();
        if (resourceBundleInjectionType instanceof final ResourceBundleInjectionTypes types) {
            return switch (types) {
                case CONSTRUCTOR, GET_BUNDLE, CONSTRUCTOR_NAME -> "resourceBundle.getString(\"" + value + "\")";
                case GETTER -> "controller.resources().getString(\"" + value + "\")";
                case CONSTRUCTOR_FUNCTION -> "resourceBundleFunction.apply(\"" + value + "\")";
            };
        } else {
            throw new GenerationException("Unknown resource bundle injection type : " + resourceBundleInjectionType);
        }
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
            return "\"" + START_BACKSLASH_PATTERN.matcher(value).replaceAll("").replace("\"", "\\\"") + "\"";
        } else if (clazz == char.class || clazz == Character.class) {
            return "'" + value + "'";
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return value;
        } else if (clazz == byte.class || clazz == Byte.class || clazz == short.class || clazz == Short.class ||
                clazz == int.class || clazz == Integer.class || clazz == long.class || clazz == Long.class) {
            return intToString(value, clazz);
        } else if (clazz == float.class || clazz == Float.class || clazz == double.class || clazz == Double.class) {
            return decimalToString(value, clazz);
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
