package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.FX_ID;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getSortedAttributes;

/**
 * Helper methods for {@link GeneratorImpl} to format Images
 */
final class ImageFormatter {

    private ImageFormatter() {

    }

    static void formatImage(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (parsedObject.children().isEmpty() && parsedObject.properties().isEmpty()) {
            doFormatImage(progress, parsedObject, variableName);
        } else {
            throw new GenerationException("Image cannot have children or properties : " + parsedObject);
        }
    }

    private static void formatInputStream(final GenerationProgress progress, final String url, final double requestedWidth,
                                          final double requestedHeight, final boolean preserveRatio, final boolean smooth, final String variableName) {
        final var inputStream = progress.getNextVariableName("inputStream");
        final var sb = progress.stringBuilder();
        sb.append("        final javafx.scene.image.Image ").append(variableName).append(";\n");
        sb.append("        try (").append(GenerationCompatibilityHelper.getStartVar(progress, "java.io.InputStream", 0)).append(inputStream).append(" = ").append(url).append(".openStream()) {\n");
        sb.append("            ").append(variableName).append(" = new javafx.scene.image.Image(").append(inputStream);
        sb.append(", ").append(requestedWidth).append(", ").append(requestedHeight).append(", ").append(preserveRatio).append(", ").append(smooth).append(");\n");
        sb.append("        } catch (final java.io.IOException e) {\n");
        sb.append("            throw new RuntimeException(e);\n");
        sb.append("        }\n");
    }

    private static void doFormatImage(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        final var sortedAttributes = getSortedAttributes(parsedObject);
        String url = null;
        var requestedWidth = 0.0;
        var requestedHeight = 0.0;
        var preserveRatio = false;
        var smooth = false;
        var backgroundLoading = false;
        for (final var property : sortedAttributes) {
            switch (property.name()) {
                case FX_ID -> {
                    //Do nothing
                }
                case "url" -> url = URLFormatter.formatURL(progress, property.value());
                case "requestedWidth" -> requestedWidth = Double.parseDouble(property.value());
                case "requestedHeight" -> requestedHeight = Double.parseDouble(property.value());
                case "preserveRatio" -> preserveRatio = Boolean.parseBoolean(property.value());
                case "smooth" -> smooth = Boolean.parseBoolean(property.value());
                case "backgroundLoading" -> backgroundLoading = Boolean.parseBoolean(property.value());
                default -> throw new GenerationException("Unknown image attribute : " + property.name());
            }
        }

        if (progress.request().parameters().useImageInputStreamConstructor()) {
            formatInputStream(progress, url, requestedWidth, requestedHeight, preserveRatio, smooth, variableName);
        } else {
            formatURL(progress, url, requestedWidth, requestedHeight, preserveRatio, smooth, backgroundLoading, variableName);
        }
        GenerationHelper.handleId(progress, parsedObject, variableName);
    }

    private static void formatURL(final GenerationProgress progress, final String url, final double requestedWidth,
                                  final double requestedHeight, final boolean preserveRatio, final boolean smooth,
                                  final boolean backgroundLoading, final String variableName) {
        final var urlString = progress.getNextVariableName("urlStr");
        final var sb = progress.stringBuilder();
        sb.append(GenerationCompatibilityHelper.getStartVar(progress, "String")).append(urlString).append(" = ").append(url).append(".toString();\n");
        sb.append(GenerationCompatibilityHelper.getStartVar(progress, "javafx.scene.image.Image")).append(variableName).append(" = new javafx.scene.image.Image(").append(urlString)
                .append(", ").append(requestedWidth).append(", ").append(requestedHeight).append(", ")
                .append(preserveRatio).append(", ").append(smooth).append(", ").append(backgroundLoading).append(");\n");
    }
}
