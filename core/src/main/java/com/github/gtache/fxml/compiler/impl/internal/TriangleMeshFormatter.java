package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import javafx.scene.shape.VertexFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.FX_ID;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getSortedAttributes;
import static java.util.Objects.requireNonNull;

/**
 * Helper methods for {@link GeneratorImpl} to format TriangleMeshes
 */
final class TriangleMeshFormatter {

    private final HelperProvider helperProvider;
    private final StringBuilder sb;

    TriangleMeshFormatter(final HelperProvider helperProvider, final StringBuilder sb) {
        this.helperProvider = requireNonNull(helperProvider);
        this.sb = requireNonNull(sb);
    }

    void formatTriangleMesh(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (parsedObject.children().isEmpty() && parsedObject.properties().isEmpty()) {
            final var sortedAttributes = getSortedAttributes(parsedObject);
            final var points = new ArrayList<Float>();
            final var texCoords = new ArrayList<Float>();
            final var normals = new ArrayList<Float>();
            final var faces = new ArrayList<Integer>();
            final var faceSmoothingGroups = new ArrayList<Integer>();
            VertexFormat vertexFormat = null;
            for (final var property : sortedAttributes) {
                switch (property.name().toLowerCase()) {
                    case FX_ID -> {
                        //Do nothing
                    }
                    case "points" -> {
                        points.clear();
                        points.addAll(parseList(property.value(), Float::parseFloat));
                    }
                    case "texcoords" -> {
                        texCoords.clear();
                        texCoords.addAll(parseList(property.value(), Float::parseFloat));
                    }
                    case "normals" -> {
                        normals.clear();
                        normals.addAll(parseList(property.value(), Float::parseFloat));
                    }
                    case "faces" -> {
                        faces.clear();
                        faces.addAll(parseList(property.value(), Integer::parseInt));
                    }
                    case "facesmoothinggroups" -> {
                        faceSmoothingGroups.clear();
                        faceSmoothingGroups.addAll(parseList(property.value(), Integer::parseInt));
                    }
                    case "vertexformat" -> vertexFormat = parseVertexFormat(property);
                    default -> throw new GenerationException("Unknown TriangleMesh attribute : " + property.name());
                }
            }
            sb.append(helperProvider.getCompatibilityHelper().getStartVar("javafx.scene.shape.TriangleMesh")).append(variableName).append(" = new javafx.scene.shape.TriangleMesh();\n");
            setPoints(variableName, points);
            setTexCoords(variableName, texCoords);
            setNormals(variableName, normals);
            setFaces(variableName, faces);
            setFaceSmoothingGroups(variableName, faceSmoothingGroups);
            setVertexFormat(variableName, vertexFormat);
        } else {
            throw new GenerationException("Image cannot have children or properties : " + parsedObject);
        }
    }

    private static VertexFormat parseVertexFormat(final ParsedProperty property) throws GenerationException {
        if (property.value().equalsIgnoreCase("point_texcoord")) {
            return VertexFormat.POINT_TEXCOORD;
        } else if (property.value().equalsIgnoreCase("point_normal_texcoord")) {
            return VertexFormat.POINT_NORMAL_TEXCOORD;
        } else {
            throw new GenerationException("Unknown vertex format : " + property.value());
        }
    }

    private void setPoints(final String variableName, final Collection<Float> points) {
        if (!points.isEmpty()) {
            sb.append("        ").append(variableName).append(".getPoints().setAll(new float[]{").append(formatList(points)).append("});\n");
        }
    }

    private void setTexCoords(final String variableName, final Collection<Float> texCoords) {
        if (!texCoords.isEmpty()) {
            sb.append("        ").append(variableName).append(".getTexCoords().setAll(new float[]{").append(formatList(texCoords)).append("});\n");
        }
    }

    private void setNormals(final String variableName, final Collection<Float> normals) {
        if (!normals.isEmpty()) {
            sb.append("        ").append(variableName).append(".getNormals().setAll(new float[]{").append(formatList(normals)).append("});\n");
        }
    }

    private void setFaces(final String variableName, final Collection<Integer> faces) {
        if (!faces.isEmpty()) {
            sb.append("        ").append(variableName).append(".getFaces().setAll(new int[]{").append(formatList(faces)).append("});\n");
        }
    }

    private void setFaceSmoothingGroups(final String variableName, final Collection<Integer> faceSmoothingGroups) {
        if (!faceSmoothingGroups.isEmpty()) {
            sb.append("        ").append(variableName).append(".getFaceSmoothingGroups().setAll(new int[]{").append(formatList(faceSmoothingGroups)).append("});\n");
        }
    }

    private void setVertexFormat(final String variableName, final VertexFormat vertexFormat) {
        if (vertexFormat != null) {
            sb.append("        ").append(variableName).append(".setVertexFormat(javafx.scene.shape.VertexFormat.").append(vertexFormat).append(");\n");
        }
    }

    private static <T> String formatList(final Collection<T> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    private static <T> List<T> parseList(final CharSequence value, final Function<? super String, ? extends T> parser) {
        final var splitPattern = Pattern.compile("\\s*,\\s*|\\s+");
        final var split = splitPattern.split(value);
        return Arrays.stream(split).map(String::trim).filter(s -> !s.isEmpty()).map(parser).collect(Collectors.toList());
    }
}
