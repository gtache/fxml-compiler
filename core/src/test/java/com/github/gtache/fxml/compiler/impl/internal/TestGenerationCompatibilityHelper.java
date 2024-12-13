package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInfo;
import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.compatibility.GenerationCompatibility;
import com.github.gtache.fxml.compiler.compatibility.ListCollector;
import com.github.gtache.fxml.compiler.impl.GenericTypesImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestGenerationCompatibilityHelper {

    private final GenerationProgress progress;
    private final GenerationRequest request;
    private final GenerationParameters parameters;
    private final GenerationCompatibility compatibility;
    private final ParsedObject parsedObject;

    TestGenerationCompatibilityHelper(@Mock final GenerationProgress progress, @Mock final GenerationRequest request,
                                      @Mock final GenerationParameters parameters, @Mock final GenerationCompatibility compatibility,
                                      @Mock final ParsedObject parsedObject) {
        this.progress = Objects.requireNonNull(progress);
        this.request = Objects.requireNonNull(request);
        this.parameters = Objects.requireNonNull(parameters);
        this.compatibility = Objects.requireNonNull(compatibility);
        this.parsedObject = Objects.requireNonNull(parsedObject);
    }

    @BeforeEach
    void beforeEach() {
        when(progress.request()).thenReturn(request);
        when(request.parameters()).thenReturn(parameters);
        when(parameters.compatibility()).thenReturn(compatibility);
        when(parsedObject.className()).thenReturn("java.lang.String");
    }

    @Test
    void testGetStartVarUseVar() {
        when(compatibility.useVar()).thenReturn(true);
        assertEquals("  final var ", GenerationCompatibilityHelper.getStartVar(progress, "class", 2));
    }

    @Test
    void testGetStartVarUseVarDefaultIndent() {
        when(compatibility.useVar()).thenReturn(true);
        assertEquals("        final var ", GenerationCompatibilityHelper.getStartVar(progress, "class"));
    }

    @Test
    void testGetStartVarUseVarObject() throws GenerationException {
        when(compatibility.useVar()).thenReturn(true);
        assertEquals("        final var ", GenerationCompatibilityHelper.getStartVar(progress, parsedObject));
    }

    @Test
    void testGetStartVarDontUseVar() {
        when(compatibility.useVar()).thenReturn(false);
        assertEquals("  final javafx.scene.control.Label ", GenerationCompatibilityHelper.getStartVar(progress, "javafx.scene.control.Label", 2));
    }

    @Test
    void testGetStartVarDontUseVarObject() throws GenerationException {
        when(compatibility.useVar()).thenReturn(false);
        when(parsedObject.className()).thenReturn("javafx.scene.control.Label");
        assertEquals("        final javafx.scene.control.Label ", GenerationCompatibilityHelper.getStartVar(progress, parsedObject));
    }

    @Test
    void testGetStartVarDontUseVarGenericObject() throws GenerationException {
        when(compatibility.useVar()).thenReturn(false);
        when(parsedObject.className()).thenReturn("javafx.scene.control.TableView");
        final var id = "tableView";
        when(parsedObject.attributes()).thenReturn(Map.of("fx:id", new ParsedPropertyImpl("fx:id", null, id)));
        final var info = mock(ControllerInfo.class);
        final var fieldInfo = mock(ControllerFieldInfo.class);
        when(info.fieldInfo(id)).thenReturn(fieldInfo);
        when(request.controllerInfo()).thenReturn(info);
        when(fieldInfo.isGeneric()).thenReturn(true);
        when(fieldInfo.genericTypes()).thenReturn(List.of(new GenericTypesImpl("java.lang.String", List.of()), new GenericTypesImpl("java.lang.Integer", List.of())));
        assertEquals("        final javafx.scene.control.TableView<java.lang.String, java.lang.Integer> ", GenerationCompatibilityHelper.getStartVar(progress, parsedObject));
    }

    @Test
    void testGetToListToList() {
        when(compatibility.listCollector()).thenReturn(ListCollector.TO_LIST);
        assertEquals(".toList()", GenerationCompatibilityHelper.getToList(progress));
    }

    @Test
    void testGetToListCollectToUnmodifiableList() {
        when(compatibility.listCollector()).thenReturn(ListCollector.COLLECT_TO_UNMODIFIABLE_LIST);
        assertEquals(".collect(java.util.stream.Collectors.toUnmodifiableList())", GenerationCompatibilityHelper.getToList(progress));
    }

    @Test
    void testGetToListCollectToList() {
        when(compatibility.listCollector()).thenReturn(ListCollector.COLLECT_TO_LIST);
        assertEquals(".collect(java.util.stream.Collectors.toList())", GenerationCompatibilityHelper.getToList(progress));
    }

    @Test
    void testGetFirstUse() {
        when(compatibility.useGetFirst()).thenReturn(true);
        assertEquals(".getFirst()", GenerationCompatibilityHelper.getGetFirst(progress));
    }

    @Test
    void testGetFirstDontUse() {
        when(compatibility.useGetFirst()).thenReturn(false);
        assertEquals(".get(0)", GenerationCompatibilityHelper.getGetFirst(progress));
    }

    @Test
    void testGetListOfUse() {
        when(compatibility.useCollectionsOf()).thenReturn(true);
        assertEquals("java.util.List.of(", GenerationCompatibilityHelper.getListOf(progress));
    }

    @Test
    void testGetListOfDontUse() {
        when(compatibility.useCollectionsOf()).thenReturn(false);
        assertEquals("java.util.Arrays.asList(", GenerationCompatibilityHelper.getListOf(progress));
    }
}
