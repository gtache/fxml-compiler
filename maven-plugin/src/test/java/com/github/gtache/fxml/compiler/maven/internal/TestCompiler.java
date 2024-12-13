package com.github.gtache.fxml.compiler.maven.internal;

import com.github.gtache.fxml.compiler.ControllerInfo;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.Generator;
import com.github.gtache.fxml.compiler.SourceInfo;
import com.github.gtache.fxml.compiler.impl.GenerationRequestImpl;
import com.github.gtache.fxml.compiler.parsing.FXMLParser;
import com.github.gtache.fxml.compiler.parsing.ParseException;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestCompiler {

    private final ControllerInfoProvider controllerInfoProvider;
    private final SourceInfoProvider sourceInfoProvider;
    private final FXMLParser fxmlParser;
    private final Generator generator;
    private final CompilationInfo compilationInfo;
    private final ParsedObject object;
    private final ControllerInfo controllerInfo;
    private final SourceInfo sourceInfo;
    private final String content;
    private final GenerationParameters parameters;
    private final Compiler compiler;

    TestCompiler(@Mock final ControllerInfoProvider controllerInfoProvider, @Mock final SourceInfoProvider sourceInfoProvider,
                 @Mock final FXMLParser fxmlParser, @Mock final CompilationInfo compilationInfo, @Mock final ParsedObject object,
                 @Mock final ControllerInfo controllerInfo, @Mock final SourceInfo sourceInfo,
                 @Mock final GenerationParameters parameters, @Mock final Generator generator) {
        this.controllerInfoProvider = Objects.requireNonNull(controllerInfoProvider);
        this.sourceInfoProvider = Objects.requireNonNull(sourceInfoProvider);
        this.fxmlParser = Objects.requireNonNull(fxmlParser);
        this.compilationInfo = Objects.requireNonNull(compilationInfo);
        this.object = Objects.requireNonNull(object);
        this.controllerInfo = Objects.requireNonNull(controllerInfo);
        this.sourceInfo = Objects.requireNonNull(sourceInfo);
        this.content = "content";
        this.parameters = Objects.requireNonNull(parameters);
        this.generator = Objects.requireNonNull(generator);
        this.compiler = new Compiler(controllerInfoProvider, sourceInfoProvider, fxmlParser, generator);
    }

    @BeforeEach
    void beforeEach() throws MojoExecutionException, GenerationException, ParseException {
        when(fxmlParser.parse((Path) any())).thenReturn(object);
        when(ControllerInfoProvider.getControllerInfo(compilationInfo)).thenReturn(controllerInfo);
        when(SourceInfoProvider.getSourceInfo(eq(compilationInfo), anyMap())).thenReturn(sourceInfo);
        when(generator.generate(any())).thenReturn(content);
    }

    @Test
    void testCompile(@TempDir final Path tempDir) throws Exception {
        final var path = tempDir.resolve("fxml1.fxml");
        final var outputPath = tempDir.resolve("subFolder").resolve("fxml1.java");
        final var outputClass = "outputClass";
        when(compilationInfo.outputFile()).thenReturn(outputPath);
        when(compilationInfo.outputClass()).thenReturn(outputClass);
        final var mapping = Map.of(path, compilationInfo);
        final var request = new GenerationRequestImpl(parameters, controllerInfo, sourceInfo, object, outputClass);
        compiler.compile(mapping, parameters);
        verify(fxmlParser).parse(path);
        ControllerInfoProvider.getControllerInfo(compilationInfo);
        SourceInfoProvider.getSourceInfo(compilationInfo, mapping);
        verify(generator).generate(request);
        assertEquals(content, Files.readString(outputPath));
    }

    @Test
    void testCompileIOException(@TempDir final Path tempDir) throws Exception {
        final var path = tempDir.resolve("fxml1.fxml");
        final var outputPath = Paths.get("/whatever");
        final var outputClass = "outputClass";
        when(compilationInfo.outputFile()).thenReturn(outputPath);
        when(compilationInfo.outputClass()).thenReturn(outputClass);
        final var mapping = Map.of(path, compilationInfo);
        final var request = new GenerationRequestImpl(parameters, controllerInfo, sourceInfo, object, outputClass);
        assertThrows(MojoExecutionException.class, () -> compiler.compile(mapping, parameters));
        verify(fxmlParser).parse(path);
        ControllerInfoProvider.getControllerInfo(compilationInfo);
        SourceInfoProvider.getSourceInfo(compilationInfo, mapping);
        verify(generator).generate(request);
    }

    @Test
    void testCompileRuntimeException(@TempDir final Path tempDir) throws Exception {
        final var path = tempDir.resolve("fxml1.fxml");
        final var outputPath = tempDir.resolve("subFolder").resolve("fxml1.java");
        final var outputClass = "outputClass";
        when(compilationInfo.outputFile()).thenReturn(outputPath);
        when(compilationInfo.outputClass()).thenReturn(outputClass);
        final var mapping = Map.of(path, compilationInfo);
        final var request = new GenerationRequestImpl(parameters, controllerInfo, sourceInfo, object, outputClass);
        when(generator.generate(request)).thenThrow(RuntimeException.class);
        assertThrows(MojoExecutionException.class, () -> compiler.compile(mapping, parameters));
        verify(fxmlParser).parse(path);
        ControllerInfoProvider.getControllerInfo(compilationInfo);
        SourceInfoProvider.getSourceInfo(compilationInfo, mapping);
        verify(generator).generate(request);
    }

    @Test
    void testCompileParseException(@TempDir final Path tempDir) throws Exception {
        when(fxmlParser.parse((Path) any())).thenThrow(ParseException.class);
        final var path = tempDir.resolve("fxml1.fxml");
        final var mapping = Map.of(path, compilationInfo);
        assertThrows(MojoExecutionException.class, () -> compiler.compile(mapping, parameters));
        verify(fxmlParser).parse(path);
        verifyNoInteractions(controllerInfoProvider, sourceInfoProvider, generator);
    }

    @Test
    void testCompileGenerationException(@TempDir final Path tempDir) throws Exception {
        final var path = tempDir.resolve("fxml1.fxml");
        final var outputPath = tempDir.resolve("subFolder").resolve("fxml1.java");
        final var outputClass = "outputClass";
        when(compilationInfo.outputFile()).thenReturn(outputPath);
        when(compilationInfo.outputClass()).thenReturn(outputClass);
        final var mapping = Map.of(path, compilationInfo);
        final var request = new GenerationRequestImpl(parameters, controllerInfo, sourceInfo, object, outputClass);
        when(generator.generate(request)).thenThrow(GenerationException.class);
        assertThrows(MojoExecutionException.class, () -> compiler.compile(mapping, parameters));
        verify(fxmlParser).parse(path);
        ControllerInfoProvider.getControllerInfo(compilationInfo);
        SourceInfoProvider.getSourceInfo(compilationInfo, mapping);
        verify(generator).generate(request);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new Compiler(null, sourceInfoProvider, fxmlParser, generator));
        assertThrows(NullPointerException.class, () -> new Compiler(controllerInfoProvider, null, fxmlParser, generator));
        assertThrows(NullPointerException.class, () -> new Compiler(controllerInfoProvider, sourceInfoProvider, null, generator));
        assertThrows(NullPointerException.class, () -> new Compiler(controllerInfoProvider, sourceInfoProvider, fxmlParser, null));
    }
}
