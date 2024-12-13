package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestGenerationProgress {

    private final GenerationRequest request;
    private final VariableInfo variableInfo;
    private final Map<String, VariableInfo> idToVariableInfo;
    private final Map<String, AtomicInteger> variableNameCounters;
    private final SequencedMap<String, String> controllerClassToVariable;
    private final SequencedCollection<String> controllerFactoryPostAction;
    private final StringBuilder sb;
    private final GenerationProgress progress;

    TestGenerationProgress(@Mock final GenerationRequest request, @Mock final VariableInfo variableInfo) {
        this.request = requireNonNull(request);
        this.variableInfo = requireNonNull(variableInfo);
        this.idToVariableInfo = new HashMap<>();
        idToVariableInfo.put("var1", variableInfo);
        this.controllerClassToVariable = new LinkedHashMap<String, String>();
        controllerClassToVariable.put("bla", "var1");
        controllerClassToVariable.put("bla2", "var2");
        this.variableNameCounters = new HashMap<>();
        variableNameCounters.put("var", new AtomicInteger(0));
        this.controllerFactoryPostAction = new ArrayList<>();
        controllerFactoryPostAction.add("bla");
        this.sb = new StringBuilder("test");
        this.progress = new GenerationProgress(request, idToVariableInfo, variableNameCounters, controllerClassToVariable, controllerFactoryPostAction, sb);
    }

    @Test
    void testGetters() {
        assertEquals(request, progress.request());
        assertEquals(idToVariableInfo, progress.idToVariableInfo());
        assertEquals(variableNameCounters, progress.variableNameCounters());
        assertEquals(controllerClassToVariable, progress.controllerClassToVariable());
        assertEquals(controllerFactoryPostAction, progress.controllerFactoryPostAction());
        assertEquals(sb, progress.stringBuilder());
    }

    @Test
    void testConstructorDoesntCopy() {
        idToVariableInfo.clear();
        assertEquals(idToVariableInfo, progress.idToVariableInfo());

        variableNameCounters.clear();
        assertEquals(variableNameCounters, progress.variableNameCounters());

        controllerClassToVariable.clear();
        assertEquals(controllerClassToVariable, progress.controllerClassToVariable());

        controllerFactoryPostAction.clear();
        assertEquals(controllerFactoryPostAction, progress.controllerFactoryPostAction());

        sb.setLength(0);
        assertEquals(sb, progress.stringBuilder());
    }

    @Test
    void testCanModify() {
        progress.idToVariableInfo().put("var3", variableInfo);
        assertEquals(idToVariableInfo, progress.idToVariableInfo());

        progress.variableNameCounters().put("var5", new AtomicInteger(0));
        assertEquals(variableNameCounters, progress.variableNameCounters());

        progress.controllerClassToVariable().put("bla3", "var3");
        assertEquals(controllerClassToVariable, progress.controllerClassToVariable());

        progress.controllerFactoryPostAction().add("bla2");
        assertEquals(controllerFactoryPostAction, progress.controllerFactoryPostAction());

        progress.stringBuilder().append("test2");
        assertEquals(sb, progress.stringBuilder());
    }

    @Test
    void testOtherConstructor() {
        final var progress2 = new GenerationProgress(request);
        assertEquals(request, progress2.request());
        assertEquals(Map.of(), progress2.idToVariableInfo());
        assertEquals(Map.of(), progress2.variableNameCounters());
        assertEquals(Map.of(), progress2.controllerClassToVariable());
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
        assertThrows(NullPointerException.class, () -> new GenerationProgress(null, idToVariableInfo, variableNameCounters, controllerClassToVariable, controllerFactoryPostAction, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, null, variableNameCounters, controllerClassToVariable, controllerFactoryPostAction, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, idToVariableInfo, null, controllerClassToVariable, controllerFactoryPostAction, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, idToVariableInfo, variableNameCounters, null, controllerFactoryPostAction, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, idToVariableInfo, variableNameCounters, controllerClassToVariable, null, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, idToVariableInfo, variableNameCounters, controllerClassToVariable, controllerFactoryPostAction, null));
    }

}
