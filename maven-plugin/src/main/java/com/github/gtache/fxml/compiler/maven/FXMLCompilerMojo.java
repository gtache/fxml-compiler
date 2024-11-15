package com.github.gtache.fxml.compiler.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;

/**
 * Main mojo for FXML compiler
 */
@Mojo(name = "fxml-compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class FXMLCompilerMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources", required = true)
    private Path outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        for (final var resource : project.getResources()) {
            final var location = resource.getLocation("");
            location.toString();
        }
        project.addCompileSourceRoot(outputDirectory.toAbsolutePath().toString());
    }
}
