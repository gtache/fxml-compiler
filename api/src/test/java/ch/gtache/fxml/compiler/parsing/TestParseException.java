package ch.gtache.fxml.compiler.parsing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestParseException {

    private final String message;
    private final Throwable throwable;
    private final String throwableString;

    TestParseException(@Mock final Throwable throwable) {
        this.message = "message";
        this.throwable = Objects.requireNonNull(throwable);
        this.throwableString = "throwable";
    }

    @BeforeEach
    void beforeEach() {
        when(throwable.toString()).thenReturn(throwableString);
    }

    @Test
    void testOnlyMessage() {
        final var exception = new ParseException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testOnlyCause() {
        final var exception = new ParseException(throwable);
        assertEquals(throwableString, exception.getMessage());
        assertEquals(throwable, exception.getCause());
    }

    @Test
    void testMessageAndCause() {
        final var exception = new ParseException(message, throwable);
        assertEquals(message, exception.getMessage());
        assertEquals(throwable, exception.getCause());
    }
}
