package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used by {@link GeneratorImpl} to track the generation progress
 *
 * @param request                     The generation request
 * @param idToVariableInfo            The id to variable info
 * @param variableNameCounters        The variable name counters for variable name generation
 * @param controllerClassToVariable   The controller class to variable mapping
 * @param controllerFactoryPostAction The controller factory post action for factory injection
 * @param stringBuilder               The string builder
 */
public record GenerationProgress(GenerationRequest request, Map<String, VariableInfo> idToVariableInfo,
                                 Map<String, AtomicInteger> variableNameCounters,
                                 SequencedMap<String, String> controllerClassToVariable,
                                 SequencedCollection<String> controllerFactoryPostAction,
                                 StringBuilder stringBuilder) {

    /**
     * Instantiates a new GenerationProgress
     *
     * @param request                     The generation request
     * @param idToVariableInfo            The id to variable info mapping
     * @param variableNameCounters        The variable name counters
     * @param controllerClassToVariable   The controller class to variable mapping
     * @param controllerFactoryPostAction The controller factory post action
     * @param stringBuilder               The string builder
     * @throws NullPointerException if any parameter is null
     */
    public GenerationProgress {
        Objects.requireNonNull(request);
        Objects.requireNonNull(idToVariableInfo);
        Objects.requireNonNull(variableNameCounters);
        Objects.requireNonNull(controllerClassToVariable);
        Objects.requireNonNull(controllerFactoryPostAction);
        Objects.requireNonNull(stringBuilder);
    }

    /**
     * Instantiates a new GenerationProgress
     *
     * @param request The generation request
     * @throws NullPointerException if request is null
     */
    public GenerationProgress(final GenerationRequest request) {
        this(request, new HashMap<>(), new HashMap<>(), new LinkedHashMap<>(), new ArrayList<>(), new StringBuilder());
    }

    /**
     * Gets the next available variable name for the given prefix
     *
     * @param prefix The variable name prefix
     * @return The next available variable name
     */
    public String getNextVariableName(final String prefix) {
        final var counter = variableNameCounters.computeIfAbsent(prefix, k -> new AtomicInteger(0));
        return prefix + counter.getAndIncrement();
    }
}
