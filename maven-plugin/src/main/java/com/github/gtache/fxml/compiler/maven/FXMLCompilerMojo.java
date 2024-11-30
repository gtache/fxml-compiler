package com.github.gtache.fxml.compiler.maven;

import com.github.gtache.fxml.compiler.ControllerInjection;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerInjectionImpl;
import com.github.gtache.fxml.compiler.impl.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.impl.GenerationParametersImpl;
import com.github.gtache.fxml.compiler.impl.GenerationRequestImpl;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;
import com.github.gtache.fxml.compiler.parsing.ParseException;
import com.github.gtache.fxml.compiler.parsing.xml.DOMFXMLParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Main mojo for FXML compiler
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class FXMLCompilerMojo extends AbstractMojo {
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "output-directory", defaultValue = "${project.build.directory}/generated-sources/java", required = true)
    private Path outputDirectory;

    @Parameter(property = "field-injection", defaultValue = "REFLECTION", required = true)
    private ControllerFieldInjectionTypes fieldInjectionTypes;

    @Parameter(property = "method-injection", defaultValue = "REFLECTION", required = true)
    private ControllerMethodsInjectionType methodsInjectionType;

    @Parameter(property = "bundle-injection", defaultValue = "CONSTRUCTOR", required = true)
    private ResourceBundleInjectionTypes bundleInjectionType;

    @Parameter(property = "bundle-map")
    private Map<String, String> bundleMap;

    @Override
    public void execute() throws MojoExecutionException {
        final var fxmls = getAllFXMLs();
        final var controllerMapping = createControllerMapping(fxmls);
        final var mapping = createMapping(fxmls, controllerMapping);
        compile(mapping);
    }

    private Map<Path, Path> getAllFXMLs() throws MojoExecutionException {
        final var map = new HashMap<Path, Path>();
        for (final var resource : project.getResources()) {
            final var path = Paths.get(resource.getDirectory());
            if (Files.isDirectory(path)) {
                try (final var stream = Files.find(path, Integer.MAX_VALUE, (p, a) -> p.toString().endsWith(".fxml"), FileVisitOption.FOLLOW_LINKS)) {
                    final var curList = stream.toList();
                    getLog().info("Found " + curList);
                    for (final var p : curList) {
                        map.put(p, path);
                    }
                } catch (final IOException e) {
                    throw new MojoExecutionException("Error reading resources", e);
                }
            } else {
                getLog().info("Directory " + path + " does not exist");
            }
        }
        return map;
    }

    private static Map<Path, String> createControllerMapping(final Map<? extends Path, ? extends Path> fxmls) throws MojoExecutionException {
        final var mapping = new HashMap<Path, String>();
        for (final var fxml : fxmls.keySet()) {
            mapping.put(fxml, getControllerClass(fxml));
        }
        return mapping;
    }

    private static String getControllerClass(final Path fxml) throws MojoExecutionException {
        try {
            final var documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            final var document = documentBuilder.parse(fxml.toFile());
            document.getDocumentElement().normalize();

            final var controller = document.getDocumentElement().getAttribute("fx:controller");
            if (controller.isBlank()) {
                throw new MojoExecutionException("Missing controller attribute for " + fxml);
            } else {
                return controller;
            }
        } catch (final SAXException | IOException | ParserConfigurationException e) {
            throw new MojoExecutionException("Error parsing fxml at " + fxml, e);
        }
    }

    private Map<Path, CompilationInfo> createMapping(final Map<? extends Path, ? extends Path> fxmls, final Map<? extends Path, String> controllerMapping) throws MojoExecutionException {
        final var compilationInfoProvider = new CompilationInfoProvider(project, outputDirectory, getLog());
        final var mapping = new HashMap<Path, CompilationInfo>();
        for (final var entry : fxmls.entrySet()) {
            final var info = compilationInfoProvider.getCompilationInfo(entry.getValue(), entry.getKey(), controllerMapping);
            mapping.put(entry.getKey(), info);
        }
        return mapping;
    }

    private void compile(final Map<Path, CompilationInfo> mapping) throws MojoExecutionException {
        final var generator = new GeneratorImpl();
        final var parser = new DOMFXMLParser();
        final var controllerInfoProvider = new ControllerInfoProvider(getLog());
        try {
            for (final var entry : mapping.entrySet()) {
                final var inputPath = entry.getKey();
                final var info = entry.getValue();
                getLog().info("Parsing " + inputPath + " with " + parser.getClass().getSimpleName());
                final var root = parser.parse(inputPath);
                final var controllerInjection = getControllerInjection(mapping, info);
                final var sourceToGeneratedClassName = getSourceToGeneratedClassName(mapping, info);
                final var sourceToControllerName = getSourceToControllerName(mapping, info);
                final var resourceBundleInjection = new ResourceBundleInjectionImpl(bundleInjectionType, getBundleName(info));
                final var parameters = new GenerationParametersImpl(controllerInjection, sourceToGeneratedClassName, sourceToControllerName, resourceBundleInjection);
                final var controllerInfo = controllerInfoProvider.getControllerInfo(info);
                final var output = info.outputFile();
                final var request = new GenerationRequestImpl(parameters, controllerInfo, root, info.outputClass());
                getLog().info("Compiling " + inputPath);
                final var content = generator.generate(request);
                final var outputDir = output.getParent();
                Files.createDirectories(outputDir);
                Files.writeString(output, content);
                getLog().info("Compiled " + inputPath + " to " + output);
            }
        } catch (final IOException | RuntimeException | ParseException | GenerationException e) {
            throw new MojoExecutionException("Error compiling fxml", e);
        }
        project.addCompileSourceRoot(outputDirectory.toAbsolutePath().toString());
    }

    private String getBundleName(final CompilationInfo info) {
        return bundleMap == null ? "" : bundleMap.getOrDefault(info.inputFile().toString(), "");
    }

    private static Map<String, String> getSourceToControllerName(final Map<Path, CompilationInfo> mapping, final CompilationInfo info) {
        final var ret = new HashMap<String, String>();
        for (final var entry : info.includes().entrySet()) {
            ret.put(entry.getKey(), mapping.get(entry.getValue()).controllerClass());
        }
        return ret;
    }

    private static Map<String, String> getSourceToGeneratedClassName(final Map<Path, CompilationInfo> mapping, final CompilationInfo info) {
        final var ret = new HashMap<String, String>();
        for (final var entry : info.includes().entrySet()) {
            ret.put(entry.getKey(), mapping.get(entry.getValue()).outputClass());
        }
        return ret;
    }

    private Map<String, ControllerInjection> getControllerInjection(final Map<Path, CompilationInfo> compilationInfoMapping, final CompilationInfo info) {
        final var ret = new HashMap<String, ControllerInjection>();
        ret.put(info.controllerClass(), getControllerInjection(info));
        for (final var entry : info.includes().entrySet()) {
            final var key = entry.getKey();
            final var value = entry.getValue();
            final var subInfo = compilationInfoMapping.get(value);
            ret.put(key, getControllerInjection(subInfo));
        }
        return ret;
    }

    private ControllerInjection getControllerInjection(final CompilationInfo info) {
        return new ControllerInjectionImpl(fieldInjectionTypes, methodsInjectionType, info.controllerClass());
    }
}
