package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.GenerationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Guesses the class of a value
 */
class ValueClassGuesser {
    private final HelperProvider helperProvider;

    ValueClassGuesser(final HelperProvider helperProvider) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
    }

    List<Class<?>> guess(final String value) throws GenerationException {
        if (value.startsWith("$")) {
            return getPossibleVariableTypes(value.substring(1));
        } else {
            return getPossibleTypes(value);
        }
    }

    private List<Class<?>> getPossibleVariableTypes(final String value) throws GenerationException {
        if (value.contains(".")) {
            throw new GenerationException("Unsupported variable : " + value);
        } else {
            final var variableInfo = helperProvider.getVariableProvider().getVariableInfo(value);
            if (variableInfo == null) {
                throw new GenerationException("Unknown variable : " + value);
            } else {
                return List.of(ReflectionHelper.getClass(variableInfo.className()));
            }
        }
    }

    private static List<Class<?>> getPossibleTypes(final String value) {
        final var ret = new ArrayList<Class<?>>();
        ret.add(String.class);
        ret.addAll(tryParse(value, LocalDateTime::parse, LocalDateTime.class));
        ret.addAll(tryParse(value, LocalDate::parse, LocalDate.class));
        ret.addAll(tryParse(value, ValueClassGuesser::parseBoolean, Boolean.class, boolean.class));
        ret.addAll(tryParse(value, BigDecimal::new, BigDecimal.class));
        ret.addAll(tryParse(value, Double::parseDouble, Double.class, double.class));
        ret.addAll(tryParse(value, Float::parseFloat, Float.class, float.class));
        ret.addAll(tryParse(value, BigInteger::new, BigInteger.class));
        ret.addAll(tryParse(value, Long::parseLong, Long.class, long.class));
        ret.addAll(tryParse(value, Integer::parseInt, Integer.class, int.class));
        ret.addAll(tryParse(value, Short::parseShort, Short.class, short.class));
        ret.addAll(tryParse(value, Byte::parseByte, Byte.class, byte.class));
        return ret.reversed();
    }

    private static boolean parseBoolean(final String value) {
        if (!value.equals("true") && !value.equals("false")) {
            throw new RuntimeException("Invalid boolean value : " + value);
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    private static <T> Collection<Class<?>> tryParse(final String value, final Function<? super String, T> parseFunction, final Class<?>... classes) {
        try {
            parseFunction.apply(value);
            return Arrays.asList(classes);
        } catch (final RuntimeException ignored) {
            //Do nothing
            return List.of();
        }
    }
}
