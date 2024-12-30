package com.github.gtache.fxml.compiler.maven.internal;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestCompilationInfoProvider {

    private final MavenProject project;

    TestCompilationInfoProvider(@Mock final MavenProject project) {
        this.project = Objects.requireNonNull(project);
    }

    @Test
    void testCompleteExample(@TempDir final Path tempDir) throws Exception {
        final var path = copyFile("infoView.fxml", tempDir);
        final var includedPath = path.getParent().resolve("includeView.fxml");
        final var controllerPath = path.getParent().resolve("InfoController.java");
        final var controllerClass = "com.github.gtache.fxml.compiler.maven.internal.InfoController";
        Files.createFile(controllerPath);
        when(project.getCompileSourceRoots()).thenReturn(List.of(tempDir.toString()));
        final var expected = new CompilationInfo(path, path.getParent().resolve("InfoView.java"),
                "com.github.gtache.fxml.compiler.maven.internal.InfoView", controllerPath, controllerClass,
                Set.of(new FieldInfo("javafx.event.EventHandler", "onContextMenuRequested"), new FieldInfo("Button", "button"),
                        new FieldInfo("com.github.gtache.fxml.compiler.maven.internal.IncludeController", "includeViewController")),
                Set.of("onAction"), Map.of("includeView.fxml", new Inclusion(path.getParent().resolve("includeView.fxml"), 1)), true);
        final var compilationInfoProvider = new CompilationInfoProvider(project, tempDir);
        final var actual = compilationInfoProvider.getCompilationInfo(tempDir, path, Map.of(includedPath, "com.github.gtache.fxml.compiler.maven.internal.IncludeController"));
        assertEquals(expected, actual);
    }

    @Test
    void testComplexFilename(@TempDir final Path tempDir) throws Exception {
        final var path = copyFile("com_plex-view.fxml", tempDir);
        final var controllerPath = path.getParent().resolve("InfoController.java");
        final var controllerClass = "com.github.gtache.fxml.compiler.maven.internal.InfoController";
        Files.createFile(controllerPath);
        when(project.getCompileSourceRoots()).thenReturn(List.of(tempDir.toString()));
        final var expected = new CompilationInfo(path, path.getParent().resolve("ComPlexView.java"),
                "com.github.gtache.fxml.compiler.maven.internal.ComPlexView", controllerPath, controllerClass,
                Set.of(), Set.of(), Map.of(), false);
        final var compilationInfoProvider = new CompilationInfoProvider(project, tempDir);
        final var actual = compilationInfoProvider.getCompilationInfo(tempDir, path, Map.of());
        assertEquals(expected, actual);
    }

    @Test
    void testNoController(@TempDir final Path tempDir) throws Exception {
        final var path = copyFile("noController.fxml", tempDir);
        final var controllerPath = path.getParent().resolve("InfoController.java");
        Files.createFile(controllerPath);
        when(project.getCompileSourceRoots()).thenReturn(List.of(tempDir.toString()));
        final var compilationInfoProvider = new CompilationInfoProvider(project, tempDir);
        assertThrows(MojoExecutionException.class, () -> compilationInfoProvider.getCompilationInfo(tempDir, path, Map.of()));
    }

    @Test
    void testIncludeNoSource(@TempDir final Path tempDir) throws Exception {
        final var path = copyFile("missingSource.fxml", tempDir);
        final var includedPath = path.getParent().resolve("includeView.fxml");
        final var controllerPath = path.getParent().resolve("InfoController.java");
        Files.createFile(controllerPath);
        when(project.getCompileSourceRoots()).thenReturn(List.of(tempDir.toString()));
        final var compilationInfoProvider = new CompilationInfoProvider(project, tempDir);
        assertThrows(MojoExecutionException.class, () -> compilationInfoProvider.getCompilationInfo(tempDir, path, Map.of(includedPath, "com.github.gtache.fxml.compiler.maven.internal.IncludeController")));
    }

    @Test
    void testCantFindControllerFile(@TempDir final Path tempDir) throws Exception {
        final var path = copyFile("infoView.fxml", tempDir);
        final var includedPath = path.getParent().resolve("includeView.fxml");
        final var controllerPath = path.getParent().resolve("InfoController.java");
        Files.createFile(controllerPath);
        when(project.getCompileSourceRoots()).thenReturn(List.of());
        final var compilationInfoProvider = new CompilationInfoProvider(project, tempDir);
        assertThrows(MojoExecutionException.class, () -> compilationInfoProvider.getCompilationInfo(tempDir, path, Map.of(includedPath, "com.github.gtache.fxml.compiler.maven.internal.IncludeController")));
    }

    @Test
    void testCantFindIncludedControllerClass(@TempDir final Path tempDir) throws Exception {
        final var path = copyFile("infoView.fxml", tempDir);
        final var controllerPath = path.getParent().resolve("InfoController.java");
        Files.createFile(controllerPath);
        when(project.getCompileSourceRoots()).thenReturn(List.of(tempDir.toString()));
        final var compilationInfoProvider = new CompilationInfoProvider(project, tempDir);
        assertThrows(MojoExecutionException.class, () -> compilationInfoProvider.getCompilationInfo(tempDir, path, Map.of()));
    }

    @Test
    void testNoResourceBundle(@TempDir final Path tempDir) throws Exception {
        final var path = copyFile("noResourceBundle.fxml", tempDir);
        final var includedPath = path.getParent().resolve("includeView.fxml");
        final var controllerPath = path.getParent().resolve("InfoController.java");
        final var controllerClass = "com.github.gtache.fxml.compiler.maven.internal.InfoController";
        Files.createFile(controllerPath);
        when(project.getCompileSourceRoots()).thenReturn(List.of(tempDir.toString()));
        final var expected = new CompilationInfo(path, path.getParent().resolve("NoResourceBundle.java"),
                "com.github.gtache.fxml.compiler.maven.internal.NoResourceBundle", controllerPath, controllerClass,
                Set.of(new FieldInfo("javafx.event.EventHandler", "onContextMenuRequested"), new FieldInfo("Button", "button"),
                        new FieldInfo("com.github.gtache.fxml.compiler.maven.internal.IncludeController", "includeViewController")),
                Set.of("onAction"), Map.of("includeView.fxml", new Inclusion(path.getParent().resolve("includeView.fxml"), 1)), false);
        final var compilationInfoProvider = new CompilationInfoProvider(project, tempDir);
        final var actual = compilationInfoProvider.getCompilationInfo(tempDir, path, Map.of(includedPath, "com.github.gtache.fxml.compiler.maven.internal.IncludeController"));
        assertEquals(expected, actual);
    }

    @Test
    void testUnknownOn(@TempDir final Path tempDir) throws Exception {
        final var path = copyFile("unknownOn.fxml", tempDir);
        final var controllerPath = path.getParent().resolve("InfoController.java");
        Files.createFile(controllerPath);
        when(project.getCompileSourceRoots()).thenReturn(List.of(tempDir.toString()));
        final var compilationInfoProvider = new CompilationInfoProvider(project, tempDir);
        assertThrows(MojoExecutionException.class, () -> compilationInfoProvider.getCompilationInfo(tempDir, path, Map.of()));
    }

    private Path copyFile(final String source, final Path tempDir) throws IOException {
        final var out = tempDir.resolve("com").resolve("github").resolve("gtache").resolve("fxml").resolve("compiler").resolve("maven").resolve("internal").resolve(source);
        Files.createDirectories(out.getParent());
        try (final var in = getClass().getResourceAsStream(source)) {
            assertNotNull(in);
            Files.copy(in, out);
        }
        return out;
    }
}
