package com.github.gtache.fxml.compiler.impl;

import javafx.beans.NamedArg;
import javafx.scene.Node;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper methods for reflection
 */
final class ReflectionHelper {
    private static final Map<Class<?>, Boolean> HAS_VALUE_OF = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Boolean> IS_GENERIC = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Method>> METHODS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Method>> STATIC_METHODS = new ConcurrentHashMap<>();

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
        return IS_GENERIC.computeIfAbsent(clazz, c -> c.getTypeParameters().length > 0);
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
        final var methodMap = METHODS.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
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
        final var methodMap = METHODS.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
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
        final var methodMap = STATIC_METHODS.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
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
        final var methodMap = STATIC_METHODS.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
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
        return HAS_VALUE_OF.computeIfAbsent(clazz, ReflectionHelper::computeHasValueOf);
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
}
