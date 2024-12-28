package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerInjectionType;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.SourceInfo;
import com.github.gtache.fxml.compiler.parsing.ParsedInclude;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Utility class to provide the view's constructor and fields
 */
public final class InitializationFormatter {

    private static final String RESOURCE_BUNDLE_TYPE = "java.util.ResourceBundle";
    private static final String RESOURCE_BUNDLE = "resourceBundle";

    private final HelperProvider helperProvider;
    private final GenerationRequest request;
    private final StringBuilder sb;
    private final Map<String, String> controllerClassToVariable;

    InitializationFormatter(final HelperProvider helperProvider, final GenerationRequest request, final StringBuilder sb) {
        this(helperProvider, request, sb, new HashMap<>());
    }

    InitializationFormatter(final HelperProvider helperProvider, final GenerationRequest request, final StringBuilder sb, final Map<String, String> controllerClassToVariable) {
        this.helperProvider = requireNonNull(helperProvider);
        this.request = requireNonNull(request);
        this.sb = requireNonNull(sb);
        this.controllerClassToVariable = requireNonNull(controllerClassToVariable);
    }

    /**
     * Formats the class initialization (fields and constructor)
     *
     * @throws GenerationException if an error occurs
     */
    public void formatFieldsAndConstructor() throws GenerationException {
        if (!controllerClassToVariable.isEmpty()) {
            throw new GenerationException("Method has already been called");
        }
        final var className = request.outputClassName();
        final var simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        final var mainControllerClass = request.controllerInfo().className();
        final var parameters = request.parameters();
        final var controllerInjectionType = parameters.controllerInjectionType();
        final var fieldInjectionType = parameters.fieldInjectionType();
        final var isFactory = controllerInjectionType == ControllerInjectionType.FACTORY;
        if (hasDuplicateControllerClass() && !isFactory) {
            throw new GenerationException("Some controllers in the view tree have the same class ; Factory controller injection is required");
        }
        fillControllers();
        final var sortedControllersKeys = controllerClassToVariable.keySet().stream().sorted().toList();
        final var controllerArg = getVariableName("controller", isFactory);
        final var controllerArgClass = getType(mainControllerClass, controllerInjectionType, fieldInjectionType);
        final var resourceBundleInfo = getResourceBundleInfo();
        final var resourceBundleType = resourceBundleInfo.type();
        final var resourceBundleArg = resourceBundleInfo.variableName();
        if (isFactory) {
            sb.append("    private final ").append(controllerArgClass).append(" ").append(controllerArg).append(";\n");
            sortedControllersKeys.forEach(e -> sb.append("    private final ").append(getType(e, controllerInjectionType, fieldInjectionType)).append(" ").append(controllerClassToVariable.get(e)).append("Factory").append(";\n"));
            sb.append("    private ").append(mainControllerClass).append(" controller;\n");
        } else {
            sb.append("    private final ").append(mainControllerClass).append(" controller;\n");
            sortedControllersKeys.forEach(e -> sb.append("    private final ").append(getType(e, controllerInjectionType, fieldInjectionType)).append(" ").append(controllerClassToVariable.get(e)).append(";\n"));
        }
        if (resourceBundleType != null) {
            sb.append("    private final ").append(resourceBundleType).append(" ").append(resourceBundleArg).append(";\n");
        }
        sb.append("    private boolean loaded;\n");
        sb.append("\n");
        sb.append("    /**\n");
        sb.append("     * Instantiates a new ").append(simpleClassName).append("\n");
        sb.append("     * @param ").append(controllerArg).append(" The controller ").append(isFactory ? "factory" : "instance").append("\n");
        sortedControllersKeys.forEach(e -> sb.append("     * @param ").append(getVariableName(controllerClassToVariable.get(e), isFactory))
                .append(" The subcontroller ").append(isFactory ? "factory" : "instance").append(" for ").append(e).append("\n"));

        if (resourceBundleType != null) {
            sb.append("     * @param ").append(resourceBundleArg).append(" The resource bundle\n");
        }
        sb.append("     */\n");
        final var arguments = "final " + controllerArgClass + " " + controllerArg +
                ((sortedControllersKeys.isEmpty()) ? "" : ", ") +
                sortedControllersKeys.stream().map(e -> "final " + getType(e, controllerInjectionType, fieldInjectionType) + " " + getVariableName(controllerClassToVariable.get(e), isFactory))
                        .sorted().collect(Collectors.joining(", "))
                + (resourceBundleType == null ? "" : ", final " + resourceBundleType + " " + resourceBundleArg);
        sb.append("    public ").append(simpleClassName).append("(").append(arguments).append(") {\n");
        sb.append("        this.").append(controllerArg).append(" = java.util.Objects.requireNonNull(").append(controllerArg).append(");\n");
        sortedControllersKeys.forEach(s -> {
            final var variableName = getVariableName(controllerClassToVariable.get(s), isFactory);
            sb.append("        this.").append(variableName).append(" = java.util.Objects.requireNonNull(").append(variableName).append(");\n");
        });
        if (resourceBundleType != null) {
            sb.append("        this.").append(resourceBundleArg).append(" = java.util.Objects.requireNonNull(").append(resourceBundleArg).append(");\n");
        }
        sb.append("    }\n");
    }

    private ResourceBundleInfo getResourceBundleInfo() {
        final var injectionType = request.parameters().resourceInjectionType();
        return switch (injectionType) {
            case CONSTRUCTOR -> new ResourceBundleInfo(RESOURCE_BUNDLE_TYPE, RESOURCE_BUNDLE);
            case CONSTRUCTOR_FUNCTION ->
                    new ResourceBundleInfo("java.util.function.Function<String, String>", "resourceBundleFunction");
            case CONSTRUCTOR_NAME -> new ResourceBundleInfo("String", "resourceBundleName");
            case GETTER, GET_BUNDLE -> new ResourceBundleInfo(null, null);
        };
    }

