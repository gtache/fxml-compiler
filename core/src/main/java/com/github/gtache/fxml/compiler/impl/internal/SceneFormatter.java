package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.FX_ID;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getSortedAttributes;
import static java.util.Objects.requireNonNull;

/**
 * Helper methods for {@link GeneratorImpl} to format Scenes
 */
final class SceneFormatter {

    private static final ParsedProperty ROOT_PROPERTY = new ParsedPropertyImpl("root", null, null);

    private final HelperProvider helperProvider;
    private final StringBuilder sb;

    SceneFormatter(final HelperProvider helperProvider, final StringBuilder sb) {
        this.helperProvider = requireNonNull(helperProvider);
        this.sb = requireNonNull(sb);
    }

    void formatScene(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        checkPropertiesAndChildren(parsedObject);
        formatDefines(parsedObject);
        final var root = findRoot(parsedObject);
        final var rootVariableName = helperProvider.getVariableProvider().getNextVariableName("root");
        helperProvider.getObjectFormatter().format(root, rootVariableName);
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
        sb.append(helperProvider.getCompatibilityHelper().getStartVar("javafx.scene.Scene")).append(variableName).append(" = new javafx.scene.Scene(").append(rootVariableName).append(", ")
                .append(width).append(", ").append(height).append(", javafx.scene.paint.Color.valueOf(\"").append(paint).append("\"));\n");
        addStylesheets(variableName, stylesheets);
    }

    private void formatDefines(final ParsedObject parsedObject) throws GenerationException {
        final var objectFormatter = helperProvider.getObjectFormatter();
        for (final var define : parsedObject.children()) {
            if (define instanceof ParsedDefine) {
                objectFormatter.format(define, helperProvider.getVariableProvider().getNextVariableName("define"));
            }
        }
        for (final var define : parsedObject.properties().getOrDefault(ROOT_PROPERTY, List.of())) {
            if (define instanceof ParsedDefine) {
                objectFormatter.format(define, helperProvider.getVariableProvider().getNextVariableName("define"));
            }
        }
    }

    private static ParsedObject findRoot(final ParsedObject parsedObject) throws GenerationException {
        final var rootPropertyChildren = parsedObject.properties().get(ROOT_PROPERTY);
        if (rootPropertyChildren == null) {
            return getNonDefineObjects(parsedObject.children()).findFirst()
                    .orElseThrow(() -> new GenerationException("Expected only one child for scene : " + parsedObject));
        } else {
            return getNonDefineObjects(rootPropertyChildren).findFirst()
                    .orElseThrow(() -> new GenerationException("Expected only one root property child for scene : " + parsedObject));
        }
    }

    private static Stream<ParsedObject> getNonDefineObjects(final Collection<ParsedObject> objects) {
        return objects.stream().filter(c -> !(c instanceof ParsedDefine));
    }

    private static void checkPropertiesAndChildren(final ParsedObject parsedObject) throws GenerationException {
        if (parsedObject.properties().keySet().stream().anyMatch(k -> !k.equals(ROOT_PROPERTY))) {
            throw new GenerationException("Unsupported scene properties : " + parsedObject);
        }
        final var nonDefineCount = getNonDefineObjects(parsedObject.children()).count();
        final var rootPropertyChildren = parsedObject.properties().get(ROOT_PROPERTY);
        if (rootPropertyChildren == null) {
            if (nonDefineCount != 1) {
                throw new GenerationException("Expected only one child for scene : " + parsedObject);
            }
        } else {
            final var nonDefinePropertyChildren = getNonDefineObjects(rootPropertyChildren).count();
            if (nonDefinePropertyChildren != 1) {
                throw new GenerationException("Expected only one root property child for scene : " + parsedObject);
            } else if (nonDefineCount != 0) {
                throw new GenerationException("Expected no children for scene : " + parsedObject);
            }
        }
    }

    private void addStylesheets(final String variableName, final Collection<String> stylesheets) {
        if (!stylesheets.isEmpty()) {
            final var urlVariables = helperProvider.getURLFormatter().formatURL(stylesheets);
            final var tmpVariable = helperProvider.getVariableProvider().getNextVariableName("stylesheets");
            final var compatibilityHelper = helperProvider.getCompatibilityHelper();
            sb.append(compatibilityHelper.getStartVar("java.util.List<String>")).append(tmpVariable).append(" = ").append(variableName).append(".getStyleSheets();\n");
            sb.append("        ").append(tmpVariable).append(".addAll(").append(compatibilityHelper.getListOf()).append(String.join(", ", urlVariables)).append("));\n");
        }
    }
}
