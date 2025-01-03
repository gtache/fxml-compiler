package ch.gtache.fxml.compiler.parsing.impl;

import ch.gtache.fxml.compiler.parsing.ParsedInclude;
import ch.gtache.fxml.compiler.parsing.ParsedProperty;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link ParsedInclude}
 *
 * @param attributes The include attributes
 */
public record ParsedIncludeImpl(Map<String, ParsedProperty> attributes) implements ParsedInclude {

    private static final String SOURCE = "source";

    /**
     * Instantiates an include
     *
     * @param attributes The include attributes
     * @throws NullPointerException     If attributes is null
     * @throws IllegalArgumentException If attributes does not contain source
     */
    public ParsedIncludeImpl {
        if (!attributes.containsKey(SOURCE)) {
            throw new IllegalArgumentException("Missing " + SOURCE);
        }
        attributes = Map.copyOf(attributes);
    }

    /**
     * Instantiates an include
     *
     * @param source    The source
     * @param resources The resources
     * @param fxId      The fx:id
     * @throws NullPointerException If source is null
     */
    public ParsedIncludeImpl(final String source, final String resources, final String fxId) {
        this(createAttributes(source, resources, fxId));
    }

    private static Map<String, ParsedProperty> createAttributes(final String source, final String resources, final String fxId) {
        requireNonNull(source);
        final var map = HashMap.<String, ParsedProperty>newHashMap(3);
        map.put(SOURCE, new ParsedPropertyImpl(SOURCE, null, source));
        if (resources != null) {
            map.put("resources", new ParsedPropertyImpl("resources", null, resources));
        }
        if (fxId != null) {
            map.put("fx:id", new ParsedPropertyImpl("fx:id", null, fxId));
        }
        return map;
    }
}
