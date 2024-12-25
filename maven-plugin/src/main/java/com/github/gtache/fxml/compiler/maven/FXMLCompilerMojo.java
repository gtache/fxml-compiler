package com.github.gtache.fxml.compiler.maven;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerInjectionType;
import com.github.gtache.fxml.compiler.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.ResourceBundleInjectionType;
import com.github.gtache.fxml.compiler.compatibility.impl.GenerationCompatibilityImpl;
import com.github.gtache.fxml.compiler.impl.GenerationParametersImpl;
import com.github.gtache.fxml.compiler.maven.internal.CompilationInfo;
import com.github.gtache.fxml.compiler.maven.internal.CompilationInfoProvider;
import com.github.gtache.fxml.compiler.maven.internal.Compiler;
import com.github.gtache.fxml.compiler.maven.internal.ControllerProvider;
import com.github.gtache.fxml.compiler.maven.internal.FXMLProvider;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main mojo for FXML compiler
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class FXMLCompilerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "output-directory", defaultValue = "${project.build.directory}/generated-sources/java", required = true)
    private Path outputDirectory;

    @Parameter(property = "target-version", defaultValue = "21", required = true)
    private int targetVersion;

    @Parameter(property = "use-image-inputstream-constructor", defaultValue = "true", required = true)
    private boolean useImageInputStreamConstructor;

    @Parameter(property = "controller-injection", defaultValue = "INSTANCE", required = true)
    private ControllerInjectionType controllerInjectionType;

    @Parameter(property = "field-injection", defaultValue = "REFLECTION", required = true)
    private ControllerFieldInjectionType fieldInjectionType;

    @Parameter(property = "method-injection", defaultValue = "REFLECTION", required = true)
    private ControllerMethodsInjectionType methodInjectionType;

    @Parameter(property = "resource-injection", defaultValue = "CONSTRUCTOR", required = true)
    private ResourceBundleInjectionType resourceInjectionType;

    @Parameter(property = "resource-map")
    private Map<String, String> resourceMap;

    @Parameter(property = "parallelism", defaultValue = "1", required = true)
    private int parallelism;


    @Override
    public void execute() throws MojoExecutionException {
        if (fieldInjectionType == ControllerFieldInjectionType.FACTORY && controllerInjectionType != ControllerInjectionType.FACTORY) {
            getLog().warn("Field injection is set to FACTORY : Forcing controller injection to FACTORY");
            controllerInjectionType = ControllerInjectionType.FACTORY;
        }
        final var fxmls = FXMLProvider.getFXMLs(project);
        if (parallelism < 1) {
            parallelism = Runtime.getRuntime().availableProcessors();
        }
        if (parallelism > 1) {
            try (final var executor = Executors.newFixedThreadPool(parallelism)) {
                final var controllerMapping = createControllerMapping(fxmls, executor);
                final var compilationInfoMapping = createCompilationInfoMapping(fxmls, controllerMapping, executor);
                compile(compilationInfoMapping, executor);
            }
        } else {
            final var controllerMapping = createControllerMapping(fxmls);
            final var compilationInfoMapping = createCompilationInfoMapping(fxmls, controllerMapping);
            compile(compilationInfoMapping);
        }
    }

    private static Map<Path, String> createControllerMapping(final Map<? extends Path, ? extends Path> fxmls) throws MojoExecutionException {
        final var mapping = new HashMap<Path, String>();
        for (final var fxml : fxmls.keySet()) {
            mapping.put(fxml, ControllerProvider.getController(fxml));
        }
        return mapping;
    }

    private Map<Path, CompilationInfo> createCompilationInfoMapping(final Map<? extends Path, ? extends Path> fxmls,
                                                                    final Map<? extends Path, String> controllerMapping) throws MojoExecutionException {
        final var mapping = new HashMap<Path, CompilationInfo>();
        for (final var entry : fxmls.entrySet()) {
            final var info = CompilationInfoProvider.getCompilationInfo(entry.getValue(), entry.getKey(), controllerMapping, outputDirectory, project);
            mapping.put(entry.getKey(), info);
        }
        return mapping;
    }

    private void compile(final Map<Path, CompilationInfo> mapping) throws MojoExecutionException {
        final var parameters = new GenerationParametersImpl(new GenerationCompatibilityImpl(targetVersion), useImageInputStreamConstructor, resourceMap,
                controllerInjectionType, fieldInjectionType, methodInjectionType, resourceInjectionType);
        Compiler.compile(mapping, parameters);
        project.addCompileSourceRoot(outputDirectory.toAbsolutePath().toString());
    }

    private static Map<Path, String> createControllerMapping(final Map<? extends Path, ? extends Path> fxmls,
                                                             final ExecutorService executor) {
        final var mapping = new ConcurrentHashMap<Path, String>();
        for (final var fxml : fxmls.keySet()) {
            executor.submit(() -> {
                try {
                    mapping.put(fxml, ControllerProvider.getController(fxml));
                } catch (final MojoExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return mapping;
    }

    private Map<Path, CompilationInfo> createCompilationInfoMapping(final Map<? extends Path, ? extends Path> fxmls,
                                                                    final Map<? extends Path, String> controllerMapping, final ExecutorService executor) {
        final var mapping = new ConcurrentHashMap<Path, CompilationInfo>();
        for (final var entry : fxmls.entrySet()) {
            executor.submit(() -> {
                try {
                    final var info = CompilationInfoProvider.getCompilationInfo(entry.getValue(), entry.getKey(), controllerMapping, outputDirectory, project);
                    mapping.put(entry.getKey(), info);
                } catch (final MojoExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return mapping;
    }

    private void compile(final Map<Path, CompilationInfo> mapping, final ExecutorService executor) throws MojoExecutionException {
        final var parameters = new GenerationParametersImpl(new GenerationCompatibilityImpl(targetVersion), useImageInputStreamConstructor, resourceMap,
                controllerInjectionType, fieldInjectionType, methodInjectionType, resourceInjectionType);
        mapping.forEach((p, i) -> executor.submit(() -> {
            try {
                Compiler.compile(p, i, mapping, parameters);
            } catch (final MojoExecutionException e) {
                throw new RuntimeException(e);
            }
        }));
        project.addCompileSourceRoot(outputDirectory.toAbsolutePath().toString());
    }
}
