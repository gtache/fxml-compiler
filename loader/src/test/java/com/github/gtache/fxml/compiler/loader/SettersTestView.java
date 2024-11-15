package com.github.gtache.fxml.compiler.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Generated code, not thread-safe
 */
public final class SettersTestView {

    private final Map<Class<?>, Object> controllersMap;
    private final Map<Class<?>, ResourceBundle> resourceBundlesMap;
    private final Map<String, Class<?>> includeControllersMap;
    private boolean loaded;
    private com.github.gtache.fxml.compiler.loader.TestController controller;

    /**
     * Instantiates a new SettersTestView with no nested controllers and no resource bundle
     *
     * @param controller The controller
     */
    public SettersTestView(final com.github.gtache.fxml.compiler.loader.TestController controller) {
        this(Map.of(com.github.gtache.fxml.compiler.loader.TestController.class, controller), Map.of(), Map.of());
    }

    /**
     * Instantiates a new SettersTestView with no nested controllers
     *
     * @param controller     The controller
     * @param resourceBundle The resource bundle
     */
    public SettersTestView(final com.github.gtache.fxml.compiler.loader.TestController controller, final ResourceBundle resourceBundle) {
        this(Map.of(com.github.gtache.fxml.compiler.loader.TestController.class, controller), Map.of(), Map.of(com.github.gtache.fxml.compiler.loader.TestController.class, resourceBundle));
    }

    /**
     * Instantiates a new SettersTestView with nested controllers and no resource bundle
     *
     * @param controllersMap        The map of controller class to controller
     * @param includeControllersMap The map of source to controller class
     */
    public SettersTestView(final Map<Class<?>, Object> controllersMap, final Map<String, Class<?>> includeControllersMap) {
        this(controllersMap, includeControllersMap, Map.of());
    }

    /**
     * Instantiates a new SettersTestView with nested controllers
     *
     * @param controllersMap        The map of controller class to controller
     * @param includeControllersMap The map of source to controller class
     * @param resourceBundlesMap    The map of controller class to resource bundle
     */
    public SettersTestView(final Map<Class<?>, Object> controllersMap, final Map<String, Class<?>> includeControllersMap, final Map<Class<?>, ResourceBundle> resourceBundlesMap) {
        this.controllersMap = Map.copyOf(controllersMap);
        this.includeControllersMap = Map.copyOf(includeControllersMap);
        this.resourceBundlesMap = Map.copyOf(resourceBundlesMap);
    }

