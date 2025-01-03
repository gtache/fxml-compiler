package ch.gtache.fxml.compiler;

/**
 * Base field {@link InjectionType}s
 */
public enum ControllerFieldInjectionType implements InjectionType {
    /**
     * Inject using variable assignment
     */
    ASSIGN,
    /**
     * Inject using a factory
     */
    FACTORY,
    /**
     * Inject using reflection
     */
    REFLECTION,
    /**
     * Inject using setters
     */
    SETTERS
}
