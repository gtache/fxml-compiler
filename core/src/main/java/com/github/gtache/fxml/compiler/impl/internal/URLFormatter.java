package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.Collection;
import java.util.List;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;


/**
 * Helper methods for {@link GeneratorImpl} to format URLs
 */
final class URLFormatter {

    private URLFormatter() {
    }

    static List<String> formatURL(final GenerationProgress progress, final Collection<String> stylesheets) {
        return stylesheets.stream().map(s -> formatURL(progress, s)).toList();
    }

    static String formatURL(final GenerationProgress progress, final String url) {
        final var variableName = progress.getNextVariableName("url");
        final var sb = progress.stringBuilder();
        if (url.startsWith(RELATIVE_PATH_PREFIX)) {
            sb.append(getStartURL(progress)).append(variableName).append(" = getClass().getResource(\"").append(url.substring(1)).append("\");\n");
        } else {
            sb.append("        final java.net.URL ").append(variableName).append(";\n");
            sb.append("        try {\n");
            sb.append("            ").append(variableName).append(" = new java.net.URI(\"").append(url).append("\").toURL();\n");
            sb.append("        } catch (final java.net.MalformedURLException | java.net.URISyntaxException e) {\n");
            sb.append("            throw new RuntimeException(\"Couldn't parse url : ").append(url).append("\", e);\n");
            sb.append("        }\n");
        }
        return variableName;
    }

    static void formatURL(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (parsedObject.children().isEmpty() && parsedObject.properties().isEmpty()) {
            final var sortedAttributes = getSortedAttributes(parsedObject);
            String value = null;
            for (final var property : sortedAttributes) {
                switch (property.name()) {
                    case FX_ID -> {
                        //Do nothing
                    }
                    case "value" -> value = property.value();
                    default -> throw new GenerationException("Unknown URL attribute : " + property.name());
                }
            }
            progress.stringBuilder().append(getStartURL(progress)).append(variableName).append(" = getClass().getResource(\"").append(value).append("\");\n");
            handleId(progress, parsedObject, variableName);
        } else {
            throw new GenerationException("URL cannot have children or properties : " + parsedObject);
        }
    }

    private static String getStartURL(final GenerationProgress progress) {
        return GenerationCompatibilityHelper.getStartVar(progress, "java.net.URL");
    }
}
