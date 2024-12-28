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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestFXMLCompilerMojo {

    private final Compiler compiler;
    private final CompilationInfoProvider compilationInfoProvider;
    private final ControllerProvider controllerProvider;
    private final FXMLProvider fxmlProvider;
    private final MavenProject mavenProject;
    private final Path outputDirectory;
    private final int targetVersion;
    private final boolean useImageInputStreamConstructor;
    private final ControllerInjectionType controllerInjectionType;
    private final ControllerFieldInjectionType controllerFieldInjectionType;
    private final ControllerMethodsInjectionType controllerMethodsInjectionType;
    private final ResourceBundleInjectionType resourceBundleInjectionType;
    private final Map<String, String> resourceMap;
    private final Map<Path, Path> fxmls;
    private final CompilationInfo compilationInfo;
    private final FXMLCompilerMojo mojo;

    TestFXMLCompilerMojo(@Mock final Compiler compiler, @Mock final CompilationInfoProvider compilationInfoProvider,
                         @Mock final ControllerProvider controllerProvider, @Mock final FXMLProvider fxmlProvider,
                         @Mock final MavenProject mavenProject,
                         @Mock final ControllerInjectionType controllerInjectionType,
                         @Mock final ControllerFieldInjectionType controllerFieldInjectionType,
                         @Mock final ControllerMethodsInjectionType controllerMethodsInjectionType,
                         @Mock final ResourceBundleInjectionType resourceBundleInjectionType,
                         @Mock final CompilationInfo compilationInfo) {
        this.compiler = Objects.requireNonNull(compiler);
        this.compilationInfoProvider = Objects.requireNonNull(compilationInfoProvider);
        this.controllerProvider = Objects.requireNonNull(controllerProvider);
        this.fxmlProvider = Objects.requireNonNull(fxmlProvider);
        this.mavenProject = Objects.requireNonNull(mavenProject);
        this.outputDirectory = Path.of("output");
        this.targetVersion = 11;
        this.useImageInputStreamConstructor = true;
        this.controllerInjectionType = Objects.requireNonNull(controllerInjectionType);
        this.controllerFieldInjectionType = Objects.requireNonNull(controllerFieldInjectionType);
        this.controllerMethodsInjectionType = Objects.requireNonNull(controllerMethodsInjectionType);
        this.resourceBundleInjectionType = Objects.requireNonNull(resourceBundleInjectionType);
        this.resourceMap = Map.of("a", "b", "c", "d");
        this.compilationInfo = Objects.requireNonNull(compilationInfo);
        this.fxmls = new HashMap<>();
        this.mojo = new FXMLCompilerMojo(compiler, (p, o) -> compilationInfoProvider, controllerProvider,
                p -> fxmlProvider);
    }

    @BeforeEach
    void beforeEach() throws Exception {
        setValue("project", mavenProject);
        setValue("outputDirectory", outputDirectory);
        setIntValue("targetVersion", targetVersion);
        setBooleanValue("useImageInputStreamConstructor", useImageInputStreamConstructor);
        setValue("controllerInjectionType", controllerInjectionType);
        setValue("fieldInjectionType", controllerFieldInjectionType);
        setValue("methodInjectionType", controllerMethodsInjectionType);
        setValue("resourceInjectionType", resourceBundleInjectionType);
        setValue("resourceMap", resourceMap);
        when(fxmlProvider.getFXMLs()).thenReturn(fxmls);
        when(controllerProvider.getController(any())).then(i -> ((Path) i.getArgument(0)).toString());
        when(compilationInfoProvider.getCompilationInfo(any(), any(), anyMap())).thenReturn(compilationInfo);
        fxmls.put(Path.of("a"), Path.of("b"));
        fxmls.put(Path.of("c"), Path.of("d"));
    }

    @Test
    void testExecuteParallel() throws Exception {
        final var pathA = Path.of("a");
        final var pathC = Path.of("c");
        setIntValue("parallelism", 4);
        mojo.execute();
        for (final var p : fxmls.keySet()) {
            verify(controllerProvider).getController(p);
        }
        final var controllerMapping = Map.of(pathA, "a", pathC, "c");
        for (final var e : fxmls.entrySet()) {
            verify(compilationInfoProvider).getCompilationInfo(e.getValue(), e.getKey(), controllerMapping);
        }
        final var parameters = new GenerationParametersImpl(new GenerationCompatibilityImpl(targetVersion), useImageInputStreamConstructor, resourceMap,
                controllerInjectionType, controllerFieldInjectionType, controllerMethodsInjectionType, resourceBundleInjectionType);
        final var compilationInfoMapping = Map.of(pathA, compilationInfo, pathC, compilationInfo);
        for (final var entry : compilationInfoMapping.entrySet()) {
            verify(compiler).compile(entry.getKey(), entry.getValue(), compilationInfoMapping, parameters);
        }
    }

    @Test
    void testExecuteDefaultCores() throws Exception {
        final var pathA = Path.of("a");
        final var pathC = Path.of("c");
        setIntValue("parallelism", 0);
        mojo.execute();
        assertEquals(Runtime.getRuntime().availableProcessors(), getValue("parallelism"));
        for (final var p : fxmls.keySet()) {
            verify(controllerProvider).getController(p);
        }
        final var controllerMapping = Map.of(pathA, "a", pathC, "c");
        for (final var e : fxmls.entrySet()) {
            verify(compilationInfoProvider).getCompilationInfo(e.getValue(), e.getKey(), controllerMapping);
        }
        final var parameters = new GenerationParametersImpl(new GenerationCompatibilityImpl(targetVersion), useImageInputStreamConstructor, resourceMap,
                controllerInjectionType, controllerFieldInjectionType, controllerMethodsInjectionType, resourceBundleInjectionType);
        final var compilationInfoMapping = Map.of(pathA, compilationInfo, pathC, compilationInfo);
        for (final var entry : compilationInfoMapping.entrySet()) {
            verify(compiler).compile(entry.getKey(), entry.getValue(), compilationInfoMapping, parameters);
        }
    }

    @Test
    void testControllerProviderException() throws Exception {
        setIntValue("parallelism", 4);
        doThrow(MojoExecutionException.class).when(controllerProvider).getController(any());
        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    @Test
    void testCompilationInfoProviderException() throws Exception {
        setIntValue("parallelism", 4);
        doThrow(MojoExecutionException.class).when(compilationInfoProvider).getCompilationInfo(any(), any(), anyMap());
        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    @Test
    void testCompilerException() throws Exception {
        setIntValue("parallelism", 4);
        doThrow(MojoExecutionException.class).when(compiler).compile(any(), any(), anyMap(), any());
        assertThrows(MojoExecutionException.class, mojo::execute);
    }

    @Test
    void testExecuteSingleCore() throws Exception {
        final var pathA = Path.of("a");
        final var pathC = Path.of("c");
        setIntValue("parallelism", 1);
        mojo.execute();
        for (final var p : fxmls.keySet()) {
            verify(controllerProvider).getController(p);
        }
        final var controllerMapping = Map.of(pathA, "a", pathC, "c");
        for (final var e : fxmls.entrySet()) {
            verify(compilationInfoProvider).getCompilationInfo(e.getValue(), e.getKey(), controllerMapping);
        }
        final var parameters = new GenerationParametersImpl(new GenerationCompatibilityImpl(targetVersion), useImageInputStreamConstructor, resourceMap,
                controllerInjectionType, controllerFieldInjectionType, controllerMethodsInjectionType, resourceBundleInjectionType);
        final var compilationInfoMapping = Map.of(pathA, compilationInfo, pathC, compilationInfo);
        verify(compiler).compile(compilationInfoMapping, parameters);
    }

    @Test
    void testOverrideControllerInjectionType() throws Exception {
        setValue("fieldInjectionType", ControllerFieldInjectionType.FACTORY);
        mojo.execute();
        assertEquals(ControllerInjectionType.FACTORY, getValue("controllerInjectionType"));
    }

    private Object getValue(final String name) throws Exception {
        final var field = FXMLCompilerMojo.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(mojo);
    }

    private void setBooleanValue(final String name, final boolean value) throws Exception {
        final var field = FXMLCompilerMojo.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setBoolean(mojo, value);
    }

    private void setIntValue(final String name, final int value) throws Exception {
        final var field = FXMLCompilerMojo.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(mojo, value);
    }

    private void setValue(final String name, final Object value) throws Exception {
        final var field = FXMLCompilerMojo.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(mojo, value);
    }
}
