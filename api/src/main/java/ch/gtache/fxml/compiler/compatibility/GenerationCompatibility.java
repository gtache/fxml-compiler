package ch.gtache.fxml.compiler.compatibility;

/**
 * Compatibility information for generated code
 */
@FunctionalInterface
public interface GenerationCompatibility {

    /**
     * Returns the minimum supported Java version
     *
     * @return The version
     */
    int javaVersion();

    /**
     * Returns whether to use var for object declaration
     *
     * @return True if var should be used
     */
    default boolean useVar() {
        return javaVersion() >= 10;
    }

    /**
     * Returns the type of list collector to use
     *
     * @return The collector
     */
    default ListCollector listCollector() {
        if (javaVersion() >= 16) {
            return ListCollector.TO_LIST;
        } else if (javaVersion() >= 10) {
            return ListCollector.COLLECT_TO_UNMODIFIABLE_LIST;
        } else {
            return ListCollector.COLLECT_TO_LIST;
        }
    }

    /**
     * Returns whether to use List.of() (or Set.of() etc.) instead of Arrays.asList()
     *
     * @return True if List.of() should be used
     */
    default boolean useCollectionsOf() {
        return javaVersion() >= 9;
    }

    /**
     * Returns whether to use getFirst() or get(0)
     *
     * @return True if getFirst() should be used
     */
    default boolean useGetFirst() {
        return javaVersion() >= 21;
    }
}
