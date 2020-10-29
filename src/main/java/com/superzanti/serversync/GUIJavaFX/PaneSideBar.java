package com.superzanti.serversync.GUIJavaFX;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class PaneSideBar extends VBox {
    public PaneSideBar(){
        Button btnSync = new Button("Sync");
        btnSync.setPrefWidth(125);
        btnSync.getStyleClass().add("sidebar-button");
        Image syncIcon = new Image(getClass().getResourceAsStream("/sidebar/sync_blue.png"));
        ImageView syncIconView = new ImageView(syncIcon);
        syncIconView.setFitHeight(18);
        syncIconView.setFitWidth(18);
        syncIconView.getStyleClass().add("sidebar-icon");
        btnSync.setGraphic(syncIconView);
        btnSync.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Gui_JavaFX.getStackMainPane().displayPanel(0);
            }
        });

        Button btnLogs = new Button("Logs");
        btnLogs.setPrefWidth(125);
        btnLogs.getStyleClass().add("sidebar-button");
        Image logsIcon = new Image(getClass().getResourceAsStream("/sidebar/desktop_blue.png"));
        ImageView logsIconView = new ImageView(logsIcon);
        logsIconView.setFitHeight(18);
        logsIconView.setFitWidth(18);
        btnLogs.setGraphic(logsIconView);
        btnLogs.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Gui_JavaFX.getStackMainPane().displayPanel(1);
            }
        });

        Button btnOptions = new Button("Options");
        btnOptions.setPrefWidth(125);
        btnOptions.getStyleClass().add("sidebar-button");
        Image optionsIcon = new Image(getClass().getResourceAsStream("/sidebar/build_blue.png"));
        ImageView optionsIconView = new ImageView(optionsIcon);
        optionsIconView.setFitHeight(18);
        optionsIconView.setFitWidth(18);
        btnOptions.setGraphic(optionsIconView);
        btnOptions.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Gui_JavaFX.getStackMainPane().displayPanel(2);
            }
        });

        this.getStyleClass().add("sidebar-vbx");
        this.getChildren().addAll(btnSync, btnLogs, btnOptions);
    }
}
