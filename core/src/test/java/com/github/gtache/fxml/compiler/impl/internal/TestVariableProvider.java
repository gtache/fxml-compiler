package com.github.gtache.fxml.compiler.impl.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class TestVariableProvider {

    private final VariableInfo variableInfo;
    private final String id;
    private final VariableProvider provider;

    TestVariableProvider(@Mock final VariableInfo variableInfo) {
        this.variableInfo = Objects.requireNonNull(variableInfo);
        this.id = "id";
        this.provider = new VariableProvider();
    }

    @Test
    void testGetVariableName() {
        assertEquals("var0", provider.getNextVariableName("var"));
        assertEquals("var1", provider.getNextVariableName("var"));
        assertEquals("other0", provider.getNextVariableName("other"));
    }

    @Test
    void testAddVariableInfo() {
        assertNull(provider.getVariableInfo(id));
        provider.addVariableInfo(id, variableInfo);
        assertEquals(variableInfo, provider.getVariableInfo(id));
    }
}
