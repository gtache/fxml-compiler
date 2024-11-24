package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInjection;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.Generator;
import com.github.gtache.fxml.compiler.parsing.ParsedConstant;
import com.github.gtache.fxml.compiler.parsing.ParsedInclude;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import javafx.event.EventHandler;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.gtache.fxml.compiler.impl.ReflectionHelper.*;

/**
 * Implementation of {@link Generator}
 */
public class GeneratorImpl implements Generator {

    private static final Pattern INT_PATTERN = Pattern.compile("\\d+");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?");

    private final Collection<String> controllerFactoryPostAction;
    private final Map<String, AtomicInteger> variableNameCounters;

    /**
     * Instantiates a new generator
     */
    public GeneratorImpl() {
        this.controllerFactoryPostAction = new ArrayList<>();
        this.variableNameCounters = new ConcurrentHashMap<>();
    }

    @Override
    public String generate(final GenerationRequest request) throws GenerationException {
        controllerFactoryPostAction.clear();
        variableNameCounters.clear();
        final var className = request.outputClassName();
        final var pkgName = className.substring(0, className.lastIndexOf('.'));
        final var simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        final var loadMethod = getLoadMethod(request);
        final var controllerInjection = getControllerInjection(request);
        final var controllerInjectionType = controllerInjection.fieldInjectionType();
        final String constructorArgument;
        final String constructorControllerJavadoc;
        final String controllerArgumentType;
        final String controllerMapType;
        final var controllerInjectionClass = controllerInjection.injectionClass();
        final var imports = getImports(request);
        if (controllerInjectionType == ControllerFieldInjectionTypes.FACTORY) {
            constructorArgument = "controllerFactory";
            constructorControllerJavadoc = "controller factory";
            controllerArgumentType = "ControllerFactory<" + controllerInjectionClass + ">";
            controllerMapType = "ControllerFactory<?>";
        } else {
            constructorArgument = "controller";
            constructorControllerJavadoc = "controller";
            controllerArgumentType = controllerInjectionClass;
            controllerMapType = "Object";
        }
        final var helperMethods = getHelperMethods(request);
        return """
                package %1$s;
                
                %9$s
                
                /**
                 * Generated code, not thread-safe
                 */
                public final class %2$s {
                
                    private final Map<Class<?>, %7$s> controllersMap;
                    private final Map<Class<?>, ResourceBundle> resourceBundlesMap;
                    private boolean loaded;
                    private %3$s controller;
                
                    /**
                     * Instantiates a new %2$s with no nested controllers and no resource bundle
                     * @param %4$s The %5$s
                     */
                    public %2$s(final %8$s %4$s) {
                        this(Map.of(%3$s.class, %4$s), Map.of());
                    }
                
                    /**
                     * Instantiates a new %2$s with no nested controllers
                     * @param %4$s The %5$s
                     * @param resourceBundle The resource bundle
                     */
                    public %2$s(final %8$s %4$s, final ResourceBundle resourceBundle) {
                        this(Map.of(%3$s.class, %4$s), Map.of(%3$s.class, resourceBundle));
                    }
                
                    /**
                     * Instantiates a new %2$s with nested controllers
                     * @param controllersMap The map of controller class to %5$s
                     * @param resourceBundlesMap The map of controller class to resource bundle
                     */
                    public %2$s(final Map<Class<?>, %7$s> controllersMap, final Map<Class<?>, ResourceBundle> resourceBundlesMap) {
                        this.controllersMap = Map.copyOf(controllersMap);
                        this.resourceBundlesMap = Map.copyOf(resourceBundlesMap);
                    }
                
                    /**
                     * Loads the view. Can only be called once.
                     *
                     * @return The view parent
                     */
                    %6$s
                
                    %10$s
                
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
                loadMethod, controllerMapType, controllerArgumentType, imports, helperMethods);
    }

    /**
     * Gets helper methods string for the given generation request
     *
     * @param request The generation request
     * @return The helper methods
     */
    private static String getHelperMethods(final GenerationRequest request) throws GenerationException {
        final var injection = getControllerInjection(request);
        final var methodInjectionType = injection.methodInjectionType();
        final var sb = new StringBuilder();
        if (methodInjectionType == ControllerMethodsInjectionType.REFLECTION) {
            sb.append("""
                    private <T extends Event> void callMethod(final String methodName, final T event) {
                        try {
                            final Method method;
                            final var methods = Arrays.stream(controller.getClass().getDeclaredMethods())
                                    .filter(m -> m.getName().equals(methodName)).toList();
                            if (methods.size() > 1) {
                                final var eventMethods = methods.stream().filter(m ->
                                        m.getParameterCount() == 1 && Event.class.isAssignableFrom(m.getParameterTypes()[0])).toList();
                                if (eventMethods.size() == 1) {
                                    method = eventMethods.getFirst();
                                } else {
                                    final var emptyMethods = methods.stream().filter(m -> m.getParameterCount() == 0).toList();
                                    if (emptyMethods.size() == 1) {
                                        method = emptyMethods.getFirst();
                                    } else {
                                        throw new IllegalArgumentException("Multiple matching methods for " + methodName);
                                    }
                                }
                            } else if (methods.size() == 1) {
                                method = methods.getFirst();
                            } else {
                                throw new IllegalArgumentException("No matching method for " + methodName);
                            }
                            method.setAccessible(true);
                            if (method.getParameterCount() == 0) {
                                method.invoke(controller);
                            } else {
                                method.invoke(controller, event);
                            }
                        } catch (final IllegalAccessException | InvocationTargetException ex) {
                            throw new RuntimeException("Error using reflection on " + methodName, ex);
                        }
                    }
                    """);
        }
        if (injection.fieldInjectionType() == ControllerFieldInjectionTypes.REFLECTION) {
            sb.append("""
                    private <T> void injectField(final String fieldName, final T object) {
                        try {
                            final var field = controller.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(controller, object);
                        } catch (final NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException("Error using reflection on " + fieldName, e);
                        }
                    }
                    """);
        }
        return sb.toString();
    }

    /**
     * Gets imports for the given generation request
     *
     * @param request The generation request
     * @return The imports
     */
    private static String getImports(final GenerationRequest request) throws GenerationException {
        final var injection = getControllerInjection(request);
        final var fieldInjectionType = injection.fieldInjectionType();
        final var sb = new StringBuilder("import java.util.Map;\nimport java.util.ResourceBundle;\nimport java.util.HashMap;\n");
        if (fieldInjectionType == ControllerFieldInjectionTypes.FACTORY) {
            sb.append("import com.github.gtache.fxml.compiler.ControllerFactory;\n");
        }
        final var methodInjectionType = injection.methodInjectionType();
        if (methodInjectionType == ControllerMethodsInjectionType.REFLECTION) {
            sb.append("import java.lang.reflect.InvocationTargetException;\n");
            sb.append("import java.util.Arrays;\n");
            sb.append("import javafx.event.Event;\n");
            sb.append("import java.lang.reflect.Method;\n");
        }
        return sb.toString();
    }

    /**
     * Computes the load method
     *
     * @param request The generation request
     * @return The load method
     */
    private String getLoadMethod(final GenerationRequest request) throws GenerationException {
        final var rootObject = request.rootObject();
        final var controllerInjection = getControllerInjection(request);
        final var controllerInjectionType = controllerInjection.fieldInjectionType();
        final var controllerClass = controllerInjection.injectionClass();
        final var sb = new StringBuilder("public <T> T load() {\n");
        sb.append("    if (loaded) {\n");
        sb.append("        throw new IllegalStateException(\"Already loaded\");\n");
        sb.append("    }\n");
        final var resourceBundleInjection = request.parameters().resourceBundleInjection();
        if (resourceBundleInjection.injectionType() == ResourceBundleInjectionTypes.GET_BUNDLE) {
            sb.append("    final var bundle = ResourceBundle.getBundle(\"").append(resourceBundleInjection.bundleName()).append("\");\n");
        } else if (resourceBundleInjection.injectionType() == ResourceBundleInjectionTypes.CONSTRUCTOR) {
            sb.append("    final var bundle = resourceBundlesMap.get(").append(controllerClass).append(".class);\n");
        }
        if (controllerInjectionType == ControllerFieldInjectionTypes.FACTORY) {
            sb.append("    final var fieldMap = new HashMap<String, Object>();\n");
        } else {
            sb.append("    controller = (").append(controllerClass).append(") controllersMap.get(").append(controllerClass).append(".class);\n");
        }
        final var variableName = getNextVariableName("object");
        format(request, rootObject, variableName, sb);
        if (controllerInjectionType == ControllerFieldInjectionTypes.FACTORY) {
            sb.append("    final var controllerFactory = controllersMap.get(").append(controllerClass).append(".class);\n");
            sb.append("    controller = (").append(controllerClass).append(") controllerFactory.create(fieldMap);\n");
            controllerFactoryPostAction.forEach(sb::append);
        }
        if (controllerInjection.methodInjectionType() == ControllerMethodsInjectionType.REFLECTION) {
            sb.append("    try {\n");
            sb.append("        final var initialize = controller.getClass().getDeclaredMethod(\"initialize\");\n");
            sb.append("        initialize.setAccessible(true);\n");
            sb.append("        initialize.invoke(controller);\n");
            sb.append("    } catch (final InvocationTargetException | IllegalAccessException e) {\n");
            sb.append("        throw new RuntimeException(\"Error using reflection\", e);\n");
            sb.append("    } catch (final NoSuchMethodException ignored) {\n");
            sb.append("    }\n");
        } else {
            sb.append("    controller.initialize();\n");
        }
        sb.append("    loaded = true;\n");
        sb.append("    return ").append(variableName).append(";\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Formats an object
     *
     * @param request      The generation request
     * @param parsedObject The parsed object to format
     * @param variableName The variable name for the object
     * @param sb           The string builder
     */
    private void format(final GenerationRequest request, final ParsedObject parsedObject, final String variableName, final StringBuilder sb) throws GenerationException {
        switch (parsedObject) {
            case final ParsedInclude include -> formatInclude(request, include, variableName, sb);
            case final ParsedConstant constant -> formatConstant(constant, variableName, sb);
            default -> {
                final var clazz = getClass(parsedObject.className());
                final var constructors = clazz.getConstructors();
                final var allPropertyNames = new HashSet<>(parsedObject.attributes().keySet());
                allPropertyNames.addAll(parsedObject.properties().keySet().stream().map(ParsedProperty::name).collect(Collectors.toSet()));
                final var constructorArgs = getMatchingConstructorArgs(constructors, allPropertyNames);
                if (constructorArgs == null) {
                    if (allPropertyNames.size() == 1 && allPropertyNames.iterator().next().equals("fx:constant")) {
                        final var property = parsedObject.attributes().get("fx:constant");
                        sb.append("    final var ").append(variableName).append(" = ").append(clazz.getCanonicalName()).append(".").append(property.value()).append(";\n");
                    } else {
                        throw new GenerationException("Cannot find constructor for " + clazz.getCanonicalName());
                    }
                } else {
                    final var args = getListConstructorArgs(constructorArgs, parsedObject);
                    final var genericTypes = getGenericTypes(request, parsedObject);
                    sb.append("    final var ").append(variableName).append(" = new ").append(clazz.getCanonicalName())
                            .append(genericTypes).append("(").append(String.join(", ", args)).append(");\n");
                    final var sortedAttributes = parsedObject.attributes().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
                    for (final var e : sortedAttributes) {
                        if (!constructorArgs.namedArgs().containsKey(e.getKey())) {
                            final var p = e.getValue();
                            formatProperty(request, p, parsedObject, variableName, sb);
                        }
                    }
                    final var sortedProperties = parsedObject.properties().entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().name())).toList();
                    for (final var e : sortedProperties) {
                        if (!constructorArgs.namedArgs().containsKey(e.getKey().name())) {
                            final var p = e.getKey();
                            final var o = e.getValue();
                            formatChild(request, parsedObject, p, o, variableName, sb);
                        }
                    }
                }
            }
        }
    }

    private static String getGenericTypes(final GenerationRequest request, final ParsedObject parsedObject) throws GenerationException {
        final var clazz = getClass(parsedObject.className());
        if (isGeneric(clazz)) {
            final var idProperty = parsedObject.attributes().get("fx:id");
            if (idProperty == null) {
                return "<>";
            } else {
                final var id = idProperty.value();
                final var genericTypes = request.controllerInfo().propertyGenericTypes(id);
                if (genericTypes == null) { //Raw
                    return "";
                } else {
                    return "<" + String.join(", ", genericTypes) + ">";
                }
            }
        }
        return "";
    }


    /**
     * Formats an include object
     *
     * @param request     The generation request
     * @param include     The include object
     * @param subNodeName The sub node name
     * @param sb          The string builder
     */
    private void formatInclude(final GenerationRequest request, final ParsedInclude include, final String subNodeName, final StringBuilder sb) throws GenerationException {
        final var subViewVariable = getNextVariableName("view");
        final var source = include.source();
        final var resources = include.resources();
        final var subControllerClass = request.parameters().sourceToControllerName().get(source);
        final var subClassName = request.parameters().sourceToGeneratedClassName().get(source);
        if (subClassName == null) {
            throw new GenerationException("Unknown include source : " + source);
        }
        if (resources == null) {
            sb.append("    final var ").append(subViewVariable).append(" = new ").append(subClassName).append("(controllersMap, resourceBundlesMap);\n");
        } else {
            final var subResourceBundlesMapVariable = getNextVariableName("map");
            final var subBundleVariable = getNextVariableName("bundle");
            sb.append("    final var ").append(subResourceBundlesMapVariable).append(" = new HashMap<>(resourceBundlesMap);\n");
            sb.append("    final var ").append(subBundleVariable).append(" = ResourceBundle.getBundle(\"").append(resources).append("\");\n");
            sb.append("    ").append(subResourceBundlesMapVariable).append(".put(").append(subControllerClass).append(", ").append(subBundleVariable).append(");\n");
            sb.append("    final var ").append(subViewVariable).append(" = new ").append(subClassName).append("(controllersMap, ").append(subResourceBundlesMapVariable).append(");\n");
        }
        sb.append("    final var ").append(subNodeName).append(" = ").append(subViewVariable).append(".load();\n");
        final var id = include.controllerId();
        if (id != null) {
            final var subControllerVariable = getNextVariableName("controller");
            sb.append("    final var ").append(subControllerVariable).append(" = ").append(subViewVariable).append(".controller();\n");
            injectControllerField(request, id, subControllerVariable, sb);
        }
    }

    /**
     * Formats a constant object
     *
     * @param constant     The constant
     * @param variableName The variable name
     * @param sb           The string builder
     */
    private static void formatConstant(final ParsedConstant constant, final String variableName, final StringBuilder sb) {
        sb.append("    final var ").append(variableName).append(" = ").append(constant.className()).append(".").append(constant.constant()).append(";\n");
    }

    /**
     * Formats a property
     *
     * @param request        The generation request
     * @param property       The property to format
     * @param parent         The property's parent object
     * @param parentVariable The parent variable
     * @param sb             The string builder
     */
    private void formatProperty(final GenerationRequest request, final ParsedProperty property, final ParsedObject parent, final String parentVariable, final StringBuilder sb) throws GenerationException {
        final var propertyName = property.name();
        final var setMethod = getSetMethod(propertyName);
        if (propertyName.equals("fx:id")) {
            final var id = property.value();
            injectControllerField(request, id, parentVariable, sb);
        } else if (propertyName.equals("fx:controller")) {
            if (parent != request.rootObject()) {
                throw new GenerationException("Invalid nested controller");
            }
        } else if (Objects.equals(property.sourceType(), EventHandler.class.getName())) {
            injectControllerMethod(request, property, parentVariable, sb);
        } else if (property.sourceType() != null) {
            final var propertySourceTypeClass = getClass(property.sourceType());
            if (hasStaticMethod(propertySourceTypeClass, setMethod)) {
                final var method = getStaticMethod(propertySourceTypeClass, setMethod);
                final var parameterType = method.getParameterTypes()[1];
                final var arg = getArg(request, property.value(), parameterType);
                setLaterIfNeeded(request, property, parameterType, "    " + property.sourceType() + "." + setMethod + "(" + parentVariable + ", " + arg + ");\n", sb);
            } else {
                throw new GenerationException("Cannot set " + propertyName + " on " + property.sourceType());
            }
        } else {
            final var getMethod = getGetMethod(propertyName);
            final var parentClass = getClass(parent.className());
            if (hasMethod(parentClass, setMethod)) {
                final var method = getMethod(parentClass, setMethod);
                final var parameterType = method.getParameterTypes()[0];
                final var arg = getArg(request, property.value(), parameterType);
                setLaterIfNeeded(request, property, parameterType, "    " + parentVariable + "." + setMethod + "(" + arg + ");\n", sb);
            } else if (hasMethod(parentClass, getMethod)) {
                final var method = getMethod(parentClass, getMethod);
                final var returnType = method.getReturnType();
                if (hasMethod(returnType, "addAll")) {
                    final var arg = getArg(request, property.value(), String.class);
                    setLaterIfNeeded(request, property, String.class, "    " + parentVariable + "." + getMethod + "().addAll(" + arg + ");\n", sb);
                }
            } else {
                throw new GenerationException("Cannot set " + propertyName + " on " + parent.className());
            }
        }
    }

    private void setLaterIfNeeded(final GenerationRequest request, final ParsedProperty property, final Class<?> type, final String arg, final StringBuilder sb) throws GenerationException {
        if (type == String.class && property.value().startsWith("%") && request.parameters().resourceBundleInjection().injectionType() == ResourceBundleInjectionTypes.GETTER
                && getControllerInjection(request).fieldInjectionType() == ControllerFieldInjectionTypes.FACTORY) {
            controllerFactoryPostAction.add(arg);
        } else {
            sb.append(arg);
        }
    }

    /**
     * Injects a controller method
     *
     * @param request        The generation request
     * @param property       The property to inject
     * @param parentVariable The parent variable
     * @param sb             The string builder
     */
    private void injectControllerMethod(final GenerationRequest request, final ParsedProperty property, final String parentVariable, final StringBuilder sb) throws GenerationException {
        final var injection = getControllerInjection(request);
        final var methodInjection = getMethodInjection(request, property, parentVariable, sb);
        if (injection.fieldInjectionType() instanceof final ControllerFieldInjectionTypes fieldTypes) {
            switch (fieldTypes) {
                case FACTORY -> controllerFactoryPostAction.add(methodInjection);
                case ASSIGN, SETTERS, REFLECTION -> sb.append(methodInjection);
            }
        } else {
            throw new GenerationException("Unknown injection type : " + injection.fieldInjectionType());
        }
    }

    /**
     * Computes the method injection
     *
     * @param request        The generation request
     * @param property       The property
     * @param parentVariable The parent variable
     * @param sb             The string builder
     * @return The method injection
     */
    private static String getMethodInjection(final GenerationRequest request, final ParsedProperty property, final String parentVariable, final StringBuilder sb) throws GenerationException {
        final var setMethod = getSetMethod(property.name());
        final var injection = getControllerInjection(request);
        final var controllerMethod = property.value().replace("#", "");
        if (injection.methodInjectionType() instanceof final ControllerMethodsInjectionType methodTypes) {
            return switch (methodTypes) {
                case REFERENCE -> {
                    final var hasArgument = request.controllerInfo().handlerHasArgument(controllerMethod);
                    if (hasArgument) {
                        yield "    " + parentVariable + "." + setMethod + "(controller::" + controllerMethod + ");\n";
                    } else {
                        yield "    " + parentVariable + "." + setMethod + "(e -> controller." + controllerMethod + "());\n";
                    }
                }
                case REFLECTION ->
                        "    " + parentVariable + "." + setMethod + "(e -> callMethod(\"" + controllerMethod + "\", e));\n";
            };
        } else {
            throw new GenerationException("Unknown injection type : " + injection.methodInjectionType());
        }
    }

    /**
     * Formats an argument to a method
     *
     * @param request       The generation request
     * @param value         The value
     * @param parameterType The parameter type
     * @return The formatted value
     */
    private static String getArg(final GenerationRequest request, final String value, final Class<?> parameterType) throws GenerationException {
        if (parameterType == String.class && value.startsWith("%")) {
            return getBundleValue(request, value.substring(1));
        } else {
            return toString(value, parameterType);
        }
    }

    /**
     * Injects the given variable into the controller
     *
     * @param request  The generation request
     * @param id       The object id
     * @param variable The object variable
     * @param sb       The string builder
     */
    private static void injectControllerField(final GenerationRequest request, final String id, final String variable, final StringBuilder sb) throws GenerationException {
        final var controllerInjection = getControllerInjection(request);
        final var controllerInjectionType = controllerInjection.fieldInjectionType();
        if (controllerInjectionType instanceof final ControllerFieldInjectionTypes types) {
            switch (types) {
                case FACTORY ->
                        sb.append("    fieldMap.put(\"").append(id).append("\", ").append(variable).append(");\n");
                case ASSIGN -> sb.append("    controller.").append(id).append(" = ").append(variable).append(";\n");
                case SETTERS -> {
                    final var setMethod = getSetMethod(id);
                    sb.append("    controller.").append(setMethod).append("(").append(variable).append(");\n");
                }
                case REFLECTION ->
                        sb.append("    injectField(\"").append(id).append("\", ").append(variable).append(");\n");
            }
        } else {
            throw new GenerationException("Unknown controller injection type : " + controllerInjectionType);
        }
    }

    /**
     * Gets the controller injection object from the generation request
     *
     * @param request The generation request
     * @return The controller injection
     */
    private static ControllerInjection getControllerInjection(final GenerationRequest request) throws GenerationException {
        final var property = request.rootObject().attributes().get("fx:controller");
        if (property == null) {
            throw new GenerationException("Root object must have a controller property");
        } else {
            final var id = property.value();
            return request.parameters().controllerInjections().get(id);
        }
    }

    /**
     * Formats the children objects of a property
     *
     * @param request        The generation request
     * @param parent         The parent object
     * @param property       The parent property
     * @param objects        The child objects
     * @param parentVariable The parent object variable
     * @param sb             The string builder
     */
    private void formatChild(final GenerationRequest request, final ParsedObject parent, final ParsedProperty property,
                             final Collection<? extends ParsedObject> objects, final String parentVariable, final StringBuilder sb) throws GenerationException {
        final var propertyName = property.name();
        final var variables = new ArrayList<String>();
        for (final var object : objects) {
            final var vn = getNextVariableName("object");
            format(request, object, vn, sb);
            variables.add(vn);
        }
        if (variables.size() > 1) {
            formatMultipleChildren(variables, propertyName, parent, parentVariable, sb);
        } else if (variables.size() == 1) {
            final var vn = variables.getFirst();
            formatSingleChild(vn, property, parent, parentVariable, sb);
        }
    }

    /**
     * Formats children objects given that they are more than one
     *
     * @param variables      The children variables
     * @param propertyName   The property name
     * @param parent         The parent object
     * @param parentVariable The parent object variable
     * @param sb             The string builder
     */
    private static void formatMultipleChildren(final Iterable<String> variables, final String propertyName, final ParsedObject parent,
                                               final String parentVariable, final StringBuilder sb) throws GenerationException {
        final var getMethod = getGetMethod(propertyName);
        if (hasMethod(getClass(parent.className()), getMethod)) {
            sb.append("    ").append(parentVariable).append(".").append(getMethod).append("().addAll(").append(String.join(", ", variables)).append(");\n");
        } else {
            throw new GenerationException("Cannot set " + propertyName + " on " + parent.className());
        }
    }

    /**
     * Formats a single child object
     *
     * @param variableName   The child's variable name
     * @param property       The parent property
     * @param parent         The parent object
     * @param parentVariable The parent object variable
     * @param sb             The string builder
     */
    private static void formatSingleChild(final String variableName, final ParsedProperty property, final ParsedObject parent,
                                          final String parentVariable, final StringBuilder sb) throws GenerationException {
        if (property.sourceType() == null) {
            formatSingleChildInstance(variableName, property, parent, parentVariable, sb);
        } else {
            formatSingleChildStatic(variableName, property, parentVariable, sb);
        }
    }

    /**
     * Formats a single child object using an instance method on the parent object
     *
     * @param variableName   The child's variable name
     * @param property       The parent property
     * @param parent         The parent object
     * @param parentVariable The parent object variable
     * @param sb             The string builder
     */
    private static void formatSingleChildInstance(final String variableName, final ParsedProperty property, final ParsedObject parent,
                                                  final String parentVariable, final StringBuilder sb) throws GenerationException {
        final var setMethod = getSetMethod(property);
        final var getMethod = getGetMethod(property);
        final var parentClass = getClass(parent.className());
        if (hasMethod(parentClass, setMethod)) {
            sb.append("    ").append(parentVariable).append(".").append(setMethod).append("(").append(variableName).append(");\n");
        } else if (hasMethod(parentClass, getMethod)) {
            //Probably a list method that has only one element
            sb.append("    ").append(parentVariable).append(".").append(getMethod).append("().addAll(").append(variableName).append(");\n");
        } else {
            throw new GenerationException("Cannot set " + property.name() + " on " + parent.className());
        }
    }

    /**
     * Formats a child object using a static method
     *
     * @param variableName   The child's variable name
     * @param property       The parent property
     * @param parentVariable The parent variable
     * @param sb             The string builder
     */
    private static void formatSingleChildStatic(final String variableName, final ParsedProperty property, final String parentVariable, final StringBuilder sb) throws GenerationException {
        final var setMethod = getSetMethod(property);
        if (hasStaticMethod(getClass(property.sourceType()), setMethod)) {
            sb.append("    ").append(property.sourceType()).append(".").append(setMethod).append("(").append(parentVariable).append(", ").append(variableName).append(");\n");
        } else {
            throw new GenerationException("Cannot set " + property.name() + " on " + property.sourceType());
        }
    }

    /**
     * Returns the getter method name for the given property
     *
     * @param property The property
     * @return The getter method name
     */
    private static String getGetMethod(final ParsedProperty property) {
        return getGetMethod(property.name());
    }

    /**
     * Returns the getter method name for the given property name
     *
     * @param propertyName The property name
     * @return The getter method name
     */
    private static String getGetMethod(final String propertyName) {
        return "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    /**
     * Returns the setter method name for the given property
     *
     * @param property The property
     * @return The setter method name
     */
    private static String getSetMethod(final ParsedProperty property) {
        return getSetMethod(property.name());
    }

    /**
     * Returns the setter method name for the given property name
     *
     * @param propertyName The property name
     * @return The setter method name
     */
    private static String getSetMethod(final String propertyName) {
        return "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }


    private static String getBundleValue(final GenerationRequest request, final String value) throws GenerationException {
        final var resourceBundleInjectionType = request.parameters().resourceBundleInjection().injectionType();
        if (resourceBundleInjectionType instanceof final ResourceBundleInjectionTypes types) {
            return switch (types) {
                case CONSTRUCTOR, GET_BUNDLE -> "bundle.getString(\"" + value + "\")";
                case GETTER -> "controller.resources().getString(\"" + value + "\")";
            };
        } else {
            throw new GenerationException("Unknown resource bundle injection type : " + resourceBundleInjectionType);
        }
    }

    /**
     * Gets the constructor arguments as a list of strings
     *
     * @param constructorArgs The constructor arguments
     * @param parsedObject    The parsed object
     * @return The list of constructor arguments
     */
    private static List<String> getListConstructorArgs(final ConstructorArgs constructorArgs, final ParsedObject parsedObject) throws GenerationException {
        final var args = new ArrayList<String>(constructorArgs.namedArgs().size());
        for (final var entry : constructorArgs.namedArgs().entrySet()) {
            final var type = entry.getValue().type();
            final var p = parsedObject.attributes().get(entry.getKey());
            if (p == null) {
                final var c = parsedObject.properties().entrySet().stream().filter(e ->
                        e.getKey().name().equals(entry.getKey())).findFirst().orElse(null);
                if (c == null) {
                    args.add(toString(entry.getValue().defaultValue(), type));
                } else {
                    throw new GenerationException("Constructor using complex property not supported yet");
                }
            } else {
                args.add(toString(p.value(), type));
            }
        }
        return args;
    }

    /**
     * Gets the constructor arguments that best match the given property names
     *
     * @param constructors     The constructors
     * @param allPropertyNames The property names
     * @return The matching constructor arguments, or null if no constructor matches and no default constructor exists
     */
    private static ConstructorArgs getMatchingConstructorArgs(final Constructor<?>[] constructors, final Set<String> allPropertyNames) {
        ConstructorArgs matchingConstructorArgs = null;
        for (final var constructor : constructors) {
            final var constructorArgs = getConstructorArgs(constructor);
            final var matchingArgsCount = getMatchingArgsCount(constructorArgs, allPropertyNames);
            if (matchingConstructorArgs == null ? matchingArgsCount > 0 : matchingArgsCount > getMatchingArgsCount(matchingConstructorArgs, allPropertyNames)) {
                matchingConstructorArgs = constructorArgs;
            }
        }
        if (matchingConstructorArgs == null) {
            return Arrays.stream(constructors).filter(c -> c.getParameterCount() == 0).findFirst().map(c -> new ConstructorArgs(c, new LinkedHashMap<>())).orElse(null);
        } else {
            return matchingConstructorArgs;
        }
    }

    /**
     * Checks how many arguments of the given constructor match the given property names
     *
     * @param constructorArgs  The constructor arguments
     * @param allPropertyNames The property names
     * @return The number of matching arguments
     */
    private static long getMatchingArgsCount(final ConstructorArgs constructorArgs, final Set<String> allPropertyNames) {
        return constructorArgs.namedArgs().keySet().stream().filter(allPropertyNames::contains).count();
    }


    /**
     * Computes the string value to use in the generated code
     *
     * @param value The value
     * @param clazz The value class
     * @return The computed string value
     */
    private static String toString(final String value, final Class<?> clazz) {
        if (clazz == String.class) {
            return "\"" + value.replace("\"", "\\\"") + "\"";
        } else if (clazz == char.class || clazz == Character.class) {
            return "'" + value + "'";
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return value;
        } else if (clazz == byte.class || clazz == Byte.class || clazz == short.class || clazz == Short.class ||
                clazz == int.class || clazz == Integer.class || clazz == long.class || clazz == Long.class) {
            if (INT_PATTERN.matcher(value).matches()) {
                return value;
            } else {
                return getWrapperClass(clazz) + ".valueOf(\"" + value + "\")";
            }
        } else if (clazz == float.class || clazz == Float.class || clazz == double.class || clazz == Double.class) {
            if (DECIMAL_PATTERN.matcher(value).matches()) {
                return value;
            } else {
                return getWrapperClass(clazz) + ".valueOf(\"" + value + "\")";
            }
        } else if (hasValueOf(clazz)) {
            if (clazz.isEnum()) {
                return clazz.getCanonicalName() + "." + value;
            } else {
                return clazz.getCanonicalName() + ".valueOf(\"" + value + "\")";
            }
        } else {
            return value;
        }
    }

    private static String getWrapperClass(final Class<?> clazz) {
        final var name = clazz.getName();
        if (name.contains(".") || Character.isUpperCase(name.charAt(0))) {
            return name;
        } else {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    /**
     * Computes the next available variable name for the given prefix
     *
     * @param prefix The variable name prefix
     * @return The next available variable name
     */
    private String getNextVariableName(final String prefix) {
        final var counter = variableNameCounters.computeIfAbsent(prefix, k -> new AtomicInteger(0));
        return prefix + counter.getAndIncrement();
    }

    private static Class<?> getClass(final String className) throws GenerationException {
        try {
            return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (final ClassNotFoundException e) {
            throw new GenerationException("Cannot find class " + className + " ; Is a dependency missing for the plugin?", e);
        }
    }
}
