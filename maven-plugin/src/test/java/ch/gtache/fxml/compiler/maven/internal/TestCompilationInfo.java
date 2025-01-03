package ch.gtache.fxml.compiler.maven.internal;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestCompilationInfo {
    private final Path inputFile;
    private final Path outputFile;
    private final String outputClass;
    private final Path controllerFile;
    private final String controllerClass;
    private final Set<FieldInfo> injectedFields;
    private final Set<String> injectedMethods;
    private final Map<String, Inclusion> includes;
    private final boolean requiresResourceBundle;
    private final CompilationInfo info;

    TestCompilationInfo(@Mock final Path inputFile, @Mock final Path outputFile, @Mock final Path controllerFile, @Mock final Inclusion inclusion, @Mock final FieldInfo fieldInfo) {
        this.inputFile = Objects.requireNonNull(inputFile);
        this.outputFile = Objects.requireNonNull(outputFile);
        this.outputClass = "outputClass";
        this.controllerFile = Objects.requireNonNull(controllerFile);
        this.controllerClass = "controllerClass";
        this.injectedFields = new HashSet<>(Set.of(fieldInfo));
        this.injectedMethods = new HashSet<>(Set.of("one", "two"));
        this.includes = new HashMap<>(Map.of("one", Objects.requireNonNull(inclusion)));
        this.requiresResourceBundle = true;
        this.info = new CompilationInfo(inputFile, outputFile, outputClass, controllerFile, controllerClass, injectedFields, injectedMethods, includes, requiresResourceBundle);
    }

    @Test
    void testGetters() {
        assertEquals(inputFile, info.inputFile());
        assertEquals(outputFile, info.outputFile());
        assertEquals(outputClass, info.outputClass());
        assertEquals(controllerFile, info.controllerFile());
        assertEquals(controllerClass, info.controllerClass());
        assertEquals(injectedFields, info.injectedFields());
        assertEquals(injectedMethods, info.injectedMethods());
        assertEquals(includes, info.includes());
        assertEquals(requiresResourceBundle, info.requiresResourceBundle());
    }

    @Test
    void testCopyInjectedFields() {
        final var originalInjectedFields = info.injectedFields();
        injectedFields.clear();
        assertEquals(originalInjectedFields, info.injectedFields());
    }

    @Test
    void testCopyInjectedMethods() {
        final var originalInjectedMethods = info.injectedMethods();
        injectedMethods.clear();
        assertEquals(originalInjectedMethods, info.injectedMethods());
    }

    @Test
    void testCopyIncludes() {
        final var originalIncludes = Map.copyOf(includes);
        includes.clear();
        assertEquals(originalIncludes, info.includes());
    }

    @Test
    void testUnmodifiableInjectedFields() {
        final var originalInjectedFields = info.injectedFields();
        assertThrows(UnsupportedOperationException.class, originalInjectedFields::clear);
    }

    @Test
    void testUnmodifiableInjectedMethods() {
        final var originalInjectedMethods = info.injectedMethods();
        assertThrows(UnsupportedOperationException.class, originalInjectedMethods::clear);
    }

    @Test
    void testUnmodifiableIncludes() {
        final var originalIncludes = Map.copyOf(includes);
        assertThrows(UnsupportedOperationException.class, originalIncludes::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new CompilationInfo(null, outputFile, outputClass, controllerFile, controllerClass, injectedFields, injectedMethods, includes, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new CompilationInfo(inputFile, null, outputClass, controllerFile, controllerClass, injectedFields, injectedMethods, includes, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new CompilationInfo(inputFile, outputFile, null, controllerFile, controllerClass, injectedFields, injectedMethods, includes, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new CompilationInfo(inputFile, outputFile, outputClass, null, controllerClass, injectedFields, injectedMethods, includes, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new CompilationInfo(inputFile, outputFile, outputClass, controllerFile, null, injectedFields, injectedMethods, includes, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new CompilationInfo(inputFile, outputFile, outputClass, controllerFile, controllerClass, null, injectedMethods, includes, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new CompilationInfo(inputFile, outputFile, outputClass, controllerFile, controllerClass, injectedFields, null, includes, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new CompilationInfo(inputFile, outputFile, outputClass, controllerFile, controllerClass, injectedFields, injectedMethods, null, requiresResourceBundle));
    }
}
