package com.superzanti.serversync.GUIJavaFX;

import com.superzanti.serversync.client.ClientWorker;
import com.superzanti.serversync.config.Mod;
import com.superzanti.serversync.config.SyncConfig;
import com.superzanti.serversync.util.enums.EValid;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

// GUI of the SYNC panel (Field ip/port, button "sync" and "check for updates", table with mods)
public class PaneSync extends BorderPane {

    private final SyncConfig config = SyncConfig.getConfig();

    private TableView table;
    private Button btnSync, btnCheckUpdate;
    private TextField fieldIp, fieldPort;
    private final ObservableList<Mod> observMods = FXCollections.observableArrayList();
    private PaneProgressBar paneProgressBar;

    public PaneSync() {
        /*TODO Filter for the table */
        /*
        Label label_filters = new Label("Filters          ");
        Label label_u = new Label("U: ");
        CheckBox cb_u = new CheckBox();
        Label label_i = new Label("I: ");
        CheckBox cb_i = new CheckBox();
        Label label_d = new Label("D: ");
        CheckBox cb_d = new CheckBox();

        HBox hbx_filters = new HBox();
        hbx_filters.setAlignment(Pos.BASELINE_RIGHT);
        hbx_filters.getChildren().addAll(label_filters, label_u, cb_u, label_i, cb_i, label_d, cb_d);
        this.setMargin(hbx_filters, new Insets(10, 10, 0, 0));
        this.setTop(hbx_filters);
        */

        /* Progress bar */
        this.setTop(getPaneProgressBar());

        /* Table with mods */
        setMargin(getTableView(), new Insets(10, 10, 10, 10));
        this.setCenter(getTableView());

        /* Bottom section with IP/Port field and label, button "Sync" and "Check for updates */
        GridPane gp = new GridPane();

        Label label_ip = new Label("IP:");
        GridPane.setRowIndex(label_ip, 0);
        GridPane.setColumnIndex(label_ip, 0);

        GridPane.setRowIndex(getFieldIp(), 1);
        GridPane.setColumnIndex(getFieldIp(), 0);

        Label label_port = new Label("Port:");
        GridPane.setRowIndex(label_port, 0);
        GridPane.setColumnIndex(label_port, 1);

        GridPane.setRowIndex(getFieldPort(), 1);
        GridPane.setColumnIndex(getFieldPort(), 1);

        GridPane.setRowIndex(getBtnSync(), 1);
        GridPane.setColumnIndex(getBtnSync(), 2);

        GridPane.setRowIndex(getBtnCheckUpdate(), 1);
        GridPane.setColumnIndex(getBtnCheckUpdate(), 3);

        gp.getChildren().addAll(label_ip, label_port, getFieldIp(), getFieldPort(), getBtnSync(), getBtnCheckUpdate());
        gp.setAlignment(Pos.CENTER);
        setMargin(gp, new Insets(0, 0, 10, 0));
        this.setBottom(gp);
    }

    public ObservableList<Mod> getObservMods() {
        return observMods;
    }

    public PaneProgressBar getPaneProgressBar() {
        if (paneProgressBar == null) {
            paneProgressBar = new PaneProgressBar();
        }
        return paneProgressBar;
    }

