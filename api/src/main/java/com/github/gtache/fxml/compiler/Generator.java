package com.github.gtache.fxml.compiler;

/**
 * Generates compiled FXML code
 */
public interface Generator {

    /**
     * Generates the java code
     *
     * @param request The request
     * @return The java code
     */
    String generate(GenerationRequest request);
}
