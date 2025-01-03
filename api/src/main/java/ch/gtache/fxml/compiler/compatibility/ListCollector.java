package ch.gtache.fxml.compiler.compatibility;

/**
 * Type of list collector to use for generated code
 */
public enum ListCollector {

    /**
     * Use .toList()
     */
    TO_LIST,

    /**
     * Use .collect(Collectors.toUnmodifiableList())
     */
    COLLECT_TO_UNMODIFIABLE_LIST,

    /**
     * Use .collect(Collectors.toList())
     */
    COLLECT_TO_LIST
}
