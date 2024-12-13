package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;

/**
 * Formats the load method for the generated code
 */
public final class LoadMethodFormatter {

    private LoadMethodFormatter() {
    }

    /**
     * Formats the load method
     *
     * @param progress The generation progress
     * @throws GenerationException if an error occurs
     */
    public static void formatLoadMethod(final GenerationProgress progress) throws GenerationException {
        final var request = progress.request();
        final var rootObject = request.rootObject();
        final var parameters = progress.request().parameters();
        final var controllerInjectionType = parameters.fieldInjectionType();
        final var controllerClass = progress.request().controllerInfo().className();
        final var sb = progress.stringBuilder();
        sb.append("    /**\n");
        sb.append("     * Loads the view. Can only be called once.\n");
        sb.append("     *\n");
        sb.append("     * @return The view parent\n");
        sb.append("     */\n");
        sb.append("    public <T> T load() {\n");
        sb.append("        if (loaded) {\n");
        sb.append("            throw new IllegalStateException(\"Already loaded\");\n");
        sb.append("        }\n");
        final var resourceBundleInjection = parameters.resourceInjectionType();
        if (resourceBundleInjection == ResourceBundleInjectionTypes.CONSTRUCTOR_NAME) {
            sb.append(GenerationCompatibilityHelper.getStartVar(progress, "java.util.ResourceBundle")).append("resourceBundle = java.util.ResourceBundle.getBundle(\"resourceBundleName\");\n");
        } else if (resourceBundleInjection == ResourceBundleInjectionTypes.GET_BUNDLE && parameters.bundleMap().containsKey(controllerClass)) {
            sb.append(GenerationCompatibilityHelper.getStartVar(progress, "java.util.ResourceBundle")).append("resourceBundle = java.util.ResourceBundle.getBundle(\"").append(parameters.bundleMap().get(controllerClass)).append("\");\n");
        }
        if (controllerInjectionType == ControllerFieldInjectionTypes.FACTORY) {
            sb.append(GenerationCompatibilityHelper.getStartVar(progress, "java.util.Map<String, Object>")).append("fieldMap = new java.util.HashMap<String, Object>();\n");
        }
        final var variableName = progress.getNextVariableName(GenerationHelper.getVariablePrefix(rootObject));
        ObjectFormatter.format(progress, rootObject, variableName);
        if (controllerInjectionType == ControllerFieldInjectionTypes.FACTORY) {
            sb.append("        controller = (").append(controllerClass).append(") controllerFactory.create(fieldMap);\n");
            progress.controllerFactoryPostAction().forEach(sb::append);
        }
        if (parameters.methodInjectionType() == ControllerMethodsInjectionType.REFLECTION) {
            sb.append("        try {\n");
            sb.append("            ").append(GenerationCompatibilityHelper.getStartVar(progress, "java.lang.reflect.Method", 0)).append("initialize = controller.getClass().getDeclaredMethod(\"initialize\");\n");
            sb.append("            initialize.setAccessible(true);\n");
            sb.append("            initialize.invoke(controller);\n");
            sb.append("        } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException e) {\n");
            sb.append("            throw new RuntimeException(\"Error using reflection\", e);\n");
            sb.append("        } catch (final NoSuchMethodException ignored) {\n");
            sb.append("        }\n");
        } else {
            sb.append("        controller.initialize();\n");
        }
        sb.append("        loaded = true;\n");
        sb.append("        return (T) ").append(variableName).append(";\n");
        sb.append("    }\n");
    }

}
