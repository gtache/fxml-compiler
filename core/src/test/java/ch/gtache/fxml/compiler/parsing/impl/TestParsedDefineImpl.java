package ch.gtache.fxml.compiler.parsing.impl;

import ch.gtache.fxml.compiler.parsing.ParsedDefine;
import ch.gtache.fxml.compiler.parsing.ParsedObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestParsedDefineImpl {

    private final List<ParsedObject> children;
    private final ParsedDefine parsedDefine;

    TestParsedDefineImpl(@Mock final ParsedObject parsedObject1, @Mock final ParsedObject parsedObject2) {
        this.children = new ArrayList<>(List.of(parsedObject1, parsedObject2));
        this.parsedDefine = new ParsedDefineImpl(children);
    }

    @Test
    void testGetters() {
        assertEquals(children, parsedDefine.children());
    }

    @Test
    void testCopy() {
        final var originalChildren = parsedDefine.children();
        children.clear();
        assertEquals(originalChildren, parsedDefine.children());
    }

    @Test
    void testUnmodifiable() {
        final var objectChildren = parsedDefine.children();
        assertThrows(UnsupportedOperationException.class, objectChildren::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ParsedDefineImpl(null));
    }
}
