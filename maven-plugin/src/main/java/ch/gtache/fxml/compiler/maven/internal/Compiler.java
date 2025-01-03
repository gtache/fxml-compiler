package ch.gtache.fxml.compiler.maven.internal;

import ch.gtache.fxml.compiler.GenerationException;
import ch.gtache.fxml.compiler.GenerationParameters;
import ch.gtache.fxml.compiler.Generator;
import ch.gtache.fxml.compiler.impl.GenerationRequestImpl;
import ch.gtache.fxml.compiler.impl.GeneratorImpl;
import ch.gtache.fxml.compiler.parsing.FXMLParser;
import ch.gtache.fxml.compiler.parsing.ParseException;
import ch.gtache.fxml.compiler.parsing.xml.DOMFXMLParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Creates compiled Java code
 */
public final class Compiler {

    private static final Logger logger = LogManager.getLogger(Compiler.class);

    private final FXMLParser parser;
    private final Generator generator;

    /**
     * Instantiates a new compiler
     *
     * @param parser    The parser to use
     * @param generator The generator to use
     * @throws NullPointerException If any parameter is null
     */
    Compiler(final FXMLParser parser, final Generator generator) {
        this.parser = Objects.requireNonNull(parser);
        this.generator = Objects.requireNonNull(generator);
    }

    /**
     * Instantiates a new compiler
     */
    public Compiler() {
        this(new DOMFXMLParser(), new GeneratorImpl());
    }

    /**
     * Compiles the given files
     *
     * @param mapping    The mapping of file to compile to compilation info
     * @param parameters The generation parameters
     * @throws MojoExecutionException If an error occurs
     */
    public void compile(final Map<Path, CompilationInfo> mapping, final GenerationParameters parameters) throws MojoExecutionException {
        for (final var entry : mapping.entrySet()) {
            compile(entry.getKey(), entry.getValue(), mapping, parameters);
        }
    }

    /**
     * Compiles the given file
     *
     * @param inputPath  The input path
     * @param info       The compilation info
     * @param mapping    The mapping of file to compile to compilation info
     * @param parameters The generation parameters
     * @throws MojoExecutionException If an error occurs
     */
    public void compile(final Path inputPath, final CompilationInfo info, final Map<Path, CompilationInfo> mapping, final GenerationParameters parameters) throws MojoExecutionException {
        try {
            logger.info("Parsing {} with {}", inputPath, parser.getClass().getSimpleName());
            final var root = parser.parse(inputPath);
            final var controllerInfo = ControllerInfoProvider.getControllerInfo(info);
            final var output = info.outputFile();
            final var sourceInfo = SourceInfoProvider.getSourceInfo(info, mapping);
            final var request = new GenerationRequestImpl(parameters, controllerInfo, sourceInfo, root, info.outputClass());
            logger.info("Compiling {}", inputPath);
            final var content = generator.generate(request);
            final var outputDir = output.getParent();
            Files.createDirectories(outputDir);
            Files.writeString(output, content);
            logger.info("Compiled {} to {}", inputPath, output);
        } catch (final IOException | RuntimeException | ParseException | GenerationException e) {
            throw new MojoExecutionException("Error compiling fxml", e);
        }
    }
}
