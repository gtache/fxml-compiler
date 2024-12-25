package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.ArrayList;
import java.util.List;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;
import static java.util.Objects.requireNonNull;


/**
 * Helper methods for {@link GeneratorImpl} to format URLs
 */
final class URLFormatter {

    private final HelperProvider helperProvider;
    private final StringBuilder sb;

    URLFormatter(final HelperProvider helperProvider, final StringBuilder sb) {
        this.helperProvider = requireNonNull(helperProvider);
        this.sb = requireNonNull(sb);
    }

    List<String> formatURL(final Iterable<String> stylesheets) {
        final var ret = new ArrayList<String>();
        for (final var styleSheet : stylesheets) {
            ret.add(formatURL(styleSheet));
        }
        return ret;
    }

    String formatURL(final String url) {
        final var variableName = helperProvider.getVariableProvider().getNextVariableName("url");
        if (url.startsWith(RELATIVE_PATH_PREFIX)) {
            sb.append(getStartURL()).append(variableName).append(" = getClass().getResource(\"").append(url.substring(1)).append("\");\n");
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

    void formatURL(final ParsedObject parsedObject, final String variableName) throws GenerationException {
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
            //FIXME only relative path (@) ?
            sb.append(getStartURL()).append(variableName).append(" = getClass().getResource(\"").append(value).append("\");\n");
        } else {
            throw new GenerationException("URL cannot have children or properties : " + parsedObject);
        }
    }

    private String getStartURL() {
        return helperProvider.getCompatibilityHelper().getStartVar("java.net.URL");
    }
}
