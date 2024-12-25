package com.github.gtache.fxml.compiler.impl.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provider of variable names and info
 */
class VariableProvider {

    private final Map<String, AtomicInteger> variableNameCounters;
    private final Map<String, VariableInfo> idToVariableInfo;

    /**
     * Instantiates a new provider
     */
    VariableProvider() {
        this.variableNameCounters = new HashMap<>();
        this.idToVariableInfo = new HashMap<>();
    }

    /**
     * Gets the next available variable name for the given prefix
     *
     * @param prefix The variable name prefix
     * @return The next available variable name
     */
    String getNextVariableName(final String prefix) {
        final var counter = variableNameCounters.computeIfAbsent(prefix, k -> new AtomicInteger(0));
        return prefix + counter.getAndIncrement();
    }

    /**
     * Adds a variable info
     * @param id The variable id
     * @param variableInfo The variable info
     */
    void addVariableInfo(final String id, final VariableInfo variableInfo) {
        idToVariableInfo.put(id, variableInfo);
    }

    /**
     * Gets the variable info
     * @param id The variable id
     * @return The variable info
     */
    VariableInfo getVariableInfo(final String id) {
        return idToVariableInfo.get(id);
    }
}
