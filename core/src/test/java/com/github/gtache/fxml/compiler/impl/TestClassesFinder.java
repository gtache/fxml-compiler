package com.github.gtache.fxml.compiler.impl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestClassesFinder {

    @Test
    void testGetClassesCurrent() throws IOException {
        final var expected = Set.of(
                "com.github.gtache.fxml.compiler.parsing.impl.TestParsedConstantImpl",
                "com.github.gtache.fxml.compiler.parsing.impl.TestParsedCopyImpl",
                "com.github.gtache.fxml.compiler.parsing.impl.TestParsedFactoryImpl",
                "com.github.gtache.fxml.compiler.parsing.impl.TestParsedIncludeImpl",
                "com.github.gtache.fxml.compiler.parsing.impl.TestParsedObjectImpl",
                "com.github.gtache.fxml.compiler.parsing.impl.TestParsedPropertyImpl",
                "com.github.gtache.fxml.compiler.parsing.impl.TestParsedReferenceImpl",
                "com.github.gtache.fxml.compiler.parsing.impl.TestParsedTextImpl",
                "com.github.gtache.fxml.compiler.parsing.impl.TestParsedValueImpl");
        final var actual = ClassesFinder.getClasses("com.github.gtache.fxml.compiler.parsing.impl");
        assertEquals(expected, actual);
    }

    @Test
    void testGetClassesJar() throws IOException {
        final var expected = Set.of("javafx.beans.DefaultProperty",
                "javafx.beans.InvalidationListener",
                "javafx.beans.NamedArg",
                "javafx.beans.Observable",
                "javafx.beans.WeakInvalidationListener",
                "javafx.beans.WeakListener");
        final var actual = ClassesFinder.getClasses("javafx.beans");
        assertEquals(expected, actual);
    }
}
