package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;
import static com.github.gtache.fxml.compiler.impl.internal.URLBuilder.formatURL;

/**
 * Helper methods for {@link GeneratorImpl} to format Images
 */
final class ImageBuilder {

    private ImageBuilder() {

    }

    static void formatImage(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (parsedObject.children().isEmpty() && parsedObject.properties().isEmpty()) {
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
                    case "url" -> url = formatURL(progress, property.value());
                    case "requestedWidth" -> requestedWidth = Double.parseDouble(property.value());
                    case "requestedHeight" -> requestedHeight = Double.parseDouble(property.value());
                    case "preserveRatio" -> preserveRatio = Boolean.parseBoolean(property.value());
                    case "smooth" -> smooth = Boolean.parseBoolean(property.value());
                    case "backgroundLoading" -> backgroundLoading = Boolean.parseBoolean(property.value());
                    default -> throw new GenerationException("Unknown image attribute : " + property.name());
                }
            }
            final var urlString = progress.getNextVariableName("urlStr");
            progress.stringBuilder().append(START_VAR).append(urlString).append(" = ").append(url).append(".toString();\n");
            progress.stringBuilder().append(START_VAR).append(variableName).append(" = new javafx.scene.image.Image(").append(urlString)
                    .append(", ").append(requestedWidth).append(", ").append(requestedHeight).append(", ")
                    .append(preserveRatio).append(", ").append(smooth).append(", ").append(backgroundLoading).append(");\n");
            handleId(progress, parsedObject, variableName);
        } else {
            throw new GenerationException("Image cannot have children or properties : " + parsedObject);
        }
    }
}
