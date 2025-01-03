package ch.gtache.fxml.compiler.maven.internal;

import ch.gtache.fxml.compiler.impl.GenericTypesImpl;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestGenericParser {

    @ParameterizedTest
    @MethodSource("providesParseTests")
    void testParse(final String genericTypes, final Map<String, String> imports, final List<GenericTypesImpl> expectedGenericTypes) throws MojoExecutionException {
        final var parser = new GenericParser(genericTypes, imports);
        final var parsed = parser.parse();
        assertEquals(expectedGenericTypes, parsed);
    }

    @Test
    void testNotFound() {
        final var parser = new GenericParser("<Collection>", Map.of());
        assertThrows(MojoExecutionException.class, parser::parse);
    }

    @ParameterizedTest
    @ValueSource(strings = {"<String, String<String>>", " < String , String < String > > ",
            "<String,String<String>>", " <String ,  String<  String>  > "})
    void testWithSpaces(final String genericTypes) throws MojoExecutionException {
        final var parser = new GenericParser(genericTypes, Map.of());
        final var expected = List.of(new GenericTypesImpl("String", List.of()), new GenericTypesImpl("String", List.of(new GenericTypesImpl("String", List.of()))));
        assertEquals(expected, parser.parse());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "<String>>", "<<String>", "<String,,String>"})
    void testInvalid(final String genericTypes) {
        final var parser = new GenericParser(genericTypes, Map.of());
        assertThrows(MojoExecutionException.class, parser::parse);
    }

    private static Stream<Arguments> providesParseTests() {
        return Stream.of(
                Arguments.of("<Map<String, TableView<Node, Node>>>",
                        Map.of("Map", "java.util.Map", "String", "java.lang.String", "TableView", "javafx.scene.control.TableView", "Node", "javafx.scene.Node"),
                        List.of(new GenericTypesImpl("java.util.Map",
                                List.of(new GenericTypesImpl("String", List.of()), new GenericTypesImpl("javafx.scene.control.TableView",
                                        List.of(new GenericTypesImpl("javafx.scene.Node", List.of()), new GenericTypesImpl("javafx.scene.Node", List.of()))))))),
                Arguments.of("<Map<String, String>>",
                        Map.of("Map", "java.util.Map"),
                        List.of(new GenericTypesImpl("java.util.Map",
                                List.of(new GenericTypesImpl("String", List.of()), new GenericTypesImpl("String", List.of()))))),
                Arguments.of("<String>",
                        Map.of(),
                        List.of(new GenericTypesImpl("String", List.of()))),
                Arguments.of("<Collection<String>>",
                        Map.of("Collection", "java.util.Collection", "String", "java.lang.String"),
                        List.of(new GenericTypesImpl("java.util.Collection",
                                List.of(new GenericTypesImpl("String", List.of()))))),
                Arguments.of("<Collection<String>>",
                        Map.of("Collection", "java.util.Collection"),
                        List.of(new GenericTypesImpl("java.util.Collection",
                                List.of(new GenericTypesImpl("String", List.of())))))
        );
    }
}
