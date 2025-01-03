package ch.gtache.fxml.compiler.maven.internal;

import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Extracts controller class from FXMLs
 */
public final class ControllerProvider {

    private final DocumentBuilder documentBuilder;

    /**
     * Instantiates a new provider
     */
    public ControllerProvider() {
        final var factory = DocumentBuilderFactory.newInstance();
        try {
            this.documentBuilder = factory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the controller class for the given FXML
     *
     * @param fxml The FXML
     * @return The controller class
     * @throws MojoExecutionException If an error occurs
     */
    public String getController(final Path fxml) throws MojoExecutionException {
        try {
            final var document = documentBuilder.parse(fxml.toFile());
            document.getDocumentElement().normalize();

            final var controller = document.getDocumentElement().getAttribute("fx:controller");
            if (controller.isBlank()) {
                throw new MojoExecutionException("Missing controller attribute for " + fxml);
            } else {
                return controller;
            }
        } catch (final SAXException | IOException e) {
            throw new MojoExecutionException("Error parsing fxml at " + fxml, e);
        }
    }
}
