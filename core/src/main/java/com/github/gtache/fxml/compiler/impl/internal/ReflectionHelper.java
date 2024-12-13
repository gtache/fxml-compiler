package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenericTypes;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import javafx.beans.DefaultProperty;
import javafx.beans.NamedArg;
import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.FX_ID;

/**
 * Helper methods for reflection
 */
final class ReflectionHelper {
    private static final Logger logger = LogManager.getLogger(ReflectionHelper.class);
    private static final Map<String, Class<?>> classMap = new HashMap<>();
    private static final Map<Class<?>, Boolean> hasValueOf = new HashMap<>();
    private static final Map<Class<?>, Boolean> isGeneric = new HashMap<>();
    private static final Map<String, String> defaultProperty = new HashMap<>();
    private static final Map<Class<?>, Map<String, Method>> methods = new HashMap<>();
    private static final Map<Class<?>, Map<String, Method>> staticMethods = new HashMap<>();

    private static final Map<String, Class<?>> PRIMITIVE_TYPES = Map.of(
            "boolean", boolean.class,
            "byte", byte.class,
            "char", char.class,
            "short", short.class,
            "int", int.class,
            "long", long.class,
            "float", float.class,
            "double", double.class
    );

    private ReflectionHelper() {
    }

    /**
     * Checks if the given class is generic
     * The result is cached
     *
     * @param clazz The class
     * @return True if the class is generic
     */
    static boolean isGeneric(final Class<?> clazz) {
        return isGeneric.computeIfAbsent(clazz, c -> c.getTypeParameters().length > 0);
    }

    /**
     * Checks if the given class has a method with the given name
     * The result is cached
     *
     * @param clazz      The class
     * @param methodName The method name
     * @return True if the class has a method with the given name
     */
    static boolean hasMethod(final Class<?> clazz, final String methodName) {
        final var methodMap = methods.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
        final var method = methodMap.computeIfAbsent(methodName, m -> computeMethod(clazz, m));
        return method != null;
    }

    /**
     * Gets the method corresponding to the given class and name
     * The result is cached
     *
     * @param clazz      The class
     * @param methodName The method name
     * @return The method
     */
    static Method getMethod(final Class<?> clazz, final String methodName) {
        final var methodMap = methods.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
        return methodMap.computeIfAbsent(methodName, m -> computeMethod(clazz, m));
    }

    /**
     * Checks if the given class has a method with the given name
     *
     * @param clazz      The class
     * @param methodName The method name
     * @return True if the class has a method with the given name
     */
    private static Method computeMethod(final Class<?> clazz, final String methodName) {
        final var matching = Arrays.stream(clazz.getMethods()).filter(m -> {
            if (m.getName().equals(methodName) && !Modifier.isStatic(m.getModifiers())) {
                final var parameterTypes = m.getParameterTypes();
                return methodName.startsWith("get") ? parameterTypes.length == 0 : parameterTypes.length >= 1; //TODO not very clean
            } else {
                return false;
            }
        }).toList();
        if (matching.size() > 1) {
            final var varargsFilter = matching.stream().filter(Method::isVarArgs).toList();
            if (varargsFilter.size() == 1) {
                return varargsFilter.getFirst();
            } else {
                throw new UnsupportedOperationException("Multiple matching methods not supported yet : " + clazz + " - " + methodName);
            }
        } else if (matching.size() == 1) {
            return matching.getFirst();
        } else {
            return null;
        }
    }

    /**
     * Checks if the given class has a static method with the given name
     * The result is cached
     *
     * @param clazz      The class
     * @param methodName The method name
     * @return True if the class has a static method with the given name
     */
    static boolean hasStaticMethod(final Class<?> clazz, final String methodName) {
        final var methodMap = staticMethods.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
        final var method = methodMap.computeIfAbsent(methodName, m -> computeStaticMethod(clazz, m));
        return method != null;
    }

