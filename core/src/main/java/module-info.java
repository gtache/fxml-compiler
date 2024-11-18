/**
 * Core module for FXML compiler
 */
module com.github.gtache.fxml.compiler.core {
    requires transitive com.github.gtache.fxml.compiler.api;
    requires transitive javafx.graphics;

    exports com.github.gtache.fxml.compiler.impl;
    exports com.github.gtache.fxml.compiler.parsing.impl;
}