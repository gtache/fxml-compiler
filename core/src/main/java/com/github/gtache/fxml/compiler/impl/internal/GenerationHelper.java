package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.List;
import java.util.Map;

/**
 * Various helper methods for {@link GeneratorImpl}
 */
final class GenerationHelper {

    static final String INDENT_8 = "        ";
    static final String FX_ID = "fx:id";
    static final String FX_VALUE = "fx:value";
    static final String VALUE = "value";

    //Taken from FXMLLoader
    static final String ESCAPE_PREFIX = "\\";
    static final String RELATIVE_PATH_PREFIX = "@";
    static final String RESOURCE_KEY_PREFIX = "%";
    static final String EXPRESSION_PREFIX = "$";
    static final String BINDING_EXPRESSION_PREFIX = "${";
    static final String BIDIRECTIONAL_BINDING_PREFIX = "#{";

    private GenerationHelper() {

    }

    /**
     * Returns the variable prefix for the given object
     *
     * @param object The object
     * @return The variable prefix
     */
    static String getVariablePrefix(final ParsedObject object) {
        return getVariablePrefix(object.className());
    }

    /**
     * Returns the variable prefix for the given class name
     *
     * @param className The class name
     * @return The variable prefix
     */
    static String getVariablePrefix(final String className) {
        return className.substring(className.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Returns the getter method name for the given property
     *
     * @param property The property
     * @return The getter method name
     */
    static String getGetMethod(final ParsedProperty property) {
        return getGetMethod(property.name());
    }

    /**
     * Returns the getter method name for the given property name
     *
     * @param propertyName The property name
     * @return The getter method name
     */
    static String getGetMethod(final String propertyName) {
        return getMethod(propertyName, "get");
    }

    /**
     * Returns the getter method name for the given property
     *
     * @param property The property
     * @return The getter method name
     */
    static String getIsMethod(final ParsedProperty property) {
        return getIsMethod(property.name());
    }

    /**
     * Returns the getter method name for the given property name
     *
     * @param propertyName The property name
     * @return The getter method name
     */
    static String getIsMethod(final String propertyName) {
        return getMethod(propertyName, "is");
    }

    /**
     * Returns the setter method name for the given property
     *
     * @param property The property
     * @return The setter method name
     */
    static String getSetMethod(final ParsedProperty property) {
        return getSetMethod(property.name());
    }

    /**
     * Returns the setter method name for the given property name
     *
     * @param propertyName The property name
     * @return The setter method name
     */
    static String getSetMethod(final String propertyName) {
        return getMethod(propertyName, "set");
    }

    private static String getMethod(final String propertyName, final String methodPrefix) {
        return methodPrefix + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    /**
     * Returns the sorted attributes of the given object
     *
     * @param parsedObject The parsed object
     * @return The sorted attributes
     */
    static List<ParsedProperty> getSortedAttributes(final ParsedObject parsedObject) {
        return parsedObject.attributes().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).toList();
    }
}
