/**
 * XML parsing module for FXML compiler
 */
module ch.gtache.fxml.compiler.xml {
    requires transitive ch.gtache.fxml.compiler.core;
    requires java.xml;
    requires org.apache.logging.log4j;

    exports ch.gtache.fxml.compiler.parsing.xml;
}