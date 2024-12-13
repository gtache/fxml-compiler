package com.github.gtache.fxml.compiler.maven.internal;

import com.github.gtache.fxml.compiler.maven.FXMLCompilerMojo;
import javafx.event.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Helper class for {@link FXMLCompilerMojo} to provides {@link CompilationInfo}
 */
public final class CompilationInfoProvider {

    private static final Logger logger = LogManager.getLogger(CompilationInfoProvider.class);
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final Pattern START_DOT_PATTERN = Pattern.compile("^\\.");

    private CompilationInfoProvider() {
    }

    /**
     * Gets the compilation info for the given input
     *
     * @param root              The root path
     * @param inputPath         The input path
     * @param controllerMapping The controller mapping
     * @param outputDirectory   The output directory
     * @param project           The Maven project
     * @return The compilation info
     * @throws MojoExecutionException If an error occurs
     */
    public static CompilationInfo getCompilationInfo(final Path root, final Path inputPath, final Map<? extends Path, String> controllerMapping,
                                                     final Path outputDirectory, final MavenProject project) throws MojoExecutionException {
        logger.info("Parsing {}", inputPath);
        try {
            final var documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            final var document = documentBuilder.parse(inputPath.toFile());
            document.getDocumentElement().normalize();
            final var builder = new CompilationInfo.Builder();
            builder.inputFile(inputPath);
            final var inputFilename = inputPath.getFileName().toString();
            final var outputFilename = getOutputFilename(inputFilename);
            final var outputClass = getOutputClass(root, inputPath, outputFilename);
            final var replacedPrefixPath = inputPath.toString().replace(root.toString(), outputDirectory.toString());
            final var targetPath = Paths.get(replacedPrefixPath.replace(inputFilename, outputFilename));
            builder.outputFile(targetPath);
            builder.outputClass(outputClass);
            handleNode(document.getDocumentElement(), builder, controllerMapping, project);
            logger.info("{} will be compiled to {}", inputPath, targetPath);
            return builder.build();
        } catch (final SAXException | IOException | ParserConfigurationException e) {
            throw new MojoExecutionException("Error parsing fxml at " + inputPath, e);
        }
    }

    private static String getOutputClass(final Path root, final Path inputPath, final String outputFilename) {
        final var inputFilename = inputPath.getFileName().toString();
        final var className = outputFilename.replace(".java", "");
        final var replacedPrefixPath = inputPath.toString().replace(root.toString(), "").replace(inputFilename, className);
        return START_DOT_PATTERN.matcher(replacedPrefixPath.replace(File.separator, ".")).replaceAll("");
    }

    private static String getOutputFilename(final CharSequence inputFilename) {
        final var builder = new StringBuilder(inputFilename.length());
        var nextUppercase = true;
        for (var i = 0; i < inputFilename.length(); i++) {
            final var c = inputFilename.charAt(i);
            if (c == '-' || c == '_') {
                nextUppercase = true;
            } else if (nextUppercase) {
                builder.append(Character.toUpperCase(c));
                nextUppercase = false;
            } else {
                builder.append(c);
            }
        }
        return builder.toString().replace(".fxml", ".java");
    }

    private static void handleNode(final Node node, final CompilationInfo.Builder builder, final Map<? extends Path, String> controllerMapping, final MavenProject project) throws MojoExecutionException {
        if (node.getNodeName().equals("fx:include")) {
            handleInclude(node, builder);
        }
        handleAttributes(node, builder, controllerMapping, project);
        handleChildren(node, builder, controllerMapping, project);
    }

    private static void handleInclude(final Node node, final CompilationInfo.Builder builder) throws MojoExecutionException {
        final var map = node.getAttributes();
        if (map == null) {
            throw new MojoExecutionException("Missing attributes for include");
        } else {
            final var sourceAttr = map.getNamedItem("source");
            if (sourceAttr == null) {
                throw new MojoExecutionException("Missing source for include");
            } else {
                final var source = sourceAttr.getNodeValue();
                final var path = getRelativePath(builder.inputFile(), source);
                logger.info("Found include {}", source);
                builder.addInclude(source, path);
            }
        }
    }

    private static Path getRelativePath(final Path base, final String relative) {
        return base.getParent().resolve(relative).normalize();
    }

    private static void handleChildren(final Node node, final CompilationInfo.Builder builder, final Map<? extends Path, String> controllerMapping, final MavenProject project) throws MojoExecutionException {
        final var nl = node.getChildNodes();
        for (var i = 0; i < nl.getLength(); i++) {
            handleNode(nl.item(i), builder, controllerMapping, project);
        }
    }

    private static void handleAttributes(final Node node, final CompilationInfo.Builder builder, final Map<? extends Path, String> controllerMapping, final MavenProject project) throws MojoExecutionException {
        final var map = node.getAttributes();
        if (map != null) {
            for (var i = 0; i < map.getLength(); i++) {
                final var item = map.item(i);
                final var name = item.getNodeName();
                final var value = item.getNodeValue();
                if (name.startsWith("on")) {
                    if (value.startsWith("#")) {
                        final var methodName = value.replace("#", "");
                        logger.debug("Found injected method {}", methodName);
                        builder.addInjectedMethod(methodName);
                    } else if (value.startsWith("$controller.")) {
                        final var fieldName = value.replace("$controller.", "");
                        logger.debug("Found injected field {}", fieldName);
                        builder.addInjectedField(fieldName, EventHandler.class.getName());
                    } else {
                        throw new MojoExecutionException("Unexpected attribute " + name + " with value " + value);
                    }
                } else if (name.equals("fx:controller")) {
                    handleController(value, builder, project);
                } else if (name.equals("fx:id")) {
                    final var type = node.getNodeName();
                    logger.debug("Found injected field {} of type {}", value, type);
                    if (type.equals("fx:include")) {
                        final var path = getRelativePath(builder.inputFile(), map.getNamedItem("source").getNodeValue()).normalize();
                        final var controllerClass = controllerMapping.get(path);
                        if (controllerClass == null) {
                            throw new MojoExecutionException("Cannot find controller for " + path);
                        }
                        builder.addInjectedField(value + "Controller", controllerClass);
                    } else {
                        builder.addInjectedField(value, type);
                    }
                } else if (value.startsWith("%")) {
                    builder.requiresResourceBundle();
                }
            }
        }
    }

    private static void handleController(final String controllerClass, final CompilationInfo.Builder builder, final MavenProject project) throws MojoExecutionException {
        final var subPath = controllerClass.replace(".", "/") + ".java";
        final var path = project.getCompileSourceRoots().stream()
                .map(s -> Paths.get(s).resolve(subPath))
                .filter(Files::exists)
                .findFirst()
                .orElseThrow(() -> new MojoExecutionException("Cannot find controller " + controllerClass));
        logger.info("Found controller {}", controllerClass);
        builder.controllerFile(path);
        builder.controllerClass(controllerClass);
    }
}
