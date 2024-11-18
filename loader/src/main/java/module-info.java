/**
 * FXML LoadListener module for FXML compiler
 */
module com.github.gtache.fxml.compiler.loader {
    requires transitive com.github.gtache.fxml.compiler.core;
    requires transitive javafx.fxml;

    exports com.github.gtache.fxml.compiler.parsing.listener;
}