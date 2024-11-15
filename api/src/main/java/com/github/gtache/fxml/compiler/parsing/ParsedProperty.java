package com.github.gtache.fxml.compiler.parsing;

/**
 * Parsed property/attribute from FXML
 */
public interface ParsedProperty {

    /**
     * @return The property name
     */
    String name();

    /**
     * @return The property source type (in case of static property)
     */
    Class<?> sourceType();

    /**
     * @return The property value
     */
    String value();
}
