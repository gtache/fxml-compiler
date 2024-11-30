package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedText;

import java.util.Objects;

/**
 * Implementation of {@link ParsedText}
 *
 * @param text The text
 */
public record ParsedTextImpl(String text) implements ParsedText {

    /**
     * Instantiates a new parsed text
     *
     * @param text The text
     * @throws NullPointerException if the text is null
     */
    public ParsedTextImpl {
        Objects.requireNonNull(text);
    }
}
