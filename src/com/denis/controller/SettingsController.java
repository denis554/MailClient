package com.denis.controller;

import com.denis.App;
import com.denis.model.*;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.view.ViewFactory;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static com.denis.model.GlobalVariables.GlobalVariables.APP_DATA_DIR;
import static com.denis.model.GlobalVariables.GlobalVariables.APP_NAME;
import static com.denis.model.GlobalVariables.GlobalVariables.DB_NAME;

public class SettingsController extends AbstractController implements Initializable {

    // General
    @FXML
    private ChoiceBox<String> GeneralCheckNewMessageChoice;

    @FXML
    TableView<RuleItem> RulesView;

    @FXML
    private TableColumn<RuleItem, String> idCol;

    @FXML
    private TableColumn<RuleItem, String> descCol;

    @FXML
    private CheckBox syncAdbCheckBox;

    @FXML
    private ChoiceBox<String> syncAdbDurationChoiceBox;

    public static SettingsController Instance;

    //server ip and port member
    Pair<String, String> ipAndPort;

    //app database path
    String mAppDataPath;
    String mAppDbName;

    public SettingsController(ModelAccess modelAccess) {
        super(modelAccess);
    }

    @Override
    public Stage getOwnerStage() {
        return (Stage) GeneralCheckNewMessageChoice.getScene().getWindow();
    }

    @FXML
    void handleClickedOnSettingServerIPButton() {
        System.out.println("Clicked On handleClickedOnSettingServerIPButton");
        showServerSettingsDlg();
    }

    /**
     * show setting server ip and port
     */
    public void showServerSettingsDlg() {

        ipAndPort = GlobalVariables.mainController.getServerIP();
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(getString("confirm"));
        dialog.setHeaderText(getString("input_server_info"));

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
        stage.setOnCloseRequest(e->e.consume());

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);

        TextField serverIp = new TextField();
        TextField portNum = new TextField();
        serverIp.setPromptText(getString("prompt_ip"));
        portNum.setPromptText(getString("prompt_port"));
        if (ipAndPort != null) {
            serverIp.setText(ipAndPort.getKey());
            portNum.setText(ipAndPort.getValue());
        }

        hBox.getChildren().add(serverIp);
        hBox.getChildren().add(new Label(":"));
        hBox.getChildren().add(portNum);

        dialog.getDialogPane().setContent(hBox);

