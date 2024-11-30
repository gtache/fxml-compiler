package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerInjection;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.List;
import java.util.Map;

import static com.github.gtache.fxml.compiler.impl.internal.ControllerInjector.injectControllerField;

/**
 * Various helper methods for {@link GeneratorImpl}
 */
public final class GenerationHelper {

    static final String FX_ID = "fx:id";
    static final String FX_VALUE = "fx:value";
    static final String VALUE = "value";
    static final String START_VAR = "    final var ";

    private GenerationHelper() {

    }

    /**
     * Returns the variable prefix for the given object
     *
     * @param object The object
     * @return The variable prefix
     */
    public static String getVariablePrefix(final ParsedObject object) {
        final var className = object.className();
        return className.substring(className.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Gets the controller injection object from the generation request
     *
     * @param progress The generation progress
     * @return The controller injection
     * @throws GenerationException If the controller is not found
     */
    public static ControllerInjection getControllerInjection(final GenerationProgress progress) throws GenerationException {
        final var request = progress.request();
        final var property = request.rootObject().attributes().get("fx:controller");
        if (property == null) {
            throw new GenerationException("Root object must have a controller property");
        } else {
            final var id = property.value();
            return request.parameters().controllerInjections().get(id);
        }
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
        return "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
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
        return "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    /**
     * Handles the fx:id attribute of an object
     *
     * @param progress     The generation progress
     * @param parsedObject The parsed object
     * @param variableName The variable name
     * @throws GenerationException if an error occurs
     */
    static void handleId(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        final var id = parsedObject.attributes().get(FX_ID);
        if (id != null) {
            progress.idToVariableName().put(id.value(), variableName);
            progress.idToObject().put(id.value(), parsedObject);
            //TODO Don't inject if variable doesn't exist
            injectControllerField(progress, id.value(), variableName);
        }
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
