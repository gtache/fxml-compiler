package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper methods for {@link GeneratorImpl} to handle objects constructors
 */
final class ConstructorHelper {


    private ConstructorHelper() {

    }

    /**
     * Gets the constructor arguments as a list of strings
     *
     * @param constructorArgs The constructor arguments
     * @param parsedObject    The parsed object
     * @return The list of constructor arguments
     * @throws GenerationException if an error occurs
     */
    static List<String> getListConstructorArgs(final ConstructorArgs constructorArgs, final ParsedObject parsedObject) throws GenerationException {
        final var args = new ArrayList<String>(constructorArgs.namedArgs().size());
        for (final var entry : constructorArgs.namedArgs().entrySet()) {
            final var parameter = entry.getValue();
            final var type = parameter.type();
            final var p = parsedObject.attributes().get(entry.getKey());
            if (p == null) {
                final var c = parsedObject.properties().entrySet().stream().filter(e ->
                        e.getKey().name().equals(entry.getKey())).findFirst().orElse(null);
                if (c == null) {
                    args.add(ValueFormatter.toString(parameter.defaultValue(), type));
                } else {
                    throw new GenerationException("Constructor using complex property not supported yet");
                }
            } else {
                args.add(ValueFormatter.toString(p.value(), type));
            }
        }
        return args;
    }

    /**
     * Gets the constructor arguments that best match the given properties
     *
     * @param constructors The constructors
     * @param properties   The mapping of properties name to possible types
     * @return The matching constructor arguments, or null if no constructor matches and no default constructor exists
     */
    static ConstructorArgs getMatchingConstructorArgs(final Constructor<?>[] constructors, final Map<String, List<Class<?>>> properties) {
        final var argsDistance = getArgsDistance(constructors, properties);
        final var distances = argsDistance.keySet().stream().sorted().toList();
        for (final var distance : distances) {
            final var matching = argsDistance.get(distance);
            final var argsTypeDistance = getArgsTypeDistance(matching, properties);
            if (!argsTypeDistance.isEmpty()) {
                return argsTypeDistance.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue)
                        .map(s -> s.iterator().next()).findFirst().orElseThrow(() -> new IllegalStateException("Shouldn't happen"));
            }
        }
        //No matching constructor
        return Arrays.stream(constructors).filter(c -> c.getParameterCount() == 0).findFirst()
                .map(c -> new ConstructorArgs(c, new LinkedHashMap<>())).orElse(null);
    }

    /**
     * Computes the mapping of distance (difference between number of properties and number of matching arguments) to constructor arguments
     *
     * @param constructors The constructors
     * @param properties   The object properties
     * @return The mapping
     */
    private static Map<Long, Set<ConstructorArgs>> getArgsDistance(final Constructor<?>[] constructors, final Map<String, List<Class<?>>> properties) {
        final var argsDistance = HashMap.<Long, Set<ConstructorArgs>>newHashMap(constructors.length);
        for (final var constructor : constructors) {
            final var constructorArgs = ReflectionHelper.getConstructorArgs(constructor);
            final var matchingArgsCount = getMatchingArgsCount(constructorArgs, properties);
            if (matchingArgsCount != 0) {
                final var difference = Math.abs(constructorArgs.namedArgs().size() - matchingArgsCount);
                argsDistance.computeIfAbsent(difference, d -> new HashSet<>()).add(constructorArgs);
            }
        }
        return argsDistance;
    }

    /**
     * Computes the mapping of type distance (the total of difference between best matching property type and constructor argument type) to constructor arguments.
     * Also filters out constructors that don't match the properties
     *
     * @param matching   The matching constructor arguments
     * @param properties The object properties
     * @return The mapping
     */
    private static Map<Long, Set<ConstructorArgs>> getArgsTypeDistance(final Collection<ConstructorArgs> matching, final Map<String, ? extends List<Class<?>>> properties) {
        final var argsTypeDistance = HashMap.<Long, Set<ConstructorArgs>>newHashMap(matching.size());
        for (final var constructorArgs : matching) {
            final var typeDistance = getTypeDistance(constructorArgs, properties);
            if (typeDistance >= 0) {
                //Valid constructor
                argsTypeDistance.computeIfAbsent(typeDistance, d -> new HashSet<>()).add(constructorArgs);
            }
        }
        return argsTypeDistance;
    }

    /**
     * Calculates the type distance between the constructor arguments and the properties
     *
     * @param constructorArgs The constructor arguments
     * @param properties      The object properties
     * @return The type distance
     */
    private static long getTypeDistance(final ConstructorArgs constructorArgs, final Map<String, ? extends List<Class<?>>> properties) {
        var typeDistance = 0L;
        for (final var namedArg : constructorArgs.namedArgs().entrySet()) {
            final var name = namedArg.getKey();
            final var parameter = namedArg.getValue();
            final var type = parameter.type();
            final var property = properties.get(name);
            if (property != null) {
                var distance = -1L;
                for (var i = 0; i < property.size(); i++) {
                    final var clazz = property.get(i);
                    if (clazz.isAssignableFrom(type)) {
                        distance = i;
                        break;
                    }
                }
                if (distance < 0) {
                    return -1;
                } else {
                    typeDistance += distance;
                }
            }
        }
        return typeDistance;
    }

    /**
     * Checks how many arguments of the given constructor match the given properties
     *
     * @param constructorArgs The constructor arguments
     * @param properties      The mapping of properties name to expected type
     * @return The number of matching arguments
     */
    private static long getMatchingArgsCount(final ConstructorArgs constructorArgs, final Map<String, List<Class<?>>> properties) {
        return constructorArgs.namedArgs().keySet().stream().filter(properties::containsKey).count();
    }


}
