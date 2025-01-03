package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.GenerationException;
import ch.gtache.fxml.compiler.compatibility.GenerationCompatibility;
import ch.gtache.fxml.compiler.compatibility.ListCollector;
import ch.gtache.fxml.compiler.parsing.ParsedObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestGenerationCompatibilityHelper {

    private final HelperProvider helperProvider;
    private final ReflectionHelper reflectionHelper;
    private final GenerationCompatibility compatibility;
    private final ParsedObject parsedObject;
    private final GenerationCompatibilityHelper compatibilityHelper;

    TestGenerationCompatibilityHelper(@Mock final GenerationCompatibility compatibility, @Mock final HelperProvider helperProvider,
                                      @Mock final ReflectionHelper reflectionHelper, @Mock final ParsedObject parsedObject) {
        this.compatibility = Objects.requireNonNull(compatibility);
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.reflectionHelper = Objects.requireNonNull(reflectionHelper);
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.compatibilityHelper = new GenerationCompatibilityHelper(helperProvider, compatibility);
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getReflectionHelper()).thenReturn(reflectionHelper);
        when(parsedObject.className()).thenReturn("java.lang.String");
    }

    @Test
    void testGetStartVarUseVar() {
        when(compatibility.useVar()).thenReturn(true);
        assertEquals("  final var ", compatibilityHelper.getStartVar("class", 2));
    }

    @Test
    void testGetStartVarUseVarDefaultIndent() {
        when(compatibility.useVar()).thenReturn(true);
        assertEquals("        final var ", compatibilityHelper.getStartVar("class"));
    }

    @Test
    void testGetStartVarUseVarObject() throws GenerationException {
        when(compatibility.useVar()).thenReturn(true);
        assertEquals("        final var ", compatibilityHelper.getStartVar(parsedObject));
    }

    @Test
    void testGetStartVarDontUseVar() {
        when(compatibility.useVar()).thenReturn(false);
        assertEquals("  final javafx.scene.control.Label ", compatibilityHelper.getStartVar("javafx.scene.control.Label", 2));
    }

    @Test
    void testGetStartVarDontUseVarObject() throws GenerationException {
        when(compatibility.useVar()).thenReturn(false);
        when(parsedObject.className()).thenReturn("javafx.scene.control.Label");
        when(reflectionHelper.getGenericTypes(parsedObject)).thenReturn("");
        assertEquals("        final javafx.scene.control.Label ", compatibilityHelper.getStartVar(parsedObject));
        verify(reflectionHelper).getGenericTypes(parsedObject);
    }

    @Test
    void testGetStartVarDontUseVarGenericObject() throws GenerationException {
        when(compatibility.useVar()).thenReturn(false);
        when(parsedObject.className()).thenReturn("javafx.scene.control.TableView");
        when(reflectionHelper.getGenericTypes(parsedObject)).thenReturn("<java.lang.String, java.lang.Integer>");
        assertEquals("        final javafx.scene.control.TableView<java.lang.String, java.lang.Integer> ", compatibilityHelper.getStartVar(parsedObject));
        verify(reflectionHelper).getGenericTypes(parsedObject);
    }

    @Test
    void testGetToListToList() {
        when(compatibility.listCollector()).thenReturn(ListCollector.TO_LIST);
        assertEquals(".toList()", compatibilityHelper.getToList());
    }

    @Test
    void testGetToListCollectToUnmodifiableList() {
        when(compatibility.listCollector()).thenReturn(ListCollector.COLLECT_TO_UNMODIFIABLE_LIST);
        assertEquals(".collect(java.util.stream.Collectors.toUnmodifiableList())", compatibilityHelper.getToList());
    }

    @Test
    void testGetToListCollectToList() {
        when(compatibility.listCollector()).thenReturn(ListCollector.COLLECT_TO_LIST);
        assertEquals(".collect(java.util.stream.Collectors.toList())", compatibilityHelper.getToList());
    }

    @Test
    void testGetFirstUse() {
        when(compatibility.useGetFirst()).thenReturn(true);
        assertEquals(".getFirst()", compatibilityHelper.getGetFirst());
    }

    @Test
    void testGetFirstDontUse() {
        when(compatibility.useGetFirst()).thenReturn(false);
        assertEquals(".get(0)", compatibilityHelper.getGetFirst());
    }

    @Test
    void testGetListOfUse() {
        when(compatibility.useCollectionsOf()).thenReturn(true);
        assertEquals("java.util.List.of(", compatibilityHelper.getListOf());
    }

    @Test
    void testGetListOfDontUse() {
        when(compatibility.useCollectionsOf()).thenReturn(false);
        assertEquals("java.util.Arrays.asList(", compatibilityHelper.getListOf());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new GenerationCompatibilityHelper(null, compatibility));
        assertThrows(NullPointerException.class, () -> new GenerationCompatibilityHelper(helperProvider, null));
    }
}
