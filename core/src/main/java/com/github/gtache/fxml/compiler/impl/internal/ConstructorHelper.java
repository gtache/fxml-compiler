package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Helper methods for {@link GeneratorImpl} to handle objects constructors
 */
final class ConstructorHelper {

    private final HelperProvider helperProvider;

    ConstructorHelper(final HelperProvider helperProvider) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
    }

    /**
     * Gets the constructor arguments as a list of strings
     *
     * @param constructorArgs The constructor arguments
     * @param parsedObject    The parsed object
     * @return The list of constructor arguments
     * @throws GenerationException if an error occurs
     */
    List<String> getListConstructorArgs(final ConstructorArgs constructorArgs, final ParsedObject parsedObject) throws GenerationException {
        final var args = new ArrayList<String>(constructorArgs.namedArgs().size());
        final var valueFormatter = helperProvider.getValueFormatter();
        for (final var entry : constructorArgs.namedArgs().entrySet()) {
            final var type = entry.getValue().type();
            final var p = parsedObject.attributes().get(entry.getKey());
            if (p == null) {
                final var c = parsedObject.properties().entrySet().stream().filter(e ->
                        e.getKey().name().equals(entry.getKey())).findFirst().orElse(null);
                if (c == null) {
                    args.add(valueFormatter.toString(entry.getValue().defaultValue(), type));
                } else {
                    throw new GenerationException("Constructor using complex property not supported yet");
                }
            } else {
                args.add(valueFormatter.toString(p.value(), type));
            }
        }
        return args;
    }

    /**
     * Gets the constructor arguments that best match the given property names
     *
     * @param constructors     The constructors
     * @param allPropertyNames The property names
     * @return The matching constructor arguments, or null if no constructor matches and no default constructor exists
     */
    static ConstructorArgs getMatchingConstructorArgs(final Constructor<?>[] constructors, final Set<String> allPropertyNames) {
        ConstructorArgs matchingConstructorArgs = null;
        for (final var constructor : constructors) {
            final var constructorArgs = ReflectionHelper.getConstructorArgs(constructor);
            final var matchingArgsCount = getMatchingArgsCount(constructorArgs, allPropertyNames);
            if (matchingConstructorArgs == null ? matchingArgsCount > 0 : matchingArgsCount > getMatchingArgsCount(matchingConstructorArgs, allPropertyNames)) {
                matchingConstructorArgs = constructorArgs;
            }
        }
        if (matchingConstructorArgs == null) {
            return Arrays.stream(constructors).filter(c -> c.getParameterCount() == 0).findFirst().map(c -> new ConstructorArgs(c, new LinkedHashMap<>())).orElse(null);
        } else {
            return matchingConstructorArgs;
        }
    }

    /**
     * Checks how many arguments of the given constructor match the given property names
     *
     * @param constructorArgs  The constructor arguments
     * @param allPropertyNames The property names
     * @return The number of matching arguments
     */
    private static long getMatchingArgsCount(final ConstructorArgs constructorArgs, final Set<String> allPropertyNames) {
        return constructorArgs.namedArgs().keySet().stream().filter(allPropertyNames::contains).count();
    }
}
