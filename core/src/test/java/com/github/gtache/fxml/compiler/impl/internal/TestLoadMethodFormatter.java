package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.ControllerInjectionType;
import com.github.gtache.fxml.compiler.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.ResourceBundleInjectionType;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestLoadMethodFormatter {

    private final HelperProvider helperProvider;
    private final ObjectFormatter objectFormatter;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final VariableProvider variableProvider;
    private final GenerationProgress progress;
    private final GenerationRequest request;
    private final GenerationParameters parameters;
    private final ParsedObject object;
    private final ControllerInfo controllerInfo;
    private final String className;
    private final StringBuilder sb;
    private final List<String> controllerFactoryPostAction;
    private final LoadMethodFormatter loadMethodFormatter;

    TestLoadMethodFormatter(@Mock final HelperProvider helperProvider, @Mock final ObjectFormatter objectFormatter,
                            @Mock final GenerationCompatibilityHelper compatibilityHelper, @Mock final VariableProvider variableProvider,
                            @Mock final GenerationProgress progress, @Mock final GenerationRequest request, @Mock final GenerationParameters parameters,
                            @Mock final ParsedObject object, @Mock final ControllerInfo controllerInfo) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.objectFormatter = Objects.requireNonNull(objectFormatter);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.request = Objects.requireNonNull(request);
        this.parameters = Objects.requireNonNull(parameters);
        this.object = Objects.requireNonNull(object);
        this.controllerInfo = Objects.requireNonNull(controllerInfo);
        this.className = "class";
        this.sb = new StringBuilder();
        this.controllerFactoryPostAction = new ArrayList<>();
        this.progress = Objects.requireNonNull(progress);
        this.loadMethodFormatter = new LoadMethodFormatter(helperProvider, progress);
    }

    @BeforeEach
    void beforeEach() throws GenerationException {
        when(helperProvider.getObjectFormatter()).thenReturn(objectFormatter);
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
        when(object.className()).thenReturn(className);
        when(progress.request()).thenReturn(request);
        when(request.controllerInfo()).thenReturn(controllerInfo);
        when(request.parameters()).thenReturn(parameters);
        when(request.rootObject()).thenReturn(object);
        when(progress.stringBuilder()).thenReturn(sb);
        when(progress.controllerFactoryPostAction()).thenReturn(controllerFactoryPostAction);
        when(variableProvider.getNextVariableName(any(String.class))).then(i -> i.getArgument(0));
        doAnswer(i -> sb.append(i.getArgument(0) + "-" + i.getArgument(1))).when(objectFormatter).format(any(), any());
        when(controllerInfo.className()).thenReturn(className);
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        when(compatibilityHelper.getStartVar(anyString(), anyInt())).then(i -> i.getArgument(0));
    }

    @Test
    void testEasiestCase() throws GenerationException {
        when(object.toString()).thenReturn("object");
        loadMethodFormatter.formatLoadMethod();
        final var expected = """
                    /**
                     * Loads the view. Can only be called once.
                     *
                     * @return The view parent
                     */
                    public <T> T load() {
                        if (loaded) {
                            throw new IllegalStateException("Already loaded");
                        }
                object-class        loaded = true;
                        return (T) class;
                    }
                """;
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(object, "class");
    }

    @Test
    void testConstructorNameFactoryInitialize() throws GenerationException {
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR_NAME);
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.FACTORY);
        when(controllerInfo.hasInitialize()).thenReturn(true);
        when(object.toString()).thenReturn("object");
        loadMethodFormatter.formatLoadMethod();
        final var expected = """
                    /**
                     * Loads the view. Can only be called once.
                     *
                     * @return The view parent
                     */
                    public <T> T load() {
                        if (loaded) {
                            throw new IllegalStateException("Already loaded");
                        }
                java.util.ResourceBundleresourceBundle = java.util.ResourceBundle.getBundle(resourceBundleName);
                        controller = controllerFactory.create();
                object-class        controller.initialize();
                        loaded = true;
                        return (T) class;
                    }
                """;
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(object, "class");
    }

    @Test
    void testGetBundleFieldFactoryReflectionInitialize() throws GenerationException {
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.GET_BUNDLE);
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionType.FACTORY);
        when(parameters.methodInjectionType()).thenReturn(ControllerMethodsInjectionType.REFLECTION);
        when(parameters.bundleMap()).thenReturn(Map.of(className, "bundle"));
        when(controllerInfo.hasInitialize()).thenReturn(true);
        when(object.toString()).thenReturn("object");
        loadMethodFormatter.formatLoadMethod();
        final var expected = """
                    /**
                     * Loads the view. Can only be called once.
                     *
                     * @return The view parent
                     */
                    public <T> T load() {
                        if (loaded) {
                            throw new IllegalStateException("Already loaded");
                        }
                java.util.ResourceBundleresourceBundle = java.util.ResourceBundle.getBundle("bundle");
                java.util.Map<String, Object>fieldMap = new java.util.HashMap<String, Object>();
                object-class        controller = controllerFactory.create(fieldMap);
                        try {
                            java.lang.reflect.Methodinitialize = controller.getClass().getDeclaredMethod("initialize");
                            initialize.setAccessible(true);
                            initialize.invoke(controller);
                        } catch (final java.lang.reflect.InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                            throw new RuntimeException("Error using reflection", e);
                        }
                        loaded = true;
                        return (T) class;
                    }
                """;
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(object, "class");
    }

    @Test
    void testGetBundleFieldFactoryReflectionNoBundle() throws GenerationException {
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.GET_BUNDLE);
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionType.FACTORY);
        when(parameters.methodInjectionType()).thenReturn(com.github.gtache.fxml.compiler.ControllerMethodsInjectionType.REFLECTION);
        when(parameters.bundleMap()).thenReturn(Map.of());
        when(object.toString()).thenReturn("object");
        loadMethodFormatter.formatLoadMethod();
        final var expected = """
                    /**
                     * Loads the view. Can only be called once.
                     *
                     * @return The view parent
                     */
                    public <T> T load() {
                        if (loaded) {
                            throw new IllegalStateException("Already loaded");
                        }
                java.util.Map<String, Object>fieldMap = new java.util.HashMap<String, Object>();
                object-class        controller = controllerFactory.create(fieldMap);
                        loaded = true;
                        return (T) class;
                    }
                """;
        assertEquals(expected, sb.toString());
        verify(objectFormatter).format(object, "class");
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new LoadMethodFormatter(null, progress));
        assertThrows(NullPointerException.class, () -> new LoadMethodFormatter(helperProvider, null));
    }
}