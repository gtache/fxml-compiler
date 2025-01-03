package ch.gtache.fxml.compiler.maven.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Extracts FXML paths from Maven project
 */
public final class FXMLProvider {
    private static final Logger logger = LogManager.getLogger(FXMLProvider.class);

    private final MavenProject project;

    /**
     * Instantiates a new provider
     *
     * @param project The Maven project
     * @throws NullPointerException If the project is null
     */
    public FXMLProvider(final MavenProject project) {
        this.project = Objects.requireNonNull(project);
    }

    /**
     * Returns all the FXML files in the project's resources
     *
     * @return A mapping of file to resource directory
     * @throws MojoExecutionException If an error occurs
     */
    public Map<Path, Path> getFXMLs() throws MojoExecutionException {
        final var map = new HashMap<Path, Path>();
        for (final var resource : project.getResources()) {
            final var path = Path.of(resource.getDirectory());
            if (Files.isDirectory(path)) {
                try (final var stream = Files.find(path, Integer.MAX_VALUE, (p, a) -> p.toString().endsWith(".fxml"), FileVisitOption.FOLLOW_LINKS)) {
                    final var curList = stream.toList();
                    logger.info("Found {}", curList);
                    for (final var p : curList) {
                        map.put(p, path);
                    }
                } catch (final IOException e) {
                    throw new MojoExecutionException("Error reading resources", e);
                }
            } else {
                logger.info("Directory {} does not exist", path);
            }
        }
        return map;
    }

    /**
     * Factory for {@link FXMLProvider}
     */
    @FunctionalInterface
    public interface Factory {
        /**
         * Creates a new provider
         *
         * @param project The Maven project
         * @return The provider
         */
        FXMLProvider create(final MavenProject project);
    }
}
