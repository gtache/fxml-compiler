package ch.gtache.fxml.compiler.parsing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Special {@link ParsedObject} for simple text
 */
@FunctionalInterface
public interface ParsedText extends ParsedObject {

    /**
     * Returns the text value
     *
     * @return The value
     */
    String text();

    @Override
    default String className() {
        return String.class.getName();
    }

    @Override
    default SequencedCollection<ParsedObject> children() {
        return List.of();
    }

    @Override
    default SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties() {
        return new LinkedHashMap<>();
    }

    @Override
    default Map<String, ParsedProperty> attributes() {
        return Map.of();
    }
}
