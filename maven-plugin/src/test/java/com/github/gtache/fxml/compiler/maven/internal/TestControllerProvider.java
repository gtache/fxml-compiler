package com.github.gtache.fxml.compiler.maven.internal;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestControllerProvider {

    @Test
    void testGetController(@TempDir final Path tempDir) throws Exception {
        final var fxml = tempDir.resolve("fxml.fxml");
        Files.writeString(fxml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<BorderPane xmlns=\"http://javafx.com/javafx/22\" xmlns:fx=\"http://javafx.com/fxml/1\"" +
                "            fx:controller=\"LoadController\">" +
                "</BorderPane>\n");
        assertEquals("LoadController", ControllerProvider.getController(fxml));
    }

    @Test
    void testGetControllerBlank(@TempDir final Path tempDir) throws Exception {
        final var fxml = tempDir.resolve("fxml.fxml");
        Files.writeString(fxml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<BorderPane xmlns=\"http://javafx.com/javafx/22\" xmlns:fx=\"http://javafx.com/fxml/1\">" +
                "</BorderPane>\n");
        assertThrows(MojoExecutionException.class, () -> ControllerProvider.getController(fxml));
    }

    @Test
    void testGetControllerError() {
        assertThrows(MojoExecutionException.class, () -> ControllerProvider.getController(Paths.get("fxml.fxml")));
    }
}
