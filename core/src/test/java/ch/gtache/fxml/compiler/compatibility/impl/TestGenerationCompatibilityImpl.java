package ch.gtache.fxml.compiler.compatibility.impl;

import ch.gtache.fxml.compiler.compatibility.GenerationCompatibility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestGenerationCompatibilityImpl {

    private final int javaVersion;
    private final GenerationCompatibility compatibility;

    TestGenerationCompatibilityImpl() {
        this.javaVersion = 8;
        this.compatibility = new GenerationCompatibilityImpl(javaVersion);
    }

    @Test
    void testGetters() {
        assertEquals(javaVersion, compatibility.javaVersion());
    }

    @Test
    void testIllegal() {
        assertThrows(IllegalArgumentException.class, () -> new GenerationCompatibilityImpl(7));
    }
}
