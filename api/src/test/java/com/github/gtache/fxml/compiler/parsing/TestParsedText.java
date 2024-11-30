package com.github.gtache.fxml.compiler.parsing;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

class TestParsedText {

    private final ParsedText parsedText;

    TestParsedText() {
        this.parsedText = spy(ParsedText.class);
    }

    @Test
    void testClassName() {
        assertEquals(String.class.getName(), parsedText.className());
    }

    @Test
    void testChildren() {
        assertEquals(List.of(), parsedText.children());
    }

    @Test
    void testProperties() {
        assertEquals(new LinkedHashMap<>(), parsedText.properties());
    }

    @Test
    void testAttributes() {
        assertEquals(Map.of(), parsedText.attributes());
    }
}
