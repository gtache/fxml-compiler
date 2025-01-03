package ch.gtache.fxml.compiler.maven.internal;

import ch.gtache.fxml.compiler.SourceInfo;
import ch.gtache.fxml.compiler.impl.SourceInfoImpl;
import ch.gtache.fxml.compiler.maven.FXMLCompilerMojo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
        includes.forEach((k, v) -> includesMapping.put(k, getSourceInfo(mapping.get(v.path()), mapping)));
        final var includesSources = new ArrayList<SourceInfo>();
        includes.forEach((key, value) -> {
            for (var i = 0; i < value.count(); ++i) {
                includesSources.add(includesMapping.get(key));
            }
        });
        return new SourceInfoImpl(outputClass, controllerClass, inputFile, includesSources, includesMapping, requiresResourceBundle);
    }
}
