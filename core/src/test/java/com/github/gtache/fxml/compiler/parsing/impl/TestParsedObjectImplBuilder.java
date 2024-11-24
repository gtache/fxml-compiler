package com.github.gtache.fxml.compiler.parsing.impl;

import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestParsedObjectImplBuilder {

    private final String clazz1;
    private final String clazz2;
    private final ParsedProperty property1;
    private final ParsedProperty property2;
    private final ParsedObject object1;
    private final ParsedObject object2;
    private final ParsedObjectImpl.Builder builder;

    TestParsedObjectImplBuilder(@Mock final ParsedProperty property1, @Mock final ParsedProperty property2,
                                @Mock final ParsedObject object1, @Mock final ParsedObject object2) {
        this.clazz1 = Object.class.getName();
        this.clazz2 = String.class.getName();
        this.property1 = Objects.requireNonNull(property1);
        this.property2 = Objects.requireNonNull(property2);
        this.object1 = Objects.requireNonNull(object1);
        this.object2 = Objects.requireNonNull(object2);
        this.builder = new ParsedObjectImpl.Builder();
    }

    @BeforeEach
    void beforeEach() {
        when(property1.name()).thenReturn("property1");
        when(property2.name()).thenReturn("property2");
    }

    @Test
    void testBuildNullClass() {
        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void testClassName() {
        builder.className(clazz1);
        final var built = builder.build();
        assertEquals(clazz1, built.className());
        assertEquals(Map.of(), built.attributes());
        assertEquals(Map.of(), built.properties());
    }

    @Test
    void testOverwriteClassName() {
        builder.className(clazz1);
        builder.className(clazz2);
        final var built = builder.build();
        assertEquals(clazz2, built.className());
        assertEquals(Map.of(), built.attributes());
        assertEquals(Map.of(), built.properties());
    }

    @Test
    void testAddAttribute() {
        builder.className(clazz1);
        builder.addAttribute(property1);
        final var built = builder.build();
        assertEquals(Map.of(property1.name(), property1), built.attributes());
        assertEquals(Map.of(), built.properties());
    }

    @Test
    void testAddMultipleAttributes() {
        builder.className(clazz1);
        builder.addAttribute(property1);
        builder.addAttribute(property2);
        final var built = builder.build();
        assertEquals(Map.of(property1.name(), property1, property2.name(), property2), built.attributes());
        assertEquals(Map.of(), built.properties());
    }

    @Test
    void testAddProperty() {
        builder.className(clazz1);
        builder.addProperty(property1, object1);
        final var built = builder.build();
        assertEquals(Map.of(), built.attributes());
        assertEquals(Map.of(property1, List.of(object1)), built.properties());
    }

    @Test
    void testAddMultipleProperties() {
        builder.className(clazz1);
        builder.addProperty(property1, object1);
        builder.addProperty(property2, object2);
        final var built = builder.build();
        assertEquals(Map.of(), built.attributes());
        assertEquals(Map.of(property1, List.of(object1), property2, List.of(object2)), built.properties());
    }
}