package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.Objects;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.FX_ID;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getSortedAttributes;

/**
 * Helper methods for {@link GeneratorImpl} to format Images
 */
final class ImageFormatter {

    private final HelperProvider helperProvider;
    private final GenerationProgress progress;

    ImageFormatter(final HelperProvider helperProvider, final GenerationProgress progress) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.progress = Objects.requireNonNull(progress);
    }

    void formatImage(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (parsedObject.children().isEmpty() && parsedObject.properties().isEmpty()) {
            doFormatImage(parsedObject, variableName);
        } else {
            throw new GenerationException("Image cannot have children or properties : " + parsedObject);
        }
    }

    private void formatInputStream(final String url, final double requestedWidth,
                                   final double requestedHeight, final boolean preserveRatio, final boolean smooth, final String variableName) {
        final var inputStream = progress.getNextVariableName("inputStream");
        final var sb = progress.stringBuilder();
        sb.append("        final javafx.scene.image.Image ").append(variableName).append(";\n");
        sb.append("        try (").append(helperProvider.getCompatibilityHelper().getStartVar("java.io.InputStream", 0)).append(inputStream).append(" = ").append(url).append(".openStream()) {\n");
        sb.append("            ").append(variableName).append(" = new javafx.scene.image.Image(").append(inputStream);
        sb.append(", ").append(requestedWidth).append(", ").append(requestedHeight).append(", ").append(preserveRatio).append(", ").append(smooth).append(");\n");
        sb.append("        } catch (final java.io.IOException e) {\n");
        sb.append("            throw new RuntimeException(e);\n");
        sb.append("        }\n");
    }

    private void doFormatImage(final ParsedObject parsedObject, final String variableName) throws GenerationException {
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
                case "url" -> url = helperProvider.getURLFormatter().formatURL(property.value());
                case "requestedWidth" -> requestedWidth = Double.parseDouble(property.value());
                case "requestedHeight" -> requestedHeight = Double.parseDouble(property.value());
                case "preserveRatio" -> preserveRatio = Boolean.parseBoolean(property.value());
                case "smooth" -> smooth = Boolean.parseBoolean(property.value());
                case "backgroundLoading" -> backgroundLoading = Boolean.parseBoolean(property.value());
                default -> throw new GenerationException("Unknown image attribute : " + property.name());
            }
        }

        if (progress.request().parameters().useImageInputStreamConstructor()) {
            formatInputStream(url, requestedWidth, requestedHeight, preserveRatio, smooth, variableName);
        } else {
            formatURL(url, requestedWidth, requestedHeight, preserveRatio, smooth, backgroundLoading, variableName);
        }
        helperProvider.getGenerationHelper().handleId(parsedObject, variableName);
    }

    private void formatURL(final String url, final double requestedWidth,
                           final double requestedHeight, final boolean preserveRatio, final boolean smooth,
                           final boolean backgroundLoading, final String variableName) {
        final var urlString = progress.getNextVariableName("urlStr");
        final var sb = progress.stringBuilder();
        final var compatibilityHelper = helperProvider.getCompatibilityHelper();
        sb.append(compatibilityHelper.getStartVar("String")).append(urlString).append(" = ").append(url).append(".toString();\n");
        sb.append(compatibilityHelper.getStartVar("javafx.scene.image.Image")).append(variableName).append(" = new javafx.scene.image.Image(").append(urlString)
                .append(", ").append(requestedWidth).append(", ").append(requestedHeight).append(", ")
                .append(preserveRatio).append(", ").append(smooth).append(", ").append(backgroundLoading).append(");\n");
    }
}
