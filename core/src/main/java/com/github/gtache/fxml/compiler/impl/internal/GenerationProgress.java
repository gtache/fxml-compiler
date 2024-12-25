package com.github.gtache.fxml.compiler.impl.internal;

import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;

import java.util.ArrayList;
import java.util.Objects;
import java.util.SequencedCollection;

/**
 * Used by {@link GeneratorImpl} to track the generation progress
 *
 * @param request                     The generation request
 * @param controllerFactoryPostAction The controller factory post action for factory injection
 * @param stringBuilder               The string builder
 */
public record GenerationProgress(GenerationRequest request,
                                 SequencedCollection<String> controllerFactoryPostAction,
                                 StringBuilder stringBuilder) {

    /**
     * Instantiates a new GenerationProgress
     *
     * @param request                     The generation request
     * @param controllerFactoryPostAction The controller factory post action
     * @param stringBuilder               The string builder
     * @throws NullPointerException if any parameter is null
     */
    public GenerationProgress {
        Objects.requireNonNull(request);
        Objects.requireNonNull(controllerFactoryPostAction);
        Objects.requireNonNull(stringBuilder);
    }

    /**
     * Instantiates a new GenerationProgress
     *
     * @param request The generation request
     * @throws NullPointerException if request is null
     */
    public GenerationProgress(final GenerationRequest request) {
        this(request, new ArrayList<>(), new StringBuilder());
    }
}
