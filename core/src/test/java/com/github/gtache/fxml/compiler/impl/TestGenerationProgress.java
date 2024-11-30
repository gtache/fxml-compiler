package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.impl.internal.GenerationProgress;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TestGenerationProgress {

    private final GenerationRequest request;
    private final Map<String, String> idToVariableName;
    private final Map<String, ParsedObject> idToObject;
    private final Map<String, AtomicInteger> variableNameCounters;
    private final SequencedCollection<String> controllerFactoryPostAction;
    private final StringBuilder sb;
    private final GenerationProgress progress;

    TestGenerationProgress(@Mock final GenerationRequest request, @Mock final ParsedObject object) {
        this.request = Objects.requireNonNull(request);
        this.idToVariableName = new HashMap<>();
        idToVariableName.put("var1", "var2");
        this.idToObject = new HashMap<>();
        idToObject.put("var1", object);
        this.variableNameCounters = new HashMap<>();
        variableNameCounters.put("var", new AtomicInteger(0));
        this.controllerFactoryPostAction = new ArrayList<>();
        controllerFactoryPostAction.add("bla");
        this.sb = new StringBuilder("test");
        this.progress = new GenerationProgress(request, idToVariableName, idToObject, variableNameCounters, controllerFactoryPostAction, sb);
    }

    @Test
    void testGetters() {
        assertEquals(request, progress.request());
        assertEquals(idToVariableName, progress.idToVariableName());
        assertEquals(variableNameCounters, progress.variableNameCounters());
        assertEquals(controllerFactoryPostAction, progress.controllerFactoryPostAction());
        assertEquals(sb, progress.stringBuilder());
    }

    @Test
    void testConstructorDoesntCopy() {
        idToVariableName.clear();
        assertEquals(idToVariableName, progress.idToVariableName());

        idToObject.clear();
        assertEquals(idToObject, progress.idToObject());

        variableNameCounters.clear();
        assertEquals(variableNameCounters, progress.variableNameCounters());

        controllerFactoryPostAction.clear();
        assertEquals(controllerFactoryPostAction, progress.controllerFactoryPostAction());

        sb.setLength(0);
        assertEquals(sb, progress.stringBuilder());
    }

    @Test
    void testCanModify() {
        progress.idToVariableName().put("var3", "var4");
        assertEquals(idToVariableName, progress.idToVariableName());

        progress.idToObject().put("var3", mock(ParsedObject.class));
        assertEquals(idToObject, progress.idToObject());

        progress.variableNameCounters().put("var5", new AtomicInteger(0));
        assertEquals(variableNameCounters, progress.variableNameCounters());

        progress.controllerFactoryPostAction().add("bla2");
        assertEquals(controllerFactoryPostAction, progress.controllerFactoryPostAction());

        progress.stringBuilder().append("test2");
        assertEquals(sb, progress.stringBuilder());
    }

    @Test
    void testOtherConstructor() {
        final var progress2 = new GenerationProgress(request);
        assertEquals(request, progress2.request());
        assertEquals(Map.of(), progress2.idToVariableName());
        assertEquals(Map.of(), progress2.variableNameCounters());
        assertEquals(List.of(), progress2.controllerFactoryPostAction());
        assertEquals("", progress2.stringBuilder().toString());
    }

    @Test
    void testGetNextVariableName() {
        assertEquals("var0", progress.getNextVariableName("var"));
        assertEquals("var1", progress.getNextVariableName("var"));
        assertEquals("var2", progress.getNextVariableName("var"));
        assertEquals("bla0", progress.getNextVariableName("bla"));
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new GenerationProgress(null, idToVariableName, idToObject, variableNameCounters, controllerFactoryPostAction, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, null, idToObject, variableNameCounters, controllerFactoryPostAction, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, idToVariableName, null, variableNameCounters, controllerFactoryPostAction, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, idToVariableName, idToObject, null, controllerFactoryPostAction, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, idToVariableName, idToObject, variableNameCounters, null, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, idToVariableName, idToObject, variableNameCounters, controllerFactoryPostAction, null));
    }

}
