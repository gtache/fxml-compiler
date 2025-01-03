package ch.gtache.fxml.compiler.impl.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestConstructorArgs {

    private final Constructor<?> constructor;
    private final SequencedMap<String, Parameter> namedArgs;
    private final ConstructorArgs constructorArgs;

    TestConstructorArgs(@Mock final Constructor<?> constructor, @Mock final Parameter parameter1, @Mock final Parameter parameter2) {
        this.constructor = Objects.requireNonNull(constructor);
        this.namedArgs = new LinkedHashMap<>();
        namedArgs.put("p1", parameter1);
        namedArgs.put("p2", parameter2);
        this.constructorArgs = new ConstructorArgs(constructor, namedArgs);
    }

    @Test
    void testGetters() {
        assertEquals(constructor, constructorArgs.constructor());
        assertEquals(namedArgs, constructorArgs.namedArgs());
    }

    @Test
    void testCopy() {
        final var original = constructorArgs.namedArgs();
        namedArgs.put("p3", null);
        assertEquals(original, constructorArgs.namedArgs());
    }

    @Test
    void testUnmodifiable() {
        final var map = constructorArgs.namedArgs();
        assertThrows(UnsupportedOperationException.class, map::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ConstructorArgs(null, namedArgs));
        assertThrows(NullPointerException.class, () -> new ConstructorArgs(constructor, null));
    }
}
