package com.github.gtache.fxml.compiler.parsing.listener;

import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import com.github.gtache.fxml.compiler.parsing.ParsedProperty;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedIncludeImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedObjectImpl;
import com.github.gtache.fxml.compiler.parsing.impl.ParsedPropertyImpl;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.LoadListener;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

/**
 * {@link LoadListener} implementation parsing the FXML file to {@link ParsedObject}
 */
public class ParsingLoadListener implements LoadListener {

    private final Deque<ParsedObjectImpl.Builder> stack;
    private final Deque<ParsedProperty> propertyStack;
    private final Deque<List<ParsedObject>> currentObjectStack;
    private ParsedObjectImpl.Builder current;
    private ParsedProperty currentProperty;
    private List<ParsedObject> currentObjects;
    private Object previousEnd;
    private boolean isInclude;
    private SequencedMap<String, ParsedProperty> currentIncludeProperties;

    /**
     * Instantiates the listener
     */
    public ParsingLoadListener() {
        this.stack = new ArrayDeque<>();
        this.propertyStack = new ArrayDeque<>();
        this.currentObjectStack = new ArrayDeque<>();
        this.currentObjects = new ArrayList<>();
        this.currentIncludeProperties = new LinkedHashMap<>();
    }

    /**
     * @return The parsed root
     */
    public ParsedObject root() {
        if (currentObjects != null && currentObjects.size() == 1) {
            return currentObjects.getFirst();
        } else {
            throw new IllegalStateException("Expected 1 root object, found " + currentObjects);
        }
    }


    @Override
    public void readImportProcessingInstruction(final String target) {
        previousEnd = null;
        //Do nothing
    }

    @Override
    public void readLanguageProcessingInstruction(final String language) {
        previousEnd = null;
        //Do nothing
    }

    @Override
    public void readComment(final String comment) {
        //Do nothing
    }

    @Override
    public void beginInstanceDeclarationElement(final Class<?> type) {
        previousEnd = null;
        if (current != null) {
            stack.push(current);
        }
        current = new ParsedObjectImpl.Builder();
        current.clazz(type);
    }

    @Override
    public void beginUnknownTypeElement(final String name) {
        throw new IllegalArgumentException("Unknown type : " + name);
    }

    @Override
    public void beginIncludeElement() {
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
        throw new UnsupportedOperationException("Reference not supported yet");
    }

    @Override
    public void beginCopyElement() {
        throw new UnsupportedOperationException("Copy not supported yet");
    }

    @Override
    public void beginRootElement() {
        throw new UnsupportedOperationException("Root element not supported yet");
    }

    @Override
    public void beginPropertyElement(final String name, final Class<?> sourceType) {
        previousEnd = null;
        if (currentProperty != null) {
            propertyStack.push(currentProperty);
        }
        currentProperty = new ParsedPropertyImpl(name, sourceType, null);
        currentObjectStack.push(currentObjects);
        currentObjects = new ArrayList<>();
    }

    @Override
    public void beginUnknownStaticPropertyElement(final String name) {
        throw new IllegalArgumentException("Unknown static property : " + name);
    }

    @Override
    public void beginScriptElement() {
        throw new UnsupportedOperationException("Script not supported yet");
    }

    @Override
    public void beginDefineElement() {
        throw new UnsupportedOperationException("Define not supported yet");
    }

    @Override
    public void readInternalAttribute(final String name, final String value) {
        previousEnd = null;
        final var property = new ParsedPropertyImpl(name, null, value);
        if (isInclude) {
            currentIncludeProperties.put(name, property);
        } else {
            current.addProperty(property);
        }
    }

    @Override
    public void readPropertyAttribute(final String name, final Class<?> sourceType, final String value) {
        previousEnd = null;
        current.addProperty(new ParsedPropertyImpl(name, sourceType, value));
    }

    @Override
    public void readUnknownStaticPropertyAttribute(final String name, final String value) {
        throw new IllegalArgumentException("Unknown static property : " + name);
    }

    @Override
    public void readEventHandlerAttribute(final String name, final String value) {
        current.addProperty(new ParsedPropertyImpl(name, EventHandler.class, value));
    }

    @Override
    public void endElement(final Object value) {
        if (isInclude) {
            currentObjects.add(new ParsedIncludeImpl(currentIncludeProperties));
            currentIncludeProperties.clear();
            isInclude = false;
        } else if (previousEnd == value || value instanceof ObservableList<?>) {
            //End of property
            if (currentProperty == null) {
                throw new IllegalStateException("Unexpected end element (property is null) : " + value);
            } else {
                currentObjects.forEach(go -> current.addChild(currentProperty, go));
                currentObjects = currentObjectStack.isEmpty() ? new ArrayList<>() : currentObjectStack.pop();
                currentProperty = propertyStack.isEmpty() ? null : propertyStack.pop();
            }
        } else if (current != null) {
            final var built = current.build();
            currentObjects.add(built);
            current = stack.isEmpty() ? null : stack.pop();
            previousEnd = value;
        } else {
            throw new IllegalStateException("Unexpected end element (current is null) : " + value);
        }
    }
}
