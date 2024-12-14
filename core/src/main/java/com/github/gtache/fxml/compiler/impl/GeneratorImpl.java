package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.Generator;
import com.github.gtache.fxml.compiler.impl.internal.GenerationProgress;
import com.github.gtache.fxml.compiler.impl.internal.HelperProvider;

//TODO handle binding (${})

/**
 * Implementation of {@link Generator}
 */
public class GeneratorImpl implements Generator {

    @Override
    public String generate(final GenerationRequest request) throws GenerationException {
        final var progress = new GenerationProgress(request);
        final var helperProvider = new HelperProvider(progress);
        final var className = request.outputClassName();
        final var pkgName = className.substring(0, className.lastIndexOf('.'));
        final var simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        final var controllerInjectionClass = request.controllerInfo().className();
        final var sb = progress.stringBuilder();
        sb.append("package ").append(pkgName).append(";\n");
        sb.append("\n");
        sb.append("/**\n");
        sb.append(" * Generated code\n");
        sb.append(" */\n");
        sb.append("public final class ").append(simpleClassName).append(" {\n");
        sb.append("\n");
        helperProvider.getInitializationFormatter().formatFieldsAndConstructor();
        sb.append("\n");
        helperProvider.getLoadMethodFormatter().formatLoadMethod();
        sb.append("\n");
        helperProvider.getHelperMethodsFormatter().formatHelperMethods();
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
