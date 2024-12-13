package com.github.gtache.fxml.compiler.parsing.xml;

import com.github.gtache.fxml.compiler.impl.ClassesFinder;
import com.github.gtache.fxml.compiler.parsing.FXMLParser;
import com.github.gtache.fxml.compiler.parsing.ParseException;
import com.github.gtache.fxml.compiler.parsing.ParsedDefine;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.*;
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
        return switch (name) {
            case "fx:include" -> new ParsedIncludeImpl(attributes);
            case "fx:reference" -> new ParsedReferenceImpl(attributes);
            case "fx:copy" -> new ParsedCopyImpl(attributes);
            case "fx:define" -> parseDefine(node, imports);
            case "fx:root", "fx:script" -> throw new ParseException("Unsupported node : " + name);
            default -> {
                if (attributes.containsKey("fx:constant")) {
                    yield new ParsedConstantImpl(imports.search(name), attributes);
                } else if (attributes.containsKey("fx:value")) {
                    yield new ParsedValueImpl(imports.search(name), attributes);
                } else if (attributes.containsKey("fx:factory")) {
                    yield parseFactory(node, attributes, imports);
                } else {
                    yield parseObject(node, attributes, imports);
                }
            }
        };
    }

    private ParsedDefine parseDefine(final Node node, final Imports imports) throws ParseException {
        final var children = node.getChildNodes();
        final var parsedChildren = new ArrayList<ParsedObject>();
        for (var i = 0; i < children.getLength(); i++) {
            final var item = children.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (isObject(item)) {
                    parsedChildren.add(parseObject(item, imports));
                } else {
                    throw new ParseException("fx:define with unexpected node : " + item.getNodeName());
                }
            }
        }
        return new ParsedDefineImpl(parsedChildren);
    }

    private ParsedObject parseObject(final Node node, final Map<String, ParsedProperty> attributes, final Imports imports) throws ParseException {
        final var name = node.getNodeName();
        final var children = node.getChildNodes();
        final var properties = new LinkedHashMap<ParsedProperty, SequencedCollection<ParsedObject>>();
        final var objects = new ArrayList<ParsedObject>();
        for (var i = 0; i < children.getLength(); i++) {
            final var item = children.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (isObject(item)) {
                    objects.add(parseObject(item, imports));
                } else if (isSimpleProperty(item)) {
                    final var sourceTypeName = getSourceTypeName(item, imports);
                    final var property = new ParsedPropertyImpl(sourceTypeName.name(), sourceTypeName.sourceType(), item.getTextContent());
                    attributes.put(property.name(), property);
                } else {
                    final var parsedProperty = parseProperty(item, imports);
                    properties.put(parsedProperty.property(), parsedProperty.objects());
                }
            } else if (item.getNodeType() == Node.TEXT_NODE && !item.getNodeValue().isBlank()) {
                objects.add(new ParsedTextImpl(item.getNodeValue().trim()));
            }
        }
        return new ParsedObjectImpl(imports.search(name), attributes, properties, objects);
    }

    private ParsedObject parseFactory(final Node node, final Map<String, ParsedProperty> attributes, final Imports imports) throws ParseException {
        final var name = node.getNodeName();
        final var children = node.getChildNodes();
        final var arguments = new ArrayList<ParsedObject>();
        final var objects = new ArrayList<ParsedObject>();
        for (var i = 0; i < children.getLength(); i++) {
            final var item = children.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (isObject(item)) {
                    final var parsed = parseObject(item, imports);
                    if (parsed instanceof ParsedDefine) {
                        objects.add(parsed);
                    } else {
                        arguments.add(parseObject(item, imports));
                    }
                } else {
                    throw new ParseException("Unexpected node : " + item.getNodeName() + " in factory " + name);
                }
            }
        }
        return new ParsedFactoryImpl(imports.search(name), attributes, arguments, objects);
    }

    private static Map<String, ParsedProperty> parseAttributes(final NamedNodeMap attributes, final Imports imports) throws ParseException {
        if (attributes == null) {
            return new HashMap<>();
        } else {
            final var map = new HashMap<String, ParsedProperty>();
            for (var i = 0; i < attributes.getLength(); i++) {
                final var attribute = attributes.item(i);
                final var value = attribute.getNodeValue();
                final var sourceTypeName = getSourceTypeName(attribute, imports);
                final var sourceType = sourceTypeName.sourceType();
                final var name = sourceTypeName.name();
                if (!name.startsWith("xmlns")) {
                    if (name.startsWith("on")) {
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

    private static boolean isObject(final Node node) {
        return !isProperty(node);
    }

    private static boolean isProperty(final Node node) {
        final var name = node.getNodeName();
        final var lastPart = name.substring(name.lastIndexOf('.') + 1);
        return !name.startsWith("fx:") && Character.isLowerCase(lastPart.charAt(0));
    }

    private static boolean isSimpleProperty(final Node item) {
        return isProperty(item) && item.getChildNodes().getLength() == 1 && item.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE;
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
