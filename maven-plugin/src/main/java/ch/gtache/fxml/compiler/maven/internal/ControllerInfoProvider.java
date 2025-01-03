package ch.gtache.fxml.compiler.maven.internal;

import ch.gtache.fxml.compiler.ControllerFieldInfo;
import ch.gtache.fxml.compiler.ControllerInfo;
import ch.gtache.fxml.compiler.impl.ClassesFinder;
import ch.gtache.fxml.compiler.impl.ControllerFieldInfoImpl;
import ch.gtache.fxml.compiler.impl.ControllerInfoImpl;
import ch.gtache.fxml.compiler.maven.FXMLCompilerMojo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helper class for {@link FXMLCompilerMojo} to provides {@link ControllerInfo}
 */
final class ControllerInfoProvider {

    private static final Logger logger = LogManager.getLogger(ControllerInfoProvider.class);
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+(?:static\\s+)?(?<import>[^;]+);");
    private static final Pattern INITIALIZE_PATTERN = Pattern.compile("void\\s+initialize\\s*\\(\\s*\\)\\s*");


    private ControllerInfoProvider() {
    }

    /**
     * Gets the controller info for the given compilation info
     *
     * @param info The compilation info
     * @return The controller info
     * @throws MojoExecutionException If an error occurs
     */
    static ControllerInfo getControllerInfo(final CompilationInfo info) throws MojoExecutionException {
        try {
            final var content = Files.readString(info.controllerFile());
            final var imports = getImports(content);
            final var propertyGenericTypes = new HashMap<String, ControllerFieldInfo>();
            for (final var fieldInfo : info.injectedFields()) {
                final var name = fieldInfo.name();
                final var type = fieldInfo.type();
                if (fillFieldInfo(type, name, content, imports, propertyGenericTypes)) {
                    logger.debug("Found injected field {} of type {} with generic types {} in controller {}", name, type, propertyGenericTypes.get(name).genericTypes(), info.controllerFile());
                } else if (type.contains(".")) {
                    final var simpleName = type.substring(type.lastIndexOf('.') + 1);
                    if (fillFieldInfo(simpleName, name, content, imports, propertyGenericTypes)) {
                        logger.debug("Found injected field {} of type {} with generic types {} in controller {}", name, simpleName, propertyGenericTypes.get(name).genericTypes(), info.controllerFile());
                    }
                } else {
                    logger.info("Field {}({}) not found in controller {}", name, type, info.controllerFile());
                }
            }
            final var handlerHasArgument = new HashMap<String, Boolean>();
            for (final var name : info.injectedMethods()) {
                final var pattern = Pattern.compile("void\\s+" + Pattern.quote(name) + "\\s*\\((?<arg>[^)]*)\\)");
                final var matcher = pattern.matcher(content);
                if (matcher.find()) {
                    final var arg = matcher.group("arg");
                    handlerHasArgument.put(name, arg != null && !arg.isBlank());
                    logger.debug("Found injected method {} with argument {} in controller {}", name, arg, info.controllerFile());
                } else {
                    throw new MojoExecutionException("Cannot find method " + name + " in controller " + info.controllerFile());
                }
            }
            final var hasInitialize = INITIALIZE_PATTERN.matcher(content).find();
            return new ControllerInfoImpl(info.controllerClass(), handlerHasArgument, propertyGenericTypes, hasInitialize);
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

    private static boolean fillFieldInfo(final String type, final String name, final CharSequence content, final Imports imports, final Map<? super String, ? super ControllerFieldInfo> fieldInfos) throws MojoExecutionException {
        final var pattern = Pattern.compile(Pattern.quote(type) + "\\s*(?<type><.+>)?\\s*" + Pattern.quote(name) + "\\s*;");
        final var matcher = pattern.matcher(content);
        if (matcher.find()) {
            final var genericTypes = matcher.group("type");
            if (genericTypes != null && !genericTypes.isBlank()) {
                final var parsed = new GenericParser(genericTypes, imports.imports()).parse();
                fieldInfos.put(name, new ControllerFieldInfoImpl(name, parsed));
            } else {
                fieldInfos.put(name, new ControllerFieldInfoImpl(name, List.of()));
            }
            return true;
        } else {
            return false;
        }
    }

    private record Imports(Map<String, String> imports, Set<String> packages) {

    }
}
