package com.github.gtache.fxml.compiler;

/**
 * A type of injection for controllers
 */
@FunctionalInterface
public interface InjectionType {

    /**
     * Returns the name of the type
     *
     * @return The name
     */
    String name();
}
