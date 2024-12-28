package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
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
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestTriangleMeshFormatter {

    private final HelperProvider helperProvider;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final StringBuilder sb;
    private final ParsedObject parsedObject;
    private final String variableName;
    private final Map<String, ParsedProperty> attributes;
    private final TriangleMeshFormatter triangleMeshFormatter;

    TestTriangleMeshFormatter(@Mock final HelperProvider helperProvider, @Mock final GenerationCompatibilityHelper compatibilityHelper,
                              @Mock final ParsedObject parsedObject) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.sb = new StringBuilder();
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.attributes = new HashMap<>();
        this.variableName = "variable";
        this.triangleMeshFormatter = new TriangleMeshFormatter(helperProvider, sb);
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(parsedObject.attributes()).thenReturn(attributes);
        when(parsedObject.children()).thenReturn(List.of());
        when(parsedObject.properties()).thenReturn(new LinkedHashMap<>());
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
    }

    @Test
    void testFormatChildren() {
        when(parsedObject.children()).thenReturn(List.of(parsedObject));
        assertThrows(GenerationException.class, () -> triangleMeshFormatter.formatTriangleMesh(parsedObject, variableName));
    }

    @Test
    void testFormatProperties() {
        final var map = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        map.put(new ParsedPropertyImpl("str", null, null), List.of());
        when(parsedObject.properties()).thenReturn(map);
        assertThrows(GenerationException.class, () -> triangleMeshFormatter.formatTriangleMesh(parsedObject, variableName));
    }

    @Test
    void testFormatUnknownAttribute() {
        attributes.put("unknown", new ParsedPropertyImpl("unknown", null, "value"));
        assertThrows(GenerationException.class, () -> triangleMeshFormatter.formatTriangleMesh(parsedObject, variableName));
    }

    @Test
    void testNoAttributes() throws GenerationException {
        triangleMeshFormatter.formatTriangleMesh(parsedObject, variableName);
        final var expected = "javafx.scene.shape.TriangleMeshvariable = new javafx.scene.shape.TriangleMesh();\n";
        assertEquals(expected, sb.toString());
    }

    @Test
    void testUnknownVertexFormat() {
        final var vertexFormat = "unknown";
        attributes.put("vertexFormat", new ParsedPropertyImpl("vertexFormat", null, vertexFormat));
        assertThrows(GenerationException.class, () -> triangleMeshFormatter.formatTriangleMesh(parsedObject, variableName));
    }

    @Test
    void testVertexFormatPointNormalTexCoord() throws GenerationException {
        final var vertexFormat = "point_normal_texcoord";
        attributes.put("vertexFormat", new ParsedPropertyImpl("vertexFormat", null, vertexFormat));
        triangleMeshFormatter.formatTriangleMesh(parsedObject, variableName);
        final var expected = """
                javafx.scene.shape.TriangleMeshvariable = new javafx.scene.shape.TriangleMesh();
                        variable.setVertexFormat(javafx.scene.shape.VertexFormat.POINT_NORMAL_TEXCOORD);
                """;
        assertEquals(expected, sb.toString());
    }

    @Test
    void testAllAttributes() throws GenerationException {
        final var points = "3.0, 4.1, 5, 6f";
        final var texCoords = " 7 8  9.3   10.0f  ";
        final var normals = "  7 , 8f  9.3f";
        final var faces = "1  2, 3 ,4";
        final var faceSmoothingGroups = "  1 22 3 ";
        final var vertexFormat = "point_texcoord";

        final var pointsProperty = new ParsedPropertyImpl("points", null, points);
        final var texCoordsProperty = new ParsedPropertyImpl("texCoords", null, texCoords);
        final var normalsProperty = new ParsedPropertyImpl("normals", null, normals);
        final var facesProperty = new ParsedPropertyImpl("faces", null, faces);
        final var faceSmoothingGroupsProperty = new ParsedPropertyImpl("faceSmoothingGroups", null, faceSmoothingGroups);
        final var vertexFormatProperty = new ParsedPropertyImpl("vertexFormat", null, vertexFormat);

        attributes.put("points", pointsProperty);
        attributes.put("texCoords", texCoordsProperty);
        attributes.put("normals", normalsProperty);
        attributes.put("faces", facesProperty);
        attributes.put("faceSmoothingGroups", faceSmoothingGroupsProperty);
        attributes.put("vertexFormat", vertexFormatProperty);
        attributes.put("fx:id", new ParsedPropertyImpl("fx:id", null, "id"));
        triangleMeshFormatter.formatTriangleMesh(parsedObject, variableName);
        final var expected = """
                javafx.scene.shape.TriangleMeshvariable = new javafx.scene.shape.TriangleMesh();
                        variable.getPoints().setAll(new float[]{3.0, 4.1, 5.0, 6.0});
                        variable.getTexCoords().setAll(new float[]{7.0, 8.0, 9.3, 10.0});
                        variable.getNormals().setAll(new float[]{7.0, 8.0, 9.3});
                        variable.getFaces().setAll(new int[]{1, 2, 3, 4});
                        variable.getFaceSmoothingGroups().setAll(new int[]{1, 22, 3});
                        variable.setVertexFormat(javafx.scene.shape.VertexFormat.POINT_TEXCOORD);
                """;
        assertEquals(expected, sb.toString());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new TriangleMeshFormatter(null, sb));
        assertThrows(NullPointerException.class, () -> new TriangleMeshFormatter(helperProvider, null));
    }
}
