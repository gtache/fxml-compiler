package ch.gtache.fxml.compiler.parsing;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestParsedReference {

    private final Map<String, ParsedProperty> attributes;
    private final ParsedProperty property;
    private final String string;
    private final ParsedReference reference;

    TestParsedReference(@Mock final ParsedProperty property) {
        this.attributes = new HashMap<>();
        this.property = Objects.requireNonNull(property);
        this.string = "str/ing";
        this.reference = spy(ParsedReference.class);
    }

    @BeforeEach
    void beforeEach() {
        when(reference.attributes()).thenReturn(attributes);
        when(property.value()).thenReturn(string);
    }

    @Test
    void testSourceNull() {
        assertThrows(IllegalStateException.class, reference::source);
    }

    @Test
    void testSource() {
        attributes.put("source", property);
        assertEquals(string, reference.source());
    }

    @Test
    void testClassName() {
        assertEquals(ParsedReference.class.getName(), reference.className());
    }

    @Test
    void testProperties() {
        assertEquals(new LinkedHashMap<>(), reference.properties());
    }

    @Test
    void testChildren() {
        assertEquals(List.of(), reference.children());
    }
}
