package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.FX_ID;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getSortedAttributes;

/**
 * Helper methods for {@link GeneratorImpl} to format fonts
 */
final class FontFormatter {

    private final HelperProvider helperProvider;
    private final StringBuilder sb;

    FontFormatter(final HelperProvider helperProvider, final StringBuilder sb) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.sb = Objects.requireNonNull(sb);
    }

    private String getStartFont() {
        return helperProvider.getCompatibilityHelper().getStartVar("javafx.scene.text.Font");
    }

    void formatFont(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (parsedObject.children().isEmpty() && parsedObject.properties().isEmpty()) {
            final var value = parseFontValue(parsedObject);
            final var url = value.url();
            final var fw = value.fontWeight();
            final var fp = value.fontPosture();
            final var size = value.size();
            final var name = value.name();
            if (url == null) {
                if (name == null) {
                    throw new GenerationException("Font must have a name or url : " + parsedObject);
                } else if (fw == null && fp == null) {
                    formatNoStyle(name, size, variableName);
                } else {
                    formatStyle(fw, fp, size, name, variableName);
                }
            } else {
                formatURL(url, size, variableName);
            }
        } else {
            throw new GenerationException("Font cannot have children or properties : " + parsedObject);
        }
    }

    private void formatURL(final URL url, final double size, final String variableName) {
        final var urlVariableName = helperProvider.getURLFormatter().formatURL(url.toString());
        sb.append("        final javafx.scene.text.Font ").append(variableName).append(";\n");
        sb.append("        try (").append(helperProvider.getCompatibilityHelper().getStartVar("java.io.InputStream", 0)).append("in = ").append(urlVariableName).append(".openStream()) {\n");
        sb.append("            ").append(variableName).append(" = javafx.scene.text.Font.loadFont(in, ").append(size).append(");\n");
        sb.append("        } catch (final java.io.IOException e) {\n");
        sb.append("            throw new RuntimeException(e);\n");
        sb.append("        }\n");
    }

    private void formatNoStyle(final String name, final double size, final String variableName) {
        sb.append(getStartFont()).append(variableName).append(" = new javafx.scene.text.Font(\"").append(name).append("\", ").append(size).append(");\n");
    }

    private void formatStyle(final FontWeight fw, final FontPosture fp, final double size, final String name, final String variableName) {
        final var finalFW = fw == null ? FontWeight.NORMAL : fw;
        final var finalFP = fp == null ? FontPosture.REGULAR : fp;
        sb.append(getStartFont()).append(variableName).append(" = new javafx.scene.text.Font(\"").append(name)
                .append("\", javafx.scene.text.FontWeight.").append(finalFW.name()).append(", javafx.scene.text.FontPosture.")
                .append(finalFP.name()).append(", ").append(size).append(");\n");
    }

    private static FontValue parseFontValue(final ParsedObject parsedObject) throws GenerationException {
        URL url = null;
        String name = null;
        double size = 12;
        FontWeight fw = null;
        FontPosture fp = null;
        final var sortedAttributes = getSortedAttributes(parsedObject);
        for (final var property : sortedAttributes) {
            switch (property.name()) {
                case FX_ID -> {
                    //Do nothing
                }
                case "name" -> {
                    try {
                        url = new URI(property.value()).toURL();
                    } catch (final MalformedURLException | URISyntaxException | IllegalArgumentException ignored) {
                        name = property.value();
                    }
                }
                case "size" -> size = Double.parseDouble(property.value());
                case "style" -> {
                    final var style = getFontStyle(property);
                    if (style.fontWeight() != null) {
                        fw = style.fontWeight();
                    }
                    if (style.fontPosture() != null) {
                        fp = style.fontPosture();
                    }
                }
                case "url" -> url = getURL(property);
                default -> throw new GenerationException("Unknown font attribute : " + property.name());
            }
        }
        return new FontValue(url, name, size, fw, fp);
    }

    private static URL getURL(final ParsedProperty property) throws GenerationException {
        try {
            return new URI(property.value()).toURL();
        } catch (final MalformedURLException | URISyntaxException e) {
            throw new GenerationException("Couldn't parse url : " + property.value(), e);
        }
    }

    private static FontStyle getFontStyle(final ParsedProperty property) {
        final var split = property.value().split(" ");
        FontWeight fw = null;
        FontPosture fp = null;
        for (final var s : split) {
            final var fontWeight = FontWeight.findByName(s);
            final var fontPosture = FontPosture.findByName(s);
            if (fontWeight != null) {
                fw = fontWeight;
            } else if (fontPosture != null) {
                fp = fontPosture;
            }
        }
        return new FontStyle(fw, fp);
    }

    private record FontValue(URL url, String name, double size, FontWeight fontWeight,
                             FontPosture fontPosture) {
    }

    private record FontStyle(FontWeight fontWeight, FontPosture fontPosture) {
    }

}
