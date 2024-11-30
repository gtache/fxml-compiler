package com.github.gtache.fxml.compiler;

/**
 * Generates compiled FXML code
 */
@FunctionalInterface
public interface Generator {

    /**
     * Generates the java code
     *
     * @param request The request
     * @return The java code
     * @throws GenerationException if an error occurs
     */
    String generate(GenerationRequest request) throws GenerationException;
}
