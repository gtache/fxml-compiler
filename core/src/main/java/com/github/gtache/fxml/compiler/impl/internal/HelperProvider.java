package com.github.gtache.fxml.compiler.impl.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provider of helper classes
 */
public class HelperProvider {

    private final Map<Class<?>, Object> helpers;
    private final GenerationProgress progress;

    /**
     * Instantiates a new helper provider
     *
     * @param progress The generation progress
     */
    public HelperProvider(final GenerationProgress progress) {
        this.progress = Objects.requireNonNull(progress);
        this.helpers = new HashMap<>();
    }

    BindingFormatter getBindingFormatter() {
        return (BindingFormatter) helpers.computeIfAbsent(BindingFormatter.class, c -> {
            final var fieldInjectionType = progress.request().parameters().fieldInjectionType();
            final var sb = progress.stringBuilder();
            final var controllerFactoryPostAction = progress.controllerFactoryPostAction();
            return new BindingFormatter(this, fieldInjectionType, sb, controllerFactoryPostAction);
        });
    }

    ControllerInjector getControllerInjector() {
        return (ControllerInjector) helpers.computeIfAbsent(ControllerInjector.class, c -> {
            final var request = progress.request();
            final var controllerInfo = request.controllerInfo();
            final var parameters = request.parameters();
            final var fieldInjectionType = parameters.fieldInjectionType();
            final var methodInjectionType = parameters.methodInjectionType();
            final var sb = progress.stringBuilder();
            final var controllerFactoryPostAction = progress.controllerFactoryPostAction();
            return new ControllerInjector(controllerInfo, fieldInjectionType, methodInjectionType, sb, controllerFactoryPostAction);
        });
    }

    ExpressionFormatter getExpressionFormatter() {
        return (ExpressionFormatter) helpers.computeIfAbsent(ExpressionFormatter.class, c -> {
            final var fieldInjectionType = progress.request().parameters().fieldInjectionType();
            final var sb = progress.stringBuilder();
            return new ExpressionFormatter(this, fieldInjectionType, sb);
        });
    }

    FieldSetter getFieldSetter() {
        return (FieldSetter) helpers.computeIfAbsent(FieldSetter.class, c -> {
            final var fieldInjectionType = progress.request().parameters().fieldInjectionType();
            final var sb = progress.stringBuilder();
            final var controllerFactoryPostAction = progress.controllerFactoryPostAction();
            return new FieldSetter(this, fieldInjectionType, sb, controllerFactoryPostAction);
        });
    }

    FontFormatter getFontFormatter() {
        return (FontFormatter) helpers.computeIfAbsent(FontFormatter.class, c -> {
            final var sb = progress.stringBuilder();
            return new FontFormatter(this, sb);
        });
    }

    GenerationCompatibilityHelper getCompatibilityHelper() {
        return (GenerationCompatibilityHelper) helpers.computeIfAbsent(GenerationCompatibilityHelper.class, c -> {
            final var compatibility = progress.request().parameters().compatibility();
            return new GenerationCompatibilityHelper(this, compatibility);
        });
    }

    public HelperMethodsFormatter getHelperMethodsFormatter() {
        return (HelperMethodsFormatter) helpers.computeIfAbsent(HelperMethodsFormatter.class, c -> {
            final var parameters = progress.request().parameters();
            final var fieldInjectionType = parameters.fieldInjectionType();
            final var methodInjectionType = parameters.methodInjectionType();
            final var sb = progress.stringBuilder();
            return new HelperMethodsFormatter(this, fieldInjectionType, methodInjectionType, sb);
        });
    }

    ImageFormatter getImageFormatter() {
        return (ImageFormatter) helpers.computeIfAbsent(ImageFormatter.class, c -> {
            final var sb = progress.stringBuilder();
            final var useImageInputStreamConstructor = progress.request().parameters().useImageInputStreamConstructor();
            return new ImageFormatter(this, sb, useImageInputStreamConstructor);
        });
    }

    public InitializationFormatter getInitializationFormatter() {
        return (InitializationFormatter) helpers.computeIfAbsent(InitializationFormatter.class, c -> {
            final var request = progress.request();
            final var sb = progress.stringBuilder();
            return new InitializationFormatter(this, request, sb);
        });
    }

    public LoadMethodFormatter getLoadMethodFormatter() {
        return (LoadMethodFormatter) helpers.computeIfAbsent(LoadMethodFormatter.class, c -> new LoadMethodFormatter(this, progress));
    }

    ObjectFormatter getObjectFormatter() {
        return (ObjectFormatter) helpers.computeIfAbsent(ObjectFormatter.class, c -> {
            final var request = progress.request();
            final var sb = progress.stringBuilder();
            return new ObjectFormatter(this, request, sb);
        });
    }

    PropertyFormatter getPropertyFormatter() {
        return (PropertyFormatter) helpers.computeIfAbsent(PropertyFormatter.class, c -> new PropertyFormatter(this, progress));
    }

    ReflectionHelper getReflectionHelper() {
        return (ReflectionHelper) helpers.computeIfAbsent(ReflectionHelper.class, c -> {
            final var controllerInfo = progress.request().controllerInfo();
            return new ReflectionHelper(controllerInfo);
        });
    }

    SceneFormatter getSceneFormatter() {
        return (SceneFormatter) helpers.computeIfAbsent(SceneFormatter.class, c -> {
            final var sb = progress.stringBuilder();
            return new SceneFormatter(this, sb);
        });
    }

    TriangleMeshFormatter getTriangleMeshFormatter() {
        return (TriangleMeshFormatter) helpers.computeIfAbsent(TriangleMeshFormatter.class, c -> {
            final var sb = progress.stringBuilder();
            return new TriangleMeshFormatter(this, sb);
        });
    }

    URLFormatter getURLFormatter() {
        return (URLFormatter) helpers.computeIfAbsent(URLFormatter.class, c -> {
            final var sb = progress.stringBuilder();
            return new URLFormatter(this, sb);
        });
    }

    ValueFormatter getValueFormatter() {
        return (ValueFormatter) helpers.computeIfAbsent(ValueFormatter.class, c -> {
            final var resourceInjectionType = progress.request().parameters().resourceInjectionType();
            return new ValueFormatter(this, resourceInjectionType);
        });
    }

    ValueClassGuesser getValueClassGuesser() {
        return (ValueClassGuesser) helpers.computeIfAbsent(ValueClassGuesser.class, c -> new ValueClassGuesser(this));
    }

    VariableProvider getVariableProvider() {
        return (VariableProvider) helpers.computeIfAbsent(VariableProvider.class, c -> new VariableProvider());
    }

    WebViewFormatter getWebViewFormatter() {
        return (WebViewFormatter) helpers.computeIfAbsent(WebViewFormatter.class, c -> {
            final var sb = progress.stringBuilder();
            return new WebViewFormatter(this, sb);
        });
    }
}
