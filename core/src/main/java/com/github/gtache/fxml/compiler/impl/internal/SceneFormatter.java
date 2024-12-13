package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.FX_ID;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getSortedAttributes;

/**
 * Helper methods for {@link GeneratorImpl} to format Scenes
 */
final class SceneFormatter {

    private SceneFormatter() {
    }

    static void formatScene(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        final var root = findRoot(parsedObject);
        final var rootVariableName = progress.getNextVariableName("root");
        ObjectFormatter.format(progress, root, rootVariableName);
        final var sortedAttributes = getSortedAttributes(parsedObject);
        double width = -1;
        double height = -1;
        var paint = Color.WHITE.toString();
        final var stylesheets = new ArrayList<String>();
        for (final var property : sortedAttributes) {
            switch (property.name()) {
                case FX_ID -> {
                    //Do nothing
                }
                case "width" -> width = Double.parseDouble(property.value());
                case "height" -> height = Double.parseDouble(property.value());
                case "fill" -> paint = property.value();
                case "stylesheets" -> stylesheets.add(property.value());
                default -> throw new GenerationException("Unknown font attribute : " + property.name());
            }
        }
        final var sb = progress.stringBuilder();
        sb.append(GenerationCompatibilityHelper.getStartVar(progress, "javafx.scene.Scene")).append(variableName).append(" = new javafx.scene.Scene(").append(rootVariableName).append(", ")
                .append(width).append(", ").append(height).append(", javafx.scene.paint.Color.valueOf(\"").append(paint).append("\"));\n");
        addStylesheets(progress, variableName, stylesheets);
        GenerationHelper.handleId(progress, parsedObject, variableName);
    }

    private static ParsedObject findRoot(final ParsedObject parsedObject) throws GenerationException {
        final var rootProperty = parsedObject.properties().entrySet().stream().filter(e -> e.getKey().name().equals("root"))
                .filter(e -> e.getValue().size() == 1)
                .map(e -> e.getValue().getFirst()).findFirst().orElse(null);
        if (rootProperty != null) {
            return rootProperty;
        } else if (parsedObject.children().size() == 1) {
            return parsedObject.children().getFirst();
        } else {
            throw new GenerationException("Scene must have a root");
        }
    }

    private static void addStylesheets(final GenerationProgress progress, final String variableName, final Collection<String> stylesheets) {
        if (!stylesheets.isEmpty()) {
            final var urlVariables = URLFormatter.formatURL(progress, stylesheets);
            final var tmpVariable = progress.getNextVariableName("stylesheets");
            final var sb = progress.stringBuilder();
            sb.append(GenerationCompatibilityHelper.getStartVar(progress, "java.util.List<String>")).append(tmpVariable).append(" = ").append(variableName).append(".getStyleSheets();\n");
            sb.append("        ").append(tmpVariable).append(".addAll(").append(GenerationCompatibilityHelper.getListOf(progress)).append(String.join(", ", urlVariables)).append("));\n");
        }
    }
}
