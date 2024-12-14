package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.compatibility.GenerationCompatibility;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;

import java.util.Objects;

/**
 * Various helper methods for {@link GeneratorImpl} to handle compatibility with older java versions
 */
final class GenerationCompatibilityHelper {

    private final HelperProvider helperProvider;
    private final GenerationCompatibility compatibility;

    GenerationCompatibilityHelper(final HelperProvider helperProvider, final GenerationCompatibility compatibility) {
        this.helperProvider = Objects.requireNonNull(helperProvider);
        this.compatibility = Objects.requireNonNull(compatibility);
    }

    String getStartVar(final ParsedObject parsedObject) throws GenerationException {
        return getStartVar(parsedObject.className() + helperProvider.getReflectionHelper().getGenericTypes(parsedObject));
    }

    String getStartVar(final String className) {
        return getStartVar(className, 8);
    }

    String getStartVar(final String className, final int indent) {
        if (compatibility.useVar()) {
            return " ".repeat(indent) + "final var ";
        } else {
            return " ".repeat(indent) + "final " + className + " ";
        }
    }

    String getToList() {
        return switch (compatibility.listCollector()) {
            case TO_LIST -> ".toList()";
            case COLLECT_TO_UNMODIFIABLE_LIST -> ".collect(java.util.stream.Collectors.toUnmodifiableList())";
            case COLLECT_TO_LIST -> ".collect(java.util.stream.Collectors.toList())";
        };
    }

    String getGetFirst() {
        if (compatibility.useGetFirst()) {
            return ".getFirst()";
        } else {
            return ".get(0)";
        }
    }

    String getListOf() {
        if (compatibility.useCollectionsOf()) {
            return "java.util.List.of(";
        } else {
            return "java.util.Arrays.asList(";
        }
    }
}
