package com.github.gtache.fxml.compiler.parsing;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestParsedDefine {

    private final ParsedProperty property;
    private final ParsedObject object;
    private final String string;
    private final ParsedDefine define;

    TestParsedDefine(@Mock final ParsedProperty property, @Mock final ParsedObject object) {
        this.property = requireNonNull(property);
        this.object = requireNonNull(object);
        this.string = "str/ing";
        this.define = spy(ParsedDefine.class);
    }

    @BeforeEach
    void beforeEach() {
        when(property.value()).thenReturn(string);
        when(define.object()).thenReturn(object);
        when(object.className()).thenReturn(string);
        when(object.children()).thenReturn(List.of(define));
        final var map = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        map.put(property, List.of(object));
        when(object.properties()).thenReturn(map);
        when(object.attributes()).thenReturn(Map.of(string, property));
    }

    @Test
    void testObject() {
        assertEquals(object, define.object());
    }

    @Test
    void testClassName() {
        assertEquals(string, define.className());
        verify(define).object();
        verify(object).className();
    }

    @Test
    void testAttributes() {
        assertEquals(Map.of(string, property), define.attributes());
        verify(define).object();
        verify(object).attributes();
    }

    @Test
    void testProperties() {
        final var map = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        map.put(property, List.of(object));
        assertEquals(map, define.properties());
        verify(define).object();
        verify(object).properties();
    }

    @Test
    void testChildren() {
        assertEquals(List.of(define), define.children());
        verify(define).object();
        verify(object).children();
    }
}
