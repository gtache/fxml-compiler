package com.github.gtache.fxml.compiler;

/**
 * A type of injection for controllers
 */
@FunctionalInterface
public interface InjectionType {

    /**
     * @return The name of the type
     */
    String name();
}
