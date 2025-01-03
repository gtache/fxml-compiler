package ch.gtache.fxml.compiler.parsing;


import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

class TestParsedDefine {

    private final ParsedDefine define;

    TestParsedDefine() {
        this.define = spy(ParsedDefine.class);
    }

    @Test
    void testClassName() {
        assertEquals(ParsedDefine.class.getName(), define.className());
    }

    @Test
    void testAttributes() {
        assertEquals(Map.of(), define.attributes());
    }

    @Test
    void testProperties() {
        assertEquals(new LinkedHashMap<>(), define.properties());
    }
}
