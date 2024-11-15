package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInjection;
import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.ResourceBundleInjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestGenerationParametersImpl {

    private final Map<String, ControllerInjection> controllerInjections;
    private final Map<String, String> sourceToGeneratedClassName;
    private final Map<String, String> sourceToControllerName;
    private final ResourceBundleInjection resourceBundleInjection;
    private final GenerationParameters parameters;

    TestGenerationParametersImpl(@Mock final ControllerInjection controllerInjection, @Mock final ResourceBundleInjection resourceBundleInjection) {
        this.controllerInjections = Map.of("class", controllerInjection);
        this.sourceToGeneratedClassName = Map.of("source", "generated");
        this.sourceToControllerName = Map.of("source", "class");
        this.resourceBundleInjection = Objects.requireNonNull(resourceBundleInjection);
        this.parameters = new GenerationParametersImpl(controllerInjections, sourceToGeneratedClassName, sourceToControllerName, resourceBundleInjection);
    }

    @Test
    void testGetters() {
        assertEquals(controllerInjections, parameters.controllerInjections());
        assertEquals(sourceToGeneratedClassName, parameters.sourceToGeneratedClassName());
        assertEquals(sourceToControllerName, parameters.sourceToControllerName());
        assertEquals(resourceBundleInjection, parameters.resourceBundleInjection());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(null, sourceToGeneratedClassName, sourceToControllerName, resourceBundleInjection));
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(controllerInjections, null, sourceToControllerName, resourceBundleInjection));
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(controllerInjections, sourceToGeneratedClassName, null, resourceBundleInjection));
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(controllerInjections, sourceToGeneratedClassName, sourceToControllerName, null));
    }
}
