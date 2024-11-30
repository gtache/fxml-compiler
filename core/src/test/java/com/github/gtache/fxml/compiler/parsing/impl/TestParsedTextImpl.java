package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestParsedTextImpl {

    private final String text;
    private final ParsedText parsedText;

    TestParsedTextImpl() {
        this.text = "text";
        this.parsedText = new ParsedTextImpl(text);
    }

    @Test
    void testText() {
        assertEquals(text, parsedText.text());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedTextImpl(null));
    }
}
