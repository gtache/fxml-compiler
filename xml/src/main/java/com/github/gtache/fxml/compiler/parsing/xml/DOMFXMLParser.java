package com.github.gtache.fxml.compiler.parsing.xml;

import com.github.gtache.fxml.compiler.impl.ClassesFinder;
import com.github.gtache.fxml.compiler.parsing.FXMLParser;
import com.github.gtache.fxml.compiler.parsing.ParseException;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedConstantImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedIncludeImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedObjectImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.event.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.stream.Collectors;

/**
 * Implementation of {@link FXMLParser} using DOM parsing
 */
public class DOMFXMLParser implements FXMLParser {

    private static final Logger logger = LogManager.getLogger(DOMFXMLParser.class);

    private final DocumentBuilderFactory documentBuilderFactory;

    /**
     * Instantiates a new parser
     */
    public DOMFXMLParser() {
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
    }

    @Override
    public ParsedObject parse(final String content) throws ParseException {
        try {
            final var documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final var document = documentBuilder.parse(new InputSource(new StringReader(content)));
            return parseDocument(document);
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            throw new ParseException("Error instantiating document builder", e);
        }
    }

    private ParsedObject parseDocument(final Document document) throws ParseException {
        final var imports = parseImports(document);
        return parseObject(document.getDocumentElement(), imports);
    }

    private static Imports parseImports(final Node document) throws ParseException {
        logger.debug("Parsing imports");
        final var imports = new Imports();
        final var children = document.getChildNodes();
        for (var i = 0; i < children.getLength(); i++) {
            final var node = children.item(i);
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && node.getNodeName().equals("import")) {
                final var value = node.getNodeValue();
                final var imported = getImports(node);
                if (value.endsWith(".*") && imported.isEmpty()) {
                    logger.debug("Found package import {} which couldn't be pre-fetched", value);
                    imports.add(value.substring(0, value.length() - 2));
                } else {
                    logger.debug("Found import {} -> {}", node.getNodeValue(), imported);
                    imported.forEach(imports::add);
                }
            }
        }
        return imports;
    }

    private ParsedObject parseObject(final Node node, final Imports imports) throws ParseException {
        final var attributes = parseAttributes(node.getAttributes(), imports);
        final var name = node.getNodeName();
        logger.debug("Parsing {}", name);
        if (name.equals("fx:include")) {
            return new ParsedIncludeImpl(attributes);
        } else if (attributes.containsKey("fx:constant")) {
            return new ParsedConstantImpl(imports.search(name), attributes);
        } else if (name.equals("fx:copy") || name.equals("fx:define") || name.equals("fx:root") || name.equals("fx:reference")) {
            throw new ParseException("Unsupported node : " + name);
        } else {
            final var children = node.getChildNodes();
            final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
            for (var i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    final var parsedProperty = parseProperty(children.item(i), imports);
                    properties.put(parsedProperty.property(), parsedProperty.objects());
                }
            }
            return new ParsedObjectImpl(imports.search(name), attributes, properties);
        }
    }

    private static Map<String, ParsedProperty> parseAttributes(final NamedNodeMap attributes, final Imports imports) throws ParseException {
        if (attributes == null) {
            return Map.of();
        } else {
            final var map = new HashMap<String, ParsedProperty>();
            for (var i = 0; i < attributes.getLength(); i++) {
                final var attribute = attributes.item(i);
                final var value = attribute.getNodeValue();
                final var sourceTypeName = getSourceTypeName(attribute, imports);
                final var sourceType = sourceTypeName.sourceType();
                final var name = sourceTypeName.name();
                if (!name.startsWith("xmlns")) {
                    if (name.startsWith("on") && value.startsWith("#")) {
                        logger.debug("Found event handler {} -> {}", name, value);
                        map.put(name, new ParsedPropertyImpl(name, EventHandler.class.getName(), value));
                    } else {
                        logger.debug("Found attribute {} ({})-> {}", name, sourceType, value);
                        map.put(name, new ParsedPropertyImpl(name, sourceType, value));
                    }
                }
            }
            return map;
        }
    }

    private ComplexProperty parseProperty(final Node node, final Imports imports) throws ParseException {
        final var sourceTypeName = getSourceTypeName(node, imports);
        logger.debug("Found property {} ({})-> {}", sourceTypeName.name(), sourceTypeName.sourceType(), node.getNodeValue());
        final var objects = new ArrayList<ParsedObject>();
        final var children = node.getChildNodes();
        for (var i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                objects.add(parseObject(children.item(i), imports));
            }
        }
        return new ComplexProperty(new ParsedPropertyImpl(sourceTypeName.name(), sourceTypeName.sourceType(), null), objects);
    }

    private static SourceTypeName getSourceTypeName(final Node node, final Imports imports) throws ParseException {
        final var nameSplit = node.getNodeName().split("\\.");
        if (nameSplit.length > 1) {
            final var sourceTypeName = Arrays.stream(nameSplit).limit(nameSplit.length - 1L).collect(Collectors.joining("."));
            final var importedSourceType = imports.search(sourceTypeName);
            final var name = nameSplit[nameSplit.length - 1];
            return new SourceTypeName(name, importedSourceType);
        } else {
            final var name = nameSplit[0];
            return new SourceTypeName(name, null);
        }
    }

    private static Map<String, String> getImports(final Node node) throws ParseException {
        final var imports = new HashMap<String, String>();
        final var importValue = node.getNodeValue();
        if (importValue.endsWith("*")) {
            final var packageName = importValue.substring(0, importValue.length() - 2);
            try {
                final var allClasses = ClassesFinder.getClasses(packageName);
                allClasses.forEach(s -> imports.put(s.substring(packageName.length() + 1), s));
            } catch (final IOException e) {
                throw new ParseException("Error reading package " + packageName, e);
            }
        } else {
            final var className = importValue.substring(importValue.lastIndexOf('.') + 1);
            imports.put(className, importValue);
        }
        return imports;
    }

    private record ComplexProperty(ParsedProperty property, SequencedCollection<ParsedObject> objects) {

        private ComplexProperty {
            Objects.requireNonNull(property);
            objects = List.copyOf(objects);
        }
    }

    private record SourceTypeName(String name, String sourceType) {

        private SourceTypeName {
            Objects.requireNonNull(name);
        }
    }

    private record Imports(Map<String, String> imports, SequencedCollection<String> packages) {

        private Imports() {
            this(new HashMap<>(), new ArrayList<>());
        }

        private void add(final String key, final String value) {
            imports.put(key, value);
        }

        private void add(final String value) {
            packages.add(value);
        }

        private String search(final String key) throws ParseException {
            if (imports.containsKey(key)) {
                return imports.get(key);
            } else {
                for (final var pkg : packages) {
                    final var className = pkg + "." + key;
                    try {
                        Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                        logger.debug("Class {} found in package {}", className, pkg);
                        imports.put(key, className);
                        return className;
                    } catch (final ClassNotFoundException e) {
                        logger.debug("Class {} not found in package {}", key, pkg);
                    }
                }
                throw new ParseException("Cannot find class " + key + " ; Is a dependency missing for the plugin?");
            }
        }
    }
}
