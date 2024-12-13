package com.github.gtache.fxml.compiler.maven.internal;

import com.github.gtache.fxml.compiler.impl.ControllerFieldInfoImpl;
import com.github.gtache.fxml.compiler.impl.ControllerInfoImpl;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestControllerInfoProvider {

    private final CompilationInfo compilationInfo;
    private final ControllerInfoProvider controllerInfoProvider;

    TestControllerInfoProvider(@Mock final CompilationInfo compilationInfo) {
        this.compilationInfo = Objects.requireNonNull(compilationInfo);
        this.controllerInfoProvider = new ControllerInfoProvider();
    }


    @Test
    void testGetControllerInfo(@TempDir final Path tempDir) throws Exception {
        final var fxml = tempDir.resolve("fxml.fxml");
        Files.writeString(fxml, """
                package com.github.gtache.fxml.compiler.maven.internal;
                
                import javafx.event.EventHandler;
                import javafx.event.KeyEvent;
                import javafx.scene.control.*;
                
                public class LoadController {
                
                    private EventHandler<KeyEvent> keyEventHandler;
                    private ComboBox<String> comboBox;
                    private Button button;
                    private ComboBox rawBox;
                    private TableColumn<Integer, ComboBox<String>> tableColumn;
                
                    @FXML
                    void initialize() {
                    }
                
                    @FXML
                    private void onClick(){
                    }
                
                    @FXML
                    private void onOtherClick(final KeyEvent event){
                    }
                }
                """);
        when(compilationInfo.controllerFile()).thenReturn(fxml);
        final var expectedInfo = new ControllerInfoImpl("com.github.gtache.fxml.compiler.maven.internal.LoadController",
                Map.of("onClick", false, "onOtherClick", true), Map.of("keyEventHandler", new ControllerFieldInfoImpl("keyEventHandler", List.of()),
                "comboBox", new ControllerFieldInfoImpl("comboBox", List.of("String")), "button", new ControllerFieldInfoImpl("button", List.of()),
                "rawBox", new ControllerFieldInfoImpl("rawBox", List.of()),
                "tableColumn", new ControllerFieldInfoImpl("tableColumn", List.of("Integer", "javafx.scene.control.ComboBox<String>"))), true);
        final var actual = ControllerInfoProvider.getControllerInfo(compilationInfo);
        assertEquals(expectedInfo, actual);
    }

    @Test
    void testGetControllerInfoException() {
        when(compilationInfo.controllerFile()).thenReturn(Paths.get("/whatever"));
        assertThrows(MojoExecutionException.class, () -> ControllerInfoProvider.getControllerInfo(compilationInfo));
    }
}
