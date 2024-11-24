/**
 * FXML LoadListener module for FXML compiler
 */
module com.github.gtache.fxml.compiler.loader {
    requires transitive com.github.gtache.fxml.compiler.core;
    requires transitive javafx.fxml;
    requires org.apache.logging.log4j;

    exports com.github.gtache.fxml.compiler.parsing.listener;
}