package ch.gtache.fxml.compiler.parsing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parses FXML files to object tree
 */
@FunctionalInterface
public interface FXMLParser {

    /**
     * Parses the given FXML content
     *
     * @param content The FXML content
     * @return The parsed object
     * @throws ParseException if an error occurs
     */
    ParsedObject parse(final String content) throws ParseException;

    /**
     * Parses the FXML file at the given path
     *
     * @param path The path
     * @return The parsed object
     * @throws ParseException if an error occurs
     */
    default ParsedObject parse(final Path path) throws ParseException {
        try {
            final var content = Files.readString(path);
            return parse(content);
        } catch (final IOException e) {
            throw new ParseException("Error parsing " + path, e);
        }
    }
}
