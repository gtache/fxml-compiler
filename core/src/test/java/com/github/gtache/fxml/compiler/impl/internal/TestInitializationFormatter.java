package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.ControllerFieldInjectionType;
import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.ControllerInjectionType;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.ResourceBundleInjectionType;
import com.github.gtache.fxml.compiler.SourceInfo;
import com.github.gtache.fxml.compiler.parsing.ParsedInclude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestInitializationFormatter {

    private final GenerationRequest request;
    private final GenerationParameters parameters;
    private final HelperProvider helperProvider;
    private final GenerationCompatibilityHelper compatibilityHelper;
    private final VariableProvider variableProvider;
    private final ControllerInfo controllerInfo;
    private final SourceInfo sourceInfo;
    private final ParsedInclude include;
    private final Map<String, SourceInfo> sourceToSourceInfo;
    private final StringBuilder sb;
    private final Map<String, String> controllerClassToVariable;
    private final InitializationFormatter initializationFormatter;


    TestInitializationFormatter(@Mock final GenerationRequest request, @Mock final GenerationParameters parameters, @Mock final GenerationCompatibilityHelper compatibilityHelper,
                                @Mock final HelperProvider helperProvider, @Mock final VariableProvider variableProvider,
                                @Mock final ControllerInfo controllerInfo, @Mock final SourceInfo sourceInfo,
                                @Mock final ParsedInclude include) {
        this.request = Objects.requireNonNull(request);
        this.parameters = Objects.requireNonNull(parameters);
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.compatibilityHelper = Objects.requireNonNull(compatibilityHelper);
        this.variableProvider = Objects.requireNonNull(variableProvider);
        this.controllerInfo = Objects.requireNonNull(controllerInfo);
        this.sourceInfo = Objects.requireNonNull(sourceInfo);
        this.include = Objects.requireNonNull(include);
        this.sourceToSourceInfo = new HashMap<>();
        this.sb = new StringBuilder();
        this.controllerClassToVariable = new HashMap<>();
        this.initializationFormatter = new InitializationFormatter(helperProvider, request, sb, controllerClassToVariable);
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getCompatibilityHelper()).thenReturn(compatibilityHelper);
        when(helperProvider.getVariableProvider()).thenReturn(variableProvider);
        when(compatibilityHelper.getStartVar(anyString())).then(i -> i.getArgument(0));
        when(variableProvider.getNextVariableName(anyString())).then(i -> i.getArgument(0));
        when(request.parameters()).thenReturn(parameters);
        when(request.outputClassName()).thenReturn("com.github.gtache.fxml.OutputClassName");
        when(request.sourceInfo()).thenReturn(sourceInfo);
        when(request.controllerInfo()).thenReturn(controllerInfo);
        when(controllerInfo.className()).thenReturn("com.github.gtache.fxml.ControllerClassName");
        when(sourceInfo.sourceToSourceInfo()).thenReturn(sourceToSourceInfo);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.GET_BUNDLE);
    }

    @Test
    void testDuplicateControllersNoFactory(@Mock final SourceInfo sourceInfo2, @Mock final SourceInfo sourceInfo3,
                                           @Mock final SourceInfo sourceInfo4) {
        when(sourceInfo.controllerClassName()).thenReturn("controller");
        when(sourceInfo.includedSources()).thenReturn(List.of(sourceInfo2));
        when(sourceInfo2.controllerClassName()).thenReturn("controller2");
        when(sourceInfo2.includedSources()).thenReturn(List.of(sourceInfo3, sourceInfo4));
        when(sourceInfo3.controllerClassName()).thenReturn("controller3");
        when(sourceInfo3.includedSources()).thenReturn(List.of(sourceInfo4));
        when(sourceInfo4.controllerClassName()).thenReturn("controller4");
        assertThrows(GenerationException.class, initializationFormatter::formatFieldsAndConstructor);
    }

    @Test
    void testFormatFieldFactory() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.FACTORY);
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionType.FACTORY);
        final var expected = """
                    private final java.util.function.Function<java.util.Map<String, Object>, com.github.gtache.fxml.ControllerClassName> controllerFactory;
                    private com.github.gtache.fxml.ControllerClassName controller;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controllerFactory The controller factory
                     */
                    public OutputClassName(final java.util.function.Function<java.util.Map<String, Object>, com.github.gtache.fxml.ControllerClassName> controllerFactory) {
                        this.controllerFactory = java.util.Objects.requireNonNull(controllerFactory);
                    }
                """;
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatBaseFactory() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.FACTORY);
        final var expected = """
                    private final java.util.function.Supplier<com.github.gtache.fxml.ControllerClassName> controllerFactory;
                    private com.github.gtache.fxml.ControllerClassName controller;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controllerFactory The controller factory
                     */
                    public OutputClassName(final java.util.function.Supplier<com.github.gtache.fxml.ControllerClassName> controllerFactory) {
                        this.controllerFactory = java.util.Objects.requireNonNull(controllerFactory);
                    }
                """;
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatInstance() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        final var expected = """
                    private final com.github.gtache.fxml.ControllerClassName controller;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controller The controller instance
                     */
                    public OutputClassName(final com.github.gtache.fxml.ControllerClassName controller) {
                        this.controller = java.util.Objects.requireNonNull(controller);
                    }
                """;
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testConstructorResourceBundle() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR);
        final var expected = """
                    private final com.github.gtache.fxml.ControllerClassName controller;
                    private final java.util.ResourceBundle resourceBundle;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controller The controller instance
                     * @param resourceBundle The resource bundle
                     */
                    public OutputClassName(final com.github.gtache.fxml.ControllerClassName controller, final java.util.ResourceBundle resourceBundle) {
                        this.controller = java.util.Objects.requireNonNull(controller);
                        this.resourceBundle = java.util.Objects.requireNonNull(resourceBundle);
                    }
                """;
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testConstructorFunctionResourceBundle() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR_FUNCTION);
        final var expected = """
                    private final com.github.gtache.fxml.ControllerClassName controller;
                    private final java.util.function.Function<String, String> resourceBundleFunction;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controller The controller instance
                     * @param resourceBundleFunction The resource bundle
                     */
                    public OutputClassName(final com.github.gtache.fxml.ControllerClassName controller, final java.util.function.Function<String, String> resourceBundleFunction) {
                        this.controller = java.util.Objects.requireNonNull(controller);
                        this.resourceBundleFunction = java.util.Objects.requireNonNull(resourceBundleFunction);
                    }
                """;
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testConstructorNamedResourceBundle() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR_NAME);
        final var expected = """
                    private final com.github.gtache.fxml.ControllerClassName controller;
                    private final String resourceBundleName;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controller The controller instance
                     * @param resourceBundleName The resource bundle
                     */
                    public OutputClassName(final com.github.gtache.fxml.ControllerClassName controller, final String resourceBundleName) {
                        this.controller = java.util.Objects.requireNonNull(controller);
                        this.resourceBundleName = java.util.Objects.requireNonNull(resourceBundleName);
                    }
                """;
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testHasControllersFieldFactory() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.FACTORY);
        when(parameters.fieldInjectionType()).thenReturn(ControllerFieldInjectionType.FACTORY);
        final var expected = """
                    private final java.util.function.Function<java.util.Map<String, Object>, com.github.gtache.fxml.ControllerClassName> controllerFactory;
                    private final java.util.function.Function<java.util.Map<String, Object>, com.github.gtache.fxml.Controller2> controller2Factory;
                    private final java.util.function.Function<java.util.Map<String, Object>, com.github.gtache.fxml.Controller3> controller3Factory;
                    private com.github.gtache.fxml.ControllerClassName controller;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controllerFactory The controller factory
                     * @param controller2Factory The subcontroller factory for com.github.gtache.fxml.Controller2
                     * @param controller3Factory The subcontroller factory for com.github.gtache.fxml.Controller3
                     */
                    public OutputClassName(final java.util.function.Function<java.util.Map<String, Object>, com.github.gtache.fxml.ControllerClassName> controllerFactory, final java.util.function.Function<java.util.Map<String, Object>, com.github.gtache.fxml.Controller2> controller2Factory, final java.util.function.Function<java.util.Map<String, Object>, com.github.gtache.fxml.Controller3> controller3Factory) {
                        this.controllerFactory = java.util.Objects.requireNonNull(controllerFactory);
                        this.controller2Factory = java.util.Objects.requireNonNull(controller2Factory);
                        this.controller3Factory = java.util.Objects.requireNonNull(controller3Factory);
                    }
                """;
        final var source2 = mock(SourceInfo.class);
        when(source2.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        final var source3 = mock(SourceInfo.class);
        when(source3.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller3");
        when(sourceInfo.includedSources()).thenReturn(List.of(source2));
        when(source2.includedSources()).thenReturn(List.of(source3));
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testHasControllersBaseFactory() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.FACTORY);
        final var expected = """
                    private final java.util.function.Supplier<com.github.gtache.fxml.ControllerClassName> controllerFactory;
                    private final java.util.function.Supplier<com.github.gtache.fxml.Controller2> controller2Factory;
                    private final java.util.function.Supplier<com.github.gtache.fxml.Controller3> controller3Factory;
                    private com.github.gtache.fxml.ControllerClassName controller;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controllerFactory The controller factory
                     * @param controller2Factory The subcontroller factory for com.github.gtache.fxml.Controller2
                     * @param controller3Factory The subcontroller factory for com.github.gtache.fxml.Controller3
                     */
                    public OutputClassName(final java.util.function.Supplier<com.github.gtache.fxml.ControllerClassName> controllerFactory, final java.util.function.Supplier<com.github.gtache.fxml.Controller2> controller2Factory, final java.util.function.Supplier<com.github.gtache.fxml.Controller3> controller3Factory) {
                        this.controllerFactory = java.util.Objects.requireNonNull(controllerFactory);
                        this.controller2Factory = java.util.Objects.requireNonNull(controller2Factory);
                        this.controller3Factory = java.util.Objects.requireNonNull(controller3Factory);
                    }
                """;
        final var source2 = mock(SourceInfo.class);
        when(source2.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        final var source3 = mock(SourceInfo.class);
        when(source3.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller3");
        when(sourceInfo.includedSources()).thenReturn(List.of(source2));
        when(source2.includedSources()).thenReturn(List.of(source3));
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testHasControllersInstance() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        final var expected = """
                    private final com.github.gtache.fxml.ControllerClassName controller;
                    private final com.github.gtache.fxml.Controller2 controller2;
                    private final com.github.gtache.fxml.Controller3 controller3;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controller The controller instance
                     * @param controller2 The subcontroller instance for com.github.gtache.fxml.Controller2
                     * @param controller3 The subcontroller instance for com.github.gtache.fxml.Controller3
                     */
                    public OutputClassName(final com.github.gtache.fxml.ControllerClassName controller, final com.github.gtache.fxml.Controller2 controller2, final com.github.gtache.fxml.Controller3 controller3) {
                        this.controller = java.util.Objects.requireNonNull(controller);
                        this.controller2 = java.util.Objects.requireNonNull(controller2);
                        this.controller3 = java.util.Objects.requireNonNull(controller3);
                    }
                """;
        final var source2 = mock(SourceInfo.class);
        when(source2.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        final var source3 = mock(SourceInfo.class);
        when(source3.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller3");
        when(sourceInfo.includedSources()).thenReturn(List.of(source2));
        when(source2.includedSources()).thenReturn(List.of(source3));
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testComplete() throws GenerationException {
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.FACTORY);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR);
        final var expected = """
                    private final java.util.function.Supplier<com.github.gtache.fxml.ControllerClassName> controllerFactory;
                    private final java.util.function.Supplier<com.github.gtache.fxml.Controller2> controller2Factory;
                    private final java.util.function.Supplier<com.github.gtache.fxml.Controller3> controller3Factory;
                    private com.github.gtache.fxml.ControllerClassName controller;
                    private final java.util.ResourceBundle resourceBundle;
                    private boolean loaded;
                
                    /**
                     * Instantiates a new OutputClassName
                     * @param controllerFactory The controller factory
                     * @param controller2Factory The subcontroller factory for com.github.gtache.fxml.Controller2
                     * @param controller3Factory The subcontroller factory for com.github.gtache.fxml.Controller3
                     * @param resourceBundle The resource bundle
                     */
                    public OutputClassName(final java.util.function.Supplier<com.github.gtache.fxml.ControllerClassName> controllerFactory, final java.util.function.Supplier<com.github.gtache.fxml.Controller2> controller2Factory, final java.util.function.Supplier<com.github.gtache.fxml.Controller3> controller3Factory, final java.util.ResourceBundle resourceBundle) {
                        this.controllerFactory = java.util.Objects.requireNonNull(controllerFactory);
                        this.controller2Factory = java.util.Objects.requireNonNull(controller2Factory);
                        this.controller3Factory = java.util.Objects.requireNonNull(controller3Factory);
                        this.resourceBundle = java.util.Objects.requireNonNull(resourceBundle);
                    }
                """;
        final var source2 = mock(SourceInfo.class);
        when(source2.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        final var source3 = mock(SourceInfo.class);
        when(source3.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller3");
        when(sourceInfo.includedSources()).thenReturn(List.of(source2, source3));
        when(source2.includedSources()).thenReturn(List.of(source3));
        initializationFormatter.formatFieldsAndConstructor();
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewConstructorCallNullSubInfo() {
        assertThrows(GenerationException.class, () -> initializationFormatter.formatSubViewConstructorCall(include));
    }

    @Test
    void testFormatSubViewInstance(@Mock final SourceInfo subInfo) throws GenerationException {
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        when(subInfo.includedSources()).thenReturn(List.of());
        final var expected = "com.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewFactory(@Mock final SourceInfo subInfo) throws GenerationException {
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.FACTORY);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        when(subInfo.includedSources()).thenReturn(List.of());
        final var expected = "com.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2Factory);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewMultipleInstanceFactory(@Mock final SourceInfo subInfo) throws GenerationException {
        final var source3 = mock(SourceInfo.class);
        when(source3.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller3");
        when(source3.includedSources()).thenReturn(List.of());
        final var source4 = mock(SourceInfo.class);
        when(source4.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller4");
        when(source4.includedSources()).thenReturn(List.of());
        when(subInfo.includedSources()).thenReturn(List.of(source3, source4));
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.FACTORY);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller3", "controller3");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller4", "controller4");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        final var expected = "com.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2Factory, controller3Factory, controller4Factory);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewMultipleInstance(@Mock final SourceInfo subInfo) throws GenerationException {
        final var source3 = mock(SourceInfo.class);
        when(source3.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller3");
        when(source3.includedSources()).thenReturn(List.of());
        final var source4 = mock(SourceInfo.class);
        when(source4.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller4");
        when(source4.includedSources()).thenReturn(List.of());
        when(subInfo.includedSources()).thenReturn(List.of(source3, source4));
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller3", "controller3");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller4", "controller4");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        final var expected = "com.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2, controller3, controller4);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewRequiresBundleGetter(@Mock final SourceInfo subInfo) throws GenerationException {
        when(subInfo.includedSources()).thenReturn(List.of());
        when(subInfo.requiresResourceBundle()).thenReturn(true);
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.GETTER);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        final var expected = "com.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewRequiresBundleGetBundle(@Mock final SourceInfo subInfo) throws GenerationException {
        when(subInfo.includedSources()).thenReturn(List.of());
        when(subInfo.requiresResourceBundle()).thenReturn(true);
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.GET_BUNDLE);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        final var expected = "com.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewRequiresBundleConstructorNoResources(@Mock final SourceInfo subInfo) throws GenerationException {
        when(subInfo.includedSources()).thenReturn(List.of());
        when(subInfo.requiresResourceBundle()).thenReturn(true);
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        final var expected = "com.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2, resourceBundle);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewRequiresBundleConstructorFunctionNoResources(@Mock final SourceInfo subInfo) throws GenerationException {
        when(subInfo.includedSources()).thenReturn(List.of());
        when(subInfo.requiresResourceBundle()).thenReturn(true);
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR_FUNCTION);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        final var expected = "com.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2, resourceBundleFunction);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewRequiresBundleConstructorNameNoResources(@Mock final SourceInfo subInfo) throws GenerationException {
        when(subInfo.includedSources()).thenReturn(List.of());
        when(subInfo.requiresResourceBundle()).thenReturn(true);
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR_NAME);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        final var expected = "com.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2, resourceBundleName);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewRequiresBundleConstructorResources(@Mock final SourceInfo subInfo) throws GenerationException {
        when(subInfo.includedSources()).thenReturn(List.of());
        when(subInfo.requiresResourceBundle()).thenReturn(true);
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        when(include.resources()).thenReturn("resources");
        final var expected = "java.util.ResourceBundleresourceBundle = java.util.ResourceBundle.getBundle(\"resources\");\ncom.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2, resourceBundle);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewRequiresBundleConstructorFunctionResources(@Mock final SourceInfo subInfo) throws GenerationException {
        when(subInfo.includedSources()).thenReturn(List.of());
        when(subInfo.requiresResourceBundle()).thenReturn(true);
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR_FUNCTION);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        when(include.resources()).thenReturn("resources");
        final var expected = "java.util.ResourceBundleresourceBundle = java.util.ResourceBundle.getBundle(\"resources\");\njava.util.function.Function<String, String>resourceBundleFunction = (java.util.function.Function<String, String>) s -> resourceBundle.getString(s);\ncom.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2, resourceBundleFunction);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewRequiresBundleConstructorName(@Mock final SourceInfo subInfo) throws GenerationException {
        when(subInfo.includedSources()).thenReturn(List.of());
        when(subInfo.requiresResourceBundle()).thenReturn(true);
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR_NAME);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        when(include.resources()).thenReturn("resources");
        final var expected = "StringresourceBundleName = \"resources\";\ncom.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2, resourceBundleName);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testFormatSubViewComplete(@Mock final SourceInfo subInfo) throws GenerationException {
        final var source3 = mock(SourceInfo.class);
        when(source3.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller3");
        when(source3.includedSources()).thenReturn(List.of());
        final var source4 = mock(SourceInfo.class);
        when(source4.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller4");
        when(source4.includedSources()).thenReturn(List.of(source3));
        when(subInfo.includedSources()).thenReturn(List.of(source4));
        when(subInfo.requiresResourceBundle()).thenReturn(true);
        when(include.source()).thenReturn("source");
        when(parameters.controllerInjectionType()).thenReturn(ControllerInjectionType.INSTANCE);
        when(parameters.resourceInjectionType()).thenReturn(ResourceBundleInjectionType.CONSTRUCTOR_NAME);
        sourceToSourceInfo.put("source", subInfo);
        when(subInfo.controllerClassName()).thenReturn("com.github.gtache.fxml.Controller2");
        when(subInfo.generatedClassName()).thenReturn("com.github.gtache.fxml.View2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller2", "controller2");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller3", "controller3");
        controllerClassToVariable.put("com.github.gtache.fxml.Controller4", "controller4");
        when(include.resources()).thenReturn("resources");
        final var expected = "StringresourceBundleName = \"resources\";\ncom.github.gtache.fxml.View2view2 = new com.github.gtache.fxml.View2(controller2, controller3, controller4, resourceBundleName);\n";
        assertEquals("view2", initializationFormatter.formatSubViewConstructorCall(include));
        assertEquals(expected, sb.toString());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new InitializationFormatter(null, request, sb));
        assertThrows(NullPointerException.class, () -> new InitializationFormatter(helperProvider, null, sb));
        assertThrows(NullPointerException.class, () -> new InitializationFormatter(helperProvider, request, null));
    }
}
