package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.InjectionType;
import com.github.gtache.fxml.compiler.ResourceBundleInjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestResourceBundleInjectionImpl {

    private final InjectionType injectionType;
    private final String bundleName;
    private final ResourceBundleInjection resourceBundleInjection;

    TestResourceBundleInjectionImpl(@Mock final InjectionType injectionType) {
        this.injectionType = Objects.requireNonNull(injectionType);
        this.bundleName = "bundle";
        this.resourceBundleInjection = new ResourceBundleInjectionImpl(injectionType, bundleName);
    }

    @Test
    void testGetters() {
        assertEquals(injectionType, resourceBundleInjection.injectionType());
        assertEquals(bundleName, resourceBundleInjection.bundleName());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ResourceBundleInjectionImpl(null, bundleName));
        assertThrows(NullPointerException.class, () -> new ResourceBundleInjectionImpl(injectionType, null));
    }
}
