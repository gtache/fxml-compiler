package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;

import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.FX_ID;
import static com.github.gtache.fxml.compiler.impl.internal.GenerationHelper.getSortedAttributes;
import static java.util.Objects.requireNonNull;

/**
 * Helper methods for {@link GeneratorImpl} to format WebViews
 */
final class WebViewFormatter {

    private final HelperProvider helperProvider;
    private final StringBuilder sb;

    WebViewFormatter(final HelperProvider helperProvider, final StringBuilder sb) {
        this.helperProvider = requireNonNull(helperProvider);
        this.sb = requireNonNull(sb);
    }

    /**
     * Formats a WebView object
     *
     * @param parsedObject The parsed object
     * @param variableName The variable name
     * @throws GenerationException if an error occurs
     */
    void formatWebView(final ParsedObject parsedObject, final String variableName) throws GenerationException {
        if (parsedObject.children().isEmpty() && parsedObject.properties().isEmpty()) {
            final var sortedAttributes = getSortedAttributes(parsedObject);
            final var compatibilityHelper = helperProvider.getCompatibilityHelper();
            sb.append(compatibilityHelper.getStartVar("javafx.scene.web.WebView")).append(variableName).append(" = new javafx.scene.web.WebView();\n");
            final var engineVariable = helperProvider.getVariableProvider().getNextVariableName("engine");
            sb.append(compatibilityHelper.getStartVar("javafx.scene.web.WebEngine")).append(engineVariable).append(" = ").append(variableName).append(".getEngine();\n");
            for (final var value : sortedAttributes) {
                formatAttribute(value, parsedObject, variableName, engineVariable);
            }
        } else {
            throw new GenerationException("WebView cannot have children or properties : " + parsedObject);
        }
    }

    private void formatAttribute(final ParsedProperty value, final ParsedObject parsedObject,
                                 final String variableName, final String engineVariable) throws GenerationException {
        switch (value.name()) {
            case FX_ID -> {
                //Do nothing
            }
            case "confirmHandler" -> injectConfirmHandler(value, engineVariable);
            case "createPopupHandler" -> injectCreatePopupHandler(value, engineVariable);
            case "onAlert", "onResized", "onStatusChanged", "onVisibilityChanged" ->
                    injectEventHandler(value, engineVariable);
            case "promptHandler" -> injectPromptHandler(value, engineVariable);
            case "location" -> injectLocation(value, engineVariable);
            default -> helperProvider.getPropertyFormatter().formatProperty(value, parsedObject, variableName);
        }
    }

    private void injectConfirmHandler(final ParsedProperty value, final String engineVariable) throws GenerationException {
        if (value.value().startsWith("#")) {
            helperProvider.getControllerInjector().injectCallbackControllerMethod(value, engineVariable, "String.class");
        } else {
            setCallback(value, engineVariable);
        }
    }

    private void injectCreatePopupHandler(final ParsedProperty value, final String engineVariable) throws GenerationException {
        if (value.value().startsWith("#")) {
            helperProvider.getControllerInjector().injectCallbackControllerMethod(value, engineVariable, "javafx.scene.web.PopupFeatures.class");
        } else {
            setCallback(value, engineVariable);
        }
    }

    private void injectEventHandler(final ParsedProperty value, final String engineVariable) throws GenerationException {
        if (value.value().startsWith("#")) {
            helperProvider.getControllerInjector().injectEventHandlerControllerMethod(value, engineVariable);
        } else {
            helperProvider.getFieldSetter().setEventHandler(value, engineVariable);
        }
    }

    private void injectPromptHandler(final ParsedProperty value, final String engineVariable) throws GenerationException {
        if (value.value().startsWith("#")) {
            helperProvider.getControllerInjector().injectCallbackControllerMethod(value, engineVariable, "javafx.scene.web.PromptData.class");
        } else {
            setCallback(value, engineVariable);
        }
    }

    private void injectLocation(final ParsedProperty value, final String engineVariable) {
        sb.append("        ").append(engineVariable).append(".load(\"").append(value.value()).append("\");\n");

    }

    /**
     * Sets a callback field
     *
     * @param property       The property to inject
     * @param parentVariable The parent variable
     */
    private void setCallback(final ParsedProperty property, final String parentVariable) throws GenerationException {
        helperProvider.getFieldSetter().setField(property, parentVariable, "javafx.util.Callback");
    }
}
