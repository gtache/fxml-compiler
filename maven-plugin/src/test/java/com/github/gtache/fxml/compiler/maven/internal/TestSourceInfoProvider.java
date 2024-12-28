package com.github.gtache.fxml.compiler.maven.internal;

import com.github.gtache.fxml.compiler.impl.SourceInfoImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestSourceInfoProvider {

    @Test
    void testGetSourceInfo(@Mock final CompilationInfo compilationInfo) {
        final var outputClass = "outputClass";
        final var controllerClass = "controllerClass";
        final var inputFile = Path.of("inputFile");
        final var includeFile = Path.of("includeFile");
        final var includes = Map.of("one", includeFile);
        when(compilationInfo.outputClass()).thenReturn(outputClass);
        when(compilationInfo.controllerClass()).thenReturn(controllerClass);
        when(compilationInfo.inputFile()).thenReturn(inputFile);
        when(compilationInfo.includes()).thenReturn(includes);
        when(compilationInfo.requiresResourceBundle()).thenReturn(true);

        final var includeCompilationInfo = mock(CompilationInfo.class);
        final var includeOutputClass = "includeOutputClass";
        final var includeControllerClass = "includeControllerClass";
        final var includeInputFile = Path.of("includeInputFile");
        when(includeCompilationInfo.outputClass()).thenReturn(includeOutputClass);
        when(includeCompilationInfo.controllerClass()).thenReturn(includeControllerClass);
        when(includeCompilationInfo.inputFile()).thenReturn(includeInputFile);
        when(includeCompilationInfo.includes()).thenReturn(Map.of());
        final var mapping = Map.of(includeFile, includeCompilationInfo);

        final var expectedIncludeSourceInfo = new SourceInfoImpl(includeOutputClass, includeControllerClass, includeInputFile, List.of(), Map.of(), false);
        assertEquals(expectedIncludeSourceInfo, SourceInfoProvider.getSourceInfo(includeCompilationInfo, mapping));
        final var expected = new SourceInfoImpl(outputClass, controllerClass, inputFile, List.of(expectedIncludeSourceInfo),
                Map.of("one", expectedIncludeSourceInfo), true);
        assertEquals(expected, SourceInfoProvider.getSourceInfo(compilationInfo, mapping));
    }
}
