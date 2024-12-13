package com.github.gtache.fxml.compiler.parsing;

/**
 * Exception thrown when a parsing error occurs
 */
public class ParseException extends Exception {

    /**
     * Instantiates a new exception
     *
     * @param message The message
     */
    public ParseException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new exception
     *
     * @param message The message
     * @param cause   The cause
     */
    public ParseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new exception
     *
     * @param cause The cause
     */
    public ParseException(final Throwable cause) {
        super(cause);
    }
}
