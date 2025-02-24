package ch.gtache.fxml.compiler;

/**
 * Base methods {@link InjectionType}s
 */
public enum ControllerMethodsInjectionType implements InjectionType {
    /**
     * Inject using visible methods
     */
    REFERENCE,
    /**
     * Inject using reflection
     */
    REFLECTION,
}
