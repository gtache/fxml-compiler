package com.github.gtache.fxml.compiler.maven.internal;

import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.Generator;
import com.github.gtache.fxml.compiler.impl.GenerationRequestImpl;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.parsing.FXMLParser;
import com.github.gtache.fxml.compiler.parsing.ParseException;
import com.github.gtache.fxml.compiler.parsing.xml.DOMFXMLParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Creates compiled Java code
 */
@Named
public final class Compiler {

    private static final Logger logger = LogManager.getLogger(Compiler.class);

    private static final FXMLParser PARSER = new DOMFXMLParser();
    private static final Generator GENERATOR = new GeneratorImpl();

    private Compiler() {
    }

    /**
     * Compiles the given files
     *
     * @param mapping    The mapping of file to compile to compilation info
     * @param parameters The generation parameters
     * @throws MojoExecutionException If an error occurs
     */
    public static void compile(final Map<Path, CompilationInfo> mapping, final GenerationParameters parameters) throws MojoExecutionException {
        for (final var entry : mapping.entrySet()) {
            compile(entry.getKey(), entry.getValue(), mapping, parameters);
        }
    }

    private static void compile(final Path inputPath, final CompilationInfo info, final Map<Path, CompilationInfo> mapping, final GenerationParameters parameters) throws MojoExecutionException {
        try {
            logger.info("Parsing {} with {}", inputPath, PARSER.getClass().getSimpleName());
            final var root = PARSER.parse(inputPath);
            final var controllerInfo = ControllerInfoProvider.getControllerInfo(info);
            final var output = info.outputFile();
            final var sourceInfo = SourceInfoProvider.getSourceInfo(info, mapping);
            final var request = new GenerationRequestImpl(parameters, controllerInfo, sourceInfo, root, info.outputClass());
            logger.info("Compiling {}", inputPath);
            final var content = GENERATOR.generate(request);
            final var outputDir = output.getParent();
            Files.createDirectories(outputDir);
            Files.writeString(output, content);
            logger.info("Compiled {} to {}", inputPath, output);
        } catch (final IOException | RuntimeException | ParseException | GenerationException e) {
            throw new MojoExecutionException("Error compiling fxml", e);
        }
    }
}
