/**
 * XML parsing module for FXML compiler
 */
module com.github.gtache.fxml.compiler.xml {
    requires transitive com.github.gtache.fxml.compiler.core;
    requires java.xml;
    requires org.apache.logging.log4j;

    exports com.github.gtache.fxml.compiler.parsing.xml;
}