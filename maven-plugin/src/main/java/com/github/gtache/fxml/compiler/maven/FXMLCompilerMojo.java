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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;

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

    private final Compiler compiler;
    private final CompilationInfoProvider.Factory compilationInfoProviderFactory;
    private final ControllerProvider controllerProvider;
    private final FXMLProvider.Factory fxmlProviderFactory;

    /**
     * Instantiates a new MOJO with the given helpers (used for testing)
     *
     * @param compiler                       The compiler
     * @param compilationInfoProviderFactory The compilation info provider
     * @param controllerProvider             The controller provider
     * @param fxmlProviderFactory            The FXML provider factory
     * @throws NullPointerException If any parameter is null
     */
    FXMLCompilerMojo(final Compiler compiler, final CompilationInfoProvider.Factory compilationInfoProviderFactory,
                     final ControllerProvider controllerProvider, final FXMLProvider.Factory fxmlProviderFactory) {
        this.compiler = requireNonNull(compiler);
        this.compilationInfoProviderFactory = requireNonNull(compilationInfoProviderFactory);
        this.controllerProvider = requireNonNull(controllerProvider);
        this.fxmlProviderFactory = requireNonNull(fxmlProviderFactory);
    }

    /**
     * Instantiates a new MOJO
     */
    FXMLCompilerMojo() {
        this(new Compiler(), CompilationInfoProvider::new, new ControllerProvider(), FXMLProvider::new);
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            if (fieldInjectionType == ControllerFieldInjectionType.FACTORY && controllerInjectionType != ControllerInjectionType.FACTORY) {
                getLog().warn("Field injection is set to FACTORY : Forcing controller injection to FACTORY");
                controllerInjectionType = ControllerInjectionType.FACTORY;
            }
            final var fxmls = fxmlProviderFactory.create(project).getFXMLs();
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
        } catch (final RuntimeException e) {
            throw new MojoExecutionException(e);
        }
    }

    private Map<Path, String> createControllerMapping(final Map<? extends Path, ? extends Path> fxmls) throws MojoExecutionException {
        final var mapping = new HashMap<Path, String>();
        for (final var fxml : fxmls.keySet()) {
            mapping.put(fxml, controllerProvider.getController(fxml));
        }
        return mapping;
    }

    private Map<Path, CompilationInfo> createCompilationInfoMapping(final Map<? extends Path, ? extends Path> fxmls,
                                                                    final Map<? extends Path, String> controllerMapping) throws MojoExecutionException {
        final var mapping = new HashMap<Path, CompilationInfo>();
        final var compilationInfoProvider = compilationInfoProviderFactory.create(project, outputDirectory);
        for (final var entry : fxmls.entrySet()) {
            final var info = compilationInfoProvider.getCompilationInfo(entry.getValue(), entry.getKey(), controllerMapping);
            mapping.put(entry.getKey(), info);
        }
        return mapping;
    }

    private void compile(final Map<Path, CompilationInfo> mapping) throws MojoExecutionException {
        final var parameters = new GenerationParametersImpl(new GenerationCompatibilityImpl(targetVersion), useImageInputStreamConstructor, resourceMap,
                controllerInjectionType, fieldInjectionType, methodInjectionType, resourceInjectionType);
        compiler.compile(mapping, parameters);
        project.addCompileSourceRoot(outputDirectory.toAbsolutePath().toString());
    }

    private Map<Path, String> createControllerMapping(final Map<? extends Path, ? extends Path> fxmls,
                                                      final Executor executor) {
        final var futures = new ArrayList<CompletableFuture<ControllerMapping>>(fxmls.size());
        for (final var fxml : fxmls.keySet()) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    final var controller = controllerProvider.getController(fxml);
                    return new ControllerMapping(fxml, controller);
                } catch (final MojoExecutionException e) {
                    throw new CompletionException(e);
                }
            }, executor));
        }
        final var mapping = new HashMap<Path, String>();
        futures.forEach(c -> {
            final var joined = c.join();
            mapping.put(joined.fxml(), joined.controller());
        });
        return mapping;
    }

    private record ControllerMapping(Path fxml, String controller) {
    }

    private Map<Path, CompilationInfo> createCompilationInfoMapping(final Map<? extends Path, ? extends Path> fxmls,
                                                                    final Map<? extends Path, String> controllerMapping,
                                                                    final Executor executor) {
        final var compilationInfoProvider = compilationInfoProviderFactory.create(project, outputDirectory);
        final var futures = new ArrayList<CompletableFuture<CompilationInfoMapping>>(fxmls.size());
        for (final var entry : fxmls.entrySet()) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    final var info = compilationInfoProvider.getCompilationInfo(entry.getValue(), entry.getKey(),
                            controllerMapping);
                    return new CompilationInfoMapping(entry.getKey(), info);
                } catch (final MojoExecutionException e) {
                    throw new CompletionException(e);
                }
            }, executor));
        }
        final var mapping = new HashMap<Path, CompilationInfo>();
        futures.forEach(c -> {
            final var joined = c.join();
            mapping.put(joined.fxml(), joined.info());
        });
        return mapping;
    }

    private record CompilationInfoMapping(Path fxml, CompilationInfo info) {

    }

    private void compile(final Map<Path, CompilationInfo> mapping, final Executor executor) {
        final var parameters = new GenerationParametersImpl(new GenerationCompatibilityImpl(targetVersion),
                useImageInputStreamConstructor, resourceMap, controllerInjectionType, fieldInjectionType,
                methodInjectionType, resourceInjectionType);
        final var futures = new ArrayList<CompletableFuture<Void>>(mapping.size());
        mapping.forEach((p, i) -> futures.add(CompletableFuture.runAsync(() -> {
            try {
                compiler.compile(p, i, mapping, parameters);
            } catch (final MojoExecutionException e) {
                throw new CompletionException(e);
            }
        }, executor)));
        futures.forEach(CompletableFuture::join);
        project.addCompileSourceRoot(outputDirectory.toAbsolutePath().toString());
    }
}
