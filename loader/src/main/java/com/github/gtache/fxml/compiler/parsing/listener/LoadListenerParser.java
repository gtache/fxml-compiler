package com.github.gtache.fxml.compiler.parsing.listener;

import com.github.gtache.fxml.compiler.parsing.FXMLParser;
import com.github.gtache.fxml.compiler.parsing.ParseException;
import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedIncludeImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedObjectImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.concurrent.CompletableFuture;

/**
 * {@link LoadListener} implementation parsing the FXML file to {@link ParsedObject}
 */
public class LoadListenerParser implements LoadListener, FXMLParser {

    private static final Logger logger = LogManager.getLogger(LoadListenerParser.class);

    private final Deque<ParsedObjectImpl.Builder> stack;
    private final Deque<ParsedProperty> propertyStack;
    private final Deque<List<ParsedObject>> currentObjectStack;
    private final SequencedMap<String, ParsedProperty> currentIncludeProperties;
    private ParsedObjectImpl.Builder current;
    private ParsedProperty currentProperty;
    private List<ParsedObject> currentObjects;
    private Object previousEnd;
    private boolean isInclude;

    /**
     * Instantiates the listener
     */
    public LoadListenerParser() {
        this.stack = new ArrayDeque<>();
        this.propertyStack = new ArrayDeque<>();
        this.currentObjectStack = new ArrayDeque<>();
        this.currentObjects = new ArrayList<>();
        this.currentIncludeProperties = new LinkedHashMap<>();
    }

    /**
     * @return The parsed root
     */
    ParsedObject root() {
        if (currentObjects != null && currentObjects.size() == 1) {
            return currentObjects.getFirst();
        } else {
            throw new IllegalStateException("Expected 1 root object, found " + currentObjects);
        }
    }


    @Override
    public void readImportProcessingInstruction(final String target) {
        logger.debug("Import processing instruction : {}", target);
        previousEnd = null;
        //Do nothing
    }

    @Override
    public void readLanguageProcessingInstruction(final String language) {
        logger.debug("Language processing instruction : {}", language);
        previousEnd = null;
        //Do nothing
    }

    @Override
    public void readComment(final String comment) {
        logger.debug("Comment : {}", comment);
        //Do nothing
    }

    @Override
    public void beginInstanceDeclarationElement(final Class<?> type) {
        logger.debug("Instance declaration : {}", type);
        previousEnd = null;
        if (current != null) {
            stack.push(current);
        }
        current = new ParsedObjectImpl.Builder();
        current.className(type.getName());
    }

    @Override
    public void beginUnknownTypeElement(final String name) {
        logger.debug("Unknown type : {}", name);
        throw new IllegalArgumentException("Unknown type : " + name);
    }

    @Override
    public void beginIncludeElement() {
        logger.debug("Include");
        previousEnd = null;
        if (isInclude) {
            throw new IllegalStateException("Nested include");
        } else if (!currentIncludeProperties.isEmpty()) {
            throw new IllegalStateException("Include properties not empty");
        }
        isInclude = true;
    }

    @Override
    public void beginReferenceElement() {
        logger.debug("Reference");
        throw new UnsupportedOperationException("Reference not supported yet");
    }

    @Override
    public void beginCopyElement() {
        logger.debug("Copy");
        throw new UnsupportedOperationException("Copy not supported yet");
    }

    @Override
    public void beginRootElement() {
        logger.debug("Root element");
        throw new UnsupportedOperationException("Root element not supported yet");
    }

    @Override
    public void beginPropertyElement(final String name, final Class<?> sourceType) {
        logger.debug("Property ({}): {}", sourceType, name);
        previousEnd = null;
        if (isInclude) {
            throw new IllegalStateException("Reading complex property for include");
        } else {
            if (currentProperty != null) {
                propertyStack.push(currentProperty);
            }
            currentProperty = new ParsedPropertyImpl(name, sourceType == null ? null : sourceType.getName(), null);
            currentObjectStack.push(currentObjects);
            currentObjects = new ArrayList<>();
        }
    }

    @Override
    public void beginUnknownStaticPropertyElement(final String name) {
        logger.debug("Unknown static property : {}", name);
        throw new IllegalArgumentException("Unknown static property : " + name);
    }

    @Override
    public void beginScriptElement() {
        logger.debug("Script");
        throw new UnsupportedOperationException("Script not supported yet");
    }

