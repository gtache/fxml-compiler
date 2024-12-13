package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.SourceInfo;
import com.github.gtache.fxml.compiler.impl.ControllerInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;
import com.github.gtache.fxml.compiler.parsing.ParsedInclude;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to provide the view's constructor and fields
 */
public final class ConstructorFormatter {

    private ConstructorFormatter() {
    }

    public static void formatFieldsAndConstructor(final GenerationProgress progress) throws GenerationException {
        final var className = progress.request().outputClassName();
        final var simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        final var mainControllerClass = progress.request().controllerInfo().className();
        final var isFactory = progress.request().parameters().controllerInjectionType() == ControllerInjectionTypes.FACTORY;
        if (hasDuplicateControllerClass(progress) && !isFactory) {
            throw new GenerationException("Some controllers in the view tree have the same class ; Factory field injection is required");
        }
        fillControllers(progress);
        final var sb = progress.stringBuilder();
        final var controllerMap = progress.controllerClassToVariable();
        controllerMap.forEach((c, v) -> sb.append("    private final ").append(c).append(" ").append(v).append(isFactory ? "Factory" : "").append(";\n"));
        final var controllerArg = getVariableName("controller", isFactory);
        final var controllerArgClass = getType(mainControllerClass, isFactory);
        final var resourceBundleInfo = getResourceBundleInfo(progress);
        final var resourceBundleType = resourceBundleInfo.type();
        final var resourceBundleArg = resourceBundleInfo.variableName();
        if (isFactory) {
            sb.append("    private final ").append(controllerArgClass).append(" ").append(controllerArg).append(";\n");
            sb.append("    private ").append(mainControllerClass).append(" controller;\n");
        } else {
            sb.append("    private final ").append(mainControllerClass).append(" controller;\n");
        }
        if (resourceBundleType != null) {
            sb.append("    private final ").append(resourceBundleType).append(" ").append(resourceBundleArg).append(";\n");
        }
        sb.append("    private boolean loaded;\n");
        sb.append("\n");
        sb.append("    /**\n");
        sb.append("     * Instantiates a new ").append(simpleClassName).append("\n");
        sb.append("     * @param ").append(controllerArg).append(" The controller ").append(isFactory ? "factory" : "instance").append("\n");
        controllerMap.forEach((c, s) -> sb.append("    * @param ").append(getVariableName(s, isFactory))
                .append(" The subcontroller ").append(isFactory ? "factory" : "instance").append(" for ").append(c).append("\n"));

        if (resourceBundleType != null) {
            sb.append("    * @param ").append(resourceBundleArg).append(" The resource bundle\n");
        }
        sb.append("     */\n");
        final var arguments = "final " + controllerArgClass + " " + controllerArg +
                ((controllerMap.isEmpty()) ? "" : ", ") +
                controllerMap.entrySet().stream().map(e -> "final " + getType(e.getKey(), isFactory) + " " + getVariableName(e.getValue(), isFactory))
                        .sorted().collect(Collectors.joining(", "))
                + (resourceBundleType == null ? "" : ", final " + resourceBundleType + " " + resourceBundleArg);
        sb.append("    public ").append(simpleClassName).append("(").append(arguments).append(") {\n");
        sb.append("        this.").append(controllerArg).append(" = java.util.Objects.requireNonNull(").append(controllerArg).append(");\n");
        controllerMap.values().forEach(s -> sb.append("        this.").append(getVariableName(s, isFactory)).append(" = java.util.Objects.requireNonNull(").append(getVariableName(s, isFactory)).append(");\n"));
        if (resourceBundleType != null) {
            sb.append("        this.").append(resourceBundleArg).append(" = java.util.Objects.requireNonNull(").append(resourceBundleArg).append(");\n");
        }
        sb.append("    }\n");
    }

    private static ResourceBundleInfo getResourceBundleInfo(final GenerationProgress progress) throws GenerationException {
        final var injectionType = progress.request().parameters().resourceInjectionType();
        if (injectionType instanceof final ResourceBundleInjectionTypes types) {
            return switch (types) {
                case CONSTRUCTOR -> new ResourceBundleInfo("java.util.ResourceBundle", "resourceBundle");
                case CONSTRUCTOR_FUNCTION ->
                        new ResourceBundleInfo("java.util.function.Function<String, String>", "resourceBundleFunction");
                case CONSTRUCTOR_NAME -> new ResourceBundleInfo("String", "resourceBundleName");
                case GETTER -> new ResourceBundleInfo(null, null);
                case GET_BUNDLE -> new ResourceBundleInfo(null, null);
            };
        } else {
            throw new GenerationException("Unknown resource injection type : " + injectionType);
        }
    }

    private record ResourceBundleInfo(String type, String variableName) {
    }

    private static String getType(final String controllerClass, final boolean isFactory) {
        if (isFactory) {
            return "java.util.function.Function<java.util.Map<String, Object>, " + controllerClass + ">";
        } else {
            return controllerClass;
        }
    }

