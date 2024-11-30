package com.github.gtache.fxml.compiler.parsing.xml;

import com.github.gtache.fxml.compiler.ControllerFieldInfo;
import com.github.gtache.fxml.compiler.GenerationException;
import com.github.gtache.fxml.compiler.GenerationRequest;
import com.github.gtache.fxml.compiler.Generator;
import com.github.gtache.fxml.compiler.impl.*;
import com.github.gtache.fxml.compiler.parsing.ParseException;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestGeneratorImpl {

    private static final Map<String, ControllerFieldInfo> FIELD_INFO_MAP;

    static {
        FIELD_INFO_MAP = new HashMap<>();
        FIELD_INFO_MAP.put("button", new ControllerFieldInfoImpl("button", List.of()));
        FIELD_INFO_MAP.put("checkBox", new ControllerFieldInfoImpl("checkBox", List.of()));
        FIELD_INFO_MAP.put("colorPicker", new ControllerFieldInfoImpl("colorPicker", List.of()));
        FIELD_INFO_MAP.put("color", new ControllerFieldInfoImpl("color", List.of()));
        FIELD_INFO_MAP.put("comboBox", new ControllerFieldInfoImpl("comboBox", List.of()));
        FIELD_INFO_MAP.put("listView", new ControllerFieldInfoImpl("listView", List.of("javafx.scene.control.Label")));
        FIELD_INFO_MAP.put("spinner", new ControllerFieldInfoImpl("spinner", List.of("Double")));
        FIELD_INFO_MAP.put("tableView", new ControllerFieldInfoImpl("tableView", List.of("javafx.scene.control.TextArea")));
        FIELD_INFO_MAP.put("treeView", new ControllerFieldInfoImpl("treeView", List.of("String")));
        FIELD_INFO_MAP.put("treeTableView", new ControllerFieldInfoImpl("treeTableView", List.of("javafx.scene.control.TreeItem<String>")));
        FIELD_INFO_MAP.put("treeTableColumn1", new ControllerFieldInfoImpl("treeTableColumn1", List.of("javafx.scene.control.TreeItem<String>", "String")));
        FIELD_INFO_MAP.put("treeTableColumn2", new ControllerFieldInfoImpl("treeTableColumn2", List.of("javafx.scene.control.TreeItem<String>", "Integer")));
        FIELD_INFO_MAP.put("tableColumn1", new ControllerFieldInfoImpl("tableColumn1", List.of("javafx.scene.control.TextArea", "Float")));
        FIELD_INFO_MAP.put("tableColumn2", new ControllerFieldInfoImpl("tableColumn2", List.of("javafx.scene.control.TextArea", "String")));
    }

    private final Generator generator;

    TestGeneratorImpl() {
        this.generator = new GeneratorImpl();
    }

    @ParameterizedTest
    @MethodSource("providesGenerationTestCases")
    void testGenerate(final String file, final ControllerFieldInjectionTypes field, final ControllerMethodsInjectionType method, final ResourceBundleInjectionTypes bundle) throws Exception {
        final var request = getRequest(file, field, method, bundle);
        final var path = Paths.get(getPath(file, field, method, bundle));
        try (final var in = getClass().getResourceAsStream("/com/github/gtache/fxml/compiler/parsing/xml/" + path)) {
            assertNotNull(in);
            final var expected = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            final var actual = generator.generate(request);
            assertEquals(expected, actual);
        } catch (final IOException | GenerationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) throws GenerationException, IOException, ParseException {
        final var generator = new GeneratorImpl();
        final var files = List.of("Controls", "Includes");
        for (final var file : files) {
            for (final var field : ControllerFieldInjectionTypes.values()) {
                for (final var method : ControllerMethodsInjectionType.values()) {
                    for (final var bundle : ResourceBundleInjectionTypes.values()) {
                        final var request = getRequest(file, field, method, bundle);
                        final var content = generator.generate(request);
                        final var path = Paths.get(getPath(file, field, method, bundle));
                        Files.writeString(path, content);
                    }
                }
            }
        }
    }

    private static String getPath(final String file, final ControllerFieldInjectionTypes field, final ControllerMethodsInjectionType method, final ResourceBundleInjectionTypes bundle) {
        return "expected-" + file.toLowerCase() + "-" + field.name().toLowerCase() + "-" + method.name().toLowerCase() + "-" + bundle.name().replace("_", "").toLowerCase() + ".txt";
    }

    private static GenerationRequest getRequest(final String file, final ControllerFieldInjectionTypes field, final ControllerMethodsInjectionType method, final ResourceBundleInjectionTypes bundle) throws IOException, ParseException {
        final var controlsControllerInfo = new ControllerInfoImpl(Map.of("keyPressed", false, "mouseClicked", false),
                FIELD_INFO_MAP);
        final var includesControllerInfo = new ControllerInfoImpl(Map.of(), Map.of());
        final var controllerInfo = file.equals("Controls") ? controlsControllerInfo : includesControllerInfo;
        final var resourceBundlePath = "com.github.gtache.fxml.compiler.parsing.xml." + file + "Bundle";
        final var viewPath = "/com/github/gtache/fxml/compiler/parsing/xml/" + file + "View.fxml";
        final var parser = new DOMFXMLParser();
        try (final var in = TestGeneratorImpl.class.getResourceAsStream(viewPath)) {
            assertNotNull(in);
            final var content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            final var root = parser.parse(content);
            return new GenerationRequestImpl(
                    new GenerationParametersImpl(Map.of(
                            "com.github.gtache.fxml.compiler.parsing.xml.ControlsController", new ControllerInjectionImpl(field, method,
                                    "com.github.gtache.fxml.compiler.parsing.xml.ControlsController"),
                            "com.github.gtache.fxml.compiler.parsing.xml.IncludesController", new ControllerInjectionImpl(field, method,
                                    "com.github.gtache.fxml.compiler.parsing.xml.IncludesController")),
                            Map.of("controlsView.fxml", "com.github.gtache.fxml.compiler.parsing.xml.ControlsView"),
                            Map.of("controlsView.fxml", "com.github.gtache.fxml.compiler.parsing.xml.ControlsController"),
                            new ResourceBundleInjectionImpl(bundle, resourceBundlePath)
                    ),
                    controllerInfo,
                    root,
                    "com.github.gtache.fxml.compiler.parsing.xml." + file + "View"
            );
        }
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
