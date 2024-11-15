package com.github.gtache.fxml.compiler.loader;

import javafx.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Generated code, not thread-safe
 */
public final class ReflectionIncludeView {

    private final Map<Class<?>, Object> controllersMap;
    private final Map<Class<?>, ResourceBundle> resourceBundlesMap;
    private final Map<String, Class<?>> includeControllersMap;
    private boolean loaded;
    private com.github.gtache.fxml.compiler.loader.IncludeController controller;

    /**
     * Instantiates a new ReflectionIncludeView with no nested controllers and no resource bundle
     *
     * @param controller The controller
     */
    public ReflectionIncludeView(final com.github.gtache.fxml.compiler.loader.IncludeController controller) {
        this(Map.of(com.github.gtache.fxml.compiler.loader.IncludeController.class, controller), Map.of(), Map.of());
    }

    /**
     * Instantiates a new ReflectionIncludeView with no nested controllers
     *
     * @param controller     The controller
     * @param resourceBundle The resource bundle
     */
    public ReflectionIncludeView(final com.github.gtache.fxml.compiler.loader.IncludeController controller, final ResourceBundle resourceBundle) {
        this(Map.of(com.github.gtache.fxml.compiler.loader.IncludeController.class, controller), Map.of(), Map.of(com.github.gtache.fxml.compiler.loader.IncludeController.class, resourceBundle));
    }

    /**
     * Instantiates a new ReflectionIncludeView with nested controllers and no resource bundle
     *
     * @param controllersMap        The map of controller class to controller
     * @param includeControllersMap The map of source to controller class
     */
    public ReflectionIncludeView(final Map<Class<?>, Object> controllersMap, final Map<String, Class<?>> includeControllersMap) {
        this(controllersMap, includeControllersMap, Map.of());
    }

    /**
     * Instantiates a new ReflectionIncludeView with nested controllers
     *
     * @param controllersMap        The map of controller class to controller
     * @param includeControllersMap The map of source to controller class
     * @param resourceBundlesMap    The map of controller class to resource bundle
     */
    public ReflectionIncludeView(final Map<Class<?>, Object> controllersMap, final Map<String, Class<?>> includeControllersMap, final Map<Class<?>, ResourceBundle> resourceBundlesMap) {
        this.controllersMap = Map.copyOf(controllersMap);
        this.includeControllersMap = Map.copyOf(includeControllersMap);
        this.resourceBundlesMap = Map.copyOf(resourceBundlesMap);
    }