    private record ResourceBundleInfo(String type, String variableName) {
    }

    private static String getType(final String controllerClass, final InjectionType controllerInjectionTypes, final InjectionType fieldInjectionTypes) {
        if (fieldInjectionTypes == ControllerFieldInjectionType.FACTORY) {
            return "java.util.function.Function<java.util.Map<String, Object>, " + controllerClass + ">";
        } else if (controllerInjectionTypes == ControllerInjectionType.FACTORY) {
            return "java.util.function.Supplier<" + controllerClass + ">";
        } else {
            return controllerClass;
        }
    }

    private static String getVariableName(final String variableName, final boolean isFactory) {
        if (isFactory) {
            return variableName + "Factory";
        } else {
            return variableName;
        }
    }

    private boolean hasDuplicateControllerClass() {
        final var set = new HashSet<String>();
        return hasDuplicateControllerClass(request.sourceInfo(), set);
    }

    private static boolean hasDuplicateControllerClass(final SourceInfo info, final Set<? super String> controllers) {
        final var controllerClass = info.controllerClassName();
        if (controllers.contains(controllerClass)) {
            return true;
        }
        controllers.add(controllerClass);
        return info.includedSources().stream().anyMatch(s -> hasDuplicateControllerClass(s, controllers));
    }

    private void fillControllers() {
        request.sourceInfo().includedSources().forEach(this::fillControllers);
    }

    private void fillControllers(final SourceInfo info) {
        controllerClassToVariable.put(info.controllerClassName(), helperProvider.getVariableProvider()
                .getNextVariableName(GenerationHelper.getVariablePrefix(info.controllerClassName())));
        info.includedSources().forEach(this::fillControllers);
    }

    private static void fillControllers(final SourceInfo info, final Set<? super String> controllers) {
        controllers.add(info.controllerClassName());
        info.includedSources().forEach(s -> fillControllers(s, controllers));
    }

    String formatSubViewConstructorCall(final ParsedInclude include) throws GenerationException {
        final var info = request.sourceInfo();
        final var subInfo = info.sourceToSourceInfo().get(include.source());
        if (subInfo == null) {
            throw new GenerationException("Unknown include source : " + include.source());
        } else {
            final var isFactory = request.parameters().controllerInjectionType() == ControllerInjectionType.FACTORY;
            final var subClassName = subInfo.controllerClassName();
            final var subControllerVariable = getVariableName(controllerClassToVariable.get(subClassName), isFactory);
            final var subControllers = new HashSet<String>();
            subInfo.includedSources().forEach(s -> fillControllers(s, subControllers));
            final var arguments = subControllers.stream().sorted().map(c -> getVariableName(controllerClassToVariable.get(c), isFactory)).collect(Collectors.joining(", "));
            final var bundleVariable = subInfo.requiresResourceBundle() ? getBundleVariable(include) : null;
            final var argumentList = subControllerVariable + (arguments.isEmpty() ? "" : ", " + arguments) + (bundleVariable == null ? "" : ", " + bundleVariable);
            final var subViewName = subInfo.generatedClassName();
            final var variable = helperProvider.getVariableProvider().getNextVariableName(GenerationHelper.getVariablePrefix(subViewName));
            sb.append(helperProvider.getCompatibilityHelper().getStartVar(subViewName)).append(variable).append(" = new ").append(subViewName).append("(").append(argumentList).append(");\n");
            return variable;
        }
    }

    private String getBundleVariable(final ParsedInclude include) {
        final var info = getResourceBundleInfo();
        if (info.type() == null) {
            return null;
        } else if (include.resources() == null) {
            return info.variableName();
        } else {
            final var compatibilityHelper = helperProvider.getCompatibilityHelper();
            final var variableProvider = helperProvider.getVariableProvider();
            return switch (request.parameters().resourceInjectionType()) {
                case GETTER, GET_BUNDLE -> null;
                case CONSTRUCTOR_NAME -> {
                    final var bundleVariable = variableProvider.getNextVariableName("resourceBundleName");
                    sb.append(compatibilityHelper.getStartVar("String")).append(bundleVariable).append(" = \"").append(include.resources()).append("\";\n");
                    yield bundleVariable;
                }
                case CONSTRUCTOR_FUNCTION -> {
                    final var bundleVariable = variableProvider.getNextVariableName(RESOURCE_BUNDLE);
                    sb.append(compatibilityHelper.getStartVar(RESOURCE_BUNDLE_TYPE)).append(bundleVariable).append(" = java.util.ResourceBundle.getBundle(\"").append(include.resources()).append("\");\n");
                    final var bundleFunctionVariable = variableProvider.getNextVariableName("resourceBundleFunction");
                    sb.append(compatibilityHelper.getStartVar("java.util.function.Function<String, String>")).append(bundleFunctionVariable).append(" = (java.util.function.Function<String, String>) s -> ").append(bundleVariable).append(".getString(s);\n");
                    yield bundleFunctionVariable;
                }
                case CONSTRUCTOR -> {
                    final var bundleVariable = variableProvider.getNextVariableName(RESOURCE_BUNDLE);
                    sb.append(compatibilityHelper.getStartVar(RESOURCE_BUNDLE_TYPE)).append(bundleVariable).append(" = java.util.ResourceBundle.getBundle(\"").append(include.resources()).append("\");\n");
                    yield bundleVariable;
                }
            };
        }
    }
}
