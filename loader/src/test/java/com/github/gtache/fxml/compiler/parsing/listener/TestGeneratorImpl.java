package com.github.gtache.fxml.compiler.parsing.listener;

import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.Generator;
import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerInfoImpl;
import com.github.gtache.fxml.compiler.impl.ControllerInjectionImpl;
import com.github.gtache.fxml.compiler.impl.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.impl.GenerationParametersImpl;
import com.github.gtache.fxml.compiler.impl.GenerationRequestImpl;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestGeneratorImpl {

    private static final Map<String, List<String>> GENERIC_TYPES;

    static {
        GENERIC_TYPES = new HashMap<>();
        GENERIC_TYPES.put("choiceBox", List.of("String"));
        GENERIC_TYPES.put("listView", List.of("javafx.scene.control.Label"));
        GENERIC_TYPES.put("spinner", List.of("Double"));
        GENERIC_TYPES.put("tableView", List.of("javafx.scene.control.TextArea"));
        GENERIC_TYPES.put("treeView", List.of("String"));
        GENERIC_TYPES.put("treeTableView", List.of("javafx.scene.control.TreeItem<String>"));
        GENERIC_TYPES.put("treeTableColumn1", List.of("javafx.scene.control.TreeItem<String>", "String"));
        GENERIC_TYPES.put("treeTableColumn2", List.of("javafx.scene.control.TreeItem<String>", "Integer"));
        GENERIC_TYPES.put("tableColumn1", List.of("javafx.scene.control.TextArea", "Float"));
        GENERIC_TYPES.put("tableColumn2", List.of("javafx.scene.control.TextArea", "String"));
    }

    private final Generator generator;

    TestGeneratorImpl() {
        this.generator = new GeneratorImpl();
    }

    @BeforeAll
    static void beforeAll() {
        try {
            Platform.startup(() -> {

            });
        } catch (final IllegalStateException ignored) {

        }
    }

    @ParameterizedTest
    @MethodSource("providesGenerationTestCases")
    public void testGenerate(final String file, final ControllerFieldInjectionTypes field, final ControllerMethodsInjectionType method, final ResourceBundleInjectionTypes bundle) {
        final var request = getRequest(file, field, method, bundle);
        final var path = Paths.get(getPath(file, field, method, bundle));
        try (final var in = getClass().getResourceAsStream("/com/github/gtache/fxml/compiler/parsing/listener/" + path)) {
            final var expected = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            final var actual = generator.generate(request);
            assertEquals(expected, actual);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) {
        //Generates the test cases
        try {
            Platform.startup(() -> {
                final var generator = new GeneratorImpl();
                final var files = List.of("Controls", "Includes");
                for (final var file : files) {
                    for (final var field : ControllerFieldInjectionTypes.values()) {
                        for (final var method : ControllerMethodsInjectionType.values()) {
                            for (final var bundle : ResourceBundleInjectionTypes.values()) {
                                final var request = getRequest(file, field, method, bundle);
                                final var content = generator.generate(request);
                                final var path = Paths.get(getPath(file, field, method, bundle));
                                try {
                                    Files.writeString(path, content);
                                } catch (final IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            });
        } finally {
            Platform.exit();
        }
    }

    private static String getPath(final String file, final ControllerFieldInjectionTypes field, final ControllerMethodsInjectionType method, final ResourceBundleInjectionTypes bundle) {
        return "expected-" + file.toLowerCase() + "-" + field.name().toLowerCase() + "-" + method.name().toLowerCase() + "-" + bundle.name().replace("_", "").toLowerCase() + ".txt";
    }

    private static GenerationRequest getRequest(final String file, final ControllerFieldInjectionTypes field, final ControllerMethodsInjectionType method, final ResourceBundleInjectionTypes bundle) {
        return CompletableFuture.supplyAsync(() -> {
            final var controlsControllerInfo = new ControllerInfoImpl(Map.of("keyPressed", false, "mouseClicked", false),
                    GENERIC_TYPES);
            final var includesControllerInfo = new ControllerInfoImpl(Map.of(), Map.of());
            final var controllerInfo = file.equals("Controls") ? controlsControllerInfo : includesControllerInfo;
            final var resourceBundlePath = "com.github.gtache.fxml.compiler.parsing.listener." + file + "Bundle";
            final var viewPath = "/com/github/gtache/fxml/compiler/parsing/listener/" + file + "View.fxml";
            final var listener = new ParsingLoadListener();
            final var loader = new FXMLLoader(TestGeneratorImpl.class.getResource(viewPath));
            loader.setLoadListener(listener);
            loader.setResources(ResourceBundle.getBundle(resourceBundlePath));
            try {
                loader.load();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            final var root = listener.root();
            return new GenerationRequestImpl(
                    new GenerationParametersImpl(Map.of(
                            "com.github.gtache.fxml.compiler.parsing.listener.ControlsController", new ControllerInjectionImpl(field, method,
                                    "com.github.gtache.fxml.compiler.parsing.listener.ControlsController"),
                            "com.github.gtache.fxml.compiler.parsing.listener.IncludesController", new ControllerInjectionImpl(field, method,
                                    "com.github.gtache.fxml.compiler.parsing.listener.IncludesController")),
                            Map.of("controlsView.fxml", "com.github.gtache.fxml.compiler.parsing.listener.ControlsView"),
                            Map.of("controlsView.fxml", "com.github.gtache.fxml.compiler.parsing.listener.ControlsController"),
                            new ResourceBundleInjectionImpl(bundle, resourceBundlePath)
                    ),
                    controllerInfo,
                    root,
                    "com.github.gtache.fxml.compiler.parsing.listener." + file + "Controller"
            );
        }, Platform::runLater).join();
    }

    private static Stream<Arguments> providesGenerationTestCases() {
        final var files = List.of("Controls", "Includes");
        final var list = new ArrayList<Arguments>();
        for (final var file : files) {
            for (final var field : ControllerFieldInjectionTypes.values()) {
                for (final var method : ControllerMethodsInjectionType.values()) {
                    for (final var bundle : ResourceBundleInjectionTypes.values()) {
                        list.add(Arguments.of(file, field, method, bundle));
                    }
                }
            }
        }
        return list.stream();
    }
}
