package com.github.gtache.fxml.compiler.loader;

import com.github.gtache.fxml.compiler.impl.ControllerFieldInjectionTypes;
import com.github.gtache.fxml.compiler.impl.ControllerInfoImpl;
import com.github.gtache.fxml.compiler.impl.ControllerInjectionImpl;
import com.github.gtache.fxml.compiler.impl.ControllerMethodsInjectionType;
import com.github.gtache.fxml.compiler.impl.GenerationParametersImpl;
import com.github.gtache.fxml.compiler.impl.GenerationRequestImpl;
import com.github.gtache.fxml.compiler.impl.GeneratorImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionImpl;
import com.github.gtache.fxml.compiler.impl.ResourceBundleInjectionTypes;
import com.github.gtache.fxml.compiler.parsing.listener.ParsingLoadListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TestApp extends Application {
    @Override
    public void start(final Stage primaryStage) throws Exception {
        final var loader = new FXMLLoader(getClass().getResource("testView.fxml"));
        final var listener = new ParsingLoadListener();
        loader.setLoadListener(listener);
        loader.setResources(ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.TestBundle"));
        loader.load();
        final var genericTypes = new HashMap<String, List<String>>();
        genericTypes.put("choiceBox", List.of("String"));
        genericTypes.put("listView", List.of("javafx.scene.control.Label"));
        genericTypes.put("spinner", List.of("Double"));
        genericTypes.put("tableView", List.of("javafx.scene.control.TextArea"));
        genericTypes.put("treeView", List.of("String"));
        genericTypes.put("treeTableView", List.of("javafx.scene.control.TreeItem<String>"));
        genericTypes.put("treeTableColumn1", List.of("javafx.scene.control.TreeItem<String>", "String"));
        genericTypes.put("treeTableColumn2", List.of("javafx.scene.control.TreeItem<String>", "Integer"));
        genericTypes.put("tableColumn1", List.of("javafx.scene.control.TextArea", "Float"));
        genericTypes.put("tableColumn2", List.of("javafx.scene.control.TextArea", "String"));
        final var includeControllerInfo = new ControllerInfoImpl(Map.of("keyPressed", false, "mouseClicked", false),
                genericTypes);
        final var parameters = new GenerationParametersImpl(Map.of(
                "com.github.gtache.fxml.compiler.loader.IncludeController", new ControllerInjectionImpl(ControllerFieldInjectionTypes.REFLECTION, ControllerMethodsInjectionType.REFLECTION,
                        "com.github.gtache.fxml.compiler.loader.IncludeController"),
                "com.github.gtache.fxml.compiler.loader.TestController", new ControllerInjectionImpl(ControllerFieldInjectionTypes.REFLECTION, ControllerMethodsInjectionType.REFLECTION,
                        "com.github.gtache.fxml.compiler.loader.TestController")),
                Map.of("includeView.fxml", "com.github.gtache.fxml.compiler.loader.ReflectionIncludeView"),
                Map.of("includeView.fxml", "com.github.gtache.fxml.compiler.loader.IncludeController"),
                new ResourceBundleInjectionImpl(ResourceBundleInjectionTypes.CONSTRUCTOR, "com.github.gtache.fxml.compiler.loader.TestBundle"));
        final var request = new GenerationRequestImpl(parameters, includeControllerInfo, listener.root(), "com.github.gtache.fxml.compiler.loader.ReflectionTestView");
        System.out.println(new GeneratorImpl().generate(request));
        Platform.exit();
    }
}
