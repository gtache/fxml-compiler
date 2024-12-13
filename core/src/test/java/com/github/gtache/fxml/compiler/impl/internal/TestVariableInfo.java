package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.parsing.ParsedObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestVariableInfo {

    private final String id;
    private final ParsedObject parsedObject;
    private final String variableName;
    private final String className;
    private final VariableInfo info;

    TestVariableInfo(@Mock final ParsedObject parsedObject) {
        this.id = "id";
        this.parsedObject = Objects.requireNonNull(parsedObject);
        this.variableName = "variableName";
        this.className = "className";
        this.info = new VariableInfo(id, parsedObject, variableName, className);
    }

    @Test
    void testGetters() {
        assertEquals(id, info.id());
        assertEquals(parsedObject, info.parsedObject());
        assertEquals(variableName, info.variableName());
        assertEquals(className, info.className());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new VariableInfo(null, parsedObject, variableName, className));
        assertThrows(NullPointerException.class, () -> new VariableInfo(id, null, variableName, className));
        assertThrows(NullPointerException.class, () -> new VariableInfo(id, parsedObject, null, className));
        assertThrows(NullPointerException.class, () -> new VariableInfo(id, parsedObject, variableName, null));
    }
}
