/**
 * Core module for FXML compiler
 */
module ch.gtache.fxml.compiler.core {
    requires transitive ch.gtache.fxml.compiler.api;
    requires transitive javafx.graphics;
    requires org.apache.logging.log4j;
    requires java.sql;

    exports ch.gtache.fxml.compiler.impl;
    exports ch.gtache.fxml.compiler.compatibility.impl;
    exports ch.gtache.fxml.compiler.parsing.impl;
}