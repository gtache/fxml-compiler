package ch.gtache.fxml.compiler.compatibility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class TestGenerationCompatibility {

    private final GenerationCompatibility compatibility;

    TestGenerationCompatibility() {
        this.compatibility = spy(GenerationCompatibility.class);
    }

    @Test
    void testUseVar() {
        when(compatibility.javaVersion()).thenReturn(10);
        assertTrue(compatibility.useVar());
    }

    @Test
    void testDontUseVar() {
        when(compatibility.javaVersion()).thenReturn(9);
        assertFalse(compatibility.useVar());
    }

    @Test
    void testToListCollector() {
        when(compatibility.javaVersion()).thenReturn(16);
        assertEquals(ListCollector.TO_LIST, compatibility.listCollector());
    }

    @Test
    void testCollectToUnmodifiableListCollector() {
        when(compatibility.javaVersion()).thenReturn(15);
        assertEquals(ListCollector.COLLECT_TO_UNMODIFIABLE_LIST, compatibility.listCollector());
        when(compatibility.javaVersion()).thenReturn(10);
        assertEquals(ListCollector.COLLECT_TO_UNMODIFIABLE_LIST, compatibility.listCollector());
    }

    @Test
    void testCollectToListCollector() {
        when(compatibility.javaVersion()).thenReturn(9);
        assertEquals(ListCollector.COLLECT_TO_LIST, compatibility.listCollector());
    }

    @Test
    void testUseCollectionsOf() {
        when(compatibility.javaVersion()).thenReturn(9);
        assertTrue(compatibility.useCollectionsOf());
    }

    @Test
    void testDontUseCollectionsOf() {
        when(compatibility.javaVersion()).thenReturn(8);
        assertFalse(compatibility.useCollectionsOf());
    }

    @Test
    void testUseGetFirst() {
        when(compatibility.javaVersion()).thenReturn(21);
        assertTrue(compatibility.useGetFirst());
    }

    @Test
    void testDontUseGetFirst() {
        when(compatibility.javaVersion()).thenReturn(20);
        assertFalse(compatibility.useGetFirst());
    }
}
