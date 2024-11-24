package com.github.gtache.fxml.compiler;

/**
 * Exception thrown when a generation error occurs
 */
public class GenerationException extends Exception {

    /**
     * Instantiates a new GenerationException
     *
     * @param message The message
     */
    public GenerationException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new GenerationException
     *
     * @param message The message
     * @param cause   The cause
     */
    public GenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new GenerationException
     *
     * @param cause The cause
     */
    public GenerationException(final Throwable cause) {
        super(cause);
    }
}
