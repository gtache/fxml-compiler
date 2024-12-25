package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestGenerationProgress {

    private final GenerationRequest request;
    private final SequencedCollection<String> controllerFactoryPostAction;
    private final StringBuilder sb;
    private final GenerationProgress progress;

    TestGenerationProgress(@Mock final GenerationRequest request) {
        this.request = requireNonNull(request);
        this.controllerFactoryPostAction = new ArrayList<>();
        controllerFactoryPostAction.add("bla");
        this.sb = new StringBuilder("test");
        this.progress = new GenerationProgress(request, controllerFactoryPostAction, sb);
    }

    @Test
    void testGetters() {
        assertEquals(request, progress.request());
        assertEquals(controllerFactoryPostAction, progress.controllerFactoryPostAction());
        assertEquals(sb, progress.stringBuilder());
    }

    @Test
    void testConstructorDoesntCopy() {
        controllerFactoryPostAction.clear();
        assertEquals(controllerFactoryPostAction, progress.controllerFactoryPostAction());

        sb.setLength(0);
        assertEquals(sb, progress.stringBuilder());
    }

    @Test
    void testCanModify() {
        progress.controllerFactoryPostAction().add("bla2");
        assertEquals(controllerFactoryPostAction, progress.controllerFactoryPostAction());

        progress.stringBuilder().append("test2");
        assertEquals(sb, progress.stringBuilder());
    }

    @Test
    void testOtherConstructor() {
        final var progress2 = new GenerationProgress(request);
        assertEquals(request, progress2.request());
        assertEquals(List.of(), progress2.controllerFactoryPostAction());
        assertEquals("", progress2.stringBuilder().toString());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new GenerationProgress(null, controllerFactoryPostAction, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, null, sb));
        assertThrows(NullPointerException.class, () -> new GenerationProgress(request, controllerFactoryPostAction, null));
    }

}
