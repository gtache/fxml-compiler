package com.github.gtache.fxml.compiler.maven.internal;

import com.github.gtache.fxml.compiler.GenericTypes;
import com.github.gtache.fxml.compiler.impl.GenericTypesImpl;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Parser of generic types
 */
final class GenericParser {

    private static final Set<String> JAVA_LANG_CLASSES = Set.of(
            "Boolean",
            "Byte",
            "Character",
            "Double",
            "Float",
            "Integer",
            "Long",
            "Object",
            "Short",
            "String"
    );

    private final String content;
    private final Map<String, String> imports;
    private int index;

    GenericParser(final String content, final Map<String, String> imports) {
        this.content = Objects.requireNonNull(content);
        this.imports = Map.copyOf(imports);
    }

    List<GenericTypes> parse() throws MojoExecutionException {
        final var parsed = parseGenericTypes();
        if (index < content.length()) {
            throw new MojoExecutionException("Expected EOF at " + index + " in " + content);
        }
        return parsed;
    }

    private List<GenericTypes> parseGenericTypes() throws MojoExecutionException {
        if (content.isEmpty()) {
            throw new MojoExecutionException("Empty generic types");
        }
        final var ret = new ArrayList<GenericTypes>();
        eatSpaces();
        eat('<');
        do {
            eatSpaces();
            final var type = parseType();
            eatSpaces();
            if (peek() == '<') {
                final var genericTypes = parseGenericTypes();
                ret.add(new GenericTypesImpl(type, genericTypes));
            } else {
                ret.add(new GenericTypesImpl(type, List.of()));
            }
            eatSpaces();
            if (peek() == ',') {
                eat(',');
            } else if (peek() == '>') {
                eat('>');
                eatSpaces();
                return ret;
            }
        } while (index < content.length());
        return ret;
    }

    private void eat(final char c) {
        if (peek() == c) {
            read();
        } else {
            throw new IllegalArgumentException("Expected " + c + " at " + index + " in " + content);
        }
    }

    private void eatSpaces() {
        while (index < content.length() && peek() == ' ') {
            read();
        }
    }

    private String parseType() throws MojoExecutionException {
        final var sb = new StringBuilder();
        while (peek() != '<' && peek() != '>' && peek() != ',' && index < content.length()) {
            sb.append(read());
        }
        final var type = sb.toString().trim();
        if (type.contains(".") || JAVA_LANG_CLASSES.contains(type)) {
            return type;
        } else if (imports.containsKey(type)) {
            return imports.get(type);
        } else {
            throw new MojoExecutionException("Cannot find class " + type + " ; Use fully qualified name or put the classes in a dependency.");
        }
    }

    private char peek() {
        return content.charAt(index);
    }

    private char read() {
        return content.charAt(index++);
    }
}
