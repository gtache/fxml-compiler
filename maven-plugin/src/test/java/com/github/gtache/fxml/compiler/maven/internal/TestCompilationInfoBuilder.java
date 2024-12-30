package com.github.gtache.fxml.compiler.maven.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TestCompilationInfoBuilder {
    private final Path inputFile;
    private final Path outputFile;
    private final String outputClass;
    private final Path controllerFile;
    private final String controllerClass;
    private final Set<FieldInfo> injectedFields;
    private final Set<String> injectedMethods;
    private final Map<String, Inclusion> includes;
    private final CompilationInfo info;

    TestCompilationInfoBuilder(@Mock final Path inputFile, @Mock final Path outputFile, @Mock final Path controllerFile) {
        this.inputFile = Objects.requireNonNull(inputFile);
        this.outputFile = Objects.requireNonNull(outputFile);
        this.outputClass = "outputClass";
        this.controllerFile = Objects.requireNonNull(controllerFile);
        this.controllerClass = "controllerClass";
        this.injectedFields = Set.of(new FieldInfo("type", "name"));
        this.injectedMethods = Set.of("one", "two");
        this.includes = Map.of("one", new Inclusion(inputFile, 1));
        this.info = new CompilationInfo(inputFile, outputFile, outputClass, controllerFile, controllerClass, injectedFields, injectedMethods, includes, true);
    }

    @Test
    void testBuilder() {
        final var builder = new CompilationInfo.Builder();
        builder.inputFile(inputFile);
        assertEquals(inputFile, builder.inputFile());
        builder.outputFile(outputFile);
        builder.outputClass(outputClass);
        builder.controllerFile(controllerFile);
        builder.controllerClass(controllerClass);
        injectedFields.forEach(f -> builder.addInjectedField(f.name(), f.type()));
        injectedMethods.forEach(builder::addInjectedMethod);
        builder.addInclude("one", inputFile);
        builder.requiresResourceBundle();
        final var actual = builder.build();
        assertEquals(info, actual);

        builder.addInclude("one", inputFile);
        final var newIncludes = Map.of("one", new Inclusion(inputFile, 2));
        final var newInfo = new CompilationInfo(inputFile, outputFile, outputClass, controllerFile, controllerClass, injectedFields, injectedMethods, newIncludes, true);
        assertEquals(newInfo, builder.build());
    }
}
