package com.github.gtache.fxml.compiler.impl.internal;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.SequencedMap;

import static java.util.Objects.requireNonNull;

/**
 * Used by {@link ReflectionHelper} to store the constructor arguments
 *
 * @param constructor The constructor
 * @param namedArgs   The named arguments
 */
record ConstructorArgs(Constructor<?> constructor,
                       SequencedMap<String, Parameter> namedArgs) {

    /**
     * Instantiates new args
     *
     * @param constructor The constructor
     * @param namedArgs   The named args
     * @throws NullPointerException if any argument is null
     */
    ConstructorArgs {
        requireNonNull(constructor);
        namedArgs = Collections.unmodifiableSequencedMap(new LinkedHashMap<>(namedArgs));
    }
}
