package com.github.gtache.fxml.compiler.maven;

import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.impl.ClassesFinder;
import com.github.gtache.fxml.compiler.impl.ControllerInfoImpl;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helper class for {@link FXMLCompilerMojo} to provides {@link ControllerInfo}
 */
class ControllerInfoProvider {

    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+(?:static\\s+)?(?<import>[^;]+);");

    private static final Set<String> JAVA_LANG_CLASSES;

    static {
        final var set = new HashSet<String>();
        set.add("Object");
        set.add("String");
        set.add("Boolean");
        set.add("Character");
        set.add("Byte");
        set.add("Short");
        set.add("Integer");
        set.add("Long");
        set.add("Float");
        set.add("Double");
        JAVA_LANG_CLASSES = Set.copyOf(set);
    }

    private final Log logger;

    ControllerInfoProvider(final Log logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    ControllerInfo getControllerInfo(final CompilationInfo info) throws MojoExecutionException {
        try {
            final var content = Files.readString(info.controllerFile());
            final var imports = getImports(content);
            final var propertyGenericTypes = new HashMap<String, List<String>>();
            for (final var fieldInfo : info.injectedFields()) {
                final var name = fieldInfo.name();
                final var type = fieldInfo.type();
                if (fillGenericTypes(type, name, content, imports, propertyGenericTypes)) {
                    logger.debug("Found injected field " + name + " of type " + type + " with generic types "
                            + propertyGenericTypes.get(name) + " in controller " + info.controllerFile());
                } else if (type.contains(".")) {
                    final var simpleName = type.substring(type.lastIndexOf('.') + 1);
                    if (fillGenericTypes(simpleName, name, content, imports, propertyGenericTypes)) {
                        logger.debug("Found injected field " + name + " of type " + simpleName + " with generic types "
                                + propertyGenericTypes.get(name) + " in controller " + info.controllerFile());
                    }
                } else {
                    throw new MojoExecutionException("Cannot find field " + name + "(" + type + ")" + " in controller " + info.controllerFile());
                }
            }
            final var handlerHasArgument = new HashMap<String, Boolean>();
            for (final var name : info.injectedMethods()) {
                final var pattern = Pattern.compile("void\\s+" + Pattern.quote(name) + "\\s*\\((?<arg>[^)]*)\\)");
                final var matcher = pattern.matcher(content);
                if (matcher.find()) {
                    final var arg = matcher.group("arg");
                    handlerHasArgument.put(name, arg != null && !arg.isBlank());
                    logger.debug("Found injected method " + name + " with argument " + arg + " in controller " + info.controllerFile());
                } else {
                    throw new MojoExecutionException("Cannot find method " + name + " in controller " + info.controllerFile());
                }
            }
            return new ControllerInfoImpl(handlerHasArgument, propertyGenericTypes);
        } catch (final IOException e) {
            throw new MojoExecutionException("Error reading controller " + info.controllerFile(), e);
        }
    }

    private static Imports getImports(final CharSequence content) throws MojoExecutionException {
        final var resolved = new HashMap<String, String>();
        final var unresolved = new HashSet<String>();
        final var matcher = IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            final var value = matcher.group("import");
            if (value.endsWith(".*")) {
                final var packagePath = value.substring(0, value.length() - 2);
                try {
                    final var classes = ClassesFinder.getClasses(packagePath);
                    if (classes.isEmpty()) {
                        unresolved.add(packagePath);
                    } else {
                        classes.forEach(s -> resolved.put(s.substring(packagePath.length() + 1), s));
                    }
                } catch (final IOException e) {
                    throw new MojoExecutionException("Error reading package " + packagePath, e);
                }
            } else {
                final var simpleName = value.substring(value.lastIndexOf('.') + 1);
                resolved.put(simpleName, value);
            }
        }
        return new Imports(resolved, unresolved);
    }

    private static boolean fillGenericTypes(final String type, final String name, final CharSequence content, final Imports imports, final Map<? super String, ? super List<String>> propertyGenericTypes) throws MojoExecutionException {
        final var pattern = Pattern.compile(Pattern.quote(type) + "(?<type><[^>]+>)?\\s+" + Pattern.quote(name) + "\\s*;");
        final var matcher = pattern.matcher(content);
        if (matcher.find()) {
            final var genericTypes = matcher.group("type");
            if (genericTypes != null && !genericTypes.isBlank()) {
                if (genericTypes.equals("<>")) {
                    propertyGenericTypes.put(name, List.of());
                } else {
                    final var split = genericTypes.replace("<", "").replace(">", "").split(",");
                    final var resolved = new ArrayList<String>();
                    for (final var s : split) {
                        final var trimmed = s.trim();
                        if (trimmed.contains(".") || JAVA_LANG_CLASSES.contains(trimmed)) {
                            resolved.add(trimmed);
                        } else {
                            final var imported = imports.imports().get(trimmed);
                            if (imported == null) {
                                throw new MojoExecutionException("Cannot find class " + trimmed + " probably in one of " + imports.packages() + " ; " +
                                        "Use non-wildcard imports, use fully qualified name or put the classes in a dependency.");
                            } else {
                                resolved.add(imported);
                            }
                        }
                    }
                    propertyGenericTypes.put(name, resolved);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private record Imports(Map<String, String> imports, Set<String> packages) {

    }
}
