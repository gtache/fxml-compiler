package ch.gtache.fxml.compiler.impl;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestClassesFinder {

    @Test
    void testGetClassesCurrent() throws IOException {
        final var expected = Set.of(
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedConstantImpl",
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedCopyImpl",
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedDefineImpl",
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedFactoryImpl",
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedIncludeImpl",
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedObjectImpl",
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedPropertyImpl",
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedReferenceImpl",
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedTextImpl",
                "ch.gtache.fxml.compiler.parsing.impl.TestParsedValueImpl");
        final var actual = ClassesFinder.getClasses("ch.gtache.fxml.compiler.parsing.impl");
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
