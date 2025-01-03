package ch.gtache.fxml.compiler.impl.internal;

import ch.gtache.fxml.compiler.GenerationException;
import ch.gtache.fxml.compiler.parsing.ParsedObject;
import ch.gtache.fxml.compiler.parsing.ParsedProperty;
import ch.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.beans.NamedArg;
import javafx.scene.control.Spinner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.time.LocalDate;
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

    private static final Annotation[][] EMPTY_ANNOTATIONS = new Annotation[0][];
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

        when(constructors[0].getParameterAnnotations()).thenReturn(EMPTY_ANNOTATIONS);
        when(constructors[1].getParameterAnnotations()).thenReturn(new Annotation[][]{{new NamedArgImpl("p1", "")}, {
                new NamedArgImpl("p2", "value2")}});
        when(constructors[1].getParameterTypes()).thenReturn(new Class[]{int.class, String.class});
        final var expectedArgs = new ConstructorArgs(constructors[1], namedArgs);
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(constructors, Map.of("p1", List.of(double.class, int.class), "p2", List.of(String.class))));
    }

    @Test
    void testGetMatchingConstructorArgsSpinnerIntFull() throws NoSuchMethodException {
        final var spinnerProperties = Map.of("min", List.<Class<?>>of(int.class, double.class, LocalDate.class),
                "max", List.<Class<?>>of(int.class, double.class, LocalDate.class), "initialValue",
                List.<Class<?>>of(int.class, double.class, LocalDate.class), "amountToStepBy", List.<Class<?>>of(int.class, double.class, LocalDate.class));
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        namedArgs.put("min", new Parameter("min", int.class, "0"));
        namedArgs.put("max", new Parameter("max", int.class, "0"));
        namedArgs.put("initialValue", new Parameter("initialValue", int.class, "0"));
        namedArgs.put("amountToStepBy", new Parameter("amountToStepBy", int.class, "0"));
        final var spinnerConstructors = Spinner.class.getConstructors();
        final var constructor = Spinner.class.getConstructor(int.class, int.class, int.class, int.class);
        final var expectedArgs = new ConstructorArgs(constructor, namedArgs);
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(spinnerConstructors, spinnerProperties));
    }

    @Test
    void testGetMatchingConstructorArgsSpinnerMismatch() throws NoSuchMethodException {
        final var spinnerProperties = Map.of("min", List.<Class<?>>of(double.class),
                "max", List.<Class<?>>of(int.class), "initialValue",
                List.<Class<?>>of(double.class, LocalDate.class));
        final var spinnerConstructors = Spinner.class.getConstructors();
        final var constructor = Spinner.class.getConstructor();
        final var expectedArgs = new ConstructorArgs(constructor, new LinkedHashMap<>());
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(spinnerConstructors, spinnerProperties));
    }


    @Test
    void testGetMatchingConstructorArgsSpinnerDouble2() throws NoSuchMethodException {
        final var spinnerProperties = Map.of("min", List.<Class<?>>of(int.class, double.class, LocalDate.class),
                "max", List.<Class<?>>of(int.class, double.class), "initialValue",
                List.<Class<?>>of(double.class, LocalDate.class));
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        namedArgs.put("min", new Parameter("min", double.class, "0"));
        namedArgs.put("max", new Parameter("max", double.class, "0"));
        namedArgs.put("initialValue", new Parameter("initialValue", double.class, "0"));
        final var spinnerConstructors = Spinner.class.getConstructors();
        final var constructor = Spinner.class.getConstructor(double.class, double.class, double.class);
        final var expectedArgs = new ConstructorArgs(constructor, namedArgs);
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(spinnerConstructors, spinnerProperties));
    }

    @Test
    void testGetMatchingConstructorArgsSpinnerInt() throws NoSuchMethodException {
        final var spinnerProperties = Map.of("min", List.<Class<?>>of(int.class, double.class, LocalDate.class),
                "max", List.<Class<?>>of(int.class, double.class, LocalDate.class), "initialValue",
                List.<Class<?>>of(int.class, double.class, LocalDate.class));
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        namedArgs.put("min", new Parameter("min", int.class, "0"));
        namedArgs.put("max", new Parameter("max", int.class, "0"));
        namedArgs.put("initialValue", new Parameter("initialValue", int.class, "0"));
        final var spinnerConstructors = Spinner.class.getConstructors();
        final var constructor = Spinner.class.getConstructor(int.class, int.class, int.class);
        final var expectedArgs = new ConstructorArgs(constructor, namedArgs);
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(spinnerConstructors, spinnerProperties));
    }

    @Test
    void testGetMatchingConstructorArgsSpinnerDouble() throws NoSuchMethodException {
        final var spinnerProperties = Map.of("min", List.<Class<?>>of(double.class, LocalDate.class),
                "max", List.<Class<?>>of(double.class, LocalDate.class), "initialValue",
                List.<Class<?>>of(double.class, LocalDate.class));
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        namedArgs.put("min", new Parameter("min", double.class, "0"));
        namedArgs.put("max", new Parameter("max", double.class, "0"));
        namedArgs.put("initialValue", new Parameter("initialValue", double.class, "0"));
        final var spinnerConstructors = Spinner.class.getConstructors();
        final var constructor = Spinner.class.getConstructor(double.class, double.class, double.class);
        final var expectedArgs = new ConstructorArgs(constructor, namedArgs);
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(spinnerConstructors, spinnerProperties));
    }

    @Test
    void testGetMatchingConstructorArgsSpinnerPartial() throws NoSuchMethodException {
        final var spinnerProperties = Map.of("min", List.<Class<?>>of(int.class, double.class, LocalDate.class));
        final var namedArgs = new LinkedHashMap<String, Parameter>();
        namedArgs.put("min", new Parameter("min", int.class, "0"));
        namedArgs.put("max", new Parameter("max", int.class, "0"));
        namedArgs.put("initialValue", new Parameter("initialValue", int.class, "0"));
        final var spinnerConstructors = Spinner.class.getConstructors();
        final var constructor = Spinner.class.getConstructor(int.class, int.class, int.class);
        final var expectedArgs = new ConstructorArgs(constructor, namedArgs);
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(spinnerConstructors, spinnerProperties));
    }

    @Test
    void testGetMatchingConstructorArgsEmpty() {
        final var namedArgs = new LinkedHashMap<String, Parameter>();

        when(constructors[0].getParameterAnnotations()).thenReturn(EMPTY_ANNOTATIONS);
        when(constructors[1].getParameterAnnotations()).thenReturn(EMPTY_ANNOTATIONS);
        when(constructors[0].getParameterCount()).thenReturn(0);
        when(constructors[1].getParameterCount()).thenReturn(1);
        final var expectedArgs = new ConstructorArgs(constructors[0], namedArgs);
        assertEquals(expectedArgs, ConstructorHelper.getMatchingConstructorArgs(constructors, Map.of("p1", List.of(int.class), "p2", List.of(int.class))));
    }

    @Test
    void testGetMatchingConstructorArgsNull() {
        when(constructors[0].getParameterAnnotations()).thenReturn(EMPTY_ANNOTATIONS);
        when(constructors[1].getParameterAnnotations()).thenReturn(EMPTY_ANNOTATIONS);
        when(constructors[0].getParameterCount()).thenReturn(1);
        when(constructors[1].getParameterCount()).thenReturn(1);
        assertNull(ConstructorHelper.getMatchingConstructorArgs(constructors, Map.of("p1", List.of(int.class), "p2", List.of(int.class))));
    }

    private record NamedArgImpl(String value, String defaultValue) implements NamedArg {

        @Override
        public Class<? extends Annotation> annotationType() {
            return NamedArg.class;
        }
    }
}
