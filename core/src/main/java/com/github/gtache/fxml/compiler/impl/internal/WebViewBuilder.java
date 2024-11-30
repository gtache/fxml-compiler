package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import static com.github.gtache.fxml.compiler.impl.internal.ControllerInjector.injectCallbackControllerMethod;
import static com.github.gtache.fxml.compiler.impl.internal.ControllerInjector.injectEventHandlerControllerMethod;
import static com.github.gtache.fxml.compiler.impl.internal.FieldSetter.setEventHandler;
import static com.github.gtache.fxml.compiler.impl.internal.FieldSetter.setField;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.*;
import static com.github.gtache.fxml.compiler.impl.internal.PropertyFormatter.formatProperty;

/**
 * Helper methods for {@link GeneratorImpl} to format WebViews
 */
final class WebViewBuilder {

    private WebViewBuilder() {

    }

    /**
     * Formats a WebView object
     *
     * @param progress     The generation progress
     * @param parsedObject The parsed object
     * @param variableName The variable name
     * @throws GenerationException if an error occurs
     */
    static void formatWebView(final GenerationProgress progress, final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (parsedObject.children().isEmpty() && parsedObject.properties().isEmpty()) {
            final var sortedAttributes = getSortedAttributes(parsedObject);
            final var sb = progress.stringBuilder();
            sb.append(START_VAR).append(variableName).append(" = new javafx.scene.web.WebView();\n");
            final var engineVariable = progress.getNextVariableName("engine");
            sb.append(START_VAR).append(engineVariable).append(" = ").append(variableName).append(".getEngine();\n");
            for (final var value : sortedAttributes) {
                formatAttribute(progress, value, parsedObject, variableName, engineVariable);
            }
            handleId(progress, parsedObject, variableName);
        } else {
            throw new GenerationException("WebView cannot have children or properties : " + parsedObject);
        }
    }

    private static void formatAttribute(final GenerationProgress progress, final ParsedProperty value, final ParsedObject parsedObject,
                                        final String variableName, final String engineVariable) throws GenerationException {
        switch (value.name()) {
            case FX_ID -> {
                //Do nothing
            }
            case "confirmHandler" -> injectConfirmHandler(progress, value, engineVariable);
            case "createPopupHandler" -> injectCreatePopupHandler(progress, value, engineVariable);
            case "onAlert", "onResized", "onStatusChanged", "onVisibilityChanged" ->
                    injectEventHandler(progress, value, engineVariable);
            case "promptHandler" -> injectPromptHandler(progress, value, engineVariable);
            case "location" -> injectLocation(progress, value, engineVariable);
            default -> formatProperty(progress, value, parsedObject, variableName);
        }
    }

    private static void injectConfirmHandler(final GenerationProgress progress, final ParsedProperty value, final String engineVariable) throws GenerationException {
        if (value.value().startsWith("#")) {
            injectCallbackControllerMethod(progress, value, engineVariable, "String.class");
        } else {
            setCallback(progress, value, engineVariable);
        }
    }

    private static void injectCreatePopupHandler(final GenerationProgress progress, final ParsedProperty value, final String engineVariable) throws GenerationException {
        if (value.value().startsWith("#")) {
            injectCallbackControllerMethod(progress, value, engineVariable, "javafx.scene.web.PopupFeatures.class");
        } else {
            setCallback(progress, value, engineVariable);
        }
    }

    private static void injectEventHandler(final GenerationProgress progress, final ParsedProperty value, final String engineVariable) throws GenerationException {
        if (value.value().startsWith("#")) {
            injectEventHandlerControllerMethod(progress, value, engineVariable);
        } else {
            setEventHandler(progress, value, engineVariable);
        }
    }

    private static void injectPromptHandler(final GenerationProgress progress, final ParsedProperty value, final String engineVariable) throws GenerationException {
        if (value.value().startsWith("#")) {
            injectCallbackControllerMethod(progress, value, engineVariable, "javafx.scene.web.PromptData.class");
        } else {
            setCallback(progress, value, engineVariable);
        }
    }

    private static void injectLocation(final GenerationProgress progress, final ParsedProperty value, final String engineVariable) {
        progress.stringBuilder().append("    ").append(engineVariable).append(".load(\"").append(value.value()).append("\");\n");

    }

    /**
     * Sets a callback field
     *
     * @param progress       The generation progress
     * @param property       The property to inject
     * @param parentVariable The parent variable
     */
    private static void setCallback(final GenerationProgress progress, final ParsedProperty property, final String parentVariable) throws GenerationException {
        setField(progress, property, parentVariable, "javafx.util.Callback");
    }
}
