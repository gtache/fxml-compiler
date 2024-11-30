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

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;

/**
 * Helper methods for {@link GeneratorImpl} to format TriangleMeshes
 */
final class TriangleMeshBuilder {

    private TriangleMeshBuilder() {

    }

    static void formatTriangleMesh(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
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
            final var sb = progress.stringBuilder();
            sb.append(START_VAR).append(variableName).append(" = new javafx.scene.shape.TriangleMesh();\n");
            setPoints(progress, variableName, points);
            setTexCoords(progress, variableName, texCoords);
            setNormals(progress, variableName, normals);
            setFaces(progress, variableName, faces);
            setFaceSmoothingGroups(progress, variableName, faceSmoothingGroups);
            setVertexFormat(progress, variableName, vertexFormat);
            handleId(progress, parsedObject, variableName);
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

    private static void setPoints(final GenerationProgress progress, final String variableName, final Collection<Float> points) {
        if (!points.isEmpty()) {
            progress.stringBuilder().append("    ").append(variableName).append(".getPoints().setAll(new float[]{").append(formatList(points)).append("});\n");
        }
    }

    private static void setTexCoords(final GenerationProgress progress, final String variableName, final Collection<Float> texCoords) {
        if (!texCoords.isEmpty()) {
            progress.stringBuilder().append("    ").append(variableName).append(".getTexCoords().setAll(new float[]{").append(formatList(texCoords)).append("});\n");
        }
    }

    private static void setNormals(final GenerationProgress progress, final String variableName, final Collection<Float> normals) {
        if (!normals.isEmpty()) {
            progress.stringBuilder().append("    ").append(variableName).append(".getNormals().setAll(new float[]{").append(formatList(normals)).append("});\n");
        }
    }

    private static void setFaces(final GenerationProgress progress, final String variableName, final Collection<Integer> faces) {
        if (!faces.isEmpty()) {
            progress.stringBuilder().append("    ").append(variableName).append(".getFaces().setAll(new int[]{").append(formatList(faces)).append("});\n");
        }
    }

    private static void setFaceSmoothingGroups(final GenerationProgress progress, final String variableName, final Collection<Integer> faceSmoothingGroups) {
        if (!faceSmoothingGroups.isEmpty()) {
            progress.stringBuilder().append("    ").append(variableName).append(".getFaceSmoothingGroups().setAll(new int[]{").append(formatList(faceSmoothingGroups)).append("});\n");
        }
    }

    private static void setVertexFormat(final GenerationProgress progress, final String variableName, final VertexFormat vertexFormat) {
        if (vertexFormat != null) {
            progress.stringBuilder().append("    ").append(variableName).append(".setVertexFormat(javafx.scene.shape.VertexFormat.").append(vertexFormat).append(");\n");
        }
    }

    private static <T> String formatList(final Collection<T> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    private static <T> List<T> parseList(final CharSequence value, final Function<? super String, ? extends T> parser) {
        final var splitPattern = Pattern.compile("[\\s+,]");
        final var split = splitPattern.split(value);
        return Arrays.stream(split).map(parser).collect(Collectors.toList());
    }

}
