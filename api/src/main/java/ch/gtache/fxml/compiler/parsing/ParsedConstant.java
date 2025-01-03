package ch.gtache.fxml.compiler.parsing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Special {@link ParsedObject} for fx:constant
 */
public interface ParsedConstant extends ParsedObject {

    /**
     * Returns the constant value from fx:constant
     *
     * @return The value
     */
    default String constant() {
        final var attribute = attributes().get("fx:constant");
        if (attribute == null) {
            throw new IllegalStateException("Missing fx:constant");
        } else {
            return attribute.value();
        }
    }

    @Override
    default SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties() {
        return new LinkedHashMap<>();
    }

    @Override
    default SequencedCollection<ParsedObject> children() {
        return List.of();
    }
}
