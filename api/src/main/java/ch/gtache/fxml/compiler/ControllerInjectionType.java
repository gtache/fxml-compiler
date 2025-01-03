package ch.gtache.fxml.compiler;

/**
 * Base controller {@link InjectionType}s
 */
public enum ControllerInjectionType implements InjectionType {
    /**
     * Inject the controller instance
     */
    INSTANCE,
    /**
     * Inject a controller factory
     */
    FACTORY
}