    /**
     * Gets the static method corresponding to the given class and name
     * The result is cached
     *
     * @param clazz      The class
     * @param methodName The method name
     * @return The method
     */
    static Method getStaticMethod(final Class<?> clazz, final String methodName) {
        final var methodMap = staticMethods.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
        return methodMap.computeIfAbsent(methodName, m -> computeStaticMethod(clazz, m));
    }

    /**
     * Gets the static method corresponding to the given class and name
     *
     * @param clazz      The class name
     * @param methodName The method name
     * @return The method, or null if not found
     */
    private static Method computeStaticMethod(final Class<?> clazz, final String methodName) {
        final var matching = Arrays.stream(clazz.getMethods()).filter(m -> {
            if (m.getName().equals(methodName) && Modifier.isStatic(m.getModifiers())) {
                final var parameterTypes = m.getParameterTypes();
                return parameterTypes.length > 1 && parameterTypes[0] == Node.class;
            } else {
                return false;
            }
        }).toList();
        if (matching.size() > 1) {
            throw new UnsupportedOperationException("Multiple matching methods not supported yet : " + clazz + " - " + methodName);
        } else if (matching.size() == 1) {
            return matching.getFirst();
        } else {
            return null;
        }
    }

    /**
     * Checks if the given class has a valueOf(String) method
     * The result is cached
     *
     * @param clazz The class
     * @return True if the class has a valueOf(String)
     */
    static boolean hasValueOf(final Class<?> clazz) {
        return hasValueOf.computeIfAbsent(clazz, ReflectionHelper::computeHasValueOf);
    }

    /**
     * Computes if the given class has a valueOf(String) method
     *
     * @param clazz The class
     * @return True if the class has a valueOf(String)
     */
    private static boolean computeHasValueOf(final Class<?> clazz) {
        try {
            clazz.getMethod("valueOf", String.class);
            return true;
        } catch (final NoSuchMethodException ignored) {
            return false;
        }
    }

    /**
     * Computes the constructor arguments for the given constructor
     *
     * @param constructor The constructor
     * @return The constructor arguments
     */
    static ConstructorArgs getConstructorArgs(final Constructor<?> constructor) {
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        final var annotationsArray = constructor.getParameterAnnotations();
        var hasNamedArgs = 0;
        for (var i = 0; i < annotationsArray.length; i++) {
            final var annotations = annotationsArray[i];
            final var getNamedArg = Arrays.stream(annotations).filter(NamedArg.class::isInstance).findFirst().orElse(null);
            if (getNamedArg != null) {
                hasNamedArgs++;
                final var namedArg = (NamedArg) getNamedArg;
                final var name = namedArg.value();
                final var clazz = constructor.getParameterTypes()[i];
                namedArgs.put(name, new Parameter(name, constructor.getParameterTypes()[i], namedArg.defaultValue().isEmpty() ?
                        getDefaultValue(clazz) : namedArg.defaultValue()));
            }
        }
        if (hasNamedArgs != 0 && hasNamedArgs != annotationsArray.length) {
            throw new IllegalStateException("Constructor " + constructor + " has both named and unnamed arguments");
        } else {
            return new ConstructorArgs(constructor, namedArgs);
        }
    }

    /**
     * Computes the default property for the given class
     *
     * @param className The class name
     * @return The default property
     * @throws GenerationException If the class is not found or no default property is found
     */
    static String getDefaultProperty(final String className) throws GenerationException {
        if (defaultProperty.containsKey(className)) {
            return defaultProperty.get(className);
        } else {
            final var property = computeDefaultProperty(className);
            if (property != null) {
                defaultProperty.put(className, property);
            }
            return property;
        }
    }

    /**
     * Gets the wrapper class for the given class
     *
     * @param clazz The class
     * @return The wrapper class (e.g. int.class -> Integer.class) or the original class if it is not a primitive
     */
    static String getWrapperClass(final Class<?> clazz) {
        final var name = clazz.getName();
        if (name.contains(".") || Character.isUpperCase(name.charAt(0))) {
            return name;
        } else {
            return MethodType.methodType(clazz).wrap().returnType().getName();
        }
    }

