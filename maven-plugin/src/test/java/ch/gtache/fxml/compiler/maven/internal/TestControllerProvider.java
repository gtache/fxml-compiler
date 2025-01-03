package ch.gtache.fxml.compiler.maven.internal;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestControllerProvider {

    private final ControllerProvider controllerProvider;

    TestControllerProvider() {
        this.controllerProvider = new ControllerProvider();
    }

    @Test
    void testGetController(@TempDir final Path tempDir) throws Exception {
        final var fxml = tempDir.resolve("fxml.fxml");
        Files.writeString(fxml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<BorderPane xmlns=\"http://javafx.com/javafx/22\" xmlns:fx=\"http://javafx.com/fxml/1\"" +
                "            fx:controller=\"LoadController\">" +
                "</BorderPane>\n");
        assertEquals("LoadController", controllerProvider.getController(fxml));
    }

    @Test
    void testGetControllerBlank(@TempDir final Path tempDir) throws Exception {
        final var fxml = tempDir.resolve("fxml.fxml");
        Files.writeString(fxml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<BorderPane xmlns=\"http://javafx.com/javafx/22\" xmlns:fx=\"http://javafx.com/fxml/1\">" +
                "</BorderPane>\n");
        assertThrows(MojoExecutionException.class, () -> controllerProvider.getController(fxml));
    }

    @Test
    void testGetControllerError() {
        assertThrows(MojoExecutionException.class, () -> controllerProvider.getController(Path.of("fxml.fxml")));
    }
}