    public javafx.scene.Parent load() {
        if (loaded) {
            throw new IllegalStateException("Already loaded");
        }
        final var bundle = resourceBundlesMap.get(com.github.gtache.fxml.compiler.loader.IncludeController.class);
        controller = (com.github.gtache.fxml.compiler.loader.IncludeController) controllersMap.get(com.github.gtache.fxml.compiler.loader.IncludeController.class);
        final var object0 = new javafx.scene.layout.GridPane();
        injectField("gridPane", object0);
        object0.setOnInputMethodTextChanged(e -> callMethod("inputMethodTextChanged", e));
        object0.setOnKeyPressed(e -> callMethod("keyPressed", e));
        object0.setOnKeyReleased(e -> callMethod("keyReleased", e));
        object0.setOnKeyTyped(e -> callMethod("keyTyped", e));
        final var object1 = new javafx.scene.layout.RowConstraints();
        final var object2 = new javafx.scene.layout.RowConstraints();
        final var object3 = new javafx.scene.layout.RowConstraints();
        final var object4 = new javafx.scene.layout.RowConstraints();
        final var object5 = new javafx.scene.layout.RowConstraints();
        final var object6 = new javafx.scene.layout.RowConstraints();
        final var object7 = new javafx.scene.layout.RowConstraints();
        final var object8 = new javafx.scene.layout.RowConstraints();
        final var object9 = new javafx.scene.layout.RowConstraints();
        final var object10 = new javafx.scene.layout.RowConstraints();
        final var object11 = new javafx.scene.layout.RowConstraints();
        final var object12 = new javafx.scene.layout.RowConstraints();
        final var object13 = new javafx.scene.layout.RowConstraints();
        final var object14 = new javafx.scene.layout.RowConstraints();
        final var object15 = new javafx.scene.layout.RowConstraints();
        final var object16 = new javafx.scene.layout.RowConstraints();
        final var object17 = new javafx.scene.layout.RowConstraints();
        final var object18 = new javafx.scene.layout.RowConstraints();
        object0.getRowConstraints().addAll(object1, object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, object12, object13, object14, object15, object16, object17, object18);
        final var object19 = new javafx.scene.layout.ColumnConstraints();
        final var object20 = new javafx.scene.layout.ColumnConstraints();
        final var object21 = new javafx.scene.layout.ColumnConstraints();
        object21.setMinWidth(10.0);
        object21.setPrefWidth(100.0);
        object0.getColumnConstraints().addAll(object19, object20, object21);
        final var object22 = new javafx.scene.control.Button();
        injectField("button", object22);
        object22.setMnemonicParsing(false);
        object22.setText("Button");
        final var object23 = new javafx.scene.control.CheckBox();
        injectField("checkBox", object23);
        object23.setIndeterminate(true);
        object23.setMnemonicParsing(false);
        object23.setText("CheckBox");
        javafx.scene.layout.GridPane.setColumnIndex(object23, 1);
        final var object24 = new javafx.scene.control.ChoiceBox<>();
        injectField("choiceBox", object24);
        object24.setCacheShape(false);
        object24.setCenterShape(false);
        object24.setDisable(true);
        object24.setFocusTraversable(false);
        object24.setPrefWidth(150.0);
        object24.setScaleShape(false);
        object24.setVisible(false);
        javafx.scene.layout.GridPane.setRowIndex(object24, 1);
        final var object25 = new javafx.scene.control.ColorPicker();
        injectField("colorPicker", object25);
        object25.setNodeOrientation(javafx.geometry.NodeOrientation.LEFT_TO_RIGHT);
        object25.setOpacity(0.5);
        javafx.scene.layout.GridPane.setColumnIndex(object25, 1);
        javafx.scene.layout.GridPane.setRowIndex(object25, 1);
        final var object26 = new javafx.scene.paint.Color(0.7894737124443054, 0.08771929889917374, 0.08771929889917374, 1);
        injectField("color", object26);
        object25.setValue(object26);
        final var object27 = new javafx.geometry.Insets(5.0, 4.0, 3.0, 2.0);
        object25.setOpaqueInsets(object27);
        final var object28 = new javafx.scene.control.ComboBox<>();
        injectField("comboBox", object28);
        object28.setEditable(true);
        object28.setPrefWidth(150.0);
        object28.setPromptText("Text");
        object28.setVisibleRowCount(5);
        javafx.scene.layout.GridPane.setRowIndex(object28, 2);
        final var object29 = javafx.scene.Cursor.CLOSED_HAND;
        object28.setCursor(object29);
        final var object30 = new javafx.scene.effect.Bloom();
        object28.setEffect(object30);
        final var object31 = new javafx.scene.control.DatePicker();
        injectField("datePicker", object31);
        object31.setShowWeekNumbers(true);
        object31.setStyle("-fx-background-color: #ffffff;");
        javafx.scene.layout.GridPane.setColumnIndex(object31, 1);
        javafx.scene.layout.GridPane.setRowIndex(object31, 2);
        final var object32 = new javafx.scene.web.HTMLEditor();
        injectField("htmlEditor", object32);
        object32.setHtmlText("<html><head></head><body contenteditable=\"true\"></body></html>");
        object32.setPrefHeight(200.0);
        object32.setPrefWidth(506.0);
        object32.getStyleClass().addAll("clazz");
        object32.getStylesheets().addAll("@style.css");
        javafx.scene.layout.GridPane.setRowIndex(object32, 3);
        final var object33 = new javafx.scene.control.Hyperlink();
        injectField("hyperlink", object33);
        object33.setText("Hyperlink");
        javafx.scene.layout.GridPane.setColumnIndex(object33, 1);
        javafx.scene.layout.GridPane.setRowIndex(object33, 3);
        final var object34 = new javafx.scene.image.ImageView();
        injectField("imageView", object34);
        object34.setFitHeight(150.0);
        object34.setFitWidth(200.0);
        object34.setPickOnBounds(true);
        object34.setPreserveRatio(true);
        javafx.scene.layout.GridPane.setRowIndex(object34, 4);
        final var object35 = new javafx.scene.control.Label();
        injectField("label", object35);
        object35.setAccessibleHelp("TTTTT");
        object35.setAccessibleText("TTT");
        object35.setBlendMode(javafx.scene.effect.BlendMode.ADD);
        object35.setCache(true);
        object35.setCacheHint(javafx.scene.CacheHint.QUALITY);
        object35.setDepthTest(javafx.scene.DepthTest.ENABLE);
        object35.setMnemonicParsing(true);
        object35.setMouseTransparent(true);
        object35.setText(bundle.getString("include.label"));
        javafx.scene.layout.GridPane.setColumnIndex(object35, 1);
        javafx.scene.layout.GridPane.setRowIndex(object35, 4);
        final var object36 = new javafx.scene.control.ListView<>();
        injectField("listView", object36);
        object36.setFixedCellSize(20.0);
        object36.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        object36.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        object36.setPrefHeight(200.0);
        object36.setPrefWidth(200.0);
        javafx.scene.layout.GridPane.setRowIndex(object36, 5);
        final var object37 = new javafx.scene.media.MediaView();
        injectField("mediaView", object37);
        object37.setFitHeight(200.0);
        object37.setFitWidth(200.0);
        javafx.scene.layout.GridPane.setColumnIndex(object37, 1);
        javafx.scene.layout.GridPane.setColumnSpan(object37, 2);
        javafx.scene.layout.GridPane.setRowIndex(object37, 5);
        javafx.scene.layout.GridPane.setRowSpan(object37, 2);
        final var object38 = new javafx.scene.control.MenuBar();
        injectField("menuBar", object38);
        javafx.scene.layout.GridPane.setHalignment(object38, javafx.geometry.HPos.RIGHT);
        javafx.scene.layout.GridPane.setHgrow(object38, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.GridPane.setRowIndex(object38, 7);
        javafx.scene.layout.GridPane.setValignment(object38, javafx.geometry.VPos.BASELINE);
        javafx.scene.layout.GridPane.setVgrow(object38, javafx.scene.layout.Priority.SOMETIMES);
        final var object39 = new javafx.scene.control.Menu();
        injectField("menu1", object39);
        object39.setMnemonicParsing(false);
        object39.setText("File");
        final var object40 = new javafx.scene.control.MenuItem();
        injectField("menuItem1", object40);
        object40.setMnemonicParsing(false);
        object40.setText("Close");
        object39.getItems().addAll(object40);
        final var object41 = new javafx.scene.control.Menu();
        object41.setMnemonicParsing(false);
        object41.setText("Edit");
        final var object42 = new javafx.scene.control.MenuItem();
        object42.setMnemonicParsing(false);
        object42.setText("Delete");
        object41.getItems().addAll(object42);
        final var object43 = new javafx.scene.control.Menu();
        object43.setMnemonicParsing(false);
        object43.setText("Help");
        final var object44 = new javafx.scene.control.MenuItem();
        object44.setMnemonicParsing(false);
        object44.setText("About");
        object43.getItems().addAll(object44);
        object38.getMenus().addAll(object39, object41, object43);
        final var object45 = new javafx.scene.control.MenuButton();
        injectField("menuButton", object45);
        object45.setMnemonicParsing(false);
        object45.setText("MenuButton");
        javafx.scene.layout.GridPane.setColumnIndex(object45, 1);
        javafx.scene.layout.GridPane.setRowIndex(object45, 7);
        final var object46 = new javafx.scene.control.MenuItem();
        object46.setMnemonicParsing(false);
        object46.setText("Action 1");
        final var object47 = new javafx.scene.control.MenuItem();
        object47.setMnemonicParsing(false);
        object47.setText("Action 2");
        object45.getItems().addAll(object46, object47);
        final var object48 = new javafx.geometry.Insets(5.0, 4.0, 3.0, 2.0);
        javafx.scene.layout.GridPane.setMargin(object45, object48);
        final var object49 = new javafx.scene.control.Pagination();
        injectField("pagination", object49);
        object49.setPrefHeight(200.0);
        object49.setPrefWidth(200.0);
        javafx.scene.layout.GridPane.setRowIndex(object49, 8);
        final var object50 = new javafx.geometry.Insets(5.0, 4.0, 3.0, 2.0);
        object49.setPadding(object50);
        final var object51 = new javafx.scene.control.PasswordField();
        injectField("passwordField", object51);
        object51.setMaxHeight(6.0);
        object51.setMaxWidth(5.0);
        object51.setMinHeight(2.0);
        object51.setMinWidth(1.0);
        object51.setPrefColumnCount(7);
        object51.setPrefHeight(4.0);
        object51.setPrefWidth(3.0);
        javafx.scene.layout.GridPane.setColumnIndex(object51, 1);
        javafx.scene.layout.GridPane.setRowIndex(object51, 8);
        final var object52 = new javafx.scene.control.ProgressBar();
        injectField("progressBar", object52);
        object52.setLayoutX(10.0);
        object52.setLayoutY(20.0);
        object52.setPrefWidth(200.0);
        object52.setProgress(0.0);
        javafx.scene.layout.GridPane.setRowIndex(object52, 9);
        final var object53 = new javafx.scene.control.ProgressIndicator();
        injectField("progressIndicator", object53);
        object53.setProgress(0.0);
        object53.setRotate(2.0);
        javafx.scene.layout.GridPane.setColumnIndex(object53, 1);
        javafx.scene.layout.GridPane.setRowIndex(object53, 9);
        final var object54 = new javafx.geometry.Point3D(4.0, 5.0, 6.0);
        object53.setRotationAxis(object54);
        final var object55 = new javafx.scene.control.RadioButton();
        injectField("radioButton", object55);
        object55.setMnemonicParsing(false);
        object55.setScaleX(7.0);
        object55.setScaleY(2.0);
        object55.setScaleZ(3.0);
        object55.setText("RadioButton");
        object55.setTranslateX(4.0);
        object55.setTranslateY(5.0);
        object55.setTranslateZ(6.0);
        javafx.scene.layout.GridPane.setRowIndex(object55, 10);
        final var object56 = new javafx.scene.control.ScrollBar();
        injectField("scrollBarH", object56);
        javafx.scene.layout.GridPane.setColumnIndex(object56, 1);
        javafx.scene.layout.GridPane.setRowIndex(object56, 10);
        final var object57 = new javafx.scene.control.ScrollBar();
        injectField("scrollBarV", object57);
        object57.setOrientation(javafx.geometry.Orientation.VERTICAL);
        javafx.scene.layout.GridPane.setRowIndex(object57, 11);
        final var object58 = new javafx.scene.control.Separator();
        injectField("separatorH", object58);
        object58.setOnDragDetected(e -> callMethod("dragDetected", e));
        object58.setOnDragDone(e -> callMethod("dragDone", e));
        object58.setOnDragDropped(e -> callMethod("dragDropped", e));
        object58.setOnDragEntered(e -> callMethod("dragEntered", e));
        object58.setOnDragExited(e -> callMethod("dragExited", e));
        object58.setOnDragOver(e -> callMethod("dragOver", e));
        object58.setOnMouseDragEntered(e -> callMethod("mouseDragEntered", e));
        object58.setOnMouseDragExited(e -> callMethod("mouseDragExited", e));
        object58.setOnMouseDragOver(e -> callMethod("mouseDragOver", e));
        object58.setOnMouseDragReleased(e -> callMethod("mouseDragReleased", e));
        object58.setPrefWidth(200.0);
        javafx.scene.layout.GridPane.setColumnIndex(object58, 1);
        javafx.scene.layout.GridPane.setRowIndex(object58, 11);
        final var object59 = new javafx.scene.control.Separator();
        injectField("separatorV", object59);
        object59.setOrientation(javafx.geometry.Orientation.VERTICAL);
        object59.setPrefHeight(200.0);
        javafx.scene.layout.GridPane.setRowIndex(object59, 12);
        final var object60 = new javafx.scene.control.Slider();
        injectField("sliderH", object60);
        object60.setOnContextMenuRequested(e -> callMethod("contextMenuRequested", e));
        object60.setOnMouseClicked(e -> callMethod("mouseClicked", e));
        object60.setOnMouseDragged(e -> callMethod("mouseDragged", e));
        object60.setOnMouseEntered(e -> callMethod("mouseEntered", e));
        object60.setOnMouseExited(e -> callMethod("mouseExited", e));
        object60.setOnMouseMoved(e -> callMethod("mouseMoved", e));
        object60.setOnMousePressed(e -> callMethod("mousePressed", e));
        object60.setOnMouseReleased(e -> callMethod("mouseReleased", e));
        object60.setOnScroll(e -> callMethod("onScroll", e));
        object60.setOnScrollFinished(e -> callMethod("onScrollFinished", e));
        object60.setOnScrollStarted(e -> callMethod("onScrollStarted", e));
        javafx.scene.layout.GridPane.setColumnIndex(object60, 1);
        javafx.scene.layout.GridPane.setRowIndex(object60, 12);
        final var object61 = new javafx.scene.control.Slider();
        injectField("sliderV", object61);
        object61.setOnZoom(e -> callMethod("onZoom", e));
        object61.setOnZoomFinished(e -> callMethod("onZoomFinished", e));
        object61.setOnZoomStarted(e -> callMethod("onZoomStarted", e));
        object61.setOrientation(javafx.geometry.Orientation.VERTICAL);
        javafx.scene.layout.GridPane.setRowIndex(object61, 13);
        final var object62 = new javafx.scene.control.Spinner<>();
        injectField("spinner", object62);
        javafx.scene.layout.GridPane.setColumnIndex(object62, 1);
        javafx.scene.layout.GridPane.setRowIndex(object62, 13);
        final var object63 = new javafx.scene.control.SplitMenuButton();
        injectField("splitMenuButton", object63);
        object63.setMnemonicParsing(false);
        object63.setText("SplitMenuButton");
        javafx.scene.layout.GridPane.setRowIndex(object63, 14);
        final var object64 = new javafx.scene.control.MenuItem();
        injectField("item1", object64);
        object64.setMnemonicParsing(false);
        object64.setText("Action 1");
        final var object65 = new javafx.scene.control.MenuItem();
        injectField("item2", object65);
        object65.setMnemonicParsing(false);
        object65.setText("Action 2");
        object63.getItems().addAll(object64, object65);
        final var object66 = new javafx.scene.control.TableView<>();
        injectField("tableView", object66);
        object66.setPrefHeight(200.0);
        object66.setPrefWidth(200.0);
        javafx.scene.layout.GridPane.setColumnIndex(object66, 1);
        javafx.scene.layout.GridPane.setRowIndex(object66, 14);
        final var object67 = new javafx.scene.control.TableColumn<>();
        injectField("tableColumn1", object67);
        object67.setPrefWidth(75.0);
        object67.setText("C1");
        final var object68 = new javafx.scene.control.TableColumn<>();
        injectField("tableColumn2", object68);
        object68.setPrefWidth(75.0);
        object68.setText("C2");
        object66.getColumns().addAll(object67, object68);
        final var object69 = new javafx.scene.control.TextArea();
        injectField("textArea", object69);
        object69.setPrefHeight(200.0);
        object69.setPrefWidth(200.0);
        javafx.scene.layout.GridPane.setRowIndex(object69, 15);
        final var object70 = new javafx.scene.control.TextField();
        injectField("textField", object70);
        javafx.scene.layout.GridPane.setColumnIndex(object70, 1);
        javafx.scene.layout.GridPane.setRowIndex(object70, 15);
        final var object71 = new javafx.scene.control.ToggleButton();
        object71.setMnemonicParsing(false);
        object71.setOnAction(e -> callMethod("onAction", e));
        object71.setOnRotate(e -> callMethod("onRotate", e));
        object71.setOnRotationFinished(e -> callMethod("onRotationFinished", e));
        object71.setOnRotationStarted(e -> callMethod("onRotationStarted", e));
        object71.setText("ToggleButton");
        javafx.scene.layout.GridPane.setRowIndex(object71, 16);
        final var object72 = new javafx.scene.control.TreeTableView<>();
        injectField("treeTableView", object72);
        object72.setPrefHeight(200.0);
        object72.setPrefWidth(200.0);
        javafx.scene.layout.GridPane.setColumnIndex(object72, 1);
        javafx.scene.layout.GridPane.setRowIndex(object72, 16);
        final var object73 = new javafx.scene.control.TreeTableColumn<>();
        injectField("treeTableColumn1", object73);
        object73.setOnEditCancel(e -> callMethod("onEditCancel", e));
        object73.setOnEditCommit(e -> callMethod("onEditCommit", e));
        object73.setOnEditStart(e -> callMethod("onEditStart", e));
        object73.setPrefWidth(75.0);
        object73.setText("C1");
        final var object74 = new javafx.scene.control.TreeTableColumn<>();
        injectField("treeTableColumn2", object74);
        object74.setPrefWidth(75.0);
        object74.setSortType(javafx.scene.control.TreeTableColumn.SortType.DESCENDING);
        object74.setText("C2");
        object72.getColumns().addAll(object73, object74);
        final var object75 = new javafx.scene.control.TreeView<>();
        injectField("treeView", object75);
        object75.setOnSwipeDown(e -> callMethod("onSwipeDown", e));
        object75.setOnSwipeLeft(e -> callMethod("onSwipeLeft", e));
        object75.setOnSwipeRight(e -> callMethod("onSwipeRight", e));
        object75.setOnSwipeUp(e -> callMethod("onSwipeUp", e));
        object75.setPrefHeight(200.0);
        object75.setPrefWidth(200.0);
        javafx.scene.layout.GridPane.setRowIndex(object75, 17);
        final var object76 = new javafx.scene.web.WebView();
        injectField("webView", object76);
        object76.setOnTouchMoved(e -> callMethod("onTouchMoved", e));
        object76.setOnTouchPressed(e -> callMethod("onTouchPressed", e));
        object76.setOnTouchReleased(e -> callMethod("onTouchReleased", e));
        object76.setOnTouchStationary(e -> callMethod("onTouchStationary", e));
        object76.setPrefHeight(200.0);
        object76.setPrefWidth(200.0);
        javafx.scene.layout.GridPane.setColumnIndex(object76, 1);
        javafx.scene.layout.GridPane.setRowIndex(object76, 17);
        object0.getChildren().addAll(object22, object23, object24, object25, object28, object31, object32, object33, object34, object35, object36, object37, object38, object45, object49, object51, object52, object53, object55, object56, object57, object58, object59, object60, object61, object62, object63, object66, object69, object70, object71, object72, object75, object76);
        try {
            final var initialize = controller.getClass().getDeclaredMethod("initialize");
            initialize.setAccessible(true);
            initialize.invoke(controller);
        } catch (final InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Error using reflection", e);
        } catch (final NoSuchMethodException ignored) {
        }
        loaded = true;
        return object0;
    }

    private <T extends Event> void callMethod(final String methodName, final T event) {
        try {
            final Method method;
            final var methods = Arrays.stream(controller.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equals(methodName)).toList();
            if (methods.size() > 1) {
                final var eventMethods = methods.stream().filter(m ->
                        m.getParameterCount() == 1 && Event.class.isAssignableFrom(m.getParameterTypes()[0])).toList();
                if (eventMethods.size() == 1) {
                    method = eventMethods.getFirst();
                } else {
                    final var emptyMethods = methods.stream().filter(m -> m.getParameterCount() == 0).toList();
                    if (emptyMethods.size() == 1) {
                        method = emptyMethods.getFirst();
                    } else {
                        throw new IllegalArgumentException("Multiple matching methods for " + methodName);
                    }
                }
            } else if (methods.size() == 1) {
                method = methods.getFirst();
            } else {
                throw new IllegalArgumentException("No matching method for " + methodName);
            }
            method.setAccessible(true);
            if (method.getParameterCount() == 0) {
                method.invoke(controller);
            } else {
                method.invoke(controller, event);
            }
        } catch (final IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Error using reflection on " + methodName, ex);
        }
    }

    private <T> void injectField(final String fieldName, final T object) {
        try {
            final var field = controller.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, object);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error using reflection on " + fieldName, e);
        }
    }

    /**
     * @return The controller
     */
    public com.github.gtache.fxml.compiler.loader.IncludeController controller() {
        if (loaded) {
            return controller;
        } else {
            throw new IllegalStateException("Not loaded");
        }
    }
}
