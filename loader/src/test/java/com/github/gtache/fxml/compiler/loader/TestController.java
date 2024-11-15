package com.github.gtache.fxml.compiler.loader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

public class TestController {
    @FXML
    StackPane stackPane;
    @FXML
    Slider volumeSlider;
    @FXML
    Label volumeValueLabel;
    @FXML
    Button playButton;
    @FXML
    Label playLabel;
    @FXML
    Slider playSlider;

    @FXML
    IncludeController includeController;
    @FXML
    VBox vbox;
    @FXML
    ToolBar toolBar;
    @FXML
    TitledPane titledPane;
    @FXML
    TilePane tilePane;
    @FXML
    TextFlow textFlow;
    @FXML
    TabPane tabPane;
    @FXML
    Tab tab;
    @FXML
    SplitPane splitPane;
    @FXML
    ScrollPane scrollPane;
    @FXML
    Pane pane;
    @FXML
    HBox hbox;
    @FXML
    Group group;
    @FXML
    GridPane gridPane;
    @FXML
    ColumnConstraints columnConstraints;
    @FXML
    FlowPane flowPane;
    @FXML
    DialogPane dialogPane;
    @FXML
    ButtonBar buttonBar;
    @FXML
    AnchorPane anchorPane;

    @FXML
    void initialize() {

    }

    @FXML
    void playPressed(final ActionEvent actionEvent) {
    }

    public void setStackPane(final StackPane stackPane) {
        this.stackPane = stackPane;
    }

    public void setVolumeSlider(final Slider volumeSlider) {
        this.volumeSlider = volumeSlider;
    }

    public void setVolumeValueLabel(final Label volumeValueLabel) {
        this.volumeValueLabel = volumeValueLabel;
    }

    public void setPlayButton(final Button playButton) {
        this.playButton = playButton;
    }

    public void setPlayLabel(final Label playLabel) {
        this.playLabel = playLabel;
    }

    public void setPlaySlider(final Slider playSlider) {
        this.playSlider = playSlider;
    }

    public void setIncludeController(final IncludeController includeController) {
        this.includeController = includeController;
    }

    public void setVbox(final VBox vbox) {
        this.vbox = vbox;
    }

    public void setToolBar(final ToolBar toolBar) {
        this.toolBar = toolBar;
    }

    public void setTitledPane(final TitledPane titledPane) {
        this.titledPane = titledPane;
    }

    public void setTilePane(final TilePane tilePane) {
        this.tilePane = tilePane;
    }

    public void setTextFlow(final TextFlow textFlow) {
        this.textFlow = textFlow;
    }

    public void setTabPane(final TabPane tabPane) {
        this.tabPane = tabPane;
    }

    public void setTab(final Tab tab) {
        this.tab = tab;
    }

    public void setSplitPane(final SplitPane splitPane) {
        this.splitPane = splitPane;
    }

    public void setScrollPane(final ScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public void setPane(final Pane pane) {
        this.pane = pane;
    }

    public void setHbox(final HBox hbox) {
        this.hbox = hbox;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    public void setGridPane(final GridPane gridPane) {
        this.gridPane = gridPane;
    }

    public void setColumnConstraints(final ColumnConstraints columnConstraints) {
        this.columnConstraints = columnConstraints;
    }

    public void setFlowPane(final FlowPane flowPane) {
        this.flowPane = flowPane;
    }

    public void setDialogPane(final DialogPane dialogPane) {
        this.dialogPane = dialogPane;
    }

    public void setButtonBar(final ButtonBar buttonBar) {
        this.buttonBar = buttonBar;
    }

    public void setAnchorPane(final AnchorPane anchorPane) {
        this.anchorPane = anchorPane;
    }
}
