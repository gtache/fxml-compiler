package com.github.gtache.fxml.compiler.parsing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestFXMLParser {

    private final FXMLParser parser;
    private final String content;
    private final ParsedObject object;

    TestFXMLParser(@Mock final ParsedObject object) {
        this.parser = spy(FXMLParser.class);
        this.content = "content";
        this.object = requireNonNull(object);
    }

    @BeforeEach
    void beforeEach() throws ParseException {
        when(parser.parse(content)).thenReturn(object);
    }

    @Test
    void testParse() throws Exception {
        final var file = Files.createTempFile("test", ".fxml");
        try {
            Files.writeString(file, content);
            assertEquals(object, parser.parse(file));
            verify(parser).parse(content);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void testParseIOException() throws Exception {
        final var file = Paths.get("whatever");
        assertThrows(ParseException.class, () -> parser.parse(file));
        verify(parser, never()).parse(content);
    }
}
