package com.github.gtache.fxml.compiler.maven.internal;

import com.github.gtache.fxml.compiler.impl.ControllerFieldInfoImpl;
import com.github.gtache.fxml.compiler.impl.ControllerInfoImpl;
import com.github.gtache.fxml.compiler.impl.GenericTypesImpl;
import org.apache.maven.plugin.MojoExecutionException;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestControllerInfoProvider {

    private final CompilationInfo compilationInfo;

    TestControllerInfoProvider(@Mock final CompilationInfo compilationInfo) {
        this.compilationInfo = Objects.requireNonNull(compilationInfo);
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
                    private ComboBox<Integer> fullBox;
                
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
        final var controllerClass = "com.github.gtache.fxml.compiler.maven.internal.LoadController";
        when(compilationInfo.controllerClass()).thenReturn(controllerClass);
        when(compilationInfo.injectedFields()).thenReturn(Set.of(new FieldInfo("EventHandler", "keyEventHandler"),
                new FieldInfo("ComboBox", "comboBox"), new FieldInfo("Button", "button"),
                new FieldInfo("ComboBox", "rawBox"), new FieldInfo("TableColumn", "tableColumn"),
                new FieldInfo("ComboBox", "abc"), new FieldInfo("javafx.scene.control.ComboBox", "fullBox")));
        when(compilationInfo.injectedMethods()).thenReturn(Set.of("onClick", "onOtherClick"));
        final var expectedInfo = new ControllerInfoImpl(controllerClass,
                Map.of("onClick", false, "onOtherClick", true), Map.of("keyEventHandler", new ControllerFieldInfoImpl("keyEventHandler", List.of(new GenericTypesImpl("javafx.event.KeyEvent", List.of()))),
                "comboBox", new ControllerFieldInfoImpl("comboBox", List.of(new GenericTypesImpl("String", List.of()))), "button", new ControllerFieldInfoImpl("button", List.of()),
                "rawBox", new ControllerFieldInfoImpl("rawBox", List.of()), "fullBox", new ControllerFieldInfoImpl("fullBox", List.of(new GenericTypesImpl("Integer", List.of()))),
                "tableColumn", new ControllerFieldInfoImpl("tableColumn", List.of(new GenericTypesImpl("Integer", List.of()),
                        new GenericTypesImpl("javafx.scene.control.ComboBox", List.of(new GenericTypesImpl("String", List.of())))))), true);
        final var actual = ControllerInfoProvider.getControllerInfo(compilationInfo);
        assertEquals(expectedInfo, actual);
    }

    @Test
    void testGetControllerInfoMethodNotFound(@TempDir final Path tempDir) throws Exception {
        final var fxml = tempDir.resolve("fxml.fxml");
        Files.writeString(fxml, """
                package com.github.gtache.fxml.compiler.maven.internal;
                
                import javafx.event.EventHandler;
                import javafx.event.KeyEvent;
                import javafx.scene.control.*;
                
                public class LoadController {
                
                }
                """);
        when(compilationInfo.controllerFile()).thenReturn(fxml);
        when(compilationInfo.controllerClass()).thenReturn("com.github.gtache.fxml.compiler.maven.internal.LoadController");
        when(compilationInfo.injectedFields()).thenReturn(Set.of());
        when(compilationInfo.injectedMethods()).thenReturn(Set.of("onClick"));
        assertThrows(MojoExecutionException.class, () -> ControllerInfoProvider.getControllerInfo(compilationInfo));
    }

    @Test
    void testGetControllerInfoException() {
        when(compilationInfo.controllerFile()).thenReturn(Path.of("/in/a/b/c/whatever"));
        assertThrows(MojoExecutionException.class, () -> ControllerInfoProvider.getControllerInfo(compilationInfo));
    }
}
