package com.github.gtache.fxml.compiler.maven.internal;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestFXMLProvider {

    private final MavenProject project;
    private final FXMLProvider provider;

    TestFXMLProvider(@Mock final MavenProject project) {
        this.project = Objects.requireNonNull(project);
        this.provider = new FXMLProvider(project);
    }

    @Test
    void testGetFXMLs(@TempDir final Path tempDir, @TempDir final Path otherTempDir) throws Exception {
        final var subFolder = tempDir.resolve("subFolder");
        Files.createDirectories(subFolder);
        Files.createFile(subFolder.resolve("subfxml1.fxml"));
        Files.createFile(subFolder.resolve("subfxml2.fxml"));
        Files.createFile(tempDir.resolve("fxml1.fxml"));
        Files.createFile(tempDir.resolve("fxml2.fxml"));

        final var otherSubFolder = otherTempDir.resolve("subFolder");
        Files.createDirectories(otherSubFolder);
        Files.createFile(otherSubFolder.resolve("subfxml1.fxml"));
        Files.createFile(otherSubFolder.resolve("subfxml2.fxml"));
        Files.createFile(otherTempDir.resolve("fxml1.fxml"));
        Files.createFile(otherTempDir.resolve("fxml2.fxml"));

        final var resource1 = mock(Resource.class);
        final var resource2 = mock(Resource.class);
        when(resource1.getDirectory()).thenReturn(tempDir.toString());
        when(resource2.getDirectory()).thenReturn(otherTempDir.toString());
        when(project.getResources()).thenReturn(List.of(resource1, resource2));

        final var expected = Map.of(
                subFolder.resolve("subfxml1.fxml"), tempDir,
                subFolder.resolve("subfxml2.fxml"), tempDir,
                tempDir.resolve("fxml1.fxml"), tempDir,
                tempDir.resolve("fxml2.fxml"), tempDir,
                otherSubFolder.resolve("subfxml1.fxml"), otherTempDir,
                otherSubFolder.resolve("subfxml2.fxml"), otherTempDir,
                otherTempDir.resolve("fxml1.fxml"), otherTempDir,
                otherTempDir.resolve("fxml2.fxml"), otherTempDir
        );
        final var map = provider.getFXMLs();
        assertEquals(expected, map);
    }
}
