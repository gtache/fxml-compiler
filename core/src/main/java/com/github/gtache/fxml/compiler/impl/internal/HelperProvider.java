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
     * @param progress The generation progress
     */
    public HelperProvider(final GenerationProgress progress) {
        this.progress = Objects.requireNonNull(progress);
        this.helpers = new HashMap<>();
    }

    ConstructorHelper getConstructorHelper() {
        return (ConstructorHelper) helpers.computeIfAbsent(ConstructorHelper.class, c -> new ConstructorHelper(this));
    }

    ControllerInjector getControllerInjector() {
        final var request = progress.request();
        final var controllerInfo = request.controllerInfo();
        final var parameters = request.parameters();
        final var fieldInjectionType = parameters.fieldInjectionType();
        final var methodInjectionType = parameters.methodInjectionType();
        final var sb = progress.stringBuilder();
        final var controllerFactoryPostAction = progress.controllerFactoryPostAction();
        return (ControllerInjector) helpers.computeIfAbsent(ControllerInjector.class, c -> new ControllerInjector(controllerInfo, fieldInjectionType, methodInjectionType, sb, controllerFactoryPostAction));
    }

    FieldSetter getFieldSetter() {
        final var fieldInjectionType = progress.request().parameters().fieldInjectionType();
        final var sb = progress.stringBuilder();
        final var controllerFactoryPostAction = progress.controllerFactoryPostAction();
        return (FieldSetter) helpers.computeIfAbsent(FieldSetter.class, c -> new FieldSetter(this, fieldInjectionType, sb, controllerFactoryPostAction));
    }

    FontFormatter getFontFormatter() {
        final var sb = progress.stringBuilder();
        return (FontFormatter) helpers.computeIfAbsent(FontFormatter.class, c -> new FontFormatter(this, sb));
    }

    GenerationCompatibilityHelper getCompatibilityHelper() {
        final var compatibility = progress.request().parameters().compatibility();
        return (GenerationCompatibilityHelper) helpers.computeIfAbsent(GenerationCompatibilityHelper.class, c -> new GenerationCompatibilityHelper(this, compatibility));
    }

    GenerationHelper getGenerationHelper() {
        final var controllerInfo = progress.request().controllerInfo();
        final var idToVariableInfo = progress.idToVariableInfo();
        return (GenerationHelper) helpers.computeIfAbsent(GenerationHelper.class, c -> new GenerationHelper(this, controllerInfo, idToVariableInfo));
    }

    public HelperMethodsFormatter getHelperMethodsFormatter() {
        final var parameters = progress.request().parameters();
        final var fieldInjectionType = parameters.fieldInjectionType();
        final var methodInjectionType = parameters.methodInjectionType();
        final var sb = progress.stringBuilder();
        return (HelperMethodsFormatter) helpers.computeIfAbsent(HelperMethodsFormatter.class, c -> new HelperMethodsFormatter(this, fieldInjectionType, methodInjectionType, sb));
    }

    ImageFormatter getImageFormatter() {
        return (ImageFormatter) helpers.computeIfAbsent(ImageFormatter.class, c -> new ImageFormatter(this, progress));
    }

    public InitializationFormatter getInitializationFormatter() {
        return (InitializationFormatter) helpers.computeIfAbsent(InitializationFormatter.class, c -> new InitializationFormatter(this, progress));
    }

    public LoadMethodFormatter getLoadMethodFormatter() {
        return (LoadMethodFormatter) helpers.computeIfAbsent(LoadMethodFormatter.class, c -> new LoadMethodFormatter(this, progress));
    }

    ObjectFormatter getObjectFormatter() {
        return (ObjectFormatter) helpers.computeIfAbsent(ObjectFormatter.class, c -> new ObjectFormatter(this, progress));
    }

    PropertyFormatter getPropertyFormatter() {
        return (PropertyFormatter) helpers.computeIfAbsent(PropertyFormatter.class, c -> new PropertyFormatter(this, progress));
    }

    ReflectionHelper getReflectionHelper() {
        final var controllerInfo = progress.request().controllerInfo();
        return (ReflectionHelper) helpers.computeIfAbsent(ReflectionHelper.class, c -> new ReflectionHelper(controllerInfo));
    }

    SceneFormatter getSceneFormatter() {
        return (SceneFormatter) helpers.computeIfAbsent(SceneFormatter.class, c -> new SceneFormatter(this, progress));
    }

    TriangleMeshFormatter getTriangleMeshFormatter() {
        final var sb = progress.stringBuilder();
        return (TriangleMeshFormatter) helpers.computeIfAbsent(TriangleMeshFormatter.class, c -> new TriangleMeshFormatter(this, sb));
    }

    URLFormatter getURLFormatter() {
        return (URLFormatter) helpers.computeIfAbsent(URLFormatter.class, c -> new URLFormatter(this, progress));
    }

    ValueFormatter getValueFormatter() {
        final var resourceInjectionType = progress.request().parameters().resourceInjectionType();
        final var idToVariableInfo = progress.idToVariableInfo();
        return (ValueFormatter) helpers.computeIfAbsent(ValueFormatter.class, c -> new ValueFormatter(resourceInjectionType, idToVariableInfo));
    }

    WebViewFormatter getWebViewFormatter() {
        return (WebViewFormatter) helpers.computeIfAbsent(WebViewFormatter.class, c -> new WebViewFormatter(this, progress));
    }
}
