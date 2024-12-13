package com.github.gtache.fxml.compiler.maven;

import com.github.gtache.fxml.compiler.compatibility.impl.GenerationCompatibilityImpl;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.impl.GenerationParametersImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;
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
    private ControllerInjectionTypes controllerInjectionType;

    @Parameter(property = "field-injection", defaultValue = "REFLECTION", required = true)
    private ControllerFieldInjectionTypes fieldInjectionType;

    @Parameter(property = "method-injection", defaultValue = "REFLECTION", required = true)
    private ControllerMethodsInjectionType methodInjectionType;

    @Parameter(property = "resource-injection", defaultValue = "CONSTRUCTOR", required = true)
    private ResourceBundleInjectionTypes resourceInjectionType;

    @Parameter(property = "resource-map")
    private Map<String, String> resourceMap;


    @Override
    public void execute() throws MojoExecutionException {
        if (fieldInjectionType == ControllerFieldInjectionTypes.FACTORY && controllerInjectionType != ControllerInjectionTypes.FACTORY) {
            getLog().warn("Field injection is set to FACTORY : Forcing controller injection to FACTORY");
            controllerInjectionType = ControllerInjectionTypes.FACTORY;
        }
        final var fxmls = FXMLProvider.getFXMLs(project);
        final var controllerMapping = createControllerMapping(fxmls);
        final var compilationInfoMapping = createCompilationInfoMapping(fxmls, controllerMapping);
        compile(compilationInfoMapping);
    }

    private static Map<Path, String> createControllerMapping(final Map<? extends Path, ? extends Path> fxmls) throws MojoExecutionException {
        final var mapping = new HashMap<Path, String>();
        for (final var fxml : fxmls.keySet()) {
            mapping.put(fxml, ControllerProvider.getController(fxml));
        }
        return mapping;
    }

    private Map<Path, CompilationInfo> createCompilationInfoMapping(final Map<? extends Path, ? extends Path> fxmls, final Map<? extends Path, String> controllerMapping) throws MojoExecutionException {
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
}