    @Override
    public void beginDefineElement() {
        logger.debug("Define");
        throw new UnsupportedOperationException("Define not supported yet");
    }

    @Override
    public void readInternalAttribute(final String name, final String value) {
        logger.debug("Internal attribute : {} = {}", name, value);
        previousEnd = null;
        final var property = new ParsedPropertyImpl(name, null, value);
        if (isInclude) {
            currentIncludeProperties.put(name, property);
        } else if (current == null) {
            throw new IllegalStateException("Current object is null (trying to add attribute " + name + " = " + value + ")");
        } else {
            current.addAttribute(property);
        }
    }

    @Override
    public void readPropertyAttribute(final String name, final Class<?> sourceType, final String value) {
        logger.debug("Property ({}): {} = {}", sourceType, name, value);
        if (isInclude) {
            throw new IllegalStateException("Reading complex property for include");
        } else if (current == null) {
            throw new IllegalStateException("Current object is null (trying to add property " + name + "/" + sourceType + " = " + value + ")");
        } else {
            previousEnd = null;
            current.addAttribute(new ParsedPropertyImpl(name, sourceType == null ? null : sourceType.getName(), value));
        }
    }

    @Override
    public void readUnknownStaticPropertyAttribute(final String name, final String value) {
        logger.debug("Unknown static property attribute : {} = {}", name, value);
        throw new IllegalArgumentException("Unknown static property : " + name);
    }

    @Override
    public void readEventHandlerAttribute(final String name, final String value) {
        logger.debug("Event handler attribute : {} = {}", name, value);
        if (isInclude) {
            throw new IllegalStateException("Reading event handler for include");
        } else if (current == null) {
            throw new IllegalStateException("Current object is null (trying to add event handler" + name + " = " + value + ")");
        } else {
            current.addAttribute(new ParsedPropertyImpl(name, EventHandler.class.getName(), value));
        }
    }

    @Override
    public void endElement(final Object value) {
        logger.debug("End element : {}", value);
        if (isInclude) {
            endInclude();
        } else if (previousEnd == value || value instanceof ObservableList<?>) {
            endProperty(value);
        } else if (current != null) {
            endObject(value);
        } else {
            throw new IllegalStateException("Unexpected end element (current is null) : " + value);
        }
    }

    private void endInclude() {
        currentObjects.add(new ParsedIncludeImpl(currentIncludeProperties));
        currentIncludeProperties.clear();
        isInclude = false;
    }

    private void endProperty(final Object value) {
        //End of property
        if (currentProperty == null) {
            throw new IllegalStateException("Unexpected end element (property is null) : " + value);
        } else {
            currentObjects.forEach(go -> current.addProperty(currentProperty, go));
            currentObjects = currentObjectStack.isEmpty() ? new ArrayList<>() : currentObjectStack.pop();
            currentProperty = propertyStack.isEmpty() ? null : propertyStack.pop();
        }
    }

    private void endObject(final Object value) {
        final var built = current.build();
        currentObjects.add(built);
        current = stack.isEmpty() ? null : stack.pop();
        previousEnd = value;
    }

    @Override
    public ParsedObject parse(final String content) throws ParseException {
        Path path = null;
        try {
            path = Files.createTempFile("temp", ".fxml");
            Files.writeString(path, content);
            return parse(path);
        } catch (final IOException e) {
            throw new ParseException("Error creating temp file", e);
        } finally {
            if (path != null) {
                try {
                    Files.deleteIfExists(path);
                } catch (final IOException ignored) {
                }
            }
        }
    }

    private void reset() {
        current = null;
        stack.clear();
        propertyStack.clear();
        currentObjects.clear();
        currentObjectStack.clear();
        currentIncludeProperties.clear();
        currentProperty = null;
        isInclude = false;
        previousEnd = null;
    }

    @Override
    public ParsedObject parse(final Path path) throws ParseException {
        reset();
        try {
            final var url = path.toUri().toURL();
            logger.info("Parsing {}", url);
            return CompletableFuture.supplyAsync(() -> {
                try {
                    final var loader = new FXMLLoader(url);
                    loader.setLoadListener(this);
                    loader.load();
                    return root();
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, Platform::runLater).join();
        } catch (final MalformedURLException | RuntimeException e) {
            throw new ParseException("Error parsing " + path, e);
        }
    }
}
