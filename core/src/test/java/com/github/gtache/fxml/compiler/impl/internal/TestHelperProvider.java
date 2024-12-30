package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.ControllerInjectionType;
import com.github.gtache.fxml.compiler.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.ResourceBundleInjectionType;
import com.github.gtache.fxml.compiler.compatibility.GenerationCompatibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestHelperProvider {
    private final GenerationProgress progress;
    private final GenerationRequest request;
    private final GenerationParameters parameters;
    private final GenerationCompatibility compatibility;
    private final ControllerInjectionType controllerInjectionType;
    private final ControllerFieldInjectionType fieldInjectionType;
    private final ControllerMethodsInjectionType methodInjectionType;
    private final ResourceBundleInjectionType resourceInjectionType;
    private final ControllerInfo controllerInfo;
    private final StringBuilder sb;
    private final List<String> controllerFactoryPostAction;
    private final HelperProvider helperProvider;

    @BeforeEach
    void beforeEach() {
        when(progress.request()).thenReturn(request);
        when(request.parameters()).thenReturn(parameters);
        when(request.controllerInfo()).thenReturn(controllerInfo);
        when(parameters.controllerInjectionType()).thenReturn(controllerInjectionType);
        when(parameters.fieldInjectionType()).thenReturn(fieldInjectionType);
        when(parameters.methodInjectionType()).thenReturn(methodInjectionType);
        when(parameters.resourceInjectionType()).thenReturn(resourceInjectionType);
        when(parameters.compatibility()).thenReturn(compatibility);
        when(progress.stringBuilder()).thenReturn(sb);
        when(progress.controllerFactoryPostAction()).thenReturn(controllerFactoryPostAction);
    }

    TestHelperProvider(@Mock final GenerationProgress progress, @Mock final GenerationRequest request, @Mock final GenerationCompatibility compatibility,
                       @Mock final GenerationParameters parameters, @Mock final ControllerInfo controllerInfo) {
        this.progress = Objects.requireNonNull(progress);
        this.request = Objects.requireNonNull(request);
        this.compatibility = Objects.requireNonNull(compatibility);
        this.parameters = Objects.requireNonNull(parameters);
        this.controllerInfo = Objects.requireNonNull(controllerInfo);
        this.controllerInjectionType = ControllerInjectionType.INSTANCE;
        this.fieldInjectionType = ControllerFieldInjectionType.FACTORY;
        this.methodInjectionType = ControllerMethodsInjectionType.REFLECTION;
        this.resourceInjectionType = ResourceBundleInjectionType.CONSTRUCTOR;
        this.sb = new StringBuilder();
        this.controllerFactoryPostAction = List.of();
        this.helperProvider = new HelperProvider(progress);
    }

    @Test
    void testGetBindingFormatter() {
        final var bindingFormatter = helperProvider.getBindingFormatter();
        assertSame(bindingFormatter, helperProvider.getBindingFormatter());
    }
    
    @Test
    void testControllerInjector() {
        final var injector = helperProvider.getControllerInjector();
        assertSame(injector, helperProvider.getControllerInjector());
    }

    @Test
    void testGetExpressionFormatter() {
        final var expressionFormatter = helperProvider.getExpressionFormatter();
        assertSame(expressionFormatter, helperProvider.getExpressionFormatter());
    }

    @Test
    void testGetFieldSetter() {
        final var fieldSetter = helperProvider.getFieldSetter();
        assertSame(fieldSetter, helperProvider.getFieldSetter());
    }

    @Test
    void testGetFontFormatter() {
        final var fontFormatter = helperProvider.getFontFormatter();
        assertSame(fontFormatter, helperProvider.getFontFormatter());
    }

    @Test
    void testGetCompatibilityHelper() {
        final var compatibilityHelper = helperProvider.getCompatibilityHelper();
        assertSame(compatibilityHelper, helperProvider.getCompatibilityHelper());
    }

    @Test
    void testGetHelperMethodsFormatter() {
        final var helperMethodsFormatter = helperProvider.getHelperMethodsFormatter();
        assertSame(helperMethodsFormatter, helperProvider.getHelperMethodsFormatter());
    }

    @Test
    void testGetImageFormatter() {
        final var imageFormatter = helperProvider.getImageFormatter();
        assertSame(imageFormatter, helperProvider.getImageFormatter());
    }

    @Test
    void testGetInitializationFormatter() {
        final var initializationFormatter = helperProvider.getInitializationFormatter();
        assertSame(initializationFormatter, helperProvider.getInitializationFormatter());
    }

    @Test
    void testGetLoadMethodFormatter() {
        final var loadMethodFormatter = helperProvider.getLoadMethodFormatter();
        assertSame(loadMethodFormatter, helperProvider.getLoadMethodFormatter());
    }

    @Test
    void testGetObjectFormatter() {
        final var objectFormatter = helperProvider.getObjectFormatter();
        assertSame(objectFormatter, helperProvider.getObjectFormatter());
    }

    @Test
    void testGetPropertyFormatter() {
        final var propertyFormatter = helperProvider.getPropertyFormatter();
        assertSame(propertyFormatter, helperProvider.getPropertyFormatter());
    }

    @Test
    void testGetReflectionHelper() {
        final var reflectionHelper = helperProvider.getReflectionHelper();
        assertSame(reflectionHelper, helperProvider.getReflectionHelper());
    }

    @Test
    void testGetSceneFormatter() {
        final var sceneFormatter = helperProvider.getSceneFormatter();
        assertSame(sceneFormatter, helperProvider.getSceneFormatter());
    }

    @Test
    void testGetTriangleMeshFormatter() {
        final var triangleMeshFormatter = helperProvider.getTriangleMeshFormatter();
        assertSame(triangleMeshFormatter, helperProvider.getTriangleMeshFormatter());
    }

    @Test
    void testGetURLFormatter() {
        final var urlFormatter = helperProvider.getURLFormatter();
        assertSame(urlFormatter, helperProvider.getURLFormatter());
    }

    @Test
    void testGetValueFormatter() {
        final var valueFormatter = helperProvider.getValueFormatter();
        assertSame(valueFormatter, helperProvider.getValueFormatter());
    }

    @Test
    void testGetValueClassGuesser() {
        final var valueClassGuesser = helperProvider.getValueClassGuesser();
        assertSame(valueClassGuesser, helperProvider.getValueClassGuesser());
    }

    @Test
    void testGetVariableProvider() {
        final var variableProvider = helperProvider.getVariableProvider();
        assertSame(variableProvider, helperProvider.getVariableProvider());
    }

    @Test
    void getWebViewFormatter() {
        final var webViewFormatter = helperProvider.getWebViewFormatter();
        assertSame(webViewFormatter, helperProvider.getWebViewFormatter());
    }
}
