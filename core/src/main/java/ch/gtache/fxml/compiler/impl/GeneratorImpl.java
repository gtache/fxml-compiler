package ch.gtache.fxml.compiler.impl;

import ch.gtache.fxml.compiler.GenerationException;
import ch.gtache.fxml.compiler.GenerationRequest;
import ch.gtache.fxml.compiler.Generator;
import ch.gtache.fxml.compiler.impl.internal.GenerationProgress;
import ch.gtache.fxml.compiler.impl.internal.HelperProvider;

import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation of {@link Generator}
 */
public class GeneratorImpl implements Generator {


    private final Function<GenerationProgress, HelperProvider> helperProviderFactory;

    /**
     * Instantiates a new generator
     */
    public GeneratorImpl() {
        this(HelperProvider::new);
    }

    /**
     * Used for testing
     *
     * @param helperProviderFactory The helper provider factory
     */
    GeneratorImpl(final Function<GenerationProgress, HelperProvider> helperProviderFactory) {
        this.helperProviderFactory = Objects.requireNonNull(helperProviderFactory);
    }

    @Override
    public String generate(final GenerationRequest request) throws GenerationException {
        final var progress = new GenerationProgress(request);
        final var helperProvider = helperProviderFactory.apply(progress);
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
        sb.append("     * Returns the controller if available\n");
        sb.append("     * @return The controller\n");
        sb.append("     * @throws IllegalStateException If the view is not loaded\n");
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
