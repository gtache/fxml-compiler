package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.beans.NamedArg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestConstructorHelper {

    private final ConstructorArgs args;
    private final Constructor<Object>[] constructors;
    private final ParsedObject parsedObject;
    private final Set<String> propertyNames;

    TestConstructorHelper(@Mock final ConstructorArgs args, @Mock final Constructor<Object> constructor1,
                          @Mock final Constructor<Object> constructor2, @Mock final ParsedObject parsedObject) {
        this.args = Objects.requireNonNull(args);
        this.constructors = new Constructor[]{constructor1, constructor2};
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.propertyNames = Set.of("p1", "p2");
    }

    @Test
    void testGetListConstructorArgsExists() throws GenerationException {
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        namedArgs.put("p1", new Parameter("p1", int.class, "1"));
        namedArgs.put("p2", new Parameter("p2", String.class, "value2"));
        when(args.namedArgs()).thenReturn(namedArgs);
        final var attributes = new HashMap<String, ParsedProperty>();
        when(parsedObject.attributes()).thenReturn(attributes);
        attributes.put("p1", new ParsedPropertyImpl("p1", null, "10"));
        attributes.put("p2", new ParsedPropertyImpl("p2", null, "value"));
        final var expected = List.of("10", "\"value\"");
        assertEquals(expected, ConstructorHelper.getListConstructorArgs(args, parsedObject));
    }

    @Test
    void tesGetListConstructorArgsDefault() throws GenerationException {
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        namedArgs.put("p1", new Parameter("p1", int.class, "1"));
        namedArgs.put("p2", new Parameter("p2", String.class, "value2"));
        when(args.namedArgs()).thenReturn(namedArgs);
        when(parsedObject.attributes()).thenReturn(Map.of());
        when(parsedObject.properties()).thenReturn(new LinkedHashMap<>());
        final var expected = List.of("1", "\"value2\"");
        assertEquals(expected, ConstructorHelper.getListConstructorArgs(args, parsedObject));
    }

    @Test
    void tetsGetListConstructorArgsComplex() {
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        namedArgs.put("p1", new Parameter("p1", int.class, "1"));
        when(args.namedArgs()).thenReturn(namedArgs);
        when(parsedObject.attributes()).thenReturn(Map.of());
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        properties.put(new ParsedPropertyImpl("p1", null, "1"), List.of());
        when(parsedObject.properties()).thenReturn(properties);
        assertThrows(GenerationException.class, () -> ConstructorHelper.getListConstructorArgs(args, parsedObject));
    }

    @Test
    void testGetMatchingConstructorArgs() {
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        namedArgs.put("p1", new Parameter("p1", int.class, "0"));
        namedArgs.put("p2", new Parameter("p2", String.class, "value2"));

        when(constructors[0].getParameterAnnotations()).thenReturn(new Annotation[0][]);
        when(constructors[1].getParameterAnnotations()).thenReturn(new Annotation[][]{{new NamedArgImpl("p1", "")}, {
                new NamedArgImpl("p2", "value2")}});
        when(constructors[1].getParameterTypes()).thenReturn(new Class[]{int.class, String.class});
        final var expectedArgs = new ConstructorArgs(constructors[1], namedArgs);
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(constructors, propertyNames));
    }

    @Test
    void testGetMatchingConstructorArgsEmpty() {
        final var namedArgs = new LinkedHashMap<String, Parameter>();

        when(constructors[0].getParameterAnnotations()).thenReturn(new Annotation[0][]);
        when(constructors[1].getParameterAnnotations()).thenReturn(new Annotation[0][]);
        when(constructors[0].getParameterCount()).thenReturn(0);
        when(constructors[1].getParameterCount()).thenReturn(1);
        final var expectedArgs = new ConstructorArgs(constructors[0], namedArgs);
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(constructors, propertyNames));
    }

    @Test
    void testGetMatchingConstructorArgsNull() {
        when(constructors[0].getParameterAnnotations()).thenReturn(new Annotation[0][]);
        when(constructors[1].getParameterAnnotations()).thenReturn(new Annotation[0][]);
        when(constructors[0].getParameterCount()).thenReturn(1);
        when(constructors[1].getParameterCount()).thenReturn(1);
        assertNull(ConstructorHelper.getMatchingConstructorArgs(constructors, propertyNames));
    }


    private record NamedArgImpl(String value, String defaultValue) implements NamedArg {

        @Override
        public Class<? extends Annotation> annotationType() {
            return NamedArg.class;
        }
    }
}
