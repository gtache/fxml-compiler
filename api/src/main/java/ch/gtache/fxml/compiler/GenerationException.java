package ch.gtache.fxml.compiler;

/**
 * Exception thrown when a generation error occurs
 */
@SuppressWarnings("serial")
public class GenerationException extends Exception {

    /**
     * Instantiates a new exception
     *
     * @param message The message
     */
    public GenerationException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new exception
     *
     * @param message The message
     * @param cause   The cause
     */
    public GenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new exception
     *
     * @param cause The cause
     */
    public GenerationException(final Throwable cause) {
        super(cause);
    }
}
