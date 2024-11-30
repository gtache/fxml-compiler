package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;

import java.util.regex.Pattern;

import static com.github.gtache.fxml.compiler.impl.internal.ReflectionHelper.getWrapperClass;
import static com.github.gtache.fxml.compiler.impl.internal.ReflectionHelper.hasValueOf;

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
        if (parameterType == String.class && value.startsWith("%")) {
            return getBundleValue(progress, value.substring(1));
        } else if (value.startsWith("@")) {
            final var subpath = value.substring(1);
            return getResourceValue(subpath);
        } else if (value.startsWith("${")) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else if (value.startsWith("$")) {
            final var variable = progress.idToVariableName().get(value.substring(1));
            if (variable == null) {
                throw new GenerationException("Unknown variable : " + value.substring(1));
            }
            return variable;
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
        final var resourceBundleInjectionType = progress.request().parameters().resourceBundleInjection().injectionType();
        if (resourceBundleInjectionType instanceof final ResourceBundleInjectionTypes types) {
            return switch (types) {
                case CONSTRUCTOR, GET_BUNDLE -> "bundle.getString(\"" + value + "\")";
                case GETTER -> "controller.resources().getString(\"" + value + "\")";
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
            if (INT_PATTERN.matcher(value).matches()) {
                return value;
            } else {
                return getValueOf(getWrapperClass(clazz), value);
            }
        } else if (clazz == float.class || clazz == Float.class || clazz == double.class || clazz == Double.class) {
            if (DECIMAL_PATTERN.matcher(value).matches()) {
                return value;
            } else {
                return getValueOf(getWrapperClass(clazz), value);
            }
        } else if (hasValueOf(clazz)) {
            if (clazz.isEnum()) {
                return clazz.getCanonicalName() + "." + value;
            } else {
                return getValueOf(clazz.getCanonicalName(), value);
            }
        } else {
            return value;
        }
    }

    private static String getValueOf(final String clazz, final String value) {
        return clazz + ".valueOf(\"" + value + "\")";
    }
}
