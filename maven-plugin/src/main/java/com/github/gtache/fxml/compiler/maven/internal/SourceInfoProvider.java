package com.github.gtache.fxml.compiler.maven.internal;

import com.github.gtache.fxml.compiler.SourceInfo;
import com.github.gtache.fxml.compiler.impl.SourceInfoImpl;
import com.github.gtache.fxml.compiler.maven.FXMLCompilerMojo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for {@link FXMLCompilerMojo} to provides {@link SourceInfo}
 */
final class SourceInfoProvider {

    private SourceInfoProvider() {
    }

    /**
     * Provides the {@link SourceInfo} for the given compilation info
     *
     * @param info    The compilation info
     * @param mapping The mapping of file to compilation info
     * @return The source info
     */
    static SourceInfo getSourceInfo(final CompilationInfo info, final Map<Path, CompilationInfo> mapping) {
        final var outputClass = info.outputClass();
        final var controllerClass = info.controllerClass();
        final var inputFile = info.inputFile();
        final var includes = info.includes();
        final var requiresResourceBundle = info.requiresResourceBundle();
        final var includesMapping = new HashMap<String, SourceInfo>();
        includes.forEach((k, v) -> includesMapping.put(k, getSourceInfo(mapping.get(v), mapping)));
        //FIXME mutliple same includes
        return new SourceInfoImpl(outputClass, controllerClass, inputFile, List.copyOf(includesMapping.values()), includesMapping, requiresResourceBundle);
    }
}
