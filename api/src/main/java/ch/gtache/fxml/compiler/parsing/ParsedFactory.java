package ch.gtache.fxml.compiler.parsing;

import java.util.LinkedHashMap;
import java.util.SequencedCollection;
import java.util.SequencedMap;

/**
 * Special {@link ParsedObject} for fx:factory
 */
public interface ParsedFactory extends ParsedObject {

    /**
     * Returns the factory value from fx:factory
     *
     * @return The value
     */
    default String factory() {
        final var attribute = attributes().get("fx:factory");
        if (attribute == null) {
            throw new IllegalStateException("Missing fx:factory");
        } else {
            return attribute.value();
        }
    }

    @Override
    default SequencedMap<ParsedProperty, SequencedCollection<ParsedObject>> properties() {
        return new LinkedHashMap<>();
    }

    /**
     * Returns the arguments for the factory.
     * Different from {@link ParsedObject#children()} (in practice, children should only contain fx:define)
     *
     * @return The arguments
     */
    SequencedCollection<ParsedObject> arguments();
}
