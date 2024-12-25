package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.Generator;
import com.github.gtache.fxml.compiler.impl.internal.HelperMethodsFormatter;
import com.github.gtache.fxml.compiler.impl.internal.HelperProvider;
import com.github.gtache.fxml.compiler.impl.internal.InitializationFormatter;
import com.github.gtache.fxml.compiler.impl.internal.LoadMethodFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestGeneratorImpl {

    private final HelperProvider helperProvider;
    private final InitializationFormatter initializationFormatter;
    private final LoadMethodFormatter loadMethodFormatter;
    private final HelperMethodsFormatter helperMethodsFormatter;
    private final GenerationRequest request;
    private final String outputClassName;
    private final ControllerInfo controllerInfo;
    private final String className;
    private final Generator generator;

    TestGeneratorImpl(@Mock final HelperProvider helperProvider, @Mock final InitializationFormatter initializationFormatter,
                      @Mock final LoadMethodFormatter loadMethodFormatter, @Mock final HelperMethodsFormatter helperMethodsFormatter,
                      @Mock final GenerationRequest request, @Mock final ControllerInfo controllerInfo) {
        this.helperProvider = requireNonNull(helperProvider);
        this.initializationFormatter = requireNonNull(initializationFormatter);
        this.loadMethodFormatter = requireNonNull(loadMethodFormatter);
        this.helperMethodsFormatter = requireNonNull(helperMethodsFormatter);
        this.request = requireNonNull(request);
        this.controllerInfo = requireNonNull(controllerInfo);
        this.outputClassName = "com.github.gtache.fxml.compiler.OutputClass";
        this.className = "com.github.gtache.fxml.compiler.ControllerClass";
        this.generator = new GeneratorImpl(p -> helperProvider);
    }

    @BeforeEach
    void beforeEach() {
        when(helperProvider.getInitializationFormatter()).thenReturn(initializationFormatter);
        when(helperProvider.getLoadMethodFormatter()).thenReturn(loadMethodFormatter);
        when(helperProvider.getHelperMethodsFormatter()).thenReturn(helperMethodsFormatter);
        when(request.outputClassName()).thenReturn(outputClassName);
        when(request.controllerInfo()).thenReturn(controllerInfo);
        when(controllerInfo.className()).thenReturn(className);
    }

    @Test
    void testGenerate() throws GenerationException {
        final var expected = """
                package com.github.gtache.fxml.compiler;
                
                /**
                 * Generated code
                 */
                public final class OutputClass {
                
                
                
                
                    /**
                     * Returns the controller if available
                     * @return The controller
                     * @throws IllegalStateException If the view is not loaded
                     */
                    public com.github.gtache.fxml.compiler.ControllerClass controller() {
                        if (loaded) {
                            return controller;
                        } else {
                            throw new IllegalStateException("Not loaded");
                        }
                    }
                }
                """;
        assertEquals(expected, generator.generate(request));
        verify(initializationFormatter).formatFieldsAndConstructor();
        verify(loadMethodFormatter).formatLoadMethod();
        verify(helperMethodsFormatter).formatHelperMethods();
    }
}