    private static String getVariableName(final String variableName, final boolean isFactory) {
        if (isFactory) {
            return getVariableName(variableName, "Factory");
        } else {
            return variableName;
        }
    }

    private static String getVariableName(final String variableName, final String suffix) {
        return variableName + suffix;
    }

    private static boolean hasDuplicateControllerClass(final GenerationProgress progress) {
        final var set = new HashSet<String>();
        return hasDuplicateControllerClass(progress.request().sourceInfo(), set);
    }

    private static boolean hasDuplicateControllerClass(final SourceInfo info, final Set<String> controllers) {
        final var controllerClass = info.controllerClassName();
        if (controllers.contains(controllerClass)) {
            return true;
        }
        return info.includedSources().stream().anyMatch(s -> hasDuplicateControllerClass(s, controllers));
    }

    private static void fillControllers(final GenerationProgress progress) {
        progress.request().sourceInfo().includedSources().forEach(s -> fillControllers(progress, s));
    }

    private static void fillControllers(final GenerationProgress progress, final SourceInfo info) {
        progress.controllerClassToVariable().put(info.controllerClassName(), progress.getNextVariableName(GenerationHelper.getVariablePrefix(info.controllerClassName())));
        info.includedSources().forEach(s -> fillControllers(progress, s));
    }

    private static void fillControllers(final SourceInfo info, final Set<? super String> controllers) {
        controllers.add(info.controllerClassName());
        info.includedSources().forEach(s -> fillControllers(s, controllers));
    }

    static String formatSubViewConstructorCall(final GenerationProgress progress, final ParsedInclude include) throws GenerationException {
        final var request = progress.request();
        final var info = request.sourceInfo();
        final var subInfo = info.sourceToSourceInfo().get(include.source());
        if (subInfo == null) {
            throw new GenerationException("Unknown include source : " + include.source());
        } else {
            final var isFactory = request.parameters().controllerInjectionType() == ControllerInjectionTypes.FACTORY;
            final var subClassName = subInfo.controllerClassName();
            final var subControllerVariable = getVariableName(progress.controllerClassToVariable().get(subClassName), isFactory);
            final var subControllers = new HashSet<String>();
            subInfo.includedSources().forEach(s -> fillControllers(s, subControllers));
            final var arguments = subControllers.stream().sorted().map(c -> getVariableName(progress.controllerClassToVariable().get(c), isFactory)).collect(Collectors.joining(", "));
            final var bundleVariable = subInfo.requiresResourceBundle() ? getBundleVariable(progress, include) : null;
            final var argumentList = subControllerVariable + (arguments.isEmpty() ? "" : ", " + arguments) + (bundleVariable == null ? "" : ", " + bundleVariable);
            final var subViewName = subInfo.generatedClassName();
            final var variable = progress.getNextVariableName(GenerationHelper.getVariablePrefix(subViewName));
            progress.stringBuilder().append(GenerationCompatibilityHelper.getStartVar(progress, subViewName)).append(variable).append(" = new ").append(subViewName).append("(").append(argumentList).append(");\n");
            return variable;
        }
    }

    private static String getBundleVariable(final GenerationProgress progress, final ParsedInclude include) throws GenerationException {
        final var info = getResourceBundleInfo(progress);
        if (info.type() == null) {
            return null;
        } else if (include.resources() == null) {
            return info.variableName();
        } else {
            final var sb = progress.stringBuilder();
            if (progress.request().parameters().resourceInjectionType() instanceof final ResourceBundleInjectionTypes types) {
                return switch (types) {
                    case GETTER, GET_BUNDLE -> null;
                    case CONSTRUCTOR_NAME -> {
                        final var bundleVariable = progress.getNextVariableName("resourceBundleName");
                        sb.append(GenerationCompatibilityHelper.getStartVar(progress, "String")).append(bundleVariable).append(" = \"").append(include.resources()).append("\";\n");
                        yield bundleVariable;
                    }
                    case CONSTRUCTOR_FUNCTION -> {
                        final var bundleVariable = progress.getNextVariableName("resourceBundleFunction");
                        sb.append(GenerationCompatibilityHelper.getStartVar(progress, "java.util.function.Function<String, String>")).append(bundleVariable).append(" = (java.util.function.Function<String, String>) s -> \"").append(include.resources()).append("\";\n");
                        yield bundleVariable;
                    }
                    case CONSTRUCTOR -> {
                        final var bundleVariable = progress.getNextVariableName("resourceBundle");
                        sb.append(GenerationCompatibilityHelper.getStartVar(progress, "java.util.ResourceBundle")).append(bundleVariable).append(" = java.util.ResourceBundle.getBundle(\"").append(include.resources()).append("\");\n");
                        yield bundleVariable;
                    }
                };
            } else {
                throw new GenerationException("Unknown resource injection type : " + progress.request().parameters().resourceInjectionType());
            }
        }
    }
}
