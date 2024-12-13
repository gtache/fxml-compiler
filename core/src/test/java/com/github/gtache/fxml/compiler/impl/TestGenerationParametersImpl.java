package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.GenerationParameters;
import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.compatibility.GenerationCompatibility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestGenerationParametersImpl {

    private final GenerationCompatibility compatibility;
    private final boolean useImageInputStreamConstructor;
    private final Map<String, String> bundleMap;
    private final InjectionType controllerInjectionType;
    private final InjectionType fieldInjectionType;
    private final InjectionType methodInjectionType;
    private final InjectionType resourceInjectionType;
    private final GenerationParameters parameters;

    TestGenerationParametersImpl(@Mock final GenerationCompatibility compatibility, @Mock final InjectionType controllerInjectionType,
                                 @Mock final InjectionType fieldInjectionType, @Mock final InjectionType methodInjectionType,
                                 @Mock final InjectionType resourceInjectionType) {
        this.compatibility = requireNonNull(compatibility);
        this.useImageInputStreamConstructor = true;
        this.controllerInjectionType = requireNonNull(controllerInjectionType);
        this.fieldInjectionType = requireNonNull(fieldInjectionType);
        this.methodInjectionType = requireNonNull(methodInjectionType);
        this.resourceInjectionType = requireNonNull(resourceInjectionType);
        this.bundleMap = Map.of("source", "generated");
        this.parameters = new GenerationParametersImpl(compatibility, useImageInputStreamConstructor, bundleMap, controllerInjectionType, fieldInjectionType, methodInjectionType, resourceInjectionType);
    }

    @Test
    void testGetters() {
        assertEquals(compatibility, parameters.compatibility());
        assertEquals(useImageInputStreamConstructor, parameters.useImageInputStreamConstructor());
        assertEquals(bundleMap, parameters.bundleMap());
        assertEquals(controllerInjectionType, parameters.controllerInjectionType());
        assertEquals(fieldInjectionType, parameters.fieldInjectionType());
        assertEquals(methodInjectionType, parameters.methodInjectionType());
        assertEquals(resourceInjectionType, parameters.resourceInjectionType());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(null, useImageInputStreamConstructor, bundleMap, controllerInjectionType, fieldInjectionType, methodInjectionType, resourceInjectionType));
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(compatibility, useImageInputStreamConstructor, null, controllerInjectionType, fieldInjectionType, methodInjectionType, resourceInjectionType));
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(compatibility, useImageInputStreamConstructor, bundleMap, null, fieldInjectionType, methodInjectionType, resourceInjectionType));
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(compatibility, useImageInputStreamConstructor, bundleMap, controllerInjectionType, null, methodInjectionType, resourceInjectionType));
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(compatibility, useImageInputStreamConstructor, bundleMap, controllerInjectionType, fieldInjectionType, null, resourceInjectionType));
        assertThrows(NullPointerException.class, () -> new GenerationParametersImpl(compatibility, useImageInputStreamConstructor, bundleMap, controllerInjectionType, fieldInjectionType, methodInjectionType, null));
    }
}
