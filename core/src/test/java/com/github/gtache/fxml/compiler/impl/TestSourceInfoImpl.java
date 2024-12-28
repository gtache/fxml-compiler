package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.SourceInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestSourceInfoImpl {

    private final String generatedClassName;
    private final String controllerClassName;
    private final Path sourceFile;
    private final List<SourceInfo> includedSources;
    private final Map<String, SourceInfo> sourceToSourceInfo;
    private final boolean requiresResourceBundle;
    private final SourceInfo info;

    TestSourceInfoImpl(@Mock final SourceInfo subInfo) {
        this.generatedClassName = "class";
        this.controllerClassName = "controller";
        this.sourceFile = Path.of("path");
        this.includedSources = new ArrayList<>(List.of(subInfo));
        this.sourceToSourceInfo = new HashMap<>(Map.of("source", subInfo));
        this.requiresResourceBundle = false;
        this.info = new SourceInfoImpl(generatedClassName, controllerClassName, sourceFile, includedSources, sourceToSourceInfo, requiresResourceBundle);
    }

    @Test
    void testGetters() {
        assertEquals(generatedClassName, info.generatedClassName());
        assertEquals(controllerClassName, info.controllerClassName());
        assertEquals(sourceFile, info.sourceFile());
        assertEquals(includedSources, info.includedSources());
        assertEquals(sourceToSourceInfo, info.sourceToSourceInfo());
        assertEquals(requiresResourceBundle, info.requiresResourceBundle());
    }

    @Test
    void testCopyList() {
        final var originalIncludedSources = info.includedSources();
        includedSources.clear();
        assertEquals(originalIncludedSources, info.includedSources());
    }

    @Test
    void testCopyMap() {
        final var originalSourceToSourceInfo = info.sourceToSourceInfo();
        sourceToSourceInfo.clear();
        assertEquals(originalSourceToSourceInfo, info.sourceToSourceInfo());
    }

    @Test
    void testUnmodifiable() {
        final var infoIncludedSources = info.includedSources();
        final var infoSourceToSourceInfo = info.sourceToSourceInfo();
        assertThrows(UnsupportedOperationException.class, infoIncludedSources::clear);
        assertThrows(UnsupportedOperationException.class, infoSourceToSourceInfo::clear);
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new SourceInfoImpl(null, controllerClassName, sourceFile, includedSources, sourceToSourceInfo, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new SourceInfoImpl(generatedClassName, null, sourceFile, includedSources, sourceToSourceInfo, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new SourceInfoImpl(generatedClassName, controllerClassName, null, includedSources, sourceToSourceInfo, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new SourceInfoImpl(generatedClassName, controllerClassName, sourceFile, null, sourceToSourceInfo, requiresResourceBundle));
        assertThrows(NullPointerException.class, () -> new SourceInfoImpl(generatedClassName, controllerClassName, sourceFile, includedSources, null, requiresResourceBundle));
    }
}