        // Request focus on the username field by default.
        Platform.runLater(() -> serverIp.requestFocus());

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        final Button btCancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        btOk.setText(getString("Cancel"));
        btCancel.setText(getString("re_register"));
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            ipAndPort = null;
            dialog.close();
        });
        btCancel.addEventFilter(ActionEvent.ACTION, event -> {
            ipAndPort = new Pair<>(serverIp.getText(), portNum.getText());
            System.out.println("IP = " + ipAndPort.getKey() + ": Port = " + ipAndPort.getValue());
            if (!isValidIP(ipAndPort.getKey())) {
                showAlertAndWait(getString("Warning"), getString("invalid_ip"), getOwnerStage(), Alert.AlertType.WARNING);
                return;
            }
            if (!isValidPort(ipAndPort.getValue())) {
                showAlertAndWait(getString("Warning"), getString("invalid_port"), getOwnerStage(), Alert.AlertType.WARNING);
                return;
            }
            GlobalVariables.mainController.mdb.removeServerInfo();
            GlobalVariables.mainController.mdb.insertServerInfo(ipAndPort.getKey(), Integer.parseInt(ipAndPort.getValue()) + "");
            dialog.close();
            getOwnerStage().close();
            GlobalVariables.mainController.initAppSettings();
        });

        stage.initOwner(getOwnerStage());

        dialog.showAndWait();
    }

    @FXML
    void handleClickedOnRefreshAppNameButton() {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                updateMessage(getString("loading_sys_title"));
                try {
                    getSystemTitle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.runLater(()->{
                    hideWaitingDialog();
                    App.setAppTitle(GlobalVariables.accountInfo.getUserName());
                });
                return null;
            }
        };
        showWaitingDialog(getOwnerStage());
        waitingMsgProperty.bind(task.messageProperty());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    void handleClickedOnDeleteDbButton() {
        System.out.println("Clicked On handleClickedOnChangeDbNameButton");

        String dbCreateDate = GlobalVariables.mainController.getDbCreatedDate();
        Optional ret = showAlertAndWait(
                getString("confirm"),
                String.format(getString("delete_db"), dbCreateDate),
                getOwnerStage(),
                Alert.AlertType.CONFIRMATION);
        if (ret.get() == ButtonType.CANCEL)
            return;
        getOwnerStage().close();
        GlobalVariables.mainController.deleteDefaultDb();
    }

    @FXML
    void handleClickedOnViewButton() {
        System.out.println("Clicked On handleClickedOnViewButton");
        Dialog dialog = new Dialog<>();
        dialog.setTitle(getString("db_property"));

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);


        VBox vBox = new VBox();
        vBox.setPrefWidth(300);
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER_LEFT);

        Label dbMailCnt = new Label(String.format(getString("total_mail_cnt"),
                GlobalVariables.mainController.getTotalMailCnt()));
        Label dbSize = new Label(String.format(getString("database_size"),
                GlobalVariables.mainController.getDbSize()));
        Label dbCreatedDate = new Label(String.format(getString("database_created_date"),
                GlobalVariables.mainController.getDbCreatedDate()));
        Label dbPath = new Label(String.format(getString("database_path"),
                APP_DATA_DIR + File.separator + DB_NAME));

        vBox.getChildren().add(dbMailCnt);
        vBox.getChildren().add(dbSize);
        vBox.getChildren().add(dbCreatedDate);
        vBox.getChildren().add(dbPath);

        dialog.getDialogPane().setContent(vBox);

        stage.initOwner(getOwnerStage());

        dialog.showAndWait();
    }

    @FXML
    void handleClickedOnChangeDbPathButton() {
        System.out.println("Clicked On handleClickedOnChangeDbPathButton");
        changeDatabase();
    }

    /**
     * chenage the datebase folder or name
     */
    public void changeDatabase() {
        Dialog dialog = new Dialog<>();
        dialog.setTitle(getString("confirm"));

        dialog.setHeaderText(getString("changing_data_base_path"));

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox containerVBox = new VBox();
        containerVBox.setPrefWidth(300);
        containerVBox.setSpacing(10);
        containerVBox.setAlignment(Pos.CENTER_RIGHT);

        HBox nameHBox = new HBox();
        HBox pathHBox = new HBox();

        nameHBox.setAlignment(Pos.CENTER_RIGHT);
        pathHBox.setAlignment(Pos.CENTER_RIGHT);

        Label nameLabel = new Label(getString("Name"));
        Label pathLabel = new Label(getString("Path"));

        nameLabel.setPrefWidth(40);
        pathLabel.setPrefWidth(40);

        nameLabel.setTextAlignment(TextAlignment.RIGHT);
        pathLabel.setTextAlignment(TextAlignment.RIGHT);

        TextField appDbName = new TextField();
        TextField appDbPath = new TextField();

        HBox.setHgrow(appDbName, Priority.ALWAYS);
        HBox.setHgrow(appDbPath, Priority.ALWAYS);

        mAppDataPath = APP_DATA_DIR;
        mAppDbName = DB_NAME;

        VBox.setMargin(containerVBox, new Insets(0, 0, 20, 0));

        Button browseBtn = new Button();
        HBox.setMargin(browseBtn, new Insets(0, 0, 0, 10));

        nameHBox.getChildren().addAll(nameLabel, appDbName);
        pathHBox.getChildren().addAll(pathLabel, appDbPath, browseBtn);

        appDbPath.setText(mAppDataPath);
        appDbPath.setEditable(false);
        browseBtn.setText(getString("browse"));
        browseBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DirectoryChooser dirChooser = new DirectoryChooser();
                dirChooser.setTitle(getString("changing_data_base_path"));
                File destF = new File(mAppDataPath);
                if (!destF.exists())
                    destF.mkdirs();
                dirChooser.setInitialDirectory(destF);
                destF = dirChooser.showDialog(getOwnerStage());
                if (destF != null) {
                    destF = new File(destF, APP_NAME);
                    mAppDataPath = destF.getPath();
                    appDbPath.setText(mAppDataPath);
                }
            }
        });

        appDbName.setPromptText(getString("prompt_db_name"));
        appDbName.setText(mAppDbName);
        appDbName.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                mAppDbName = newValue;
            }
        });

        containerVBox.getChildren().add(nameHBox);
        containerVBox.getChildren().add(pathHBox);

        dialog.getDialogPane().setContent(containerVBox);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        final Button btCancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        btOk.setText(getString("confirm"));

        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            if (!isValidation())
                return;
            String cmp1 = mAppDataPath + File.separator + mAppDbName;
            String cmp2 = APP_DATA_DIR + File.separator + DB_NAME;
            if (cmp1.compareTo(cmp2) != 0) {
                //show restart the app
                Platform.runLater(()->showConfirmRestartApp());
            }
            dialog.close();
        });

        btCancel.addEventFilter(ActionEvent.ACTION, event -> {
            new File(mAppDataPath).delete();
        });

        stage.initOwner(getOwnerStage());

        dialog.showAndWait();
    }

    /**
     * get the validation
     * @return
     */
    public boolean isValidation() {

        File f = new File(mAppDataPath);
        if (APP_DATA_DIR.compareTo(mAppDataPath) != 0 && f.exists()) {
            showAlertOkAndWait(
                    getString("Warning"),
                    getString("already_exist_path"),
                    getOwnerStage(),
                    Alert.AlertType.CONFIRMATION
            );
            return false;
        }
        if (mAppDbName == null || mAppDbName.trim().length() == 0) {
            showAlertAndWait(
                    getString("Warning"),
                    getString("prompt_db_name"),
                    getOwnerStage(),
                    Alert.AlertType.WARNING);
            return false;
        }
        if (!isValidFileName(mAppDbName)) {
            showAlertAndWait(
                    getString("Warning"),
                    getString("invalid_db_name"),
                    getOwnerStage(),
                    Alert.AlertType.WARNING);
            return false;
        }
        f = new File(mAppDataPath, mAppDbName);
        if (DB_NAME.compareTo(mAppDbName) != 0 && f.exists()) {
            showAlertOkAndWait(
                    getString("Warning"),
                    getString("exist_specified_db_name"),
                    getOwnerStage(),
                    Alert.AlertType.CONFIRMATION
            );
            return false;
        }

        return true;
    }

    /**
     * confirm the app restart
     */
    public void showConfirmRestartApp() {
        //change app data path
        Optional<ButtonType> ret = showAlertAndWait(
                getString("confirm"),
                getString("restart_confirm"),
                getOwnerStage(),
                Alert.AlertType.CONFIRMATION
        );
        if (ret.get() == ButtonType.CANCEL)
            return;
        getOwnerStage().close();
        GlobalVariables.mainController.changeAppDataPath(mAppDataPath, mAppDbName);
    }

    @FXML
    void handleClickedOnRefreshSecurityLevelsButton() {
        System.out.println("Clicked On Refresh SecurityLevel Levels");
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                updateMessage(getString("loading_security_level"));
                StringBuffer buf = new StringBuffer();
                int ret = GlobalVariables.mainController.getSecurityLevels(buf);
                Platform.runLater(()->{
                    hideWaitingDialog();
                    if (ret == HttpURLConnection.HTTP_OK) {
                        showAlertAndWait(getString("confirm"), getString("sync_sec_level_success"), getOwnerStage(), Alert.AlertType.INFORMATION);
                    } else {
                        showAlertAndWait(getString("Critical"), getString("sync_sec_level_fail") + "\n\n" + buf.toString(), getOwnerStage(), Alert.AlertType.ERROR);
                    }
                });
                return null;
            }
        };
        showWaitingDialog(getOwnerStage());
        waitingMsgProperty.bind(task.messageProperty());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public static final int[] CHECK_NEW_INTERVALS_ARY = new int[] {
        5, // automatically
        60, // 1 min
        300, // 5 mins
        600, // 10 mins
    };

    public static final int[] SYNC_ADB_INTERVALS_ARY = new int[] {
        30 * 60, // automatically 30 mins
        60 * 60, // 1 hour
        120 * 60, // 2 hours
        480 * 60, // 8 hours
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Instance = this;

        //check for the new message duration
        ObservableList<String> newMsgCheckIntervals = FXCollections.observableArrayList();
        newMsgCheckIntervals.addAll(
                getString("automatically"),
                getString("every_minute"),
                getString("every_5_minutes"),
                getString("every_10_minutes"));

        ObservableList<String> syncAdbIntervals = FXCollections.observableArrayList();
        syncAdbIntervals.addAll(
                getString("automatically"),
                getString("every_hour"),
                getString("every_2_hours"),
                getString("every_8_hours"));

        GeneralCheckNewMessageChoice.setItems(newMsgCheckIntervals);
        GeneralCheckNewMessageChoice.setValue(newMsgCheckIntervals.get( GlobalVariables.mainController.getNewMailInterval()));

        GeneralCheckNewMessageChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                GlobalVariables.mainController.updateCheckDuration((Integer) number2);
                GlobalVariables.mainController.restartFetchMailService();
            }
        });

        //check enable sync for the addressbook
        syncAdbCheckBox.setSelected(GlobalVariables.mainController.isSetSyncAdb());
        syncAdbCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("set sync = " + newValue);
                GlobalVariables.mainController.setSyncAdb(newValue);
                syncAdbDurationChoiceBox.setDisable(!GlobalVariables.mainController.isSetSyncAdb());
                if (newValue) {
                    GlobalVariables.mainController.restartFetchAdbService();
                } else {
                    GlobalVariables.mainController.stopFetchAdbService();
                }
            }
        });

        //check sync addressbook status
        syncAdbDurationChoiceBox.setDisable(!GlobalVariables.mainController.isSetSyncAdb());

        //getString sync for the addressbook duration
        syncAdbDurationChoiceBox.setItems(syncAdbIntervals);
        syncAdbDurationChoiceBox.setValue(syncAdbIntervals.get( GlobalVariables.mainController.getSyncAdbInterval()));

        syncAdbDurationChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                GlobalVariables.mainController.updateSyncAdbDuration((Integer) number2);
                GlobalVariables.mainController.restartFetchAdbService();
            }
        });

        // Rules initialization
        idCol.setCellValueFactory(new PropertyValueFactory<RuleItem, String>("id"));
        descCol.setCellValueFactory(new PropertyValueFactory<RuleItem, String>("description"));

        RulesView.setItems(GlobalVariables.mainController.mRulesList);
    }

    @FXML
    public void actionAddRuleButton() {
        Scene scene = ViewFactory.defaultFactory.getAddRuleScene();
        Stage stage = new CustomStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle(getString("NewRule"));
        stage.showAndWait();
    }

    @FXML
    public void actionUpdateRuleButton() {
        RuleItem item = RulesView.getSelectionModel().getSelectedItem();
        if (item == null){
            return;
        }

        Scene scene = ViewFactory.defaultFactory.getUpdateRuleScene(item);
        Stage stage = new CustomStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle(getString("UpdateRule"));
        stage.showAndWait();
    }

    @FXML
    public void actionRemoveRuleButton() {
        RuleItem item = RulesView.getSelectionModel().getSelectedItem();
        if (item == null){
            return;
        }

        GlobalVariables.mainController.removeRule(item);
    }

}