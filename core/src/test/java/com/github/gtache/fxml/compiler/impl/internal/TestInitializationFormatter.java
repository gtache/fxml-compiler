package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.SourceInfo;
import com.github.gtache.fxml.compiler.parsing.ParsedInclude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestInitializationFormatter {

    private final GenerationProgress progress;
    private final GenerationRequest request;
    private final GenerationParameters parameters;
    private final HelperProvider helperProvider;
    private final StringBuilder stringBuilder;
    private final SourceInfo sourceInfo;
    private final ParsedInclude include;
    private final Map<String, SourceInfo> sourceToSourceInfo;
    private final InitializationFormatter initializationFormatter;


    TestInitializationFormatter(@Mock final GenerationProgress progress, @Mock final GenerationRequest request,
                                @Mock final GenerationParameters parameters, @Mock final HelperProvider helperProvider,
                                @Mock final SourceInfo sourceInfo, @Mock final ParsedInclude include) {
        this.progress = Objects.requireNonNull(progress);
        this.request = Objects.requireNonNull(request);
        this.parameters = Objects.requireNonNull(parameters);
        this.stringBuilder = new StringBuilder();
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.sourceInfo = Objects.requireNonNull(sourceInfo);
        this.include = Objects.requireNonNull(include);
        this.sourceToSourceInfo = new HashMap<>();
        this.initializationFormatter = new InitializationFormatter(helperProvider, progress);
    }

    @BeforeEach
    void beforeEach() {
        when(request.parameters()).thenReturn(parameters);
        when(progress.request()).thenReturn(request);
        when(progress.stringBuilder()).thenReturn(stringBuilder);
        when(request.sourceInfo()).thenReturn(sourceInfo);
        when(sourceInfo.sourceToSourceInfo()).thenReturn(sourceToSourceInfo);
    }

    @Test
    void testFormatSubViewConstructorCallNullSubInfo() {
        assertThrows(GenerationException.class, () -> initializationFormatter.formatSubViewConstructorCall(include));
    }
}
