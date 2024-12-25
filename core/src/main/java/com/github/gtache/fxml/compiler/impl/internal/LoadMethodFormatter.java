package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerInjectionType;
import com.github.gtache.fxml.compiler.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.ResourceBundleInjectionType;

import static java.util.Objects.requireNonNull;

/**
 * Formats the load method for the generated code
 */
public final class LoadMethodFormatter {

    private final HelperProvider helperProvider;
    private final GenerationProgress progress;

    LoadMethodFormatter(final HelperProvider helperProvider, final GenerationProgress progress) {
        this.helperProvider = requireNonNull(helperProvider);
        this.progress = requireNonNull(progress);
    }

    /**
     * Formats the load method
     *
     * @throws GenerationException if an error occurs
     */
    public void formatLoadMethod() throws GenerationException {
        final var request = progress.request();
        final var rootObject = request.rootObject();
        final var parameters = progress.request().parameters();
        final var controllerInjectionType = parameters.controllerInjectionType();
        final var fieldInjectionType = parameters.fieldInjectionType();
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
        final var generationCompatibilityHelper = helperProvider.getCompatibilityHelper();
        if (resourceBundleInjection == ResourceBundleInjectionType.CONSTRUCTOR_NAME) {
            sb.append(generationCompatibilityHelper.getStartVar("java.util.ResourceBundle")).append("resourceBundle = java.util.ResourceBundle.getBundle(resourceBundleName);\n");
        } else if (resourceBundleInjection == ResourceBundleInjectionType.GET_BUNDLE && parameters.bundleMap().containsKey(controllerClass)) {
            sb.append(generationCompatibilityHelper.getStartVar("java.util.ResourceBundle")).append("resourceBundle = java.util.ResourceBundle.getBundle(\"")
                    .append(parameters.bundleMap().get(controllerClass)).append("\");\n");
        }
        if (fieldInjectionType == ControllerFieldInjectionType.FACTORY) {
            sb.append(generationCompatibilityHelper.getStartVar("java.util.Map<String, Object>")).append("fieldMap = new java.util.HashMap<String, Object>();\n");
        } else if (controllerInjectionType == ControllerInjectionType.FACTORY) {
            sb.append("        controller = controllerFactory.create();\n");
        }
        final var variableName = helperProvider.getVariableProvider().getNextVariableName(GenerationHelper.getVariablePrefix(rootObject));
        helperProvider.getObjectFormatter().format(rootObject, variableName);
        if (fieldInjectionType == ControllerFieldInjectionType.FACTORY) {
            sb.append("        controller = controllerFactory.create(fieldMap);\n");
            progress.controllerFactoryPostAction().forEach(sb::append);
        }
        if (request.controllerInfo().hasInitialize()) {
            if (parameters.methodInjectionType() == ControllerMethodsInjectionType.REFLECTION) {
                sb.append("        try {\n");
                sb.append("            ").append(generationCompatibilityHelper.getStartVar("java.lang.reflect.Method", 0)).append("initialize = controller.getClass().getDeclaredMethod(\"initialize\");\n");
                sb.append("            initialize.setAccessible(true);\n");
                sb.append("            initialize.invoke(controller);\n");
                sb.append("        } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {\n");
                sb.append("            throw new RuntimeException(\"Error using reflection\", e);\n");
                sb.append("        }\n");
            } else {
                sb.append("        controller.initialize();\n");
            }
        }
        sb.append("        loaded = true;\n");
        sb.append("        return (T) ").append(variableName).append(";\n");
        sb.append("    }\n");
    }
}