    public javafx.scene.Parent load() {
        if (loaded) {
            throw new IllegalStateException("Already loaded");
        }
        final var bundle = resourceBundlesMap.get(com.github.gtache.fxml.compiler.loader.TestController.class);
        controller = (com.github.gtache.fxml.compiler.loader.TestController) controllersMap.get(com.github.gtache.fxml.compiler.loader.TestController.class);
        final var object0 = new javafx.scene.layout.BorderPane();
        final var object1 = new javafx.scene.layout.VBox();
        javafx.scene.layout.BorderPane.setAlignment(object1, javafx.geometry.Pos.CENTER);
        final var object2 = new javafx.scene.layout.HBox();
        object2.setAlignment(javafx.geometry.Pos.CENTER);
        object2.setSpacing(10.0);
        final var object3 = new javafx.scene.control.Slider();
        controller.setPlaySlider(object3);
        javafx.scene.layout.HBox.setHgrow(object3, javafx.scene.layout.Priority.ALWAYS);
        final var object4 = new javafx.geometry.Insets(0, 0, 0, 10.0);
        object3.setPadding(object4);
        final var object5 = new javafx.scene.control.Label();
        controller.setPlayLabel(object5);
        object5.setText("Label");
        final var object6 = new javafx.geometry.Insets(0, 10.0, 0, 0);
        object5.setPadding(object6);
        object2.getChildren().addAll(object3, object5);
        final var object7 = new javafx.geometry.Insets(10.0, 0, 0, 0);
        object2.setPadding(object7);
        final var object8 = new javafx.scene.layout.HBox();
        object8.setAlignment(javafx.geometry.Pos.CENTER);
        object8.setSpacing(10.0);
        final var object9 = new javafx.scene.control.Button();
        controller.setPlayButton(object9);
        object9.setMnemonicParsing(false);
        object9.setOnAction(controller::playPressed);
        final var object10 = new javafx.geometry.Insets(0, 20.0, 0, 0);
        javafx.scene.layout.HBox.setMargin(object9, object10);
        final var object11 = new javafx.scene.control.Label();
        object11.setText(bundle.getString("media.volume.label"));
        final var object12 = new javafx.scene.control.Slider();
        controller.setVolumeSlider(object12);
        object12.setValue(100);
        final var object13 = new javafx.scene.control.Label();
        controller.setVolumeValueLabel(object13);
        object13.setText("Label");
        final var class0 = includeControllersMap.get("includeView.fxml");
        final var map0 = new HashMap<>(resourceBundlesMap);
        final var bundle0 = ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle");
        map0.put(class0, bundle0);
        final var view0 = new com.github.gtache.fxml.compiler.loader.SettersIncludeView(controllersMap, includeControllersMap, map0);
        final var object14 = view0.load();
        final var controller0 = view0.controller();
        controller.setIncludeController(controller0);
        object8.getChildren().addAll(object9, object11, object12, object13, object14);
        final var object15 = new javafx.geometry.Insets(10.0, 10.0, 10.0, 10.0);
        object8.setPadding(object15);
        object1.getChildren().addAll(object2, object8);
        object0.setBottom(object1);
        final var object16 = new javafx.scene.layout.VBox();
        controller.setVbox(object16);
        final var object17 = new javafx.scene.control.ToolBar();
        controller.setToolBar(object17);
        final var object18 = new javafx.scene.control.TitledPane();
        controller.setTitledPane(object18);
        final var object19 = new javafx.scene.layout.TilePane();
        controller.setTilePane(object19);
        final var object20 = new javafx.scene.text.TextFlow();
        controller.setTextFlow(object20);
        final var object21 = new javafx.scene.control.TabPane();
        controller.setTabPane(object21);
        final var object22 = new javafx.scene.control.Tab();
        controller.setTab(object22);
        final var object23 = new javafx.scene.layout.StackPane();
        controller.setStackPane(object23);
        final var object24 = new javafx.scene.control.SplitPane();
        controller.setSplitPane(object24);
        final var object25 = new javafx.scene.control.ScrollPane();
        controller.setScrollPane(object25);
        final var object26 = new javafx.scene.layout.Pane();
        controller.setPane(object26);
        final var object27 = new javafx.scene.layout.HBox();
        controller.setHbox(object27);
        final var object28 = new javafx.scene.Group();
        controller.setGroup(object28);
        final var object29 = new javafx.scene.layout.GridPane();
        controller.setGridPane(object29);
        final var object30 = new javafx.scene.layout.ColumnConstraints();
        controller.setColumnConstraints(object30);
        object30.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        object30.setMinWidth(10.0);
        object29.getColumnConstraints().addAll(object30);
        final var object31 = new javafx.scene.layout.RowConstraints();
        object31.setMinHeight(10.0);
        object31.setVgrow(javafx.scene.layout.Priority.SOMETIMES);
        object29.getRowConstraints().addAll(object31);
        final var object32 = new javafx.scene.layout.FlowPane();
        controller.setFlowPane(object32);
        final var object33 = new javafx.scene.control.DialogPane();
        controller.setDialogPane(object33);
        final var object34 = new javafx.scene.control.ButtonBar();
        controller.setButtonBar(object34);
        final var object35 = new javafx.scene.layout.AnchorPane();
        controller.setAnchorPane(object35);
        final var object36 = new javafx.scene.control.Label();
        object36.setManaged(false);
        object35.getChildren().addAll(object36);
        object34.getButtons().addAll(object35);
        object33.setContent(object34);
        object32.getChildren().addAll(object33);
        object29.getChildren().addAll(object32);
        object28.getChildren().addAll(object29);
        object27.getChildren().addAll(object28);
        object26.getChildren().addAll(object27);
        object25.setContent(object26);
        object24.getItems().addAll(object25);
        object23.getChildren().addAll(object24);
        object22.setContent(object23);
        object21.getTabs().addAll(object22);
        object20.getChildren().addAll(object21);
        object19.getChildren().addAll(object20);
        object18.setContent(object19);
        object17.getItems().addAll(object18);
        object16.getChildren().addAll(object17);
        object0.setCenter(object16);
        controller.initialize();
        loaded = true;
        return object0;
    }


    /**
     * @return The controller
     */
    public com.github.gtache.fxml.compiler.loader.TestController controller() {
        if (loaded) {
            return controller;
        } else {
            throw new IllegalStateException("Not loaded");
        }
    }
}