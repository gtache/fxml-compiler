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

    private final Class<?> clazz1;
    private final Class<?> clazz2;
    private final ParsedProperty property1;
    private final ParsedProperty property2;
    private final ParsedObject object1;
    private final ParsedObject object2;
    private final ParsedObjectImpl.Builder builder;

    TestParsedObjectImplBuilder(@Mock final ParsedProperty property1, @Mock final ParsedProperty property2,
                                @Mock final ParsedObject object1, @Mock final ParsedObject object2) {
        this.clazz1 = Object.class;
        this.clazz2 = String.class;
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
    void testClazz() {
        builder.clazz(clazz1);
        final var built = builder.build();
        assertEquals(clazz1, built.clazz());
        assertEquals(Map.of(), built.properties());
        assertEquals(Map.of(), built.children());
    }

    @Test
    void testOverwriteClazz() {
        builder.clazz(clazz1);
        builder.clazz(clazz2);
        final var built = builder.build();
        assertEquals(clazz2, built.clazz());
        assertEquals(Map.of(), built.properties());
        assertEquals(Map.of(), built.children());
    }

    @Test
    void testAddProperty() {
        builder.clazz(clazz1);
        builder.addProperty(property1);
        final var built = builder.build();
        assertEquals(Map.of(property1.name(), property1), built.properties());
        assertEquals(Map.of(), built.children());
    }

    @Test
    void testAddMultipleProperties() {
        builder.clazz(clazz1);
        builder.addProperty(property1);
        builder.addProperty(property2);
        final var built = builder.build();
        assertEquals(Map.of(property1.name(), property1, property2.name(), property2), built.properties());
        assertEquals(Map.of(), built.children());
    }

    @Test
    void testAddChild() {
        builder.clazz(clazz1);
        builder.addChild(property1, object1);
        final var built = builder.build();
        assertEquals(Map.of(), built.properties());
        assertEquals(Map.of(property1, List.of(object1)), built.children());
    }

    @Test
    void testAddMultipleChildren() {
        builder.clazz(clazz1);
        builder.addChild(property1, object1);
        builder.addChild(property2, object2);
        final var built = builder.build();
        assertEquals(Map.of(), built.properties());
        assertEquals(Map.of(property1, List.of(object1), property2, List.of(object2)), built.children());
    }
}