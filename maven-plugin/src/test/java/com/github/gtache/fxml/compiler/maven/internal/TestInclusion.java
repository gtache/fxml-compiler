package com.github.gtache.fxml.compiler.maven.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestInclusion {

    private final Path path;
    private final int count;
    private final Inclusion inclusion;

    TestInclusion(@Mock final Path path) {
        this.path = Objects.requireNonNull(path);
        this.count = 1;
        this.inclusion = new Inclusion(path, count);
    }

    @Test
    void testGetters() {
        assertEquals(path, inclusion.path());
        assertEquals(count, inclusion.count());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new Inclusion(null, count));
        assertThrows(IllegalArgumentException.class, () -> new Inclusion(path, 0));
    }
}
