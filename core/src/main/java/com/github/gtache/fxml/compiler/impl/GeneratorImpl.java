package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.Generator;
import com.github.gtache.fxml.compiler.impl.internal.ConstructorFormatter;
import com.github.gtache.fxml.compiler.impl.internal.GenerationProgress;
import com.github.gtache.fxml.compiler.impl.internal.HelperMethodsFormatter;
import com.github.gtache.fxml.compiler.impl.internal.LoadMethodFormatter;

//TODO handle binding (${})

/**
 * Implementation of {@link Generator}
 */
public class GeneratorImpl implements Generator {
    

    @Override
    public String generate(final GenerationRequest request) throws GenerationException {
        final var progress = new GenerationProgress(request);
        final var className = request.outputClassName();
        final var pkgName = className.substring(0, className.lastIndexOf('.'));
        final var simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        final var controllerInjectionClass = request.controllerInfo().className();
        final var sb = progress.stringBuilder();
        sb.append("package ").append(pkgName).append(";\n");
        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * Generated code, not thread-safe\n");
        sb.append(" */\n");
        sb.append("public final class ").append(simpleClassName).append(" {\n");
        sb.append("\n");
        ConstructorFormatter.formatFieldsAndConstructor(progress);
        sb.append("\n");
        LoadMethodFormatter.formatLoadMethod(progress);
        sb.append("\n");
        HelperMethodsFormatter.formatHelperMethods(progress);
        sb.append("\n");
        formatControllerMethod(progress, controllerInjectionClass);
        sb.append("}\n");
        return sb.toString();
    }

    private static void formatControllerMethod(final GenerationProgress progress, final String controllerInjectionClass) {
        final var sb = progress.stringBuilder();
        sb.append("    /**\n");
        sb.append("     * @return The controller\n");
        sb.append("     */\n");
        sb.append("    public ").append(controllerInjectionClass).append(" controller() {\n");
        sb.append("        if (loaded) {\n");
        sb.append("            return controller;\n");
        sb.append("        } else {\n");
        sb.append("            throw new IllegalStateException(\"Not loaded\");\n");
        sb.append("        }\n");
        sb.append("    }\n");
    }
}
