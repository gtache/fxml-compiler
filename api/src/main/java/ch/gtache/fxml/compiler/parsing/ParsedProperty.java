package ch.gtache.fxml.compiler.parsing;

/**
 * Parsed property/attribute from FXML
 */
public interface ParsedProperty {

    /**
     * Returns the name of the property
     *
     * @return The name
     */
    String name();

    /**
     * Returns the source type (owner class) of the property (in case of static property)
     *
     * @return The source type name
     */
    String sourceType();

    /**
     * Returns the value of the property
     *
     * @return The value
     */
    String value();
}
