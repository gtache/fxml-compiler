package ch.gtache.fxml.compiler.impl;

import ch.gtache.fxml.compiler.ControllerInfo;
import ch.gtache.fxml.compiler.GenerationParameters;
import ch.gtache.fxml.compiler.GenerationRequest;
import ch.gtache.fxml.compiler.SourceInfo;
import ch.gtache.fxml.compiler.parsing.ParsedObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestGenerationRequestImpl {

    private final GenerationParameters parameters;
    private final ControllerInfo controllerInfo;
    private final SourceInfo sourceInfo;
    private final ParsedObject rootObject;
    private final String outputClassName;
    private final GenerationRequest request;

    TestGenerationRequestImpl(@Mock final GenerationParameters parameters, @Mock final ControllerInfo controllerInfo,
                              @Mock final SourceInfo sourceInfo, @Mock final ParsedObject rootObject) {
        this.parameters = Objects.requireNonNull(parameters);
        this.controllerInfo = Objects.requireNonNull(controllerInfo);
        this.sourceInfo = Objects.requireNonNull(sourceInfo);
        this.rootObject = Objects.requireNonNull(rootObject);
        this.outputClassName = "class";
        this.request = new GenerationRequestImpl(parameters, controllerInfo, sourceInfo, rootObject, outputClassName);
    }

    @Test
    void testGetters() {
        assertEquals(parameters, request.parameters());
        assertEquals(controllerInfo, request.controllerInfo());
        assertEquals(sourceInfo, request.sourceInfo());
        assertEquals(rootObject, request.rootObject());
        assertEquals(outputClassName, request.outputClassName());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new GenerationRequestImpl(null, controllerInfo, sourceInfo, rootObject, outputClassName));
        assertThrows(NullPointerException.class, () -> new GenerationRequestImpl(parameters, null, sourceInfo, rootObject, outputClassName));
        assertThrows(NullPointerException.class, () -> new GenerationRequestImpl(parameters, controllerInfo, null, rootObject, outputClassName));
        assertThrows(NullPointerException.class, () -> new GenerationRequestImpl(parameters, controllerInfo, sourceInfo, null, outputClassName));
        assertThrows(NullPointerException.class, () -> new GenerationRequestImpl(parameters, controllerInfo, sourceInfo, rootObject, null));
    }
}
