package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.Generator;
import com.github.gtache.fxml.compiler.impl.internal.GenerationProgress;
import com.github.gtache.fxml.compiler.impl.internal.HelperMethodsProvider;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getControllerInjection;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getVariablePrefix;
import static com.github.gtache.fxml.compiler.impl.internal.ObjectFormatter.format;

//TODO handle binding (${})

/**
 * Implementation of {@link Generator}
 */
public class GeneratorImpl implements Generator {


    @Override
    public String generate(final GenerationRequest request) throws GenerationException {
        final var progress = new GenerationProgress(request);
        final var className = request.outputClassName();
        final var pkgName = className.substring(0, className.lastIndexOf('.'));
        final var simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        final var loadMethod = getLoadMethod(progress);
        final var controllerInjection = getControllerInjection(progress);
        final var controllerInjectionType = controllerInjection.fieldInjectionType();
        final var controllerInjectionClass = controllerInjection.injectionClass();
        final String constructorArgument;
        final String constructorControllerJavadoc;
        final String controllerArgumentType;
        final String controllerMapType;
        if (controllerInjectionType == ControllerFieldInjectionTypes.FACTORY) {
            constructorArgument = "controllerFactory";
            constructorControllerJavadoc = "controller factory";
            controllerArgumentType = "com.github.gtache.fxml.compiler.ControllerFactory<" + controllerInjectionClass + ">";
            controllerMapType = "com.github.gtache.fxml.compiler.ControllerFactory<?>";
        } else {
            constructorArgument = "controller";
            constructorControllerJavadoc = "controller";
            controllerArgumentType = controllerInjectionClass;
            controllerMapType = "Object";
        }
        final var helperMethods = HelperMethodsProvider.getHelperMethods(progress);
        return """
                package %1$s;
                
                /**
                 * Generated code, not thread-safe
                 */
                public final class %2$s {
                
                    private final java.util.Map<Class<?>, %7$s> controllersMap;
                    private final java.util.Map<Class<?>, java.util.ResourceBundle> resourceBundlesMap;
                    private boolean loaded;
                    private %3$s controller;
                
                    /**
                     * Instantiates a new %2$s with no nested controllers and no resource bundle
                     * @param %4$s The %5$s
                     */
                    public %2$s(final %8$s %4$s) {
                        this(java.util.Map.of(%3$s.class, %4$s), java.util.Map.of());
                    }
                
                    /**
                     * Instantiates a new %2$s with no nested controllers
                     * @param %4$s The %5$s
                     * @param resourceBundle The resource bundle
                     */
                    public %2$s(final %8$s %4$s, final java.util.ResourceBundle resourceBundle) {
                        this(java.util.Map.of(%3$s.class, %4$s), java.util.Map.of(%3$s.class, resourceBundle));
                    }
                
                    /**
                     * Instantiates a new %2$s with nested controllers
                     * @param controllersMap The map of controller class to %5$s
                     * @param resourceBundlesMap The map of controller class to resource bundle
                     */
                    public %2$s(final java.util.Map<Class<?>, %7$s> controllersMap, final java.util.Map<Class<?>, java.util.ResourceBundle> resourceBundlesMap) {
                        this.controllersMap = java.util.Map.copyOf(controllersMap);
                        this.resourceBundlesMap = java.util.Map.copyOf(resourceBundlesMap);
                    }
                
                    /**
                     * Loads the view. Can only be called once.
                     *
                     * @return The view parent
                     */
                    %6$s
                
                    %9$s
                
                    /**
                     * @return The controller
                     */
                    public %3$s controller() {
                        if (loaded) {
                            return controller;
                        } else {
                            throw new IllegalStateException("Not loaded");
                        }
                    }
                }
                """.formatted(pkgName, simpleClassName, controllerInjectionClass, constructorArgument, constructorControllerJavadoc,
                loadMethod, controllerMapType, controllerArgumentType, helperMethods);
    }


    /**
     * Computes the load method
     *
     * @param progress The generation progress
     * @return The load method
     */
    private static String getLoadMethod(final GenerationProgress progress) throws GenerationException {
        final var request = progress.request();
        final var rootObject = request.rootObject();
        final var controllerInjection = getControllerInjection(progress);
        final var controllerInjectionType = controllerInjection.fieldInjectionType();
        final var controllerClass = controllerInjection.injectionClass();
        final var sb = progress.stringBuilder();
        sb.append("public <T> T load() {\n");
        sb.append("    if (loaded) {\n");
        sb.append("        throw new IllegalStateException(\"Already loaded\");\n");
        sb.append("    }\n");
        final var resourceBundleInjection = request.parameters().resourceBundleInjection();
        if (resourceBundleInjection.injectionType() == ResourceBundleInjectionTypes.GET_BUNDLE) {
            sb.append("    final var bundle = java.util.ResourceBundle.getBundle(\"").append(resourceBundleInjection.bundleName()).append("\");\n");
        } else if (resourceBundleInjection.injectionType() == ResourceBundleInjectionTypes.CONSTRUCTOR) {
            sb.append("    final var bundle = resourceBundlesMap.get(").append(controllerClass).append(".class);\n");
        }
        if (controllerInjectionType == ControllerFieldInjectionTypes.FACTORY) {
            sb.append("    final var fieldMap = new HashMap<String, Object>();\n");
        } else {
            sb.append("    controller = (").append(controllerClass).append(") controllersMap.get(").append(controllerClass).append(".class);\n");
        }
        final var variableName = progress.getNextVariableName(getVariablePrefix(rootObject));
        format(progress, rootObject, variableName);
        if (controllerInjectionType == ControllerFieldInjectionTypes.FACTORY) {
            sb.append("    final var controllerFactory = controllersMap.get(").append(controllerClass).append(".class);\n");
            sb.append("    controller = (").append(controllerClass).append(") controllerFactory.create(fieldMap);\n");
            progress.controllerFactoryPostAction().forEach(sb::append);
        }
        if (controllerInjection.methodInjectionType() == ControllerMethodsInjectionType.REFLECTION) {
            sb.append("    try {\n");
            sb.append("        final var initialize = controller.getClass().getDeclaredMethod(\"initialize\");\n");
            sb.append("        initialize.setAccessible(true);\n");
            sb.append("        initialize.invoke(controller);\n");
            sb.append("    } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException e) {\n");
            sb.append("        throw new RuntimeException(\"Error using reflection\", e);\n");
            sb.append("    } catch (final NoSuchMethodException ignored) {\n");
            sb.append("    }\n");
        } else {
            sb.append("    controller.initialize();\n");
        }
        sb.append("    loaded = true;\n");
        sb.append("    return (T) ").append(variableName).append(";\n");
        sb.append("}");
        return sb.toString();
    }


}