    public TableView getTableView() {
        if (table == null) {

            table = new TableView();
            table.setEditable(true);
            table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            //Create column
            TableColumn<Mod, String> colFileName = new TableColumn<>("File name");
            TableColumn<Mod, String> colStatus = new TableColumn("Status");
            TableColumn<Mod, Boolean> colIgnored = new TableColumn("Ignored");

            colFileName.prefWidthProperty().bind(table.widthProperty().multiply(0.7));
            colFileName.setCellValueFactory(
                    new PropertyValueFactory<>("name"));
            colStatus.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
            colStatus.setCellValueFactory(
                    new PropertyValueFactory<>("status"));
            colIgnored.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
            colIgnored.setCellValueFactory(
                    new PropertyValueFactory<>("ignoreValue"));

            table.getColumns().addAll(colFileName, colStatus);

            /* Change the color of the font text of columns "Status" */
            table.setItems(observMods);
            colStatus.setCellFactory(tc -> {

                TableCell<Mod, String> cell = new TableCell<Mod, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item);
                        if (item == null) {
                            setStyle("");
                        } else if (item.equals(EValid.INVALID.toString())) {
                            setStyle("-fx-text-fill: #db5461;");
                        } else if (item.equals(EValid.OUTDATED.toString())) {
                            setStyle("-fx-text-fill: #dfa06e;");
                        } else if (item.equals(EValid.UPTODATE.toString())) {
                            setStyle("-fx-text-fill: #86ba90;");
                        } else {
                            setStyle("");
                        }
                    }
                };
                return cell;
            });

        }

        return table;
    }

    public Boolean checkIpAndPort(String ip, int port) {
        boolean valid = true;
        if (ip.equals("") && !setPort(port)) {
            updateLogsArea("No config found, requesting details");
            displayAlert("Bad config", "IP field is wrong \nPort out of range, valid range: 1 - 49151");
            valid = false;
        } else if (ip.equals("")) {
            updateLogsArea("The ip field is empty");
            displayAlert("Wrong IP", "The IP field is empty");
            valid = false;
        } else if (!setPort(port)) {
            updateLogsArea("The ip field is empty");
            displayAlert("Wrong port", "Port out of range, valid range: 1 - 49151");
            valid = false;
        }
        return valid;
    }

    public Button getBtnSync() {
        if (btnSync == null) {
            btnSync = new Button("Sync");
            btnSync.getStyleClass().add("btn");
            btnSync.setTooltip(new Tooltip("Synchronize client & server"));
            btnSync.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    getBtnSync().setDisable(true);
                    getBtnCheckUpdate().setDisable(true);

                    int port = getPort();
                    String ip = getFieldIp().getText();
                    if (checkIpAndPort(ip, port)) {
                        config.SERVER_IP = ip;
                        config.SERVER_PORT = port;
                        updateLogsArea("Starting update process...");
                        Gui_JavaFX.getStackMainPane().getPaneSync().getPaneProgressBar().setPathText("Getting manifest...");
                        Gui_JavaFX.getStackMainPane().getPaneSync().getPaneProgressBar().updateGUI();
                        new Thread(new ClientWorker()).start();
                    } else {
                        getBtnSync().setDisable(false);
                        getBtnCheckUpdate().setDisable(false);
                    }
                }
            });
        }
        return btnSync;
    }

    public Button getBtnCheckUpdate() {
        if (btnCheckUpdate == null) {
            btnCheckUpdate = new Button("Check for updates");
            btnCheckUpdate.getStyleClass().add("btn");
            btnCheckUpdate.getStyleClass().add("btnCheckUpdate");
            btnCheckUpdate.setTooltip(new Tooltip("Check update in table"));
            btnCheckUpdate.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    getBtnSync().setDisable(true);
                    getBtnCheckUpdate().setDisable(true);

                    int port = getPort();
                    String ip = getFieldIp().getText();
                    if (checkIpAndPort(ip, port)) {
                        config.SERVER_IP = ip;
                        config.SERVER_PORT = port;
                        updateLogsArea("Starting update process...");
                        SyncConfig.getConfig().SYNC_MODE = 3;
                        Gui_JavaFX.getStackMainPane().getPaneSync().getPaneProgressBar().setPathText("Getting manifest...");
                        Gui_JavaFX.getStackMainPane().getPaneSync().getPaneProgressBar().updateGUI();
                        new Thread(new ClientWorker()).start();
                    } else {
                        getBtnSync().setDisable(false);
                        getBtnCheckUpdate().setDisable(false);
                    }
                }
            });
        }
        return btnCheckUpdate;
    }

    public TextField getFieldIp() {
        if (fieldIp == null) {
            fieldIp = new TextField();
            fieldIp.setText(SyncConfig.getConfig().SERVER_IP);
        }
        return fieldIp;
    }

    public TextField getFieldPort() {
        if (fieldPort == null) {
            fieldPort = new TextField();
            fieldPort.setText(String.valueOf(SyncConfig.getConfig().SERVER_PORT));
        }
        return fieldPort;
    }

    public int getPort() {
        int port;
        try {
            port = Integer.parseInt(fieldPort.getText());
        } catch (NumberFormatException e) {
            updateLogsArea("Invalid port");
            port = -1;
        }

        return port;
    }

    public boolean setPort(int port) {
        if (!(port <= 49151 && port > 0)) {
            updateLogsArea("Port out of range, valid range: 1 - 49151");
            return false;
        }
        Platform.runLater(() -> fieldPort.setText(String.valueOf(port)));
        return true;
    }

    public void setIPAddress(String ip) {
        Platform.runLater(() -> fieldIp.setText(ip));
    }

    public void updateLogsArea(String text) {
        Gui_JavaFX.getStackMainPane().getPaneLogs().updateLogsArea(text);
    }

    public void displayAlert(String header, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