    /**
     * Gets the class for the given class name
     *
     * @param className The class name
     * @return The class
     * @throws GenerationException If the class is not found
     */
    static Class<?> getClass(final String className) throws GenerationException {
        if (classMap.containsKey(className)) {
            return classMap.get(className);
        } else {
            final var clazz = computeClass(className);
            classMap.put(className, clazz);
            return clazz;
        }
    }

    private static Class<?> computeClass(final String className) throws GenerationException {
        if (PRIMITIVE_TYPES.containsKey(className)) {
            return PRIMITIVE_TYPES.get(className);
        } else {
            try {
                return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            } catch (final ClassNotFoundException e) {
                throw new GenerationException("Cannot find class " + className + " ; Is a dependency missing for the plugin?", e);
            }
        }
    }

    private static String computeDefaultProperty(final String className) throws GenerationException {
        try {
            final var clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            final var annotation = clazz.getAnnotation(DefaultProperty.class);
            if (annotation == null) {
                return null;
            } else {
                return annotation.value();
            }
        } catch (final ClassNotFoundException e) {
            throw new GenerationException("Class " + className + " not found ; Either specify the property explicitly or put the class in a dependency", e);
        }
    }

    /**
     * Computes the default value for the given class
     *
     * @param clazz The class
     * @return The value
     */
    private static String getDefaultValue(final Class<?> clazz) {
        final var primitiveWrappers = Set.of(Integer.class, Byte.class, Short.class, Long.class, Float.class, Double.class);
        if (clazz == char.class || clazz == Character.class) {
            return "\u0000";
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return "false";
        } else if (clazz.isPrimitive() || primitiveWrappers.contains(clazz)) {
            return "0";
        } else {
            return "null";
        }
    }

    /**
     * Gets the generic types for the given object
     *
     * @param progress     The generation progress
     * @param parsedObject The parsed object
     * @return The generic types
     * @throws GenerationException if an error occurs
     */
    static String getGenericTypes(final GenerationProgress progress, final ParsedObject parsedObject) throws GenerationException {
        final var clazz = getClass(parsedObject.className());
        if (isGeneric(clazz)) {
            final var idProperty = parsedObject.attributes().get(FX_ID);
            if (idProperty == null) {
                logger.warn("No id found for generic class {} ; Using raw", clazz.getName());
                return "";
            } else {
                final var id = idProperty.value();
                final var fieldInfo = progress.request().controllerInfo().fieldInfo(id);
                if (fieldInfo == null) { //Not found
                    logger.warn("No field found for generic class {} (id={}) ; Using raw", clazz.getName(), id);
                    return "";
                } else if (fieldInfo.isGeneric()) {
                    return formatGenerics(fieldInfo.genericTypes());
                }
            }
        }
        return "";
    }

    private static String formatGenerics(final List<? extends GenericTypes> genericTypes) {
        final var sb = new StringBuilder();
        sb.append("<");
        for (var i = 0; i < genericTypes.size(); i++) {
            final var genericType = genericTypes.get(i);
            sb.append(genericType.name());
            formatGenerics(genericType.subTypes(), sb);
            if (i < genericTypes.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(">");
        return sb.toString();
    }

    private static void formatGenerics(final List<? extends GenericTypes> genericTypes, final StringBuilder sb) {
        if (!genericTypes.isEmpty()) {
            sb.append("<");
            for (var i = 0; i < genericTypes.size(); i++) {
                final var genericType = genericTypes.get(i);
                sb.append(genericType.name());
                formatGenerics(genericType.subTypes(), sb);
                if (i < genericTypes.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(">");
        }
    }

    /**
     * Gets the return type of the given method for the given class
     *
     * @param className  The class
     * @param methodName The method
     * @return The return type
     * @throws GenerationException if an error occurs
     */
    static String getReturnType(final String className, final String methodName) throws GenerationException {
        final var clazz = getClass(className);
        final var method = Arrays.stream(clazz.getMethods()).filter(m -> m.getName().equals(methodName))
                .findFirst().orElseThrow(() -> new GenerationException("Method " + methodName + " not found in class " + className));
        return method.getReturnType().getName();
    }
}
