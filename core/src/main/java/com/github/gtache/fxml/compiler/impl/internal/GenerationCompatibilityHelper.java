package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

/**
 * Various helper methods for {@link GeneratorImpl} to handle compatibility with older java versions
 */
final class GenerationCompatibilityHelper {

    private GenerationCompatibilityHelper() {

    }

    static String getStartVar(final GenerationProgress progress, final ParsedObject parsedObject) throws GenerationException {
        return getStartVar(progress, parsedObject.className() + ReflectionHelper.getGenericTypes(progress, parsedObject));
    }

    static String getStartVar(final GenerationProgress progress, final String className) {
        return getStartVar(progress, className, 8);
    }

    static String getStartVar(final GenerationProgress progress, final String className, final int indent) {
        if (progress.request().parameters().compatibility().useVar()) {
            return " ".repeat(indent) + "final var ";
        } else {
            return " ".repeat(indent) + "final " + className + " ";
        }
    }

    static String getToList(final GenerationProgress progress) {
        return switch (progress.request().parameters().compatibility().listCollector()) {
            case TO_LIST -> ".toList()";
            case COLLECT_TO_UNMODIFIABLE_LIST -> ".collect(java.util.stream.Collectors.toUnmodifiableList())";
            case COLLECT_TO_LIST -> ".collect(java.util.stream.Collectors.toList())";
        };
    }

    static String getGetFirst(final GenerationProgress progress) {
        if (progress.request().parameters().compatibility().useGetFirst()) {
            return ".getFirst()";
        } else {
            return ".get(0)";
        }
    }

    static String getListOf(final GenerationProgress progress) {
        if (progress.request().parameters().compatibility().useCollectionsOf()) {
            return "java.util.List.of(";
        } else {
            return "java.util.Arrays.asList(";
        }
    }
}
