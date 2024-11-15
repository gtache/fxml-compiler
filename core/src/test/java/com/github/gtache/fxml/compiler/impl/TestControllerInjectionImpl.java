package com.github.gtache.fxml.compiler.impl;

import com.github.gtache.fxml.compiler.ControllerInjection;
import com.github.gtache.fxml.compiler.InjectionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TestControllerInjectionImpl {

    private final InjectionType fieldInjectionType;
    private final InjectionType methodInjectionType;
    private final String injectionClass;
    private final ControllerInjection controllerInjection;

    TestControllerInjectionImpl(@Mock final InjectionType fieldInjectionType, @Mock final InjectionType methodInjectionType) {
        this.fieldInjectionType = Objects.requireNonNull(fieldInjectionType);
        this.methodInjectionType = Objects.requireNonNull(methodInjectionType);
        this.injectionClass = "class";
        this.controllerInjection = new ControllerInjectionImpl(fieldInjectionType, methodInjectionType, injectionClass);
    }

    @Test
    void testGetters() {
        assertEquals(fieldInjectionType, controllerInjection.fieldInjectionType());
        assertEquals(methodInjectionType, controllerInjection.methodInjectionType());
        assertEquals(injectionClass, controllerInjection.injectionClass());
    }

    @Test
    void testIllegal() {
        assertThrows(NullPointerException.class, () -> new ControllerInjectionImpl(null, methodInjectionType, injectionClass));
        assertThrows(NullPointerException.class, () -> new ControllerInjectionImpl(fieldInjectionType, null, injectionClass));
        assertThrows(NullPointerException.class, () -> new ControllerInjectionImpl(fieldInjectionType, methodInjectionType, null));
    }
}
