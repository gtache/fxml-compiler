package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import javafx.scene.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestValueClassGuesser {

    private final HelperProvider helperProvider;
    private final VariableProvider variableProvider;
    private final ValueClassGuesser valueClassGuesser;

    TestValueClassGuesser(@Mock final HelperProvider helperProvider, @Mock final VariableProvider variableProvider) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.valueClassGuesser = new ValueClassGuesser(helperProvider);
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
    }

    @Test
    void testGuessVariableUnsupported() {
        assertThrows(GenerationException.class, () -> valueClassGuesser.guess("$controller.value"));
    }

    @Test
    void testGuessVariableNotFound() {
        assertThrows(GenerationException.class, () -> valueClassGuesser.guess("$value"));
    }

    @Test
    void testGuessVariable() throws GenerationException {
        final var object = mock(ParsedObject.class);
        when(variableProvider.getVariableInfo("value")).thenReturn(new VariableInfo("value", object, "value", "javafx.scene.Node"));
        assertEquals(List.of(Node.class), valueClassGuesser.guess("$value"));
    }

    @Test
    void testGuessNotDecimal() throws GenerationException {
        assertEquals(List.of(String.class), valueClassGuesser.guess("value"));
    }

    @Test
    void testGuessDouble() throws GenerationException {
        assertEquals(List.of(float.class, Float.class, double.class, Double.class, BigDecimal.class, String.class), valueClassGuesser.guess("1.0"));
        assertEquals(List.of(float.class, Float.class, double.class, Double.class, String.class), valueClassGuesser.guess("-Infinity"));
        assertEquals(List.of(float.class, Float.class, double.class, Double.class, String.class), valueClassGuesser.guess("Infinity"));
        assertEquals(List.of(float.class, Float.class, double.class, Double.class, String.class), valueClassGuesser.guess("NaN"));
    }

    @Test
    void testGuessInteger() throws GenerationException {
        assertEquals(List.of(byte.class, Byte.class, short.class, Short.class, int.class, Integer.class, long.class, Long.class, BigInteger.class,
                float.class, Float.class, double.class, Double.class, BigDecimal.class, String.class), valueClassGuesser.guess("1"));
    }

    @Test
    void testGuessNotByte() throws GenerationException {
        assertEquals(List.of(short.class, Short.class, int.class, Integer.class, long.class, Long.class, BigInteger.class, float.class, Float.class, double.class, Double.class, BigDecimal.class, String.class), valueClassGuesser.guess("256"));
    }

    @Test
    void testGuessLocalDate() throws GenerationException {
        assertEquals(List.of(LocalDate.class, String.class), valueClassGuesser.guess("2022-01-01"));
    }

    @Test
    void testGuessLocalDateTime() throws GenerationException {
        assertEquals(List.of(LocalDateTime.class, String.class), valueClassGuesser.guess("2022-01-01T00:00:00"));
    }

    @Test
    void testGuessBoolean() throws GenerationException {
        assertEquals(List.of(boolean.class, Boolean.class, String.class), valueClassGuesser.guess("true"));
        assertEquals(List.of(boolean.class, Boolean.class, String.class), valueClassGuesser.guess("false"));
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ValueClassGuesser(null));
    }
}
