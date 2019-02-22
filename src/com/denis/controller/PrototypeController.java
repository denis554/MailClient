package com.denis.controller;

import com.denis.App;
import com.denis.controller.persistence.ValidAccount;
import com.denis.controller.services.*;
import com.denis.model.*;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.model.database.MailDatabase;
import com.denis.model.http.Apis;
import com.denis.model.http.XMailHttpRequest;
import com.denis.view.ViewFactory;
import com.sun.mail.pop3.POP3Folder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.*;
import javafx.util.Callback;

import javafx.print.PrinterJob;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.util.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import tray.animations.AnimationType;
import tray.notification.NotificationType;

import javax.mail.*;

import static com.denis.controller.SettingsController.CHECK_NEW_INTERVALS_ARY;
import static com.denis.controller.SettingsController.SYNC_ADB_INTERVALS_ARY;
import static com.denis.controller.persistence.ValidAccount.getFormattedEmailFrom;
import static com.denis.model.GlobalVariables.GlobalVariables.*;
import static com.denis.model.MailItem.*;
import static com.denis.model.database.MailDatabase.*;

public class PrototypeController extends AbstractController implements Initializable {

    //toolbar buttons
    @FXML
    private Button newMailMenuButton;
    @FXML
    private Button printButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button replyButton;
    @FXML
    private Button replyToAllButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button followUpButton;
    @FXML
    private Button sendReceiveButton;
    @FXML
    private Button oneNoteButton;
    @FXML
    private Button addressBookButton;
    @FXML
    private ComboBox addressBookComboBox;
    @FXML
    private Button helpButton;
    @FXML
    private Button pendingApproveMailButon;
    @FXML
    private Button mailApproveMailButon;

    //main split
    @FXML
    private SplitPane mainSplitePane;
    @FXML
    private AnchorPane leftAnchorPane;
    @FXML
    private AnchorPane centerAnchorPane;
    @FXML
    private AnchorPane rightAnchorPane;
    @FXML
    private Label categoryNameLabel;
    //collapse
    @FXML
    private Button leftPanCollapseButton;
    @FXML
    private Button leftPanExpandButton;
    @FXML
    private HBox categoryListHBox;
    //expand
    @FXML
    private AnchorPane expandAnchorPane;
    @FXML
    private ListView categoryListView;
    @FXML
    private Label selectionStatusLabel;
    @FXML
    private Label appStatusLabel;
    @FXML
    private ListView categoryItemListView;
    @FXML
    private VBox mailContentsVBox;
    @FXML
    private TableView mainTableView;
    @FXML
    private WebView approveMailWebView;
    @FXML
    private CheckBox selectAllCheckBox;
    @FXML
    private WebView mailContentWebView;
    @FXML
    private ToggleButton mailCatButton;
    @FXML
    private ToggleButton addressBookCatButton;
    @FXML
    private ToggleButton toDoCatButton;
    @FXML
    private Button addBoxButton;
    //search box for mail category
    @FXML
    private ComboBox searchComboBox;
    @FXML
    private Button exSearchButton;
    @FXML
    private FlowPane exSearchFlowPane;
    @FXML
    private VBox searchVBox;
    @FXML
    private Label categoryNameLabel1;
    @FXML
    private Button btnSync;

    //preview layout
    @FXML
    private Button replyInContentButton;

    @FXML
    private Button replyAllInContentButton;

    @FXML
    private Button forwardInContentButton;

    @FXML
    private WebView mailPrevContentWebView;

    @FXML
    private Label fromLabel;

    @FXML
    private Label subjectLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private FlowPane toFollowPane;

    @FXML
    private FlowPane ccFlowPane;

    @FXML
    private FlowPane bccFlowPane;

    @FXML
    private Label mailSecurityClassLabel;

    @FXML
    private FlowPane attachFlowPane;

    @FXML
    private Label statusLabel;

    @FXML
    private Label favLabel;

    @FXML
    private Label importantLabel;

    @FXML
    private Label importantLabel1;

    @FXML
    private VBox approveStatVBox;

    @FXML
    private HBox headerMenuHBox;

    @FXML
    private HBox followContainerHBox;

    @FXML
    private HBox followHBox;

    // ExSearch Pane
    @FXML
    private TextField senderSearchField;
    @FXML
    private TextField receiverSearchField;
    @FXML
    private TextField subjectSearchField;
    @FXML
    private ComboBox readStatusCombo;
    @FXML
    private ComboBox securityLevelComboBox;
    @FXML
    private DatePicker fromSearchPicker;
    @FXML
    private DatePicker toSearchPicker;
    @FXML
    private TextField contentSearchField;
    @FXML
    private ToolBar actionToolBar;
    @FXML
    private Label exSearchSenderLabel;
    @FXML
    private Label exSearchReceiverLabel;

    private int curSelCategoryIndex = -1;
    private int curSelAddrCategoryIndex = 0;
    private int curSelMailCategoryIndex = 0;
    private int curSelToDoMailCategoryIndex = 1;
    private int selectedItemCnt = 0;
    private SecurityLevel secNoLevel;

    private List<String> mAryCategory = new ArrayList<>();

    //show/hide preview flag
    private boolean isShowingMode = false;
    public final static int CAT_MAIL = 0;
    public final static int CAT_ADB = CAT_MAIL + 1;
    public final static int CAT_TODO = CAT_ADB + 1;

    public MailDatabase mdb;

    private final ObservableList<MailItem> mMailList = FXCollections.observableArrayList();
    private final ObservableList<AddressBookItem> mAdbItemList = FXCollections.observableArrayList();
    public final ObservableList<RuleItem> mRulesList = FXCollections.observableArrayList();

    public ObservableList<MailBox> mMailBoxList = FXCollections.observableArrayList();
    private ObservableList<AddressBox> mAdbBoxList = FXCollections.observableArrayList();
    private ObservableList<String> mToDoBoxList = FXCollections.observableArrayList();

    private boolean isNotBinded = false;

    //preview members
    private MailItem mCurSelectedMailItem;

    //address book sync thread
    private WorkerThread adbWorker;
    private WorkerThread inboxWorker;
    private WorkerThread pendingWorker;
    private WorkerThread approveWorker;
    private WorkerThread trayWorker;
    private WorkerThread updateWorker;
    private WorkerThread fetchFolderWorker;
    private static LinkedList<CustomTrayNotification> mAryTray;
    private HashMap<String, CustomTrayNotification> mSendingTrayMap;
    private static ExecutorService executor = Executors.newFixedThreadPool(20);
    public boolean isShownNewApproveNotification;
    private static boolean isOfflineMode = false;

    private boolean isInit = true;

    //width members
    private double mainSplitePaneWidth;
    private double[] mainSlpitePaneDivPos;
    private double screenWidth;
    private boolean isSetMaxSize = false;
    private boolean isSetMinSize = false;

    //approve relation members
    private String approveCntTxt = "";
    private int approveCnt = 0;

    //email detail scene list
    HashMap<String, Scene> emailDetailMap = new HashMap<>();

    //server ip and port member
    Pair<String, String> ipAndPort;

    //exit message
    String mExitTitle;
    String mExitMsg;

    public PrototypeController(ModelAccess modelAccess) {
        super(modelAccess);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        String osName = System.getProperty("os.name");
        App.trackLog("==========================app-------start=========================");
        App.trackLog(osName);

        GlobalVariables.mainController = this;

        //check base db
        if (!new File(BASE_DB_NAME).exists()) {
            showAlertAndWait(getString("Critical") + "!", getString("halt_app_dose_not_exist_database"), getOwnerStage(), Alert.AlertType.ERROR);
            shutdown();
            return;
        }

        GlobalVariables.primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (isInit) {
                    isInit = false;
                    mainSplitePaneWidth = mainSplitePane.getWidth();
                    mainSlpitePaneDivPos = mainSplitePane.getDividerPositions();
//                    mainSlpitePaneDivPos = new double[]{0.2, 0.7};
                    mainSplitePane.getItems().remove(rightAnchorPane);
                    screenWidth = mainSplitePane.getScene().getWindow().getWidth();
                    Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
                    screenWidth = visualBounds.getMaxX();
                    isSetMaxSize = true;
                }
                layoutSizeOnChanged();
            }
        });

        GlobalVariables.primaryStage.maximizedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (t1) { //max
                    System.out.println("max size");
                    isSetMaxSize = true;
                } else { //no max
                    System.out.println("no max size");
                    isSetMinSize = true;
                }
            }
        });

        initCategoryButtons();
        initToolbarButtons();
        initSearchCategory();
        initCategoryList();
        setTextToMailApproveButton(0);

        readStatusCombo.getItems().addAll(getString("All"), getString("Read"), getString("Unread"));

        KeyCombination keyCtrlA = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_ANY);
        mainTableView.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keyCtrlA.match(event)) {
                    handleSelectionItems();
                }
            }
        });

        mainTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectAllCheckBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (selectAllCheckBox.isSelected()) {
                    mainTableView.getSelectionModel().selectAll();
                } else {
                    mainTableView.getSelectionModel().clearSelection();
                }
                handleSelectionItems();
            }
        });

        categoryListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleClickedOnCategory(event);
            }
        });

        categoryListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null) {
                    handleClickedOnCategory(null);
                }
            }
        });

        categoryItemListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null) {
                    handleClickedOnCategoryItem(null);
                }
            }
        });

        categoryItemListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleClickedOnCategoryItem(event);
            }
        });

        mainTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            handleSelectionItems();
        });

        //hide expand pan and button
        categoryListHBox.getChildren().remove(expandAnchorPane);

        categoryListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                // TODO Auto-generated method stub
                return new CategoryListCell();
            }
        });

        categoryItemListView.setCellFactory(new Callback<ListView<Object>, ListCell<Object>>() {

            @Override
            public ListCell<Object> call(ListView<Object> param) {
                // TODO Auto-generated method stub
                return new CategoryListItemCell();
            }
        });

        mainTableView.setRowFactory(tv -> {
            TableRow row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {

                    Object rowData = row.getItem();
                    if (rowData instanceof MailItem) {
                        System.out.println("Double Clicked On MailItem -> {" + rowData.toString() + "}");

                        MailBox mailBox = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
//                        GlobalVariables.selectedMessage = ((MailItem) rowData).referenceMsg;
                        if (mailBox.getBoxName().equalsIgnoreCase("DRAFT")) {

                            ComposeMailController composeMailController = new ComposeMailController(getModelAccess(), ((MailItem) rowData).referenceMsg, "Draft");
                            Scene scene = ViewFactory.defaultFactory.getComposeMailScene(composeMailController);
                            CustomStage stage = new CustomStage();
                            stage.setScene(scene);
                            stage.setTitle(getString("NewMail"));
                            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                @Override
                                public void handle(WindowEvent event) {
                                    composeMailController.saveDrafts();
                                }
                            });
                            stage.show();

                        } else {
                            showMailDetailScene((MailItem) rowData);
                        }
                    } else {
                        System.out.println("Double Clicked On AddressBookItem -> {" + rowData.toString() + "}");
                    }
                }
            });
            return row ;
        });

        Platform.runLater(()->initDataBase());
    }

    public ObservableList<AddressBox> getAddrBoxList () {
        return mAdbBoxList;
    }

    /**
     * show the first login dialog
     */
    public Optional<ButtonType> showFirstLoginDlgAndWait() {
        Dialog dialog = new Dialog();
        dialog.setTitle(getString("confirm"));

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button)dialog.getDialogPane().lookupButton(ButtonType.OK)).setText(getString("login"));
        ((Button)dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setText(getString("exit"));

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);

        HBox hBoxUserName = new HBox();
        hBoxUserName.setSpacing(10);
        hBoxUserName.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(getString("User_Name") + ":");
        Label nameValLabel = new Label(GlobalVariables.accountInfo.getUserName());

        hBoxUserName.getChildren().add(nameLabel);
        hBoxUserName.getChildren().add(nameValLabel);

        HBox hBoxUserMail = new HBox();
        hBoxUserMail.setSpacing(10);
        hBoxUserMail.setAlignment(Pos.CENTER);

        Label mailAddrLabel = new Label(getString("Mail_Address") + ":");
        Label mailAddrValLabel = new Label(GlobalVariables.account.getAddress());

        nameLabel.setPrefWidth(100);
        mailAddrLabel.setPrefWidth(100);

        nameValLabel.setPrefWidth(150);
        mailAddrValLabel.setPrefWidth(150);

        hBoxUserMail.getChildren().add(mailAddrLabel);
        hBoxUserMail.getChildren().add(mailAddrValLabel);

        vBox.getChildren().add(hBoxUserName);
        vBox.getChildren().add(hBoxUserMail);

        dialog.getDialogPane().setContent(vBox);
        stage.initOwner(GlobalVariables.mainController.getOwnerStage());
        return dialog.showAndWait();
    }

    /**
     * show setting server ip and port
     */
    public void showServerSettingsDlg(boolean isInit) {

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
//        serverIp.setText(Apis.HTTP_HOST);
//        portNum.setText(Apis.HTTP_PORT);
//        serverIp.setPromptText(Apis.HTTP_HOST);
//        portNum.setPromptText(Apis.HTTP_PORT);

        hBox.getChildren().add(serverIp);
        hBox.getChildren().add(new Label(":"));
        hBox.getChildren().add(portNum);

        dialog.getDialogPane().setContent(hBox);

        // Request focus on the username field by default.
        Platform.runLater(() -> serverIp.requestFocus());

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        final Button btCancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        if (isInit) {

            btOk.addEventFilter(ActionEvent.ACTION, event -> {
                event.consume();
                ipAndPort = new Pair<>(serverIp.getText(), portNum.getText());
                System.out.println("IP = " + ipAndPort.getKey() + ": Port = " + ipAndPort.getValue());
                if (!isValidIP(ipAndPort.getKey())) {
                    showAlertAndWait(getString("Warning"), getString("invalid_ip"), null, Alert.AlertType.WARNING);
                    return;
                }
                if (!isValidPort(ipAndPort.getValue())) {
                    showAlertAndWait(getString("Warning"), getString("invalid_port"), null, Alert.AlertType.WARNING);
                    return;
                }
                mdb.removeServerInfo();
                mdb.insertServerInfo(ipAndPort.getKey(), ipAndPort.getValue());
                dialog.close();
            });

            btCancel.addEventFilter(ActionEvent.ACTION, event -> {
                shutdown();
            });

        } else {
            btOk.setText(getString("return"));
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
                mdb.removeServerInfo();
                mdb.insertServerInfo(ipAndPort.getKey(), ipAndPort.getValue());
                dialog.close();
                initAppSettings();
            });
        }

        if (!isInit)
            stage.initOwner(GlobalVariables.mainController.getOwnerStage());

        dialog.showAndWait();
        if (!isInit && ipAndPort == null) {
            showExitOrServerSettingsDialog(mExitTitle, mExitMsg);
        }
    }

    /**
     * load the database and load the all contents
     */
    public void initDataBase() {
        Task <Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage(getString("loading_database"));
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!GlobalVariables.isRegApp) {
                    Platform.runLater(()->{
                        hideWaitingExitDialog();
                        Platform.runLater(()->showSettingAppDataPath());
                    });
                    return null;
                }
                mdb = new MailDatabase(APP_DATA_DIR + File.separator + DB_NAME);
                Platform.runLater(()-> initAppMembers());
                return null;
            }
        };

        showWaitingExitDialog(getOwnerStage());
        waitingExitMsgProperty.bind(task.messageProperty());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * set the application data path
     */
    public void showSettingAppDataPath() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(getString("changing_data_base_path"));
        File destF = dirChooser.showDialog(getOwnerStage());
        if (destF == null) {
            App.killProcess(App.getRuntimePid());
            return;
        }
        App.setRegisterApp(new File(destF, APP_NAME).getPath(), DB_NAME);
        initDataBase();
    }

    /**
     * initialize the UI components
     */
    public void initAppMembers() {

        if (!mdb.isSetServer()) {
            //setting server ip and port dialog
            hideWaitingExitDialog();
            showServerSettingsDlg(true);
        }

        isNotBinded = mdb.isNotBindedAccount();
        if (!mdb.isValidAccount(getPCName())) {
            hideWaitingExitDialog();
            showAlertAndWait(getString("login_error") + "!", getString("halt_app_no_match_user"), getOwnerStage(), Alert.AlertType.ERROR);
            shutdown();
            return;
        }

        if (!isOfflineMode) {
            Platform.runLater(()->initAppSettings());
            fetchFolderWorker = new WorkerThread("update folder");
            adbWorker = new WorkerThread("address book");
            updateWorker = new WorkerThread("update");
            executor.execute(updateWorker);
            if (!isNotBindedAccount()) {
                if (isSetSyncAdb()) {
                    executor.execute(adbWorker);
                }
            }
        } else {

            hideWaitingExitDialog();
            GlobalVariables.accountInfo = new ValidAccount("user3", "", 1, 1, 1);
            SecurityLevel security = new SecurityLevel(1, "", "");
            GlobalVariables.securityList = FXCollections.observableArrayList();
            GlobalVariables.securityList.add(security);
            GlobalVariables.userLevelList = new ArrayList<>();
            GlobalVariables.userLevelList.add(security);

            loadAdbBoxFromDB();
            loadMailBoxFromDB();
            loadToDoBoxList();
            mRulesList.addAll(loadRules());
            startInitMailSystem();
        }
    }

    /**
     * get the server ip and port
     * @return
     */
    public Pair<String, String> getServerIP() {
        return mdb.getServerInfo();
    }

    /**
     * initialization of all app settings
     */
    public void initAppSettings() {

        ipAndPort = mdb.getServerInfo();

        if (ipAndPort != null) {
            Apis.HTTP_HOST = ipAndPort.getKey();
            Apis.HTTP_PORT = ipAndPort.getValue();
        }

        Task <Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                updateMessage(getString("loading_mail_box_info"));

                try {

                    GlobalVariables.account = new ValidAccount("", "", "");
                    int ret = getMailBoxInfo(GlobalVariables.account);

                    if (!GlobalVariables.account.isValidAccount()) {
                        int finalRet = ret;
                        Platform.runLater(()->{
                            hideWaitingExitDialog();
                            switch (finalRet) {
                                case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                                    showExitOrServerSettingsDialog(getString("network_error"), getString("server_req_failed"));
                                    break;
                                default:
                                    showAlertAndWait(getString("login_error"), getString("halt_app_dose_not_exist_user"), getOwnerStage(), Alert.AlertType.ERROR);
                                    shutdown();
                                    break;
                            }
                        });
                        return null;
                    }

                    updateMessage(getString("loading_user_info"));

                    GlobalVariables.accountInfo = new ValidAccount("", "", "");
                    ret = getUserInfo(GlobalVariables.accountInfo);

                    if (!GlobalVariables.accountInfo.isValidUserInfo()) {
                        int finalRet = ret;
                        Platform.runLater(()->{
                            hideWaitingExitDialog();
                            switch (finalRet) {
                                case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                                    showExitOrServerSettingsDialog(getString("network_error"), getString("server_req_failed"));
                                    break;
                                default:
                                    showAlertAndWait(getString("login_error"), getString("halt_app_dose_not_exist_user"), getOwnerStage(), Alert.AlertType.ERROR);
                                    shutdown();
                                    break;
                            }
                        });
                        return null;
                    }
//
//                    loadMailBoxFromDB();
//                    loadAdbBoxFromDB();
//                    loadToDoBoxList();

//                    mRulesList.clear();
//                    mRulesList.addAll(loadRules());

                    getModelAccess().setMailType("other");

                    Platform.runLater(()->{

                        if (isNotBindedAccount()) {
                            hideWaitingExitDialog();
                            Optional<ButtonType> result = showFirstLoginDlgAndWait();
                            if (result.get() != ButtonType.OK) {
                                shutdown();
                                return;
                            }
                        }

                        Task <Void> task = new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                updateMessage(getString("logining"));

                                Platform.runLater(()->{
                                    CreateAndRegisterEmailAccountService loginService =
                                            new CreateAndRegisterEmailAccountService(
                                                    GlobalVariables.account.getAddress(),
                                                    GlobalVariables.account.getPassword(),
                                                    getModelAccess());

                                    loginService.parent = PrototypeController.this;
                                    loginService.start();
                                    loginService.setOnSucceeded(e-> {

                                        hideWaitingExitDialog();

                                        if(loginService.getValue() != EmailConstants.LOGIN_STATE_SUCCEDED) {
                                            System.out.println("login error");
                                            showAlertAndWait(getString("login_error"), getString("halt_app_dose_not_exist_user"), getOwnerStage(), Alert.AlertType.ERROR);
                                            shutdown();
                                            //force exit
                                        } else {
                                        /*String password = GlobalVariables.account.getPassword();
                                        if (!isNotBindedAccount())
                                            password = mdb.getBindedPassword();
                                        Scene scene = ViewFactory.defaultFactory.getAccountPasswordScene(GlobalVariables.account.getAddress(), password);
                                        CustomStage stage = new CustomStage();
                                        stage.setScene(scene);
                                        stage.initOwner(categoryItemListView.getScene().getWindow());
                                        stage.initModality(Modality.APPLICATION_MODAL);
                                        stage.setTitle(getString("AccountPassword"));
                                        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                            @Override
                                            public void handle(WindowEvent event) {
                                                System.out.println("handle");
                                                System.exit(0);
                                            }
                                        });
                                        stage.show();*/
                                            setBindAccount(GlobalVariables.account.getPassword());
                                            loadServerSettings();
                                        }
                                    });
                                });

                                return null;
                            }
                        };

                        showWaitingExitDialog(getOwnerStage());
                        waitingExitMsgProperty.bind(task.messageProperty());
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(()->showExitOrServerSettingsDialog(getString("network_error"), getString("server_req_failed")));
                }
                return null;
            }
        };

        showWaitingExitDialog(getOwnerStage());
        waitingExitMsgProperty.bind(task.messageProperty());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * get all server settings
     */
    public void loadServerSettings() {

        Task <Void> task = new Task<Void>() {
            @Override public Void call() throws InterruptedException {
                updateMessage(getString("loading_sys_title"));
                try {
                    getSystemTitle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateMessage(getString("loading_security_level"));
                try {
                    getSecurityLevels(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateMessage(getString("loading_system_security_level"));
                try {
                    getSysSecurityLevel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateMessage(getString("loading_user_level"));
                try {
                    getUserLevels();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                loadAdbBoxFromDB();
                loadMailBoxFromDB();
                loadToDoBoxList();
                mRulesList.clear();
                mRulesList.addAll(loadRules());

                updateMessage(getString("loading_addressbook_list"));
                try {
                    syncAddressBook(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Platform.runLater(()-> hideWaitingExitDialog());
                Platform.runLater(()->startInitMailSystem());
                return null;
            }
        };

        showWaitingExitDialog(getOwnerStage());
        waitingExitMsgProperty.bind(task.messageProperty());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * it calls when ui components layout changes
     */
    public void layoutSizeOnChanged() {
        double ratio = mainSplitePaneWidth / screenWidth;
        int index = mainSplitePane.getItems().indexOf(leftAnchorPane);
        if (isSetMaxSize) {
            isSetMaxSize = false;
            if (mainSplitePane.getDividers().size() == 2) {
                mainSplitePane.setDividerPosition(0, mainSlpitePaneDivPos[0] * ratio);
                mainSplitePane.setDividerPosition(1, 0.9 * ratio);
            } else {
                if (index > -1)
                    mainSplitePane.setDividerPosition(0, mainSlpitePaneDivPos[0] * ratio);
                else
                    mainSplitePane.setDividerPosition(0, 0.9 * ratio);
            }
        }
        if (isSetMinSize) {
            isSetMinSize = false;
            if (mainSplitePane.getDividers().size() == 2) {
                mainSplitePane.setDividerPosition(0, mainSlpitePaneDivPos[0] * ratio);
                mainSplitePane.setDividerPosition(1, 0.9 * ratio);
            } else {
                if (index > -1)
                    mainSplitePane.setDividerPosition(0, mainSlpitePaneDivPos[0] * ratio);
                else
                    mainSplitePane.setDividerPosition(0, 0.9 * ratio);
            }
        }
    }

    /**
     * start mail system
     */
    public void startInitMailSystem() {

        App.setAppTitle(GlobalVariables.accountInfo.getUserName());

        handleClickedOnCategory(null);
        if (!fetchFolderWorker.isWorking())
            executor.execute(fetchFolderWorker);
        parseAttachFile();
    }

    /**
     * restart the fetch mail service
     */
    public void restartFetchMailService() {
        System.out.println("Called on restartFetchMailService func");

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    while (fetchFolderWorker.isWorking()) {
                        fetchFolderWorker.setCancel();
                        Thread.sleep(10);
                    }
                    Platform.runLater(()->{
                        if (!fetchFolderWorker.isWorking())
                            executor.execute(fetchFolderWorker);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void stopFetchAdbService() {
        System.out.println("Called on stopFetchAdbService func");
        adbWorker.setCancel();
        adbWorker = null;
    }

    public void restartFetchAdbService() {
        System.out.println("Called on restartFetchAdbService func");
        adbWorker = null;
        adbWorker = new WorkerThread("address book");
        executor.execute(adbWorker);
    }

    /**
     * show the mail detail scene for the clicked item
     */
    public void showMailDetailScene(MailItem aItem) {

        Scene scene = emailDetailMap.get(aItem.getUid());
        if (scene != null) {
            ((Stage)scene.getWindow()).show();
            ((Stage)scene.getWindow()).toFront();
            return;
        }

        if (aItem.unread)
            updateMessageReadStatus(aItem, true);

        scene = ViewFactory.defaultFactory.getEmailDetailScene(aItem);
        CustomStage stage = new CustomStage();
        stage.setScene(scene);
        stage.setTitle(getString("DetailedEmail"));
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                emailDetailMap.remove(aItem.getUid());
            }
        });
        emailDetailMap.put(aItem.getUid(), scene);
        stage.show();
    }

    @FXML
    public void actionOnShowPreviewMenuItem() {
        System.out.println("OnClicked actionOnShowPreviewMenuItem");
        if (isForbiddenOperationBox()) {
            showAlertAndWait(getString("Warning") + "!", getString("operation_forbidden"), getOwnerStage(), Alert.AlertType.WARNING);
            return;
        }
        if (mCurSelectedMailItem != null && isForbiddenOperationBox(mCurSelectedMailItem.boxName)) {
            return;
        }
        int index = mainSplitePane.getItems().indexOf(rightAnchorPane);
        if (index >= 0)
            return;
        mainSplitePane.getItems().add(rightAnchorPane);
        isShowingMode = true;
        showPrevMail(mCurSelectedMailItem);
        isSetMaxSize = true;
        layoutSizeOnChanged();
    }

    @FXML
    public void actionOnHidePreviewMenuItem(ActionEvent event) {
        System.out.println("OnClicked actionOnHidePreviewMenuItem");
        if (event != null) {
            if (isForbiddenOperationBox()) {
                showAlertAndWait(getString("Warning") + "!", getString("operation_forbidden"), getOwnerStage(), Alert.AlertType.WARNING);
                return;
            }
            if (mCurSelectedMailItem != null && isForbiddenOperationBox(mCurSelectedMailItem.boxName)) {
                return;
            }
        }
        int index = mainSplitePane.getItems().indexOf(rightAnchorPane);
        if (index == -1)
            return;
        mainSplitePane.getItems().remove(rightAnchorPane);
        isShowingMode = false;
        isSetMaxSize = true;
        layoutSizeOnChanged();
    }

    @FXML
    public void actionOnSettingsMenuItem() {
        if (SettingsController.Instance != null) {
            Stage stage = (Stage) SettingsController.Instance.getOwnerStage().getScene().getWindow();
            stage.show();
            stage.toFront();
            return;
        }
//        GlobalVariables.replyType = "";
        Scene scene = ViewFactory.defaultFactory.getSettingsScene();
        CustomStage stage = new CustomStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle(getString("Settings"));
        stage.initOwner(getOwnerStage());
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                SettingsController.Instance = null;
            }
        });
    }

    @FXML
    public void handleClickedOnNewMailMenuButton() {

        //for test
//        MailItem item = (MailItem) mainTableView.getSelectionModel().getSelectedItem();
//        showNewMailNotification(item.referenceMsg);
        showNewMailScene();
    }

    /**
     * parse the app attach file if some attach files exist, app will go to the new mail with attach files
     */
    public void parseAttachFile() {
        List<File> files = getAttachFiles();

        if (files.size() > 0)
            showNewMailScene();
    }

    public void showNewMailScene() {
        ComposeMailController composeMailController = new ComposeMailController(getModelAccess(), null, "");
        Scene scene = ViewFactory.defaultFactory.getComposeMailScene(composeMailController);
        CustomStage stage = new CustomStage();
        stage.setScene(scene);
        stage.setTitle(getString("NewMail"));
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                composeMailController.saveDrafts();
            }
        });
        stage.show();
    }

    public void showNewMailSceneWithAttachments(List<File> files) {
        ComposeMailController composeMailController = new ComposeMailController(getModelAccess(), files);
        Scene scene = ViewFactory.defaultFactory.getComposeMailScene(composeMailController);
        CustomStage stage = new CustomStage();
        stage.setScene(scene);
        stage.setTitle(getString("NewMail"));
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                composeMailController.saveDrafts();
            }
        });
        stage.show();
    }


    /**
     * @author pilot
     * new mail with recipients
     * @param addressBookItems
     */
    public void newMailWithRecipient(ObservableList<AddressBookItem> addressBookItems) {
        ComposeMailController composeMailController = new ComposeMailController(getModelAccess(), addressBookItems);
        Scene scene = ViewFactory.defaultFactory.getComposeMailScene(composeMailController);
        CustomStage stage = new CustomStage();
        stage.setScene(scene);
        stage.setTitle(getString("NewMail"));
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                composeMailController.saveDrafts();
            }
        });
        stage.show();
    }

    @FXML
    public void handleClickedOnReplyMenuButton() {

        if (isSelectedMailCategory()) {

            if (isForbiddenOperationBox()) {
                showAlertAndWait(getString("Warning") + "!", getString("operation_forbidden"), getOwnerStage(), Alert.AlertType.WARNING);
                return;
            }

            ObservableList<MailItem> mailItems = mainTableView.getSelectionModel().getSelectedItems();
            for (MailItem item : mailItems) {
//                if (item.isApproveMail()) {
//                    updateAppStatusLabel(getString("operation_forbidden"));
//                    continue;
//                }
                updateMessageReadStatus(item, true);
                Random random = new Random();
//                GlobalVariables.replyType = "Reply";
//                GlobalVariables.selectedMessage = item.referenceMsg;
                Scene scene = ViewFactory.defaultFactory.getDraftMailScene(item.referenceMsg, "Reply");
                CustomStage stage = new CustomStage();
                stage.setX(random.nextInt(200) + 300);
                stage.setY(random.nextInt(200) + 300);
                stage.setScene(scene);

                ComposeMailController composeMailController = ((FXMLLoader)scene.getUserData()).getController();
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        composeMailController.saveDrafts();
                    }
                });

                stage.setTitle(getString("ReplyMail"));
                stage.show();
            }
        }
    }

    @FXML
    public void handleClickedOnReplyAllMenuButton() {

        if (isSelectedMailCategory()) {

            if (isForbiddenOperationBox()) {
                showAlertAndWait(getString("Warning") + "!", getString("operation_forbidden"), getOwnerStage(), Alert.AlertType.WARNING);
                return;
            }

            ObservableList<MailItem> mailItems = mainTableView.getSelectionModel().getSelectedItems();
            for (MailItem item : mailItems) {
//                if (item.isApproveMail()) {
//                    updateAppStatusLabel(getString("operation_forbidden"));
//                    continue;
//                }
                Random random = new Random();
                updateMessageReadStatus(item, true);
//                GlobalVariables.replyType = "Reply To All";
//                GlobalVariables.selectedMessage = item.referenceMsg;
                Scene scene = ViewFactory.defaultFactory.getDraftMailScene(item.referenceMsg, "Reply To All");
                CustomStage stage = new CustomStage();
                stage.setX(random.nextInt(200) + 300);
                stage.setY(random.nextInt(200) + 300);
                stage.setScene(scene);
                ComposeMailController composeMailController = ((FXMLLoader)scene.getUserData()).getController();
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        composeMailController.saveDrafts();
                    }
                });
                stage.setTitle(getString("ReplyToAll"));
                stage.show();
            }
        }
    }

    @FXML
    public void handleClickedOnForwardMenuButton() {

        if (isSelectedMailCategory()) {

            if (isForbiddenOperationBox()) {
                showAlertAndWait(getString("Warning") + "!", getString("operation_forbidden"), getOwnerStage(), Alert.AlertType.WARNING);
                return;
            }

            ObservableList<MailItem> mailItems = mainTableView.getSelectionModel().getSelectedItems();
            for (MailItem item : mailItems) {
//                if (item.isApproveMail()) {
//                    updateAppStatusLabel(getString("operation_forbidden"));
//                    continue;
//                }
                Random random = new Random();
                updateMessageReadStatus(item, true);
//                GlobalVariables.replyType = "Forward";
//                GlobalVariables.selectedMessage = item.referenceMsg;
                Scene scene = ViewFactory.defaultFactory.getDraftMailScene(item.referenceMsg, "Forward");
                CustomStage stage = new CustomStage();
                stage.setX(random.nextInt(200) + 300);
                stage.setY(random.nextInt(200) + 300);
                stage.setScene(scene);
                ComposeMailController composeMailController = ((FXMLLoader)scene.getUserData()).getController();
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        composeMailController.saveDrafts();
                    }
                });
                stage.setTitle(getString("ForwardMail"));
                stage.show();
            }
        }
    }

    @FXML
    public void handleClickedOnDeleteMenuButton() {
        if (isSelectedMailCategory()) {

            ObservableList<MailItem> mailItems = mainTableView.getSelectionModel().getSelectedItems();
            MailBox mailBox = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
            if (mailItems.size() > 0) {
                if (mailBox.isTrash()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
                    alert.setTitle(getString("question_complete_delete"));
                    alert.setHeaderText(getString("question_complete_delete_content"));
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {

                        Task task = new Task() {
                            @Override
                            protected Object call() throws Exception {
                                updateMessage(getString("deleting"));
                                try {
                                    for (MailItem item: mailItems) {
                                        File dir = new File(GlobalVariables.APP_DATA_DIR, item.getUid());
                                        if (dir.exists()) {
                                            deleteFile(dir);
                                        }
                                    }
                                    mdb.removeMessage(mailItems, mailBox);
                                    Platform.runLater(()->{
                                        hideWaitingDialog();
                                        mailBox.removeAllMsg(mailItems);
                                        int iCnt = mailItems.size();
                                        String txt = (iCnt == 1 ? getString("one_item_deleted") : (iCnt + " " + getString("multi_item_deleted")));
                                        updateAppStatusLabel(txt);
                                        mMailList.removeAll(mailItems);
                                        mainTableView.getSelectionModel().clearSelection();
                                        mainTableView.refresh();
                                    });
                                } catch (Exception e) {
                                    Platform.runLater(()->{
                                        showAlertAndWait(
                                                getString("Warning"),
                                                e.getMessage(),
                                                getOwnerStage(),
                                                Alert.AlertType.WARNING
                                        );
                                    });
                                }
                                return null;
                            }
                        };

                        showWaitingDialog(getOwnerStage());
                        waitingMsgProperty.bind(task.messageProperty());
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();
                    }
                } else {
                    Task task = new Task() {
                        @Override
                        protected Object call() throws Exception {
                            updateMessage(getString("deleting"));
                            mdb.removeMessage(mailItems, mailBox);
                            Platform.runLater(()->{
                                hideWaitingDialog();
                                MailBox trashBox = MailBox.getTrashBox(mMailBoxList);
                                trashBox.addAllMessage(mailItems);
                                mailBox.removeAllMsg(mailItems);
                                int iCnt = mailItems.size();
                                String txt = (iCnt == 1 ? getString("one_item_deleted") : (iCnt + getString("multi_item_deleted")));
                                updateAppStatusLabel(txt);
                                mMailList.removeAll(mailItems);
                                mainTableView.getSelectionModel().clearSelection();
                                mainTableView.refresh();
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
            }
        }
    }

    /**
     * displays the app status label for a while seconds
     */
    public void updateAppStatusLabel(String aTxt) {
        appStatusLabel.setText(aTxt);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        appStatusLabel.setText("");
                    }
                });

            }
        }).start();
    }

    @FXML
    public void handleClickedOnFollowButton() { //preview star button
        if (mCurSelectedMailItem != null) {
            setStarInUI(mCurSelectedMailItem);
        }
    }

    @FXML
    public void handleClickedOnFollowMenuButton() { // menu star button

        if (isSelectedMailCategory()) {

//            if (isForbiddenOperationBox()) {
//                showAlertAndWait(getString("Warning") + "!", getString("operation_forbidden"), getOwnerStage(), Alert.AlertType.WARNING);
//                return;
//            }

            ObservableList<MailItem> mailItems = mainTableView.getSelectionModel().getSelectedItems();
            setStarInUI(mailItems);
//            setImportantInUI(mailItems);
        }
    }

    /**
     * update important status if some opened email detail scenes exist
     * @param aItem
     */
    public void updateImportantOfEmailDetailScene(MailItem aItem) {
        Scene scene = emailDetailMap.get(aItem.getUid());
        if (scene != null) {
            FXMLLoader loader = (FXMLLoader) scene.getUserData();
            EmailDetailController detailController = loader.getController();
            detailController.updateImportantStat(aItem.isImportantMail());
        }
    }

    /**
     * update approve status if some opened email detail scenes exist
     * @param aItem
     */
    public void updateApproveOfEmailDetailScene(MailItem aItem) {
        Scene scene = emailDetailMap.get(aItem.getUid());
        if (scene != null) {
            FXMLLoader loader = (FXMLLoader) scene.getUserData();
            EmailDetailController detailController = loader.getController();
            detailController.updateApproveStat(aItem);
        }
    }

    /**
     * update flag status if some opened email detail scenes exist
     * @param aItem
     */
    public void updateStarOfEmailDetailScene(MailItem aItem) {
        Scene scene = emailDetailMap.get(aItem.getUid());
        if (scene != null) {
            FXMLLoader loader = (FXMLLoader) scene.getUserData();
            EmailDetailController detailController = loader.getController();
            detailController.updateStarStat(aItem.starProperty().getValue());
        }
    }

    /**
     * update flag status to main controller
     * @param aItem
     */
    public void setStarInUI(MailItem aItem) {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                updateMessage(getString("updating"));
                updateStarInDB(aItem);
                Platform.runLater(()->{
                    hideWaitingDialog();
                    for (MailBox box: mMailBoxList) {
                        box.updateMessage(aItem.getUid(), aItem.referenceMsg);
                    }
                    mainTableView.refresh();
                    updateStarButtonStatInPrevMail(aItem);
                    updateStarOfEmailDetailScene(aItem);
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

    /**
     * update started status with items
     * @param mailItems
     */
    public void setStarInUI(ObservableList<MailItem> mailItems) {
        if (mailItems == null || mailItems.size() == 0)
            return;

        for (MailItem eachItem: mailItems) {
            eachItem.setStar(!eachItem.starProperty().getValue());
            try {
                eachItem.referenceMsg.setFlag(Flags.Flag.FLAGGED, eachItem.starProperty().getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mCurSelectedMailItem != null && mCurSelectedMailItem.getUid() == eachItem.getUid()) {
                updateStarButtonStatInPrevMail(eachItem);
            }
            updateStarOfEmailDetailScene(eachItem);
            for (MailBox box: mMailBoxList) {
                box.updateMessage(eachItem.getUid(), eachItem.referenceMsg);
            }
        }

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                updateMessage(getString("updating"));
                mdb.setStartedMessage(mailItems);
                Platform.runLater(()->{
                    hideWaitingDialog();
                    mainTableView.refresh();
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

    /**
     * update important status with items
     * @param mailItems
     */
    public void setImportantInUI(ObservableList<MailItem> mailItems) {
        if (mailItems == null || mailItems.size() == 0)
            return;

        for (MailItem eachItem: mailItems) {
            eachItem.setImportant(!eachItem.isImportantMail());
            if (mCurSelectedMailItem != null && mCurSelectedMailItem.getUid() == eachItem.getUid()) {
                updateImportantButtonStatInPrevMail(eachItem);
            }
            updateImportantOfEmailDetailScene(eachItem);
        }
//        mdb.setImportantMessage(mailItems);
//        mainTableView.refresh();

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                updateMessage(getString("updating"));
                mdb.setImportantMessage(mailItems);
                Platform.runLater(()->{
                    hideWaitingDialog();
                    mainTableView.refresh();
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

    /**
     * update the mail flag
     */
    public void updateStarInDB(MailItem aItem) {
        boolean flag = aItem.starProperty().getValue();
        flag = !flag;
        aItem.setStar(flag);
        try {
            aItem.referenceMsg.setFlag(Flags.Flag.FLAGGED, flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mdb.updateMessageFlag(aItem.referenceMsg, flag);
    }

    @FXML
    public void handleClickedOnPrintMenuButton() {

        if (isSelectedMailCategory()) {

            MailBox mailBox = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
            MailItem mailItem = (MailItem) mainTableView.getSelectionModel().getSelectedItem();
            if (mailItem != null) {

                int mailIndex = 0;
                for (int i = 0; i < mailBox.getMsgList().size(); i++) {
                    Message msg = mailBox.getMsgList().get(i).referenceMsg;
                    try {
                        Date sentDate = msg.getSentDate();
                        String uid = "" + sentDate.getTime();
                        if (uid.equals(mailItem.uid)) {
                            mailIndex = i;
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    String content = "";
                    Message message = mailBox.getMsgList().get(mailIndex).referenceMsg;
                    Multipart multipart = (Multipart) message.getContent();
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        if (bodyPart.getContentType().contains("text/html")) {
                            content = bodyPart.getContent().toString();
                        }
                    }
                    PrinterJob job = PrinterJob.createPrinterJob();
                    WebEngine engine = mailContentWebView.getEngine();
                    engine.loadContent(content);
                    engine.print(job);
                } catch (Exception e) {
                    e.printStackTrace();
                    updateAppStatusLabel(e.getMessage());
                }
            }
        }
    }

    public void initSearchCategory() {
        searchVBox.getChildren().remove(searchComboBox);
        searchVBox.getChildren().remove(exSearchButton);
        searchVBox.getChildren().remove(exSearchFlowPane);

        searchComboBox.setEditable(true);
        exSearchButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/arrows_down.png"));
        exSearchButton.setText("");

        initExSearchValues();
    }

    public void initExSearchValues() {
        senderSearchField.setText("");
        receiverSearchField.setText("");
        subjectSearchField.setText("");
        contentSearchField.setText("");
        fromSearchPicker.setValue(null);
        toSearchPicker.setValue(null);
        readStatusCombo.getSelectionModel().select(0);

        searchComboBox.setVisible(true);
        exSearchButton.setVisible(true);
    }

    @FXML
    public void handleClickedOnExSearchButon() {

        if (isSelectedMailCategory()) {
            int index = searchVBox.getChildren().indexOf(exSearchFlowPane);
            if (index == -1) {
                exSearchButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/arrows_up.png"));
                searchVBox.getChildren().add(exSearchFlowPane);
            } else {
                initExSearchValues();
                exSearchButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/arrows_down.png"));
                searchVBox.getChildren().remove(exSearchFlowPane);
            }
        }
    }

    public void initCategoryList() {
        mAryCategory.add(getString("Mail"));
        mAryCategory.add(getString("AddressBook"));
        mAryCategory.add(getString("to_do_mail"));
        categoryListView.getItems().addAll(mAryCategory);
        categoryListView.getSelectionModel().select(0);
    }

    /**
     * handles the selection of mail items
     */
    public void handleSelectionItems() {

        ObservableList selItems = mainTableView.getSelectionModel().getSelectedItems();
        ObservableList allItems = mainTableView.getItems();

        selectedItemCnt = 0;
        for (Object org: allItems) {
            if (org == null)
                continue;
            if (org instanceof MailItem)
                ((MailItem)org).setSelect(false);
            else if (org instanceof AddressBookItem)
                ((AddressBookItem)org).setSelect(false);
        }

        for (Object org: selItems) {
            if (org == null)
                continue;
            selectedItemCnt++;
            if (org instanceof MailItem)
                ((MailItem)org).setSelect(true);
            else if (org instanceof AddressBookItem)
                ((AddressBookItem)org).setSelect(true);
        }

        if (selectedItemCnt == mainTableView.getItems().size()) {
            selectAllCheckBox.setIndeterminate(false);
            selectAllCheckBox.setSelected(true);
        } else {
            if (selectedItemCnt <=0) {
                selectAllCheckBox.setIndeterminate(false);
                selectAllCheckBox.setSelected(false);
            } else
                selectAllCheckBox.setIndeterminate(true);
        }

        updateSelectionStatusLabel();

        mainTableView.refresh();
    }

    public void updateSelectionStatusLabel() {
        ObservableList selItems = mainTableView.getSelectionModel().getSelectedItems();
        ObservableList allItems = mainTableView.getItems();
        selectionStatusLabel.setText(String.format(getString("selection_stat"), allItems.size(), selItems.size()));
    }

    /**
     * init mail box table
     * @param mailBox
     */
    public void initMailBoxTable(MailBox mailBox) {

        mainTableView.getColumns().clear();

        TableColumn selectAll = new TableColumn();
        TableColumn markCol = new TableColumn();
        TableColumn replyCol = new TableColumn();
        TableColumn attachCol = new TableColumn();

        TableColumn fromCol = new TableColumn();
        TableColumn toCol = new TableColumn();
        TableColumn securityCol = new TableColumn();
        TableColumn subjectCol = new TableColumn();
        TableColumn sentDateCol = new TableColumn();
        TableColumn sizeCol = new TableColumn();
        TableColumn starCol = new TableColumn();
        TableColumn importantCol = new TableColumn();

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(selectAllCheckBox);
        selectAllCheckBox.setText("");
        selectAll.setGraphic(box);

        int index;
        switch (mailBox.getBoxName().toLowerCase()) {
            case "inbox":
            case "junk":
            case "trash":
                fromCol.setVisible(true);
                index = exSearchFlowPane.getChildren().indexOf(senderSearchField);
                if (index == -1)
                    exSearchFlowPane.getChildren().add(0, senderSearchField);
                index = exSearchFlowPane.getChildren().indexOf(exSearchSenderLabel);
                if (index == -1)
                    exSearchFlowPane.getChildren().add(0, exSearchSenderLabel);
                break;
            default:
                fromCol.setVisible(false);
                exSearchFlowPane.getChildren().remove(exSearchSenderLabel);
                exSearchFlowPane.getChildren().remove(senderSearchField);
                break;
        }

        switch (mailBox.getBoxName().toLowerCase()) {
            case "sent":
            case "draft":
                toCol.setVisible(true);
                index = exSearchFlowPane.getChildren().indexOf(receiverSearchField);
                if (index == -1)
                    exSearchFlowPane.getChildren().add(0, receiverSearchField);
                index = exSearchFlowPane.getChildren().indexOf(exSearchReceiverLabel);
                if (index == -1)
                    exSearchFlowPane.getChildren().add(0, exSearchReceiverLabel);
                break;
            default:
                toCol.setVisible(false);
                exSearchFlowPane.getChildren().remove(exSearchReceiverLabel);
                exSearchFlowPane.getChildren().remove(receiverSearchField);
                break;
        }

        if (mailBox.isCustomBox())
            fromCol.setVisible(true);

        selectAll.setPrefWidth(26);
        markCol.setPrefWidth(24);
        attachCol.setPrefWidth(24);
        replyCol.setPrefWidth(24);
        fromCol.setPrefWidth(206);
        securityCol.setPrefWidth(100);
        toCol.setPrefWidth(206);
        subjectCol.setPrefWidth(350);
        sentDateCol.setPrefWidth(205);
        sizeCol.setPrefWidth(64);
        starCol.setPrefWidth(24);
        importantCol.setPrefWidth(24);

        fromCol.setText(getString("From"));
        toCol.setText(getString("To"));
        sentDateCol.setText(getString("SentDate"));
        subjectCol.setText(getString("Subject"));
        securityCol.setText(getString("Security_Level"));
        sizeCol.setText(getString("Size"));

        selectAll.setText("");
        attachCol.setText("");
        replyCol.setText("");
        starCol.setText("");
        importantCol.setText("");

        markCol.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/mark.png"));
        attachCol.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/attach.png"));
        replyCol.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/reply_forward.png"));
        starCol.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/fav_no.png", 12));
        importantCol.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/follow_down_small.png"));

        markCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (getTableRow() != null &&
                                getTableRow().getItem() != null &&
                                getTableRow().getItem() instanceof  MailItem) {
                            try {
                                MailItem mailItem = (MailItem) getTableRow().getItem();
                                ImageView imageview = null;
                                if (mailItem.unread)
                                    imageview = (ImageView) ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/unread.png");
                                else
                                    imageview = (ImageView) ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/read.png");
                                setGraphic(imageview);
                            } catch (Exception e) {
                                setGraphic(null);
                            }
                        } else {
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        });

        selectedItemCnt = 0;
        selectAllCheckBox.setIndeterminate(false);
        selectAllCheckBox.setSelected(false);
        selectAll.setCellValueFactory(new PropertyValueFactory("selected"));
        selectAll.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            HBox box = new HBox();
                            box.setAlignment(Pos.CENTER);
                            CheckBox chkBox = new CheckBox();
                            chkBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {

                                    int index = getIndex();

                                    if (chkBox.isSelected())
                                        mainTableView.getSelectionModel().select(index);
                                    else
                                        mainTableView.getSelectionModel().clearSelection(index);

                                    handleSelectionItems();
                                }
                            });
                            chkBox.setSelected(Boolean.parseBoolean(item.toString()));
                            box.getChildren().add(chkBox);
                            setGraphic(box);
                        } else {
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        });

        importantCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (getTableRow() != null &&
                                getTableRow().getItem() != null &&
                                getTableRow().getItem() instanceof  MailItem) {
                            try {
                                MailItem mailItem = (MailItem) getTableRow().getItem();
                                boolean isImportant = mailItem.isImportantMail();
                                ImageView imageview = null;
                                if (isImportant)
                                    imageview = (ImageView) ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/follow_up_small.png");
                                setGraphic(imageview);
                            } catch (Exception e) {
                                setGraphic(null);
                            }
                        } else {
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        });

        replyCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (getTableRow() != null &&
                                getTableRow().getItem() != null &&
                                getTableRow().getItem() instanceof  MailItem) {
                            MailItem mailItem = (MailItem) getTableRow().getItem();
                            Message message = mailItem.referenceMsg;
                            boolean isReplyAndForward = MailItem.getStatOf(message, MailItem.REPLY_AND_FORWARD);
                            HBox box = new HBox();
                            box.setAlignment(Pos.CENTER);
                            ImageView imageview = null;
                            if (isReplyAndForward) {
                                imageview = (ImageView) ViewFactory.defaultFactory.resolveIconWithName("images/reply_forward.png");
                                box.getChildren().add(imageview);
                            } else {
                                boolean isReplied = MailItem.getStatOf(message, MailItem.REPLIED);
                                boolean isForwarded = MailItem.getStatOf(message, MailItem.FORWARDED);
                                if (isReplied) {
                                    imageview = (ImageView) ViewFactory.defaultFactory.resolveIconWithName("images/reply_small.png");
                                }
                                if (isForwarded) {
                                    imageview = (ImageView) ViewFactory.defaultFactory.resolveIconWithName("images/forward_small.png");
                                }
                                if (imageview != null)
                                    box.getChildren().add(imageview);
                            }
                            setGraphic(box);
                        } else {
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        });

        attachCol.setCellValueFactory(new PropertyValueFactory("attach"));
        attachCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            if ((Boolean) item) {
                                setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/attach.png"));
                            } else {
                                setGraphic(null);
                            }
                        } else {
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        });

        securityCol.setCellValueFactory(new PropertyValueFactory("secLevel"));
        securityCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            HBox box= new HBox();
                            box.setSpacing(10) ;
                            VBox vbox = new VBox();

                            if(!item.toString().isEmpty())
                                vbox.getChildren().add(new Label(item.toString() + ""));
                            else
                                vbox.getChildren().add(new Label(""));

                            Node imageview = ViewFactory.defaultFactory.resolveMailSecurityColor(item.toString());

                            box.getChildren().addAll(imageview,vbox);
                            setGraphic(box);
                        } else {
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        });

        starCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {

                        if (getTableRow() != null &&
                                getTableRow().getItem() != null &&
                                getTableRow().getItem() instanceof  MailItem) {

                            MailItem mailItem = (MailItem) getTableRow().getItem();
                            HBox box = new HBox();
                            box.setAlignment(Pos.CENTER);

                            Node img;
                            if (mailItem.starProperty().getValue()) {
                                img = ViewFactory.defaultFactory.resolveIconWithName("images/fav_yes.png", 12);
                            } else {
                                img = ViewFactory.defaultFactory.resolveIconWithName("images/fav_no.png", 12);
                            }

                            getStyleClass().add("start-button");

                            box.getChildren().add(img);

                            setGraphic(box);

                            setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                    setStarInUI(mailItem);
                                }
                            });

                        } else {
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        });

        subjectCol.setCellValueFactory(new PropertyValueFactory("subject"));
        subjectCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {

                        if (item != null) {
                            MailBox box = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
                            if (!box.getBoxName().equalsIgnoreCase("draft") && item.toString().isEmpty())
                                setText(getString("no_subject"));
                            else
                                setText(item.toString());
                        } else {
                            setText("");
                        }
                        setGraphic(null);

                        TableRow<Object> currentRow = getTableRow();
                        if (!isForbiddenOperationBox() &&
                                currentRow != null &&
                                currentRow.getItem() != null &&
                                getTableRow().getItem() instanceof  MailItem &&
                                ((MailItem)currentRow.getItem()).unread) {
                            setStyle("-fx-font-weight: 800");
                        } else {
                            setStyle("-fx-font-weight: 400");
                        }
                    }
                };

                return cell;
            }
        });

        sizeCol.setCellValueFactory(new PropertyValueFactory("size"));
        sizeCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            setText(item.toString());
                        } else {
                            setText("");
                        }
                        setGraphic(null);

                        TableRow<Object> currentRow = getTableRow();
                        if (!isForbiddenOperationBox() &&
                                currentRow != null &&
                                currentRow.getItem() != null &&
                                getTableRow().getItem() instanceof  MailItem &&
                                ((MailItem)currentRow.getItem()).unread) {
                            setStyle("-fx-font-weight: 800");
                        } else {
                            setStyle("-fx-font-weight: 400");
                        }
                    }
                };

                return cell;
            }
        });

        fromCol.setCellValueFactory(new PropertyValueFactory("from"));
        fromCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            setText(item.toString());
                        } else {
                            setText("");
                        }
                        setGraphic(null);

                        TableRow<Object> currentRow = getTableRow();
                        if (!isForbiddenOperationBox() &&
                                currentRow != null &&
                                currentRow.getItem() != null &&
                                getTableRow().getItem() instanceof  MailItem &&
                                ((MailItem)currentRow.getItem()).unread) {
                            setStyle("-fx-font-weight: 800");
                        } else {
                            setStyle("-fx-font-weight: 400");
                        }
                    }
                };

                return cell;
            }
        });

        toCol.setCellValueFactory(new PropertyValueFactory("to"));
        toCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            setText(item.toString());
                        } else {
                            setText("");
                        }
                        setGraphic(null);

                        TableRow<Object> currentRow = getTableRow();
                        if (!isForbiddenOperationBox() &&
                                currentRow != null &&
                                currentRow.getItem() != null &&
                                getTableRow().getItem() instanceof  MailItem &&
                                ((MailItem)currentRow.getItem()).unread) {
                            setStyle("-fx-font-weight: 800");
                        } else {
                            setStyle("-fx-font-weight: 400");
                        }
                    }
                };

                return cell;
            }
        });

        sentDateCol.setCellValueFactory(new PropertyValueFactory("sentDate"));
        sentDateCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            setText(item.toString());
                        } else {
                            setText("");
                        }
                        setGraphic(null);

                        TableRow<Object> currentRow = getTableRow();
                        if (!isForbiddenOperationBox() &&
                                currentRow != null &&
                                currentRow.getItem() != null &&
                                getTableRow().getItem() instanceof  MailItem &&
                                ((MailItem)currentRow.getItem()).unread) {
                            setStyle("-fx-font-weight: 800");
                        } else {
                            setStyle("-fx-font-weight: 400");
                        }
                    }
                };

                return cell;
            }
        });

        searchComboBox.getEditor().setText("");
        FXCollections.sort(mMailList);
        FilteredList<MailItem> filteredData = new FilteredList<>(mMailList, p -> true);
        searchComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {

            filteredData.setPredicate(mailItem -> {

                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if (mailItem.fromProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (mailItem.subjectProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (mailItem.secLevelProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (mailItem.toProperty().get().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (mailItem.sizeProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (mailItem.receivedDateProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (mailItem.sentDateProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }

                return false;

            });
        });

        ChangeListener exListner = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                filteredData.setPredicate(mailItem -> {

                    boolean flag = false;

                    String sender = senderSearchField.getText();
                    if (sender == null || sender.isEmpty()) {
                        flag = true;
                    } else if (mailItem.fromProperty().getValue().toLowerCase().contains(sender.toLowerCase())){
                        flag = true;
                    } else {
                        return false;
                    }

                    String receiver = receiverSearchField.getText();
                    if (receiver == null || receiver.isEmpty()) {
                        flag = true;
                    } else if (mailItem.toProperty().getValue().toLowerCase().contains(receiver.toLowerCase())) {
                        flag = true;
                    } else {
                        return false;
                    }

                    String subject = subjectSearchField.getText();
                    if (subject == null || subject.isEmpty()) {
                        flag = true;
                    } else if (mailItem.subjectProperty().getValue().toLowerCase().contains(subject.toLowerCase())) {
                        flag = true;
                    } else {
                        return false;
                    }

                    String content = contentSearchField.getText();
                    if (content == null || content.isEmpty()) {
                        flag = true;
                    } else if (mailItem.content.toLowerCase().contains(content.toLowerCase())) {
                        flag = true;
                    } else {
                        return false;
                    }

                    if (fromSearchPicker.getValue() == null || fromSearchPicker.getValue().toString().isEmpty()) {
                        flag = true;
                    } else {
                        LocalDateTime fromDateTime = fromSearchPicker.getValue().atStartOfDay();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd(EEEE) hh:mm a");
                        String sentString = mailItem.sentDateProperty().getValue();
                        LocalDateTime sentDateTime = LocalDateTime.parse(sentString, formatter);
                        if (sentDateTime.compareTo(fromDateTime) > 0) {
                            flag = true;
                        } else {
                            return false;
                        }
                    }

                    if (toSearchPicker.getValue() == null || toSearchPicker.getValue().toString().isEmpty()) {
                        flag = true;
                    } else {
                        LocalDateTime toDateTime = toSearchPicker.getValue().atStartOfDay();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd(EEEE) hh:mm a");
                        String sentString = mailItem.sentDateProperty().getValue();
                        LocalDateTime sentDateTime = LocalDateTime.parse(sentString, formatter);
                        if (toDateTime.compareTo(sentDateTime) > 0) {
                            flag = true;
                        } else {
                            return false;
                        }
                    }

                    if (readStatusCombo.getSelectionModel().getSelectedIndex() == 1 && mailItem.unread == false) {
                        flag = true;
                    } else if (readStatusCombo.getSelectionModel().getSelectedIndex() == 2 && mailItem.unread == true) {
                        flag = true;
                    } else if (readStatusCombo.getSelectionModel().getSelectedIndex() == 0) {
                        flag = true;
                    } else {
                        return false;
                    }

                    int secIndex = securityLevelComboBox.getSelectionModel().getSelectedIndex();
                    if (secIndex != -1 && secIndex != 0) {
                        SecurityLevel selsecLevel = GlobalVariables.securityList.get(secIndex - 1);
                        setSecurityLevelCssTo(securityLevelComboBox, selsecLevel);
                        SecurityLevel mailSecLevel = AbstractController.getSecLevel(mailItem.referenceMsg);
                        if (selsecLevel.level == mailSecLevel.level)
                            flag = true;
                        else return false;
                    } else {
                        setSecurityLevelCssTo(securityLevelComboBox, secNoLevel);
                    }

                    return flag;
                });
                mainTableView.refresh();
            }
        };
        senderSearchField.textProperty().addListener(exListner);
        receiverSearchField.textProperty().addListener(exListner);
        subjectSearchField.textProperty().addListener(exListner);
        fromSearchPicker.valueProperty().addListener(exListner);
        toSearchPicker.valueProperty().addListener(exListner);
        readStatusCombo.valueProperty().addListener(exListner);
        securityLevelComboBox.valueProperty().addListener(exListner);
        contentSearchField.textProperty().addListener(exListner);

        // 3. Wrap the FilteredList in a SortedList.
        SortedList<MailItem> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(mainTableView.comparatorProperty());

        //add columns
        mainTableView.getColumns().add(selectAll);
        mainTableView.getColumns().add(markCol);
        mainTableView.getColumns().add(replyCol);
        mainTableView.getColumns().add(attachCol);
        mainTableView.getColumns().add(fromCol);
        mainTableView.getColumns().add(toCol);
        mainTableView.getColumns().add(securityCol);
        mainTableView.getColumns().add(subjectCol);
        mainTableView.getColumns().add(sentDateCol);
        mainTableView.getColumns().add(sizeCol);
        mainTableView.getColumns().add(starCol);
        mainTableView.getColumns().add(importantCol);

        // 5. Add sorted (and filtered) data to the table.
        mainTableView.setItems(sortedData);

        updateSelectionStatusLabel();

        handleClickedOnTableView(null);
    }

    /**
     * initialize the AddressBook table
     * @param type
     */
    public void initAddressBookTable(AddressBox type) {

        mainTableView.getColumns().clear();

        TableColumn selectAll = new TableColumn();
        TableColumn userNameCol = new TableColumn();
        TableColumn userDepartmentCol = new TableColumn();
        TableColumn mailAddressCol = new TableColumn();
        TableColumn userLevelCol = new TableColumn();
        TableColumn userSecurityLevelCol = new TableColumn();

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(selectAllCheckBox);
        selectAllCheckBox.setText("");
        selectAll.setGraphic(box);

        mainTableView.getColumns().add(selectAll);
        mainTableView.getColumns().add(userNameCol);
        mainTableView.getColumns().add(userDepartmentCol);
        mainTableView.getColumns().add(mailAddressCol);
        mainTableView.getColumns().add(userSecurityLevelCol);
        mainTableView.getColumns().add(userLevelCol);

        selectAll.setPrefWidth(26);
        userDepartmentCol.setPrefWidth(150);
        userNameCol.setPrefWidth(100);
        mailAddressCol.setPrefWidth(150);
        userLevelCol.setPrefWidth(150);
        userSecurityLevelCol.setPrefWidth(150);

        selectAll.setText("");
        userDepartmentCol.setText(getString("User_Department"));
        userNameCol.setText(getString("User_Name"));
        mailAddressCol.setText(getString("Mail_Address"));
        userLevelCol.setText(getString("User_Level"));
        userSecurityLevelCol.setText(getString("User_Security_Level"));

        userDepartmentCol.setCellValueFactory(new PropertyValueFactory("userDepartment"));
        userNameCol.setCellValueFactory(new PropertyValueFactory("userName"));
        mailAddressCol.setCellValueFactory(new PropertyValueFactory("mailAddress"));
        userLevelCol.setCellValueFactory(new PropertyValueFactory("userLevel"));
        userSecurityLevelCol.setCellValueFactory(new PropertyValueFactory("userSecurityLevel"));

        if (type.getBoxName().equalsIgnoreCase("public"))
            userDepartmentCol.setVisible(true);
        else {
            userDepartmentCol.setVisible(false);
            userSecurityLevelCol.setVisible(false);
            userLevelCol.setText(getString("note"));
            userLevelCol.setCellValueFactory(new PropertyValueFactory("userNote"));
        }

        selectedItemCnt = 0;
        selectAllCheckBox.setIndeterminate(false);
        selectAllCheckBox.setSelected(false);
        selectAll.setCellValueFactory(new PropertyValueFactory("selected"));
        selectAll.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            HBox box = new HBox();
                            box.setAlignment(Pos.CENTER);
                            CheckBox chkBox = new CheckBox();
                            chkBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {

                                    int index = getIndex();

                                    if (chkBox.isSelected())
                                        mainTableView.getSelectionModel().select(index);
                                    else
                                        mainTableView.getSelectionModel().clearSelection(index);

                                    handleSelectionItems();
                                }
                            });
                            chkBox.setSelected(Boolean.parseBoolean(item.toString()));
                            box.getChildren().add(chkBox);
                            setGraphic(box);
                        } else {
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        });

        userSecurityLevelCol.setCellFactory(new Callback<TableColumn<Object, Object>, TableCell<Object, Object>>() {
            @Override
            public TableCell<Object, Object> call(TableColumn<Object, Object> param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        if (item != null) {
                            HBox box= new HBox();
                            box.setSpacing(10) ;
                            VBox vbox = new VBox();

                            if(!item.toString().isEmpty())
                                vbox.getChildren().add(new Label(item.toString() + ""));
                            else
                                vbox.getChildren().add(new Label(""));

                            Node imageview = ViewFactory.defaultFactory.resolveUserSecurityColor(item.toString());

                            box.getChildren().addAll(imageview,vbox);
                            setGraphic(box);
                        } else {
                            setGraphic(null);
                        }
                    }
                };

                return cell;
            }
        });

        searchComboBox.getEditor().setText("");
        FilteredList<AddressBookItem> filteredData = new FilteredList<>(mAdbItemList, p -> true);
        searchComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("textfield changed from " + oldValue + " to " + newValue);
            filteredData.setPredicate(addressBookItem -> {

                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();
                if (addressBookItem.userIDProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (addressBookItem.userDepartmentProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (addressBookItem.userPathProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (addressBookItem.userNameProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (addressBookItem.mailAddressProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (addressBookItem.userLevelProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                if (addressBookItem.userSecurityLevelProperty().getValue().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches last name.
                }
                return false;

            });
        });

        // 3. Wrap the FilteredList in a SortedList.
        SortedList<AddressBookItem> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(mainTableView.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        mainTableView.setItems(sortedData);

        updateSelectionStatusLabel();
        mainTableView.refresh();
    }

    public static class WorkerThread implements Runnable {

        private String command;
        private boolean isCancel = false;
        private boolean isWorking = false;

        MailDatabase mdb;

        public WorkerThread(String s, MailDatabase mdb){
            this.command = s;
            this.mdb = mdb;
        }

        public WorkerThread(String s) {
            this.command = s;
        }

        public void setCancel() {
            isCancel = true;
        }

        public boolean isWorking() {
            return isWorking;
        }

        @Override
        public void run() {
            isCancel = false;
            isWorking = true;
            System.out.println(Thread.currentThread().getName()+" Start. Command = "+command);
            switch (this.command.toLowerCase()) {
                case "inbox":
                    GlobalVariables.IS_WORKING_INBOX = true;
                    try {
                        inboxCommand();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    GlobalVariables.IS_WORKING_INBOX = false;
                    break;
                case "pendingbox":
                    GlobalVariables.IS_WORKING_PENDING = true;
                    try {
                        pendingBoxCommand();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.gc();
                    GlobalVariables.IS_WORKING_PENDING = false;
                    break;
                case "approvebox":
                    GlobalVariables.IS_WORKING_APPROVE = true;
                    try {
                        approveBoxCommand();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    GlobalVariables.IS_WORKING_APPROVE = false;
                    break;
                case "update folder":
                    GlobalVariables.IS_WORKING_FOLDER_UPDATE = true;
                    startFetchFolder();
                    GlobalVariables.IS_WORKING_FOLDER_UPDATE = false;
                    break;
                case "update":
                    GlobalVariables.IS_WORKING_APP_UPDATE = true;
//                    updateCommand();
                    GlobalVariables.IS_WORKING_APP_UPDATE = false;
                    break;
                case "address book":
                    GlobalVariables.IS_WORKING_ADB = true;
                    syncAddressBook();
                    GlobalVariables.IS_WORKING_ADB = false;
                    break;
                case "tray":
                    GlobalVariables.IS_WORKING_TRAY = true;
                    startTrayMonitory();
                    GlobalVariables.IS_WORKING_TRAY = false;
                    break;
                default:
                    break;
            }
            isWorking = false;
            System.out.println(Thread.currentThread().getName()+" End."+command);
        }

        /**
         * fetch the folders
         */
        private void startFetchFolder() {
            try {
                int iCnt = 0;
                int totalCnt;
                for(;;) {

                    if (isCancel) {
                        System.out.println("---------fetchFolderWorker case 0 -------by-------exit");
                        break;
                    }

                    int interval = CHECK_NEW_INTERVALS_ARY[GlobalVariables.mainController.getNewMailInterval()];
                    totalCnt = interval * 1000;
                    if (iCnt <= totalCnt) {

                        Thread.sleep(10);

                        if (isCancel) {
                            System.out.println("---------fetchFolderWorker case 1 -------by-------exit");
                            break;
                        }

                        iCnt += 10;
                        continue;
                    }

                    if (isCancel) {
                        System.out.println("---------fetchFolderWorker case 2 -------by-------exit");
                        break;
                    }

                    iCnt = 0;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GlobalVariables.mainController.fetchMailBoxs();
                        }
                    });

                }
            } catch (Exception e) {
                System.out.println("---------fetchFolderWorker case 3 -------by--------exit----with follow messages----\n" + AbstractController.getStackTrace(e));
            }
        }

        /**
         * monitors the tray notification
         */
        private void startTrayMonitory() {
            try {
                int iCnt = 0;
                int totalCnt = 5000;
                for (;;) {
                    if (isCancel)
                        break;
                    CustomTrayNotification tray = mAryTray.peek();
                    if (tray == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    if (isCancel)
                        break;
                    if (tray != null) {
                        if (iCnt <= totalCnt) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (isCancel)
                                break;
                            iCnt += 10;
                            continue;
                        }
                        iCnt = 0;
                        Platform.runLater(()->{
                            if (!tray.isDismissed())
                                tray.dismiss(true);
                            mAryTray.remove(tray);
                        });
                    }
                    if (isCancel)
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * sync address book
         */
        private void syncAddressBook() {
            try {
                int iCnt = 0;
                int totalCnt;
                for (;;) {
                    if (isCancel)
                        break;
                    int interval = SYNC_ADB_INTERVALS_ARY[GlobalVariables.mainController.getSyncAdbInterval()];
                    totalCnt = interval * 1000;
                    if (iCnt <= totalCnt) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (isCancel)
                            break;
                        iCnt += 10;
                        continue;
                    }
                    iCnt = 0;
                    if (isCancel)
                        break;
                    GlobalVariables.mainController.syncAddressBook(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * update the app
         */
        private void updateCommand() {
            int responseCode = HttpURLConnection.HTTP_OK;
            StringBuffer response = new StringBuffer();
            response.setLength(0);

            try {

                XMailHttpRequest req = XMailHttpRequest.get(Apis.GET_UPDATE_INFO());
                HttpURLConnection con = req.getConnection();
                responseCode = con.getResponseCode();

                InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
                BufferedReader in = new BufferedReader(inputStreamReader);

                String inputLine;

                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                inputStreamReader.close();
                con.disconnect();
                System.out.println("responseCode = " + responseCode + ", app version = " + response.toString());
            } catch (Exception e) {
                System.out.println("responseCode = " + responseCode + ", exception = " + getStackTrace(e));
            }

            try {

                JSONParser jsonParser = new JSONParser();
                JSONObject jobj = (JSONObject) jsonParser.parse(response.toString());
                String serverVersion = jobj.get("version").toString().replaceAll("\\.", "");
                String localVersion = Apis.APP_VERSION.replaceAll("\\.", "");
                if (Integer.parseInt(serverVersion) > Integer.parseInt(localVersion)) {
                    System.out.println("-------new version exist!!!!");

                    //download the main.jar
                    File downloadDir = new File(GlobalVariables.APP_DATA_DIR, GlobalVariables.APP_DOWNLOAD_DIR);
                    if (!downloadDir.exists())
                        downloadDir.mkdirs();

                    XMailHttpRequest req = XMailHttpRequest.get(Apis.GET_APP_DOWNLOAD_URL());
                    HttpURLConnection con = req.getConnection();
                    responseCode = con.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream is = con.getInputStream();
                        File appF = new File(downloadDir.getAbsolutePath(), GlobalVariables.MAIN_JAR_NAME);
                        if (appF.exists())
                            appF.delete();

                        FileOutputStream fos = new FileOutputStream(appF);
                        int inByte;
                        byte[] buf = new byte[4096];
                        while((inByte = is.read(buf)) != -1) {
                            fos.write(buf, 0, inByte);
                        }
                        is.close();
                        fos.flush();
                        fos.close();

                        System.out.println("new version download complete");

                        //show the new version notification
                        Platform.runLater(()->GlobalVariables.mainController.showNewAppVersionNotification());
                    }

                    con.disconnect();
                }
            } catch (Exception e) {
                System.out.println("version parse error exception = " + getStackTrace(e));
            }
        }

        /**
         * fetch the approve mail status from the server
         */
        private void pendingBoxCommand() {
//            int iCnt = 0;
            ArrayList<HashMap<String, Object>> aryNotifyMap = new ArrayList<>();
            ObservableList<MailItem> msglist = mdb.loadApproveMails();
            for (MailItem item:msglist) {

                StringBuffer response = new StringBuffer();
                response.setLength(0);
                int responseCode = HttpURLConnection.HTTP_OK;
                JSONParser jsonParser = new JSONParser();
                String muid = "";

                try {
                    muid = item.referenceMsg.getHeader(MAIL_MUID_HEADER)[0];

                    String url = String.format(Apis.GET_MAIL_APPROVAL_STATUS(),
                            getPCName(),
                            muid,
                            GlobalVariables.account.getAddress());
                    XMailHttpRequest req = XMailHttpRequest.get(url);
                    HttpURLConnection con = req.getConnection();
                    responseCode = con.getResponseCode();

                    InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
                    BufferedReader in = new BufferedReader(inputStreamReader);

                    String inputLine;

                    while((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    in.close();
                    con.disconnect();
                    inputStreamReader.close();
                    System.out.println("pending status for muid = " + muid + ", responseCode = " + responseCode + ", pending response = " + response.toString());
                } catch (Exception e) {
                    System.out.println("pending status for muid = " + muid +
                            ", responseCode = " + responseCode +
                            ", exception = " + getStackTrace(e));
                }

                //for test approve
//                    response.setLength(0);
//                    response.append(
//                            "{" +
//                            "\"status\":20," +
//                            "\"rDate\":\"2018-10-16 15:38:26\"," +
//                            "\"rNote\":\"我想吃遍全世界\",\"curLevel\":2," +
//                            "\"approvers\":[[\"张三\",\"李四\"],[\"王五\"]]" +
//                            "}");

                try {

                    String finalApprover = "";
                    String notifyMsg = "";
                    HashMap<Integer, String> notifyMsgMap = new HashMap<>();

                    JSONObject jobj = (JSONObject) jsonParser.parse(response.toString());
                    int stat = Integer.parseInt(jobj.get("status").toString());

                    notifyMsgMap.put(APP_STAT_APPROVED, getString("notify_approved_mail"));
                    notifyMsgMap.put(APP_STAT_REJECTED, getString("notify_rejected_mail"));
                    notifyMsgMap.put(APP_STAT_CANCELED, getString("notify_canceled_mail"));

                    String base64String = Base64.getEncoder().encodeToString((jobj.toString().getBytes("utf-8")));

                    switch (stat) {
                        case APP_STAT_NO_APPROVE://no approve
                            break;
                        case APP_STAT_PENDING://pending
//                                iCnt++;
                            break;
                        case APP_STAT_APPROVED:
                        case APP_STAT_REJECTED:
                        case APP_STAT_CANCELED:

                            //approvers
                            JSONArray jAryApprovers = (JSONArray) jobj.get("approvers");
                            if (jAryApprovers != null && jAryApprovers.size() > 0) {
                                int iLen = jAryApprovers.size();
                                JSONArray jAryFinalApprover = (JSONArray) jAryApprovers.get(iLen - 1);
                                if (jAryFinalApprover != null && jAryFinalApprover.size() > 0) {
                                    finalApprover = jAryFinalApprover.get(jAryFinalApprover.size() - 1).toString();
                                }
                            }
                            //add approve status header with the approved status(20)
                            item.referenceMsg.setHeader(muid + "-" + MAIL_APPROVE_HEADER_INFO, base64String);

                            //move to sent box
                            if (!item.unread)
                                item.referenceMsg.setFlag(Flags.Flag.SEEN, true);
                            mdb.updateMessage(item.getUid(), item.referenceMsg);

                            notifyMsg = String.format(notifyMsgMap.get(stat), finalApprover);

                            HashMap<String, Object> appMap = new HashMap<>();
                            appMap.put("item", item);
                            appMap.put("msg", notifyMsg);
                            aryNotifyMap.add(appMap);

                            break;
                        default:
                            break;
                    }

                } catch (Exception e) {
                    System.out.println("pending status for muid = " + muid +
                            ", responseCode = " + responseCode);
                }

            }

//            final int msgCnt = iCnt;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    /*
                    String txt = "";
                    if (msgCnt > 0) {
                        txt = getString("tb.pending_approval") + " (" + msgCnt + ")";
                    } else {
                        txt = getString("tb.pending_approval");
                    }
                    GlobalVariables.mainController.setTextToPendingApproveButton(txt);
                    */
                    if (aryNotifyMap.size() > 0) {
                        for (HashMap<String, Object> eachMap : aryNotifyMap) {
                            GlobalVariables.mainController.showApproveNotification(eachMap);
                        }
                        GlobalVariables.mainController.updateApproveStatus(aryNotifyMap);
                    }
                }
            });
        }

        /**
         * fetch the approve mail count from the server
         */
        private void approveBoxCommand() {
            int iCnt = 0;
            try {
                String url = String.format(Apis.GET_MAIL_APPROVAL_CNT(), getPCName());
                XMailHttpRequest req = XMailHttpRequest.get(url);
                HttpURLConnection con = req.getConnection();
                int responseCode = con.getResponseCode();

                InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
                BufferedReader in = new BufferedReader(inputStreamReader);

                String inputLine;
                StringBuffer response = new StringBuffer();
                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                System.out.println("approve count = " + response.toString() + ", responseCode = " + responseCode);
                JSONParser jsonParser = new JSONParser();
                try {
                    JSONObject jobj = (JSONObject) jsonParser.parse(response.toString());
                    iCnt = Integer.parseInt(jobj.get("approveNumber").toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                in.close();
                con.disconnect();
                inputStreamReader.close();
                inputStreamReader = null;
                jsonParser = null;
                response = null;
                in = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //for test
//            iCnt = 1;
            final int msgCnt = iCnt;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (msgCnt > 0) {
                        //notify new approval mail exists
                        GlobalVariables.mainController.showNewApproveTrayNotification(String.format(getString("notify_new_approve_mail"), msgCnt), NotificationType.NOTICE);
                    } else {
                        GlobalVariables.mainController.isShownNewApproveNotification = false;
                    }

                    GlobalVariables.mainController.setTextToMailApproveButton(msgCnt);
                }
            });
        }

        /**
         * fetch the new message for the server
         */
        private void inboxCommand() {
            if (GlobalVariables.mainController.getModelAccess() != null && FetchFoldersService.noServicesActive()) {
                try {

                    Folder inboxFolder = null;
                    for(Folder folder: GlobalVariables.mainController.getModelAccess().getFolderList()) {
                        if (folder.getName().equalsIgnoreCase("inbox")) {
                            inboxFolder = folder;
                            break;
                        }
                    }

                    System.out.println("Checking for folders!! === " + inboxFolder.getName());

                    try {

                        if(inboxFolder.getType() != Folder.HOLDS_FOLDERS && !inboxFolder.isOpen()){
                            inboxFolder.open(Folder.READ_WRITE);
                        }

                        int msgCnt = inboxFolder.getMessageCount();
                        int iNewCnt = 0;
                        int isNew;
                        HashMap<String, Message> mesageMap = new HashMap<>();

                        for(int i = msgCnt; i > 0 ; i--) {
                            POP3Folder pop3Folder = (POP3Folder) inboxFolder;
                            Message currentMessage = inboxFolder.getMessage(i);
                            String uid = pop3Folder.getUID(currentMessage);
                            mesageMap.put(uid, currentMessage);
                        }

                        Set<Map.Entry<String, Message>> msgEntry = mesageMap.entrySet();
                        HashMap<String, String> msgMap = new HashMap<>();
                        for (Map.Entry<String, Message> eachEntry: msgEntry) {
                            try {
                                StringBuffer outBox = new StringBuffer();
                                outBox.setLength(0);
                                isNew = GlobalVariables.mainController.insertMailItem(inboxFolder.getName(), eachEntry.getKey(), eachEntry.getValue(), true, null);
                                if (isNew == MailDatabase.STAT_NEW) {
                                    System.out.println("insertMailItemToUI " + outBox + " box uid = " + eachEntry.getKey());
                                    eachEntry.getValue().setFlag(Flags.Flag.DELETED, true);
                                    msgMap.put(eachEntry.getKey(), outBox.toString());
                                    iNewCnt++;
                                } else if (isNew == MailDatabase.STAT_ALREADY) {
                                    System.out.println("already exist " + outBox + " box uid = " + eachEntry.getKey());
                                    eachEntry.getValue().setFlag(Flags.Flag.DELETED, true);
                                    System.out.println("set the delete flag " + eachEntry.getKey());
                                }
                            } catch (Exception e) {
                                System.out.println("can't read the message num = " + eachEntry.getValue().getMessageNumber());
                            }
                        }

                        inboxFolder.close(true);

                        if (iNewCnt > 0) {
                            int finalINewCnt = iNewCnt;
                            Message msg = null;
                            Map.Entry<String, String> oneMap = msgMap.entrySet().iterator().next();
                            if (iNewCnt == 1) {
                                msg = GlobalVariables.mainController.getOneMessage(oneMap.getKey());
                            }
                            Message oneMsg = msg;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {

                                    System.out.println("notify new mail uid = " + oneMap.getKey());

                                    if (finalINewCnt > 1)
                                        GlobalVariables.mainController.showNewEmailNotification(oneMap.getValue(), finalINewCnt, null, "");
                                    else
                                        GlobalVariables.mainController.showNewEmailNotification(oneMap.getValue(), finalINewCnt, oneMsg, oneMap.getKey());

                                    GlobalVariables.mainController.insertMailItemToUI(msgMap.entrySet());
                                }
                            });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            inboxFolder.close(true);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.gc();
                }
            }
        }

        @Override
        public String toString(){
            return this.command;
        }
    }

    /**
     * move pending items to the sent box
     * @param itemList
     */
    public void updateApproveStatus(ArrayList<HashMap<String, Object>> itemList) {
        for (HashMap eachMap: itemList) {
            MailItem mapItem = (MailItem) eachMap.get("item");
            for (MailBox boxItem: mMailBoxList) {
                boxItem.updateMessage(mapItem.getUid(), mapItem.referenceMsg);
                if (mMailBoxList.get(curSelMailCategoryIndex).getBoxName().equalsIgnoreCase(mapItem.boxName)) {
                    int index = 0;
                    int selIndex = -1;
                    for (MailItem item1: mMailList) {
                        if (item1.getUid().compareToIgnoreCase(mapItem.getUid()) == 0) {
                            selIndex = index;
                            break;
                        }
                        index++;
                    }
                    if (selIndex >= 0) {
                        mMailList.get(selIndex).copyFrom(mapItem.referenceMsg);
                        updateApproveStatInPrevMail(mMailList.get(selIndex));
                    }
                }
            }

            updateApproveOfEmailDetailScene(mapItem);
        }
        categoryItemListView.refresh();
        mainTableView.refresh();
    }

    /**
     * set the approve mail count text to the mail approval button
     * @param aTxt
     */
    public void setTextToPendingApproveButton(String aTxt) {
        pendingApproveMailButon.setText(aTxt);
    }

    /**
     * set the approve mail count text to the mail approval button
     * @param aCnt
     */
    public void setTextToMailApproveButton(int aCnt) {
        String txt = getString("tb.mail_approval");
        if (aCnt > 0)
            txt = getString("tb.mail_approval") + " (" + aCnt + ")";
        approveCntTxt = txt;
        approveCnt = aCnt;
        mailApproveMailButon.setText(approveCntTxt);
        categoryItemListView.refresh();
        categoryListView.refresh();
    }

    /**
     * cycle service according to the settings inbox interval
     */
    public void fetchMailBoxs() {

        if (inboxWorker == null)
            inboxWorker = new WorkerThread("Inbox", mdb);

        if (pendingWorker == null)
            pendingWorker = new WorkerThread("PendingBox", mdb);

        if (approveWorker == null)
            approveWorker = new WorkerThread("ApproveBox", mdb);

        if (!inboxWorker.isWorking())
            executor.execute(inboxWorker);

        if (!pendingWorker.isWorking())
            executor.execute(pendingWorker);

        if (!approveWorker.isWorking())
            executor.execute(approveWorker);
    }

    @FXML
    public void handleClickedOnSendReceiveMenuButton() {
        System.out.println("handleClickedOnSendReceiveMenuButton");
        fetchMailBoxs();
    }

    /**
     * show preview view
     * @param mailItem
     */
    public void showPrevMail(MailItem mailItem) {
        showPrevMail(mailItem, false);
    }

    /**
     * show preview mail contents
     * @param mailItem
     */
    public void showPrevMail(MailItem mailItem, boolean isForceRefresh) {
        if (mailItem != null) {

            if (!isForceRefresh && mCurSelectedMailItem != null && mCurSelectedMailItem.equals(mailItem))
                return;

            mCurSelectedMailItem = mailItem;

            int index = mainSplitePane.getItems().indexOf(rightAnchorPane);
            if (index == -1)
                return;

            if (mailItem.boxName.equalsIgnoreCase("draft")) {
                actionOnHidePreviewMenuItem(null);
                return;
            }

            if (mailItem.unread)
                updateMessageReadStatus(mailItem, true);

            Message message = mailItem.referenceMsg;

            fromLabel.setText(String.format(getString("mail_from"), mailItem.fromProperty().getValue()));
            subjectLabel.setText(mailItem.subjectProperty().getValue());
            dateLabel.setText(mailItem.receivedDateProperty().getValue());

            updateImportantButtonStatInPrevMail(mCurSelectedMailItem);

            try {

                approveStatVBox.getChildren().clear();
                toFollowPane.getChildren().clear();
                ccFlowPane.getChildren().clear();
                bccFlowPane.getChildren().clear();
                attachFlowPane.getChildren().clear();

                //to header
                {
                    Label header = new Label(getString("ToColon"));
                    header.setFont(new Font(15));
                    toFollowPane.getChildren().add(header);
                }

                //mail security level
                String secLevel[] = message.getHeader(MAIL_SEC_LEVEL_HEADER);

                //check approve mail
                if (mailItem.isApproveMail()) {

                    //approve status header info
                    String[] appStatInfo = message.getHeader(MailItem.getMuidFromMsg(message) + "-" + MAIL_APPROVE_HEADER_INFO);
                    JSONParser jsonParser = new JSONParser();
                    if (appStatInfo != null && appStatInfo.length > 0) {
                        String res = new String(Base64.getDecoder().decode(appStatInfo[0]));
                        JSONObject jAppStat = (JSONObject) jsonParser.parse(res);

                        //approve status box
                        int appStat = Integer.parseInt(jAppStat.get("status").toString());
                        String appStatString = getStringOfApproveStat(appStat);
                        HBox approverStatHBox = getApproveInfoHBox(getString("approve_status"), appStatString);
                        approveStatVBox.getChildren().add(approverStatHBox);

                        //approve date
                        HBox approveDateHBox = getApproveInfoHBox(getString("approve_date"), formattedDateString(jAppStat.get("rDate").toString()));
                        approveStatVBox.getChildren().add(approveDateHBox);

                        HBox approversHBox = getApproveInfoHBox(getString("approver"), jAppStat.get("approvers").toString());
                        approveStatVBox.getChildren().add(approversHBox);

                        HBox approveInstructionHBox = getApproveInfoHBox(getString("approve_info"), jAppStat.get("rNote").toString());
                        approveStatVBox.getChildren().add(approveInstructionHBox);
                    } else {
                        //approve status box
                        String appStatString = getStringOfApproveStat(10); //pending
                        HBox approverStatHBox = getApproveInfoHBox(getString("approve_status"), appStatString);
                        approveStatVBox.getChildren().add(approverStatHBox);

                        //approve date
                        HBox approveDateHBox = getApproveInfoHBox(getString("approve_date"), mCurSelectedMailItem.sentDateProperty().getValue());
                        approveStatVBox.getChildren().add(approveDateHBox);

                        Task task = new Task() {
                            @Override
                            protected Object call() throws Exception {
                                updateMessage(getString("loading"));
                                String approvers = getApprovers(message);
                                Platform.runLater(()->{
                                    hideWaitingDialog();
                                    try {
                                        HBox approversHBox = getApproveInfoHBox(getString("approver"), "[" + approvers + "]");
                                        approveStatVBox.getChildren().add(approversHBox);

                                        String desc = new String(Base64.getDecoder().decode(message.getHeader(MAIL_APPROVE_HEADER_DESC)[0]), "utf-8");
                                        HBox approveInstructionHBox = getApproveInfoHBox(getString("approve_info"), getUtf8String(desc.trim()));
                                        approveStatVBox.getChildren().add(approveInstructionHBox);
                                    } catch (Exception e) {
                                        e.printStackTrace();
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

                    //to header
                    String[] appInfo = message.getHeader(MAIL_APPROVE_HEADER_ORG_TO);
                    String[] toList = appInfo[0].split(",");
                    for (int i = 0; i < toList.length; i++) {
                        Label to1 = new Label(getFormattedEmailFrom(getUtf8String(toList[i])));
                        to1.setFont( new Font(15));
                        if (i  > 0) {
                            to1.setText("; " + getFormattedEmailFrom(getUtf8String(toList[i])));
                        }
                        toFollowPane.getChildren().add(to1);
                    }
                } else {

                    Address[] addresslist = message.getRecipients(Message.RecipientType.TO);
                    if (addresslist != null && addresslist.length > 0) {
                        for (int i = 0; i < addresslist.length; i++) {
                            Label to1 = new Label(getFormattedEmailFrom(getUtf8String(addresslist[i].toString())));
                            to1.setFont(new Font(15));
                            if (i  > 0) {
                                to1.setText("; " + getFormattedEmailFrom(getUtf8String(addresslist[i].toString())));
                            }
                            toFollowPane.getChildren().add(to1);
                        }
                    }
                }

                Address[] addresslist = message.getRecipients(Message.RecipientType.CC);
                if (addresslist != null) {
                    Label header = new Label(getString("mail_cc"));
                    header.setFont(new Font(15));
                    ccFlowPane.getChildren().add(header);
                    for (int i = 0; i < addresslist.length; i++) {
                        Label to1 = new Label(getFormattedEmailFrom(getUtf8String(addresslist[i].toString())));
                        to1.setFont( new Font(15));
                        if (i  > 0) {
                            to1.setText("; " + getFormattedEmailFrom(getUtf8String(addresslist[i].toString())));
                        }
                        ccFlowPane.getChildren().add(to1);
                    }
                }

                addresslist = message.getRecipients(Message.RecipientType.BCC);
                if (addresslist != null) {
                    Label header = new Label(getString("mail_bcc"));
                    bccFlowPane.getChildren().add(header);
                    for (int i = 0; i < addresslist.length; i++) {
                        Label to1 = new Label(getFormattedEmailFrom(getUtf8String(addresslist[i].toString())));
                        to1.setFont( new Font(15));
                        if (i  > 0) {
                            to1.setText("; " + getFormattedEmailFrom(getUtf8String(addresslist[i].toString())));
                        }
                        bccFlowPane.getChildren().add(to1);
                    }
                }

                //mail security level
                SecurityLevel mailSecLevel = null;
                if (secLevel != null && secLevel.length > 0) {
                    mailSecLevel = getSecLevel(secLevel[0]);
//                    System.out.println("security level = " + mailSecLevel);
                    mailSecurityClassLabel.setText("[" + mailSecLevel.toString() + "]");
                    mailSecurityClassLabel.setStyle("-fx-text-fill: " + mailSecLevel.levelColor);
                } else {
                    mailSecLevel = GlobalVariables.securityList.get(0);
                    mailSecurityClassLabel.setText("[" + mailSecLevel.toString() + "]");
                    mailSecurityClassLabel.setStyle("-fx-text-fill: " + mailSecLevel.levelColor);
                }

                String content = mCurSelectedMailItem.content;

                //mail return note
                procReturnNoteMail(mailItem);

                WebEngine engine = mailPrevContentWebView.getEngine();
                if(content.contains("<body contenteditable=\"true\"")){
                    content = content.replaceAll("<body contenteditable=\"true\"", "<body contenteditable=\"false\"");
                }

//                final LongProperty startTime   = new SimpleLongProperty();
//                final LongProperty endTime     = new SimpleLongProperty();
//                final LongProperty elapsedTime = new SimpleLongProperty();
//
//                statusLabel.textProperty().bind(
//                        Bindings.when(elapsedTime.greaterThan(0))
//                                .then(Bindings.concat(getString("loaded_page_in"), elapsedTime.divide(1_000_000), "ms")
//                                ).otherwise(getString("loading"))
//                );
//
//                elapsedTime.bind(Bindings.subtract(endTime, startTime));
//
//                engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
//                    @Override
//                    public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State state) {
//                        switch (state) {
//                            case RUNNING:
//                                startTime.set(System.nanoTime());
//                                break;
//
//                            case SUCCEEDED:
//                                endTime.set(System.nanoTime());
//                                break;
//                        }
//                    }
//                });

                engine.loadContent(content);

                if (mCurSelectedMailItem.attachProperty().getValue()) {
                    //attach file
                    clearAttachFiles(MailItem.getUidFromMsg(message));
                    Task <Void> task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            updateMessage(getString("loading_attach"));
                            Multipart multipart = (Multipart) getMailContents(MailItem.getUidFromMsg(message)).getContent();
                            HashMap<EmailSenderService.AttachFile, BodyPart> bodyPartMap = new HashMap<>();
                            for (int i = 0; i < multipart.getCount(); i++) {
                                BodyPart bodyPart = multipart.getBodyPart(i);
                                System.out.println(bodyPart.getContentType());
                                if (bodyPart.getFileName() != null && bodyPart.getFileName().length() > 0) {

                                    String fileName = bodyPart.getFileName();
                                    System.out.println(fileName);

                                    File attachFile = getAttachFile(bodyPart, MailItem.getUidFromMsg(message));

                                    String[] attachLevel = bodyPart.getHeader(MAIL_ATTACH_SEC_LEVEL_HEADER);
                                    SecurityLevel attachFileLevel = null;
                                    if (secLevel != null && secLevel.length > 0) {
                                        System.out.println("attach security level = " + attachLevel[0]);
                                        attachFileLevel = getSecLevel(attachLevel[0]);
                                    }

                                    bodyPartMap.put(new EmailSenderService.AttachFile(attachFile, attachFileLevel), bodyPart);
                                }
                            }

                            Platform.runLater(()->{
                                hideWaitingDialog();
                                Set<Map.Entry<EmailSenderService.AttachFile, BodyPart>> mapEntry = bodyPartMap.entrySet();
                                for (Map.Entry<EmailSenderService.AttachFile, BodyPart> eachMap: mapEntry) {

                                    BodyPart bodyPart = eachMap.getValue();
                                    EmailSenderService.AttachFile attachFile = eachMap.getKey();
                                    SecurityLevel attachFileLevel = attachFile.getSecLevel();

                                    try {
                                        String fileName = getUtf8String(bodyPart.getFileName());
                                        System.out.println(fileName);

                                        SplitMenuButton attachButton = new SplitMenuButton();

                                        attachButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                            @Override
                                            public void handle(MouseEvent event) {
                                                System.out.println("clicked on");
                                                File f = getAttachFile(bodyPart, MailItem.getUidFromMsg(message));
                                                openFileAsSystemDefaultApp(f);
                                            }
                                        });

                                        ImageView imgV = getFileIcon(new File(fileName));
                                        attachButton.setGraphic(imgV);

                                        attachButton.getStyleClass().add("attach-menu-button1");

                                        attachButton.setMnemonicParsing(false);
                                        attachButton.setText(fileName + "(" + getFormattedSize((int)attachFile.getFile().length()) + ")");

                                        MenuItem actionItemView = new MenuItem();
                                        MenuItem actionItemOpenFolder = new MenuItem();
                                        MenuItem actionItemSaveAs = new MenuItem();

                                        actionItemView.setText(getString("Open"));
                                        actionItemOpenFolder.setText(getString("Open_Folder"));
                                        actionItemSaveAs.setText(getString("Save_as"));

                                        actionItemView.setOnAction(new EventHandler<ActionEvent>() {
                                            public void handle(ActionEvent t) {
                                                File f = getAttachFile(bodyPart, MailItem.getUidFromMsg(message));
                                                openFileAsSystemDefaultApp(f);
                                            }
                                        });

                                        actionItemSaveAs.setOnAction(new EventHandler<ActionEvent>() {
                                            public void handle(ActionEvent t) {
                                                try {

                                                    File srcF = getAttachFile(bodyPart, MailItem.getUidFromMsg(message));
                                                    FileChooser fileChooser = new FileChooser();
                                                    fileChooser.setTitle(getString("save_as_file"));
                                                    fileChooser.setInitialFileName(fileName);
                                                    fileChooser.setInitialDirectory(srcF.getParentFile());
                                                    File destF = fileChooser.showSaveDialog(getOwnerStage());
                                                    if (destF != null) {
                                                        if (destF.exists())
                                                            destF.delete();
                                                        srcF.renameTo(destF);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                        actionItemOpenFolder.setOnAction(new EventHandler<ActionEvent>() {
                                            public void handle(ActionEvent t) {
                                                System.out.println("Delete action on the [" + attachFile.getFile().getName() + "] file");
                                                try {
                                                    Desktop.getDesktop().open(attachFile.getFile().getParentFile());
                                                } catch ( Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                        attachButton.getItems().add(actionItemView);
                                        attachButton.getItems().add(actionItemOpenFolder);
                                        attachButton.getItems().add(actionItemSaveAs);

                                        HBox box= new HBox();
                                        box.setSpacing(5) ;
                                        box.setAlignment(Pos.CENTER_LEFT);
                                        box.getStyleClass().add("attach-menu-button");
                                        Label attachClassLabel = new Label();

                                        attachClassLabel.setText("[" + attachFileLevel.toString() + "]");
                                        attachClassLabel.setStyle("-fx-text-fill: " + attachFileLevel.levelColor);

                                        addAttachFile(MailItem.getUidFromMsg(message), new EmailSenderService.AttachFile(attachFile.getFile(), attachFileLevel));
                                        box.getChildren().addAll(attachButton, attachClassLabel);

                                        box.setUserData(imgV.getUserData());
                                        attachFlowPane.setHgap(5);
                                        attachFlowPane.setVgap(5);
                                        attachFlowPane.setPadding(new Insets(5, 0, 5,0));
                                        attachFlowPane.getChildren().add(box);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
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

            } catch (Exception e) {
                e.printStackTrace();
            }

            favLabel.setText("");
            updateStarButtonStatInPrevMail(mailItem);
            categoryItemListView.refresh();
        }
    }

    @Override
    public Stage getOwnerStage() {
        return (Stage) mainTableView.getScene().getWindow();
    }

    /**
     * update the flag status of the preview mail layout's follow button
     * @param aItem
     */
    public void updateImportantButtonStatInPrevMail(MailItem aItem) {
        if (mCurSelectedMailItem != null && aItem.getUid() == mCurSelectedMailItem.getUid()) {
            if (aItem.isImportantMail()) {
                importantLabel1.setText(getString("ImportantMail"));
                importantLabel.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/follow_up_small.png"));
            } else {
                importantLabel1.setText("");
                importantLabel.setGraphic(null);
            }
        }
    }

    /**
     * update the approve status of the preview mail layout's follow button
     * @param aItem
     */
    public void updateApproveStatInPrevMail(MailItem aItem) {
        if (mCurSelectedMailItem != null && aItem.getUid() == mCurSelectedMailItem.getUid()) {
            showPrevMail(aItem, true);
        }
    }

    /**
     * update the flag status of the preview mail layout's follow button
     * @param aItem
     */
    public void updateStarButtonStatInPrevMail(MailItem aItem) {
        if (mCurSelectedMailItem != null && aItem.getUid() == mCurSelectedMailItem.getUid()) {
            try {
                boolean isFav = aItem.referenceMsg.isSet(Flags.Flag.FLAGGED);
                if (isFav) {
                    favLabel.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/fav_yes.png"));
                } else {
                    favLabel.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/fav_no.png"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleClickedOnTableView(MouseEvent event) {

        if (isSelectedMailCategory()) {
            MailItem item = (MailItem) mainTableView.getSelectionModel().getSelectedItem();
            if (event != null)
                showPrevMail(item);
            if(event != null && event.isControlDown()) {
                handleSelectionItems();
            }
        }

        if (event != null && event.getButton()== MouseButton.SECONDARY && isSelectedAdbCategory()) {

            AddressBox selectedAdbBox = (AddressBox)categoryItemListView.getSelectionModel().getSelectedItem();
            ContextMenu contextMenu = new ContextMenu();
            ObservableList<AddressBookItem> selectedAdbItems = mainTableView.getSelectionModel().getSelectedItems();

            MenuItem addMenuItem = new MenuItem(getString("AddUser"));
            addMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String adbBox = ((AddressBox)categoryItemListView.getSelectionModel().getSelectedItem()).getBoxName();
                    Scene scene = ViewFactory.defaultFactory.getAddUserScene(null, adbBox, false);
                    CustomStage stage = new CustomStage();
                    stage.setScene(scene);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle(getString("AddUser"));
                    stage.show();
                }
            });

            MenuItem moveToOftenMenuItem = new MenuItem(getString("mv_to_often_used"));
            moveToOftenMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AddressBox oftenUsedBox = null;
                    for (AddressBox eachBox: mAdbBoxList) {
                        if (eachBox.getBoxName().equalsIgnoreCase("often")) {
                            oftenUsedBox = eachBox;
                            break;
                        }
                    }
                    if (oftenUsedBox != null) {

                        AddressBox finalOftenUsedBox = oftenUsedBox;
                        Task task = new Task() {
                            @Override
                            protected Object call() throws Exception {
                                updateMessage(getString("updating"));
                                ObservableList<AddressBookItem> copiedItems = finalOftenUsedBox.adbCopyFrom(selectedAdbItems);
                                mdb.removeAddress(selectedAdbBox.getBoxName(), selectedAdbItems);
                                mdb.updateAdbItem("Often", selectedAdbBox.getBoxName(), copiedItems);
                                Platform.runLater(()->{
                                    hideWaitingDialog();
                                    finalOftenUsedBox.addAdbItem(copiedItems);
                                    selectedAdbBox.getAddrlist().removeAll(selectedAdbItems);
                                    mAdbItemList.removeAll(selectedAdbItems);
                                    mainTableView.getSelectionModel().clearSelection();
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
                }
            });

            MenuItem deleteMenuItem = new MenuItem(getString("DeleteUser"));
            deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    Task task = new Task() {
                        @Override
                        protected Object call() throws Exception {
                            updateMessage(getString("deleting"));
                            mdb.removeAddress(selectedAdbBox.getBoxName(), selectedAdbItems);
                            Platform.runLater(()->{
                                hideWaitingDialog();
                                selectedAdbBox.getAddrlist().removeAll(selectedAdbItems);
                                mAdbItemList.removeAll(selectedAdbItems);
                                mainTableView.getSelectionModel().clearSelection();
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
            });

            MenuItem editMenuItem = new MenuItem(getString("EditUser"));
            editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String adbBox = selectedAdbBox.getBoxName();
                    for (AddressBookItem addressItem : selectedAdbItems) {
                        Random random = new Random();
                        Scene scene = ViewFactory.defaultFactory.getAddUserScene(addressItem, adbBox, true);
                        CustomStage stage = new CustomStage();
                        stage.setScene(scene);
                        stage.setX(random.nextInt(100) + 400);
                        stage.setY(random.nextInt(100) + 200);
                        stage.setTitle(getString("EditUser"));
                        stage.show();
                    }
                }
            });

            MenuItem newMailMenuItem = new MenuItem(getString("NewMail"));
            newMailMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    newMailWithRecipient(selectedAdbItems);
                }
            });

            Menu copyToMenu = new Menu(getString("CopyTo"));
            for (int i = 0; i < mAdbBoxList.size(); i ++ ) {
                AddressBox eachAdbBox = mAdbBoxList.get(i);
                if (eachAdbBox.getBoxName().equalsIgnoreCase("public") || eachAdbBox.getBoxName().equalsIgnoreCase(selectedAdbBox.getBoxName()))
                    continue;
                MenuItem boxMenuItem = new MenuItem(eachAdbBox.toString());
                boxMenuItem.setId("" + i);
                boxMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        MenuItem menuItem = (MenuItem) event.getSource();
                        AddressBox destBox = mAdbBoxList.get(Integer.parseInt(menuItem.getId()));
                        ObservableList<AddressBookItem> copiedItems = destBox.adbCopyFrom(selectedAdbItems);

                        Task task = new Task() {
                            @Override
                            protected Object call() throws Exception {
                                updateMessage(getString("copying"));
                                mdb.insertAddress(destBox.getBoxName(), copiedItems);
                                Platform.runLater(()->{
                                    hideWaitingDialog();
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
                });
                copyToMenu.getItems().add(boxMenuItem);
            }

            if (selectedAdbBox != null) {
                if (!selectedAdbBox.getBoxName().equalsIgnoreCase("public")) {
                    if (mainTableView.getItems().size() == 0) {
                        contextMenu.getItems().addAll(addMenuItem);
                    } else {
                        if (mainTableView.getSelectionModel().getSelectedIndex() == -1) {
                            contextMenu.getItems().addAll(addMenuItem);
                        } else {
                            if (!selectedAdbBox.getBoxName().equalsIgnoreCase("often"))
                                contextMenu.getItems().addAll(newMailMenuItem, addMenuItem, editMenuItem, deleteMenuItem, moveToOftenMenuItem, copyToMenu);
                            else
                                contextMenu.getItems().addAll(newMailMenuItem, addMenuItem, editMenuItem, deleteMenuItem, copyToMenu);
                        }
                    }
                } else {
                    if (selectedAdbItems != null && selectedAdbItems.size() > 0) {
                        contextMenu.getItems().addAll(newMailMenuItem, copyToMenu);
                    }
                }
                mainTableView.setContextMenu(contextMenu);
            } else {
                mainTableView.setContextMenu(null);
            }
        }

        if (event != null && event.getButton()== MouseButton.SECONDARY && isSelectedMailCategory()) {
            ContextMenu contextMenu = new ContextMenu();
            ObservableList<MailItem> selectedMailItems = mainTableView.getSelectionModel().getSelectedItems();

            MenuItem markUnread = new MenuItem(getString("mark_unread"));
            MenuItem markRead = new MenuItem(getString("mark_read"));
            Menu moveToMenu = new Menu(getString("MoveTo"));

            for (int i = 0; i < mMailBoxList.size(); i ++ ) {
                MailBox eachMBox = mMailBoxList.get(i);
                if (isForbiddenOperationBox(eachMBox.getBoxName()) || isEqualBoxWith(eachMBox.getBoxName()))
                    continue;
                MenuItem boxMenuItem = new MenuItem(eachMBox.toString());
                boxMenuItem.setId("" + i);
                boxMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        // Move selected mail into selected box
                        MenuItem mItem = (MenuItem) event.getSource();
                        MailBox srcBox = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
                        MailBox destBox = mMailBoxList.get(Integer.parseInt(mItem.getId()));

                        Task <Void> task = new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                updateMessage(getString("moving"));
                                mdb.moveMailToBox(destBox.getBoxName(), selectedMailItems);
                                Platform.runLater(()->{
                                    hideWaitingDialog();
                                    destBox.addAllMessage(selectedMailItems);
                                    srcBox.removeAllMsg(selectedMailItems);
                                    mMailList.removeAll(selectedMailItems);
                                    mainTableView.getSelectionModel().clearSelection();
                                    mainTableView.refresh();
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
                });
                // Add sub menu items for each custom mail boxes
                moveToMenu.getItems().add(boxMenuItem);
            }

            mainTableView.setContextMenu(null);

            if (!isForbiddenOperationBox() && selectedMailItems.size() > 0) {
                boolean unReadExist = false;
                for (MailItem mailItem : selectedMailItems) {
                    if (mailItem.unread) {
                        unReadExist = true;
                    }
                }

                if (unReadExist) {
                    contextMenu.getItems().add(markRead);
                    contextMenu.getItems().add(moveToMenu);
                } else {
                    contextMenu.getItems().add(markUnread);
                    contextMenu.getItems().add(moveToMenu);
                }
                mainTableView.setContextMenu(contextMenu);
            }

            // These below blocks are imported from MainController.java#252
            markUnread.setOnAction(e->{
                try {

                    for (MailItem mailItem : selectedMailItems) {
                        mailItem.referenceMsg.setFlag(Flags.Flag.SEEN, false);
                        mailItem.unread = true;
                        for (MailBox box: mMailBoxList) {
                            box.updateMessage(mailItem.getUid(), mailItem.referenceMsg);
                        }
                    }

                    Task <Void> task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            updateMessage(getString("updating"));
                            mdb.updateMessageReadStatus(selectedMailItems, false);
                            Platform.runLater(()->{
                                hideWaitingDialog();
                                mainTableView.refresh();
                                categoryItemListView.refresh();
                            });
                            return null;
                        }
                    };

                    showWaitingDialog(getOwnerStage());
                    waitingMsgProperty.bind(task.messageProperty());
                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
            markRead.setOnAction(e->{
                try {
                    for (MailItem mailItem : selectedMailItems) {
                        mailItem.referenceMsg.setFlag(Flags.Flag.SEEN, true);
                        mailItem.unread = false;
                        for (MailBox box: mMailBoxList) {
                            box.updateMessage(mailItem.getUid(), mailItem.referenceMsg);
                        }
                    }

                    Task <Void> task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            updateMessage(getString("updating"));
                            mdb.updateMessageReadStatus(selectedMailItems, true);
                            Platform.runLater(()->{
                                hideWaitingDialog();
                                mainTableView.refresh();
                                categoryItemListView.refresh();
                            });
                            return null;
                        }
                    };

                    showWaitingDialog(getOwnerStage());
                    waitingMsgProperty.bind(task.messageProperty());
                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        }
    }

    public boolean isForbiddenOperationBox() {
        if (categoryItemListView.getSelectionModel().getSelectedItem() instanceof  MailBox) {
            MailBox box = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
            return isForbiddenOperationBox(box.getBoxName());
        }
        return false;
    }

    public boolean isEqualBoxWith(String aBoxName) {
        if (categoryItemListView.getSelectionModel().getSelectedItem() instanceof  MailBox) {
            MailBox box = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
            return box.getBoxName().equalsIgnoreCase(aBoxName);
        }
        return false;
    }

    public boolean isForbiddenOperationBox(String aBoxName) {
        return aBoxName.equalsIgnoreCase("draft");
    }

    private class CategoryListCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setPrefHeight(35.0);
            if (empty) {
                setGraphic(null);
                setText(null);
            } else {
                setText(item);
                setGraphic(ViewFactory.defaultFactory.resolveCategoryIcon(getIndex(), 25));
                if (getIndex() == CAT_TODO && approveCnt > 0) {
                    setText(item + "(" + approveCnt + ")");
                }
            }
        }
    }

    //for test
    private class CategoryListItemCell extends ListCell<Object> {
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                setText(null);
            } else {
                setText(item.toString());
                if (item instanceof MailBox) {
                    int iCnt = ((MailBox) item).getUnreadCount();
                    if (iCnt > 0)
                        setText(item.toString() + " (" + iCnt + ")");
                }
                switch (categoryListView.getSelectionModel().getSelectedIndex()) {
                    case CAT_MAIL:
                        setGraphic(ViewFactory.defaultFactory.resolveMailBoxListItemIcon(((MailBox)getItem()).getBoxName()));
                        break;
                    case CAT_ADB:
                        setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/user.png"));
                        break;
                    case CAT_TODO:
                        if (getIndex() == 0)// pending mail
                            setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/pending.png"));
                        else {// approve mail
                            setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/approve.png"));
                            setText(approveCntTxt);
                        }
                        break;
                }
//                if (isSelectedMailCategory()) {
//                    setGraphic(ViewFactory.defaultFactory.resolveMailBoxListItemIcon(((MailBox)getItem()).getBoxName()));
//                } else {
//                    setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/user.png"));
//                }
            }
        }
    }

    @FXML
    public void handleClickedOnAdressBookCatButton() {
        categoryListView.getSelectionModel().select(CAT_ADB);
        handleClickedOnCategory(null);
    }

    @FXML
    public void handleClickedOnSyncButton() {
        Platform.runLater(()->{
            StringBuffer buf = new StringBuffer();
            int ret = syncAddressBook(buf);
            if (ret == HttpURLConnection.HTTP_OK) {
                showAlertAndWait(getString("confirm"), getString("sync_adb_success"), getOwnerStage(), Alert.AlertType.INFORMATION);
            } else {
                showAlertAndWait(getString("Critical"), getString("sync_adb_fail") + "\n\n" + buf.toString(), getOwnerStage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    public void handleClickedOnMailApproveMenuButton() {
        System.out.println("handleClickedOnMailApproveMenuButton");
        handleApproveRelatedMail(false);
    }

    @FXML
    public void handleClickedOnPendingApproveMenuButton() {
        System.out.println("handleClickedOnPendingApproveMenuButton");
        handleApproveRelatedMail(true);
    }

    /**
     * handles the approve mail
     * @param isPending
     */
    public void handleApproveRelatedMail(boolean isPending) {
        int index = mailContentsVBox.getChildren().indexOf(mainTableView);
        if (index != -1)
            mailContentsVBox.getChildren().remove(mainTableView);
        index = mailContentsVBox.getChildren().indexOf(approveMailWebView);
        if (index == -1)
            mailContentsVBox.getChildren().add(approveMailWebView);

        approveMailWebView.setVisible(true);
        mainTableView.setVisible(false);

        WebEngine engine = approveMailWebView.getEngine();
        String url = String.format(Apis.GET_MAIL_APPROVAL(), getPCName());
        if(isPending)
            url = String.format(Apis.GET_PENDING_APPROVE_LIST(), getPCName());

        engine.load(url);

        searchVBox.getChildren().remove(exSearchFlowPane);
        searchComboBox.setVisible(false);
        exSearchButton.setVisible(false);
//        selectMailCategoryButton();

        if (isPending) {
            categoryNameLabel1.setGraphic(ViewFactory.defaultFactory.resolveMailBoxListItemIcon("PENDING APPROVAL"));
            categoryNameLabel1.setText(getString("tb.pending_approval"));
        } else {
            categoryNameLabel1.setGraphic(ViewFactory.defaultFactory.resolveMailBoxListItemIcon("MAIL APPROVAL"));
            categoryNameLabel1.setText(getString("tb.mail_approval"));
        }
    }

    /**
     * selects the mail category button at the bottom
     */
    public void selectMailCategoryButton() {
        int index = mainSplitePane.getItems().indexOf(rightAnchorPane);
        if (isShowingMode) {
            if (index == -1) {
                mainSplitePane.getItems().add(rightAnchorPane);
                isSetMaxSize = true;
                layoutSizeOnChanged();
            }
        }
//        if (isExpandedOfLeftPane()) {
//            mainSplitePane.setDividerPositions(mDivPosOfMailExp);
//            mDivPosOfMailExp = mainSplitePane.getDividerPositions();
//        }
        addressBookCatButton.setSelected(false);
        mailCatButton.setSelected(true);
        toDoCatButton.setSelected(false);
    }

    @FXML
    public void handleClickedOnCloseMenuButton() {
        System.out.println("handleClickedOnCloseMenuButton");
        Stage stage = (Stage) mainTableView.getScene().getWindow();
        stage.close();
        shutdown();
    }

    @FXML
    public void handleClickedOnMailCatButton() {
        categoryListView.getSelectionModel().select(CAT_MAIL);
        handleClickedOnCategory(null);
    }

    @FXML
    public void handleClickedOnToDoCatButton() {
        categoryListView.getSelectionModel().select(CAT_TODO);
        handleClickedOnCategory(null);
    }

    @FXML
    public void handleClickedOnAddBoxButton() {
        int selId = categoryListView.getSelectionModel().getSelectedIndex();
        switch (selId) {
            case CAT_MAIL:
            {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle(getString("AddMailBox"));
                dialog.setHeaderText(getString("input_mail_box_name"));
//                dialog.setContentText(getString("nameColon"));
                dialog.initOwner(GlobalVariables.mainController.getOwnerStage());
                Button buttonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                buttonOk.setText(getString("btn_add"));
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(name -> {
                    if ( name == null || name.length() == 0) {
                        showAlert(getString("Warning"), getString("invalid_name"), getOwnerStage(), Alert.AlertType.WARNING);
                    } else if (mdb.checkNewMailBoxName(name)) {

                        Task task = new Task() {
                            @Override
                            protected Object call() throws Exception {
                                updateMessage(getString("adding"));
                                mdb.addMailBox(name);
                                Platform.runLater(()->{
                                    hideWaitingDialog();
                                    MailBox newBox = new MailBox(name, FXCollections.observableArrayList());
                                    mMailBoxList.add(newBox);
                                });
                                return null;
                            }
                        };

                        showWaitingDialog(getOwnerStage());
                        waitingMsgProperty.bind(task.messageProperty());
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();

                    } else {
                        showAlert(getString("Warning"), getString("dup_box_name"), getOwnerStage(), Alert.AlertType.WARNING);
                    }
                });
            }
                break;
            case CAT_ADB:
            {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle(getString("AddAdbBox"));
                dialog.setHeaderText(getString("input_adb_box_name"));
//                dialog.setContentText(getString("nameColon"));
                dialog.initOwner(mainTableView.getScene().getWindow());
                dialog.initOwner(GlobalVariables.mainController.getOwnerStage());
                Button buttonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                buttonOk.setText(getString("btn_add"));
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(destName -> {
                    if ( destName == null || destName.length() == 0) {
                        showAlert(getString("Warning"), getString("invalid_name"), getOwnerStage(), Alert.AlertType.WARNING);
                    } else if (mdb.checkNewAddressBoxName(destName)) {

                        Task task = new Task() {
                            @Override
                            protected Object call() throws Exception {
                                updateMessage(getString("adding"));
                                mdb.addAdbBox(destName);
                                Platform.runLater(()->{
                                    hideWaitingDialog();
                                    AddressBox newBox = new AddressBox(destName, new ArrayList<>());
                                    mAdbBoxList.add(newBox);
                                });
                                return null;
                            }
                        };

                        showWaitingDialog(getOwnerStage());
                        waitingMsgProperty.bind(task.messageProperty());
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();
                    } else {
                        showAlert(getString("Warning"), getString("dup_box_name"), getOwnerStage(), Alert.AlertType.WARNING);
                    }
                });
            }
                break;
        }
    }

    @FXML
    public void handleClickedOnCategoryItem(MouseEvent event) {

        initExSearchValues();

        if (event != null) {

            if (event.getButton()== MouseButton.SECONDARY) {
                int selId = categoryListView.getSelectionModel().getSelectedIndex();
                switch (selId) {
                    case CAT_MAIL:
                    {
                        ContextMenu contextMenu = new ContextMenu();
                        MenuItem emptyMenuItem = new MenuItem(getString("Empty"));
                        emptyMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                                stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
                                alert.setTitle(getString("question_empty_trash_box"));
                                alert.setHeaderText(getString("question_empty_trash_box_content"));
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == ButtonType.OK) {

                                    MailBox mailBox = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
                                    ObservableList<MailItem> mailItems = mainTableView.getItems();

                                    Task task = new Task() {
                                        @Override
                                        protected Object call() throws Exception {
                                            updateMessage(getString("deleting"));
                                            try {
                                                for (MailItem item: mailItems) {
                                                    File dir = new File(GlobalVariables.APP_DATA_DIR, item.getUid());
                                                    if (dir.exists()) {
                                                        deleteFile(dir);
                                                    }
                                                }
                                                mdb.removeMessage(mailItems, mailBox);
                                                Platform.runLater(()->{
                                                    hideWaitingDialog();
                                                    mailBox.removeAllMsg(mailItems);
                                                    int iCnt = mailItems.size();
                                                    String txt = (iCnt == 1 ? getString("one_item_deleted") : (iCnt + " " + getString("multi_item_deleted")));
                                                    updateAppStatusLabel(txt);
                                                    mMailList.removeAll(mailItems);
                                                    mainTableView.getSelectionModel().clearSelection();
                                                    mainTableView.refresh();
                                                });
                                            } catch (Exception e) {
                                                Platform.runLater(()->{
                                                    showAlertAndWait(
                                                            getString("Warning"),
                                                            e.getMessage(),
                                                            getOwnerStage(),
                                                            Alert.AlertType.WARNING
                                                    );
                                                });
                                            }
                                            return null;
                                        }
                                    };

                                    showWaitingDialog(getOwnerStage());
                                    waitingMsgProperty.bind(task.messageProperty());
                                    Thread thread = new Thread(task);
                                    thread.setDaemon(true);
                                    thread.start();
                                }
                            }
                        });

                        MenuItem deleteMenuItem = new MenuItem(getString("DeleteMailBox"));
                        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                                stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
                                alert.setTitle(getString("question_delete_mail_box"));
                                alert.setHeaderText(getString("question_delete_mail_box_content"));
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == ButtonType.OK) {
                                    int index = categoryItemListView.getSelectionModel().getSelectedIndex();

                                    Task task = new Task() {
                                        @Override
                                        protected Object call() throws Exception {
                                            updateMessage(getString("deleting"));
                                            mdb.removeMailBox(mMailBoxList.get(index).getBoxName());
                                            Platform.runLater(()->{
                                                hideWaitingDialog();
                                                mMailBoxList.remove(index);
                                                categoryItemListView.getSelectionModel().select(0);
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
                            }
                        });
                        MenuItem editMenuItem = new Menu(getString("EditMailBox"));
                        editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setTitle(getString("EditMailBox"));
                                dialog.setHeaderText(getString("input_mail_box_name"));
                                dialog.setContentText(getString("nameColon"));
                                MailBox box = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
                                dialog.getEditor().setText(box.getBoxName());
                                dialog.initOwner(GlobalVariables.mainController.getOwnerStage());
                                Optional<String> result = dialog.showAndWait();
                                result.ifPresent(destName -> {
                                    if (!mdb.checkNewMailBoxName(destName)) {
                                        showAlert(getString("Warning"), getString("dup_box_name"), getOwnerStage(), Alert.AlertType.WARNING);
                                        return;
                                    }
                                    String oriName = box.getBoxName();
                                    box.changeBoxName(destName);

                                    Task task = new Task() {
                                        @Override
                                        protected Object call() throws Exception {
                                            updateMessage(getString("updating"));
                                            mdb.changeMailBox(oriName, destName);
                                            Platform.runLater(()->{
                                                hideWaitingDialog();
                                                categoryItemListView.refresh();
                                            });
                                            return null;
                                        }
                                    };

                                    showWaitingDialog(getOwnerStage());
                                    waitingMsgProperty.bind(task.messageProperty());
                                    Thread thread = new Thread(task);
                                    thread.setDaemon(true);
                                    thread.start();
                                });
                            }
                        });

                        categoryItemListView.setContextMenu(null);
                        MailBox box = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
                        if (box.isCustomBox()) {
                            contextMenu.getItems().addAll(/*addMenuItem, */editMenuItem, deleteMenuItem);
                        } else {
//                            contextMenu.getItems().addAll(addMenuItem);
                            if (box.isTrash()) {
                                MailBox mailBox = (MailBox) categoryItemListView.getSelectionModel().getSelectedItem();
                                if (mailBox.getMsgList().size() > 0)
                                    contextMenu.getItems().addAll(emptyMenuItem);
                            }
                        }
                        if (contextMenu.getItems().size() > 0)
                            categoryItemListView.setContextMenu(contextMenu);
                    }
                        break;
                    case CAT_ADB:
                    {
                        ContextMenu contextMenu = new ContextMenu();
                        MenuItem deleteMenuItem = new MenuItem(getString("DeleteAdbBox"));
                        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                                stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
                                alert.setTitle(getString("question_delete_adb_box"));
                                alert.setHeaderText(getString("question_delete_adb_box_content"));
                                stage.initOwner(GlobalVariables.mainController.getOwnerStage());
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.get() == ButtonType.OK) {
                                    int index = categoryItemListView.getSelectionModel().getSelectedIndex();
                                    Task task = new Task() {
                                        @Override
                                        protected Object call() throws Exception {
                                            updateMessage(getString("deleting"));
                                            mdb.removeAddressBox(mAdbBoxList.get(index).getBoxName());
                                            Platform.runLater(()->{
                                                hideWaitingDialog();
                                                mAdbBoxList.remove(index);
                                                categoryItemListView.getSelectionModel().select(0);
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
                            }
                        });
                        MenuItem editMenuItem = new Menu(getString("EditAdbBox"));
                        editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setTitle(getString("EditAdbBox"));
                                dialog.setHeaderText(getString("input_adb_box_name"));
                                dialog.setContentText(getString("nameColon"));
                                AddressBox box = (AddressBox) categoryItemListView.getSelectionModel().getSelectedItem();
                                dialog.getEditor().setText(box.getBoxName());
                                dialog.initOwner(GlobalVariables.mainController.getOwnerStage());
                                Optional<String> result = dialog.showAndWait();
                                result.ifPresent(destName -> {
                                    if (!mdb.checkNewAddressBoxName(destName)) {
                                        showAlert(getString("Warning"), getString("dup_box_name"), getOwnerStage(), Alert.AlertType.WARNING);
                                        return;
                                    }
                                    String oriName = box.getBoxName();
                                    box.changeBoxName(destName);

                                    Task task = new Task() {
                                        @Override
                                        protected Object call() throws Exception {
                                            updateMessage(getString("updating"));
                                            mdb.changeAddressBox(oriName, destName);
                                            Platform.runLater(()->{
                                                hideWaitingDialog();
                                                categoryItemListView.refresh();
                                            });
                                            return null;
                                        }
                                    };

                                    showWaitingDialog(getOwnerStage());
                                    waitingMsgProperty.bind(task.messageProperty());
                                    Thread thread = new Thread(task);
                                    thread.setDaemon(true);
                                    thread.start();
                                });
                            }
                        });

                        categoryItemListView.setContextMenu(null);
                        AddressBox box = (AddressBox) categoryItemListView.getSelectionModel().getSelectedItem();
                        if (box.isCustomBox()) {
                            contextMenu.getItems().addAll(/*addMenuItem, */editMenuItem, deleteMenuItem);
                        } else {
//                            contextMenu.getItems().addAll(addMenuItem);
                        }
                        if (contextMenu.getItems().size() > 0)
                            categoryItemListView.setContextMenu(contextMenu);
                    }
                        break;
                    case CAT_TODO:
                    default:
                        categoryItemListView.setContextMenu(null);
                        break;
                }

            } else if (event.getButton()== MouseButton.PRIMARY) {
                switch (categoryListView.getSelectionModel().getSelectedIndex()) {
                    case CAT_MAIL:
                    case CAT_ADB:
                        if (!mainTableView.isVisible())
                            removeApproveMailView();
                        searchComboBox.setVisible(true);
                        exSearchButton.setVisible(true);
                        break;
                    case CAT_TODO:
                        if (mainTableView.isVisible())
                            removeMailAndAdbView();
                        searchComboBox.setVisible(false);
                        exSearchButton.setVisible(false);
                        break;
                }
            }
        } else {
            selectCategoryItem();
        }
    }

    /**
     * get the address book item for an email
     * @param email
     * @return
     */
    public AddressBookItem getAdbItemOf(String email) {
        for (AddressBox box: mAdbBoxList) {
            for (AddressBookItem eachItem: box.getAddrlist()) {
                if (eachItem.mailAddressProperty().getValue().compareToIgnoreCase(email) == 0)
                    return eachItem;
            }
        }
        return null;
    }

    /**
     * get the mailbox for an email
     * @param email
     * @return
     */
    public AddressBox getAdbBoxOf(String email) {
        AddressBox ret = null;
        for (AddressBox box: mAdbBoxList) {
            for (AddressBookItem eachBox: box.getAddrlist()) {
                if (eachBox.mailAddressProperty().getValue().compareToIgnoreCase(email) == 0)
                    return box;
            }
        }
        return ret;
    }

    /**
     * select category item
     */
    public void selectCategoryItem() {
        String catTitle = "";

        switch (categoryListView.getSelectionModel().getSelectedIndex()) {

            case CAT_MAIL:


                catTitle = categoryItemListView.getSelectionModel().getSelectedItem().toString();
                curSelMailCategoryIndex = categoryItemListView.getSelectionModel().getSelectedIndex();

                MailBox mailBox = mMailBoxList.get(curSelMailCategoryIndex);
                categoryNameLabel1.setGraphic(ViewFactory.defaultFactory.resolveMailBoxListItemIcon(mailBox.getBoxName()));

                mMailList.clear();
                mailBox.clearSelection();
                mMailList.addAll(mailBox.getMsgList());
//                ObservableList <Pair<Integer, Message>> msglist = mailBox.getMsgList();
//                for (int i = 0; i < msglist.size(); i++) {
//                    Pair<Integer, Message> msg = msglist.get(i);
//                    try {
//                        MailItem mailItem = convertFrom(mailBox.getBoxName(), msg.getValue(), MailItem.getUidFromMsg(msg.getValue()));
//                        mailItem.setFillId(msg.getKey());
//                        mMailList.add(mailItem);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }

                initMailBoxTable(mailBox);
                selectMailCategoryButton();
                categoryNameLabel1.setText(catTitle);
                break;
            case CAT_ADB:

                catTitle = getString("AddressBook");
                categoryNameLabel1.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/contacts.png"));

                curSelAddrCategoryIndex = categoryItemListView.getSelectionModel().getSelectedIndex();
                updateSelectedAdbItem();

                initAddressBookTable((AddressBox) categoryItemListView.getSelectionModel().getSelectedItem());

                if (!isShowingMode) {
                    mainSplitePane.getItems().remove(rightAnchorPane);
                    isSetMaxSize = true;
                    layoutSizeOnChanged();
                }

//                if (isExpandedOfLeftPane())
//                    mDivPosOfAddressBookExp = mainSplitePane.getDividerPositions();

                AddressBox adbBox = mAdbBoxList.get(curSelAddrCategoryIndex);
                if (adbBox.isPublic())
                    btnSync.setVisible(true);
                else
                    btnSync.setVisible(false);

                adbBox.clearSelection();

                addressBookCatButton.setSelected(true);
                mailCatButton.setSelected(false);
                toDoCatButton.setSelected(false);
                categoryNameLabel1.setText(catTitle);
                break;
            case CAT_TODO:
                categoryNameLabel1.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/contacts.png"));

                curSelToDoMailCategoryIndex = categoryItemListView.getSelectionModel().getSelectedIndex();

                toDoCatButton.setSelected(true);
                mailCatButton.setSelected(false);
                addressBookCatButton.setSelected(false);

                if (curSelToDoMailCategoryIndex == 0)
                    handleApproveRelatedMail(true);
                else
                    handleApproveRelatedMail(false);

                break;
        }
    }

    /**
     * check the body text
     * @param uid
     * @return
     */
    public Message getMailContents(String uid) {
        return mdb.getMailContents(uid);
    }

    /**
     * check the body text
     * @param uid
     * @return
     */
    public String getBodyText(String uid) {
        return mdb.getBodyText(uid);
    }

    /**
     * check the attachment if uid is the item
     * @param uid
     * @return
     */
    public boolean hasAttachment(String uid) {
        return mdb.hasAttach(uid);
    }

    /**
     * check the attachment if uid is the draft item
     * @param uid
     * @return
     */
    public boolean hasAttachmentInDraft(String uid) {
        return mdb.hasAttachInDraft(uid);
    }

    /**
     * get the total mail count
     * @return
     */
    public int getTotalMailCnt() {
        if (mMailBoxList.size() == 0)
            return 0;
        int iCnt = 0;
        for (MailBox box: mMailBoxList) {
            iCnt += box.getMsgList().size();
        }
        return iCnt;
    }

    /**
     * get the datebase size
     * @return
     */
    public String getDbSize() {
        return getFormattedSize((int) new File(APP_DATA_DIR, DB_NAME).length());
    }

    /**
     * get the datebase size
     * @return
     */
    public String getDbCreatedDate() {
        File f = new File(APP_DATA_DIR, DB_NAME);
        Path p = Paths.get(f.getAbsolutePath());
        try {
            BasicFileAttributes view
                    = Files.getFileAttributeView(p, BasicFileAttributeView.class)
                    .readAttributes();
            Date date = new Date(view.creationTime().toMillis());
            return getFormattedTimeString(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * get the mail size
     * @param uid
     * @return
     */
    public int getMailSize(String uid) {
        return mdb.getMailSize(uid);
    }

    /**
     * get the total attachments file size
     * @param uid
     * @return
     */
    public int getAttachFilesSizeInDraft(String uid) {
        return mdb.getAttachSizeInDraft(uid);
    }

    /**
     * remove approve mail web view
     */
    public void removeMailAndAdbView() {
        int index = mailContentsVBox.getChildren().indexOf(approveMailWebView);
        if (index == -1)
            mailContentsVBox.getChildren().add(approveMailWebView);
        index = mailContentsVBox.getChildren().indexOf(mainTableView);
        if (index != -1)
            mailContentsVBox.getChildren().remove(mainTableView);

        mainTableView.setVisible(false);
        approveMailWebView.setVisible(true);
    }

    /**
     * remove approve mail web view
     */
    public void removeApproveMailView() {
        int index = mailContentsVBox.getChildren().indexOf(mainTableView);
        if (index == -1)
            mailContentsVBox.getChildren().add(mainTableView);
        index = mailContentsVBox.getChildren().indexOf(approveMailWebView);
        if (index != -1)
            mailContentsVBox.getChildren().remove(approveMailWebView);

        mainTableView.setVisible(true);
        approveMailWebView.setVisible(false);

        initSearchCategory();
    }

    public void updateSelectedAdbItem() {
        if (curSelAddrCategoryIndex == -1) {
            curSelAddrCategoryIndex = 0;
        }
        AddressBox addrbox = mAdbBoxList.get(curSelAddrCategoryIndex);
        mAdbItemList.clear();
        mAdbItemList.addAll(addrbox.getAddrlist());
    }

    public void handleClickedOnCategory(MouseEvent event) {

        if (event != null) {
            if (event.getButton()== MouseButton.PRIMARY) {
                switch (categoryListView.getSelectionModel().getSelectedIndex()) {
                    case CAT_MAIL:
                    case CAT_ADB:
                        if (!mainTableView.isVisible())
                            removeApproveMailView();
                        break;
                    case CAT_TODO:
                        if (mainTableView.isVisible())
                            removeMailAndAdbView();
                        break;
                }
            }
        } else {
            if (!isSelectedMailCategory()) {
                exSearchButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/arrows_down.png"));
                searchVBox.getChildren().remove(exSearchFlowPane);
            }

            String catTitle = categoryListView.getSelectionModel().getSelectedItem().toString();
            categoryNameLabel.setText(catTitle);

            if (categoryListView.getSelectionModel().getSelectedIndex() == curSelCategoryIndex)
                return;

            curSelCategoryIndex = categoryListView.getSelectionModel().getSelectedIndex();

            int selIndex = 0;
            switch (curSelCategoryIndex) {
                case CAT_MAIL:
                case CAT_ADB:
                    removeApproveMailView();
                    if (isSelectedMailCategory()) {
                        categoryItemListView.setItems(mMailBoxList);
                        selIndex = curSelMailCategoryIndex;
                        btnSync.setVisible(false);
                    } else {
                        categoryItemListView.setItems(mAdbBoxList);
                        selIndex = curSelAddrCategoryIndex;
                        AddressBox adbBox = mAdbBoxList.get(selIndex);
                        if (adbBox.isPublic())
                            btnSync.setVisible(true);
                        else
                            btnSync.setVisible(false);
                    }
                    selectionStatusLabel.setVisible(true);
                    break;
                case CAT_TODO:
                    removeMailAndAdbView();
                    categoryItemListView.setItems(mToDoBoxList);
                    selIndex = curSelToDoMailCategoryIndex;
                    btnSync.setVisible(false);
                    selectionStatusLabel.setVisible(false);
                    break;
            }

            categoryItemListView.getSelectionModel().select(selIndex);

            mainTableView.refresh();
        }
    }

    public boolean isSelectedMailCategory() {
        return categoryListView.getSelectionModel().getSelectedIndex() == CAT_MAIL;
    }

    public boolean isSelectedAdbCategory() {
        return categoryListView.getSelectionModel().getSelectedIndex() == CAT_ADB;
    }

    public void initCategoryButtons() {
        ImageView img = (ImageView) ViewFactory.defaultFactory.resolveCategoryIcon(CAT_MAIL);
        mailCatButton.setGraphic(img);
        mailCatButton.setText("");

        img = (ImageView) ViewFactory.defaultFactory.resolveCategoryIcon(CAT_ADB);
        addressBookCatButton.setGraphic(img);
        addressBookCatButton.setText("");

        img = (ImageView) ViewFactory.defaultFactory.resolveCategoryIcon(CAT_TODO);
        toDoCatButton.setGraphic(img);
        toDoCatButton.setText("");

        img = (ImageView) ViewFactory.defaultFactory.resolveIconWithName("images/sync.png");
        btnSync.setGraphic(img);
        btnSync.setText("");

        img = (ImageView) ViewFactory.defaultFactory.resolveIconWithName("images/add_box.png");
        addBoxButton.setGraphic(img);
        addBoxButton.setText("");
    }

    /**
     * get all address
     * @return
     */
    public ArrayList<String> getAllAddress() {
        ArrayList<String> aryList = new ArrayList<>();
        for (AddressBox box: mAdbBoxList) {
            ArrayList<String> subList = box.getAdblist();
            for (String item1: subList) {
                boolean isExist = false;
                for (String item2: aryList) {
                    if (item1.compareToIgnoreCase(item2) == 0) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist)
                    aryList.add(item1);
            }
        }
        return aryList;
    }


    /**
     * @autbor Pilot
     * Initialization for the toolbar button icons and titles
     */
    public void initToolbarButtons() {

        leftPanCollapseButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/arrows_left.png"));
        leftPanCollapseButton.setText("");

        leftPanExpandButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/arrows_right.png"));
        leftPanExpandButton.setText("");

        newMailMenuButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/mail_new.png"));

        printButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/print.png"));
        printButton.setText("");

        deleteButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/delete.png"));

        replyInContentButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/mail_reply.png"));
        replyButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/mail_reply.png"));

        replyAllInContentButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/reply_to_all.png"));
        replyToAllButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/reply_to_all.png"));

        forwardInContentButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/forward.png"));
        forwardButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/forward.png"));

        followUpButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/fav_yes.png"));

        sendReceiveButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/sendReceive.png"));

        oneNoteButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/one_note.png"));
        oneNoteButton.setText("");

        addressBookButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/contacts.png"));
        addressBookButton.setText("");

        addressBookComboBox.setEditable(true);
        addressBookComboBox.getEditor().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                System.out.println("Text changed");
            }
        });

        helpButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/help.png"));
        helpButton.setText("");

        pendingApproveMailButon.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/pending.png"));
        mailApproveMailButon.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/approve.png"));

        //remove toolbar buttons
        actionToolBar.getItems().remove(oneNoteButton);
        actionToolBar.getItems().remove(addressBookButton);
        actionToolBar.getItems().remove(addressBookComboBox);
        actionToolBar.getItems().remove(helpButton);
        actionToolBar.getItems().remove(printButton);
    }

    @FXML
    public void handleClickedOnLeftPanCollapseButton() {
        System.out.println("OnClicked : Collapse index = " + mainSplitePane.getItems());

        Node child = mainSplitePane.lookup("#leftAnchorPane");

        if (child != null) {
            //before collapse
//            if (isSelectedMailCategory())
//                mDivPosOfMailExp = mainSplitePane.getDividerPositions();
//            else
//                mDivPosOfAddressBookExp = mainSplitePane.getDividerPositions();

            mainSplitePane.getItems().remove(leftAnchorPane);
            categoryListHBox.getChildren().add(0, expandAnchorPane);

            isSetMaxSize = true;
            layoutSizeOnChanged();
//            if (isSelectedMailCategory())
//                mainSplitePane.setDividerPositions(mDivPosOfMailExp[1]);

        } else {
            //before expand
            mainSplitePane.getItems().add(0, leftAnchorPane);
            categoryListHBox.getChildren().remove(expandAnchorPane);

            isSetMaxSize = true;
            layoutSizeOnChanged();
//            if (isSelectedMailCategory())
//                mainSplitePane.setDividerPositions(mDivPosOfMailExp);
//            else
//                mainSplitePane.setDividerPositions(mDivPosOfAddressBookExp);
        }

    }

    public boolean isExpandedOfLeftPane() {
        return mainSplitePane.lookup("#leftAnchorPane") != null;
    }

    /**
     * getString this device os name and system user name
     * @return user name
     */
    public static String getPCName() {
        String defaultUsername = "user3";
        String osName = System.getProperty("os.name");
        System.out.println("Os - " + osName);

        //for domain user mode. if you don't need domain,comment these lines.

//        if(!System.getenv().containsKey("USERDNSDOMAIN")) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
//            stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
//            alert.setTitle(getString("login_error"));
//            alert.setHeaderText(AbstractController.getString("not_in_domain"));
//            alert.showAndWait();
//            System.exit(-1);
//        }

        //for test
//        defaultUsername = "userxp";
//        osName = "";
        switch (osName.toLowerCase()) {
            case "windows 10":
                defaultUsername = System.getenv().get("COMPUTERNAME");
                break;
            case "windows 7":
            case "windows xp":
                defaultUsername = System.getProperty("user.name").split("\\.")[0];
                break;
//            case "windows xp":
//                {
//                    String myDocuments = null;
//
//                    try {
//                        Process p =  Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
//                        p.waitFor();
//
//                        InputStream in = p.getInputStream();
//                        byte[] b = new byte[in.available()];
//                        in.read(b);
//                        in.close();
//
//                        myDocuments = new String(b);
//                        myDocuments = myDocuments.split("\\s\\s+")[4];
//
//                    } catch(Throwable t) {
//                        t.printStackTrace();
//                    }
//
//                    defaultUsername = myDocuments;
//                }
            default:
                break;
        }

        System.out.println("OS user name - " + defaultUsername);

        return defaultUsername.toLowerCase();
    }

    /**
     * load mail box
     */
    public void loadMailBoxFromDB() {
        mMailBoxList = mdb.getMailBoxList();
    }

    public void loadAdbBoxFromDB() {
        mAdbBoxList = mdb.getAdbBoxs();
    }

    public void loadToDoBoxList() {
        mToDoBoxList = FXCollections.observableArrayList();
        mToDoBoxList.add(getString("tb.pending_approval"));
        mToDoBoxList.add(getString("tb.mail_approval"));
    }

    /**
     * get user info
     * @param account
     * @return
     */
    public int getUserInfo(ValidAccount account) {

        int ret;

        try {
            String url = String.format(Apis.GET_USER_INFO(), getPCName());
            XMailHttpRequest req = XMailHttpRequest.get(url);
            HttpURLConnection con = req.getConnection();

            ret = con.getResponseCode();

            InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
            BufferedReader in = new BufferedReader( inputStreamReader);

            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObj = (JSONObject)parser.parse(response.toString());
                String userName = jsonObj.get("userName").toString();
                String deptName = jsonObj.get("deptName").toString();
                int deptCode = Integer.parseInt(jsonObj.get("deptCode").toString());
                int userLevel = Integer.parseInt(jsonObj.get("userLevel").toString());
                int userSecurityLevel = Integer.parseInt(jsonObj.get("userSecurityLevel").toString());
                account.setUserName(userName);
                account.setDeptName(deptName);
                account.setDeptCode(deptCode);
                account.setUserLevel(userLevel);
                account.setUserSecLevel(userSecurityLevel);
            } catch (Exception e) {
                System.out.println("HTTP_NOT_FOUND******************getMailBoxInfo failed by ==== " + getStackTrace(e));
                ret = HttpURLConnection.HTTP_NOT_FOUND;
            }
        } catch (Exception e) {
            System.out.println("HTTP_CLIENT_TIMEOUT*******************getMailBoxInfo failed by ==== " + getStackTrace(e));
            ret = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
        }

        return ret;
    }

    /**
     * get the valid account
     * @param account
     * @return
     */
    public int getMailBoxInfo(ValidAccount account) {

        int ret;
        try {
            //for test
//            String url = String.format(Apis.GET_MAILBOX_INFO(), "user4");
            String url = String.format(Apis.GET_MAILBOX_INFO(), getPCName());
            XMailHttpRequest req = XMailHttpRequest.get(url);
            HttpURLConnection con = req.getConnection();

            ret = con.getResponseCode();

            InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
            BufferedReader in = new BufferedReader(inputStreamReader);

            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();
            JSONParser parser = new JSONParser();
            try {
                JSONObject jsonObj = (JSONObject)parser.parse(response.toString());
                account.setAddress(jsonObj.get("mailbox").toString());
                account.setPassword(jsonObj.get("mailboxPwd").toString());
            } catch (Exception e) {
                System.out.println("HTTP_NOT_FOUND******************getMailBoxInfo failed by ==== " + getStackTrace(e));
                ret = HttpURLConnection.HTTP_NOT_FOUND;
            }
        } catch (Exception e) {
            System.out.println("HTTP_CLIENT_TIMEOUT*******************getMailBoxInfo failed by ==== " + getStackTrace(e));
            ret = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
        }
        return ret;
    }

    /**
     * show exit or server settings
     */
    public void showExitOrServerSettingsDialog(String aTitle, String aMsg) {

        mExitTitle = aTitle;
        mExitMsg = aMsg;
        Dialog dialog = new Dialog();
        dialog.setTitle(aTitle);
        dialog.setHeaderText(aMsg);

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
        stage.setOnCloseRequest(e->e.consume());

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        final Button btSetting = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        btOk.setText(getString("exit"));
        btSetting.setText(getString("settings"));

        dialog.initOwner(GlobalVariables.mainController.getOwnerStage());
        Optional<ButtonType> ret = dialog.showAndWait();
        if (ret.get() == ButtonType.OK) {
            shutdown();
            return;
        }

        showServerSettingsDlg(false);
    }

    public boolean isNotBindedAccount() {
        return isNotBinded;
    }

    public void setBindAccount(String aPwd) {
        mdb.insertAccount(getPCName(), aPwd);
    }

    public void finishApp() {
        shutdown();
    }

    /**
     * synchronization address book with server
     */
    public synchronized int syncAddressBook(StringBuffer errBuf) {

        int ret;
        try {
            String url = Apis.GET_ADDRESSBOOK();
            XMailHttpRequest req = XMailHttpRequest.get(url);
            HttpURLConnection con = req.getConnection();

            ret = con.getResponseCode();

            InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
            BufferedReader in = new BufferedReader(inputStreamReader);

            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();
            try {

                if (mAdbBoxList.size() > 0) {
                    int boxIndex = 0;
                    for (int i = 0; i < mAdbBoxList.size(); i++) {
                        if (mAdbBoxList.get(i).getBoxName().equalsIgnoreCase("public")) {
                            boxIndex = i;
                            break;
                        }
                    }
                    AddressBox adbBox = mAdbBoxList.get(boxIndex);

                    JSONParser parser = new JSONParser();
                    JSONArray array = (JSONArray)parser.parse(response.toString());
                    ArrayList<AddressBookItem> serverList = new ArrayList<>();

                    //insert or update for the all address book
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject jsonObj = (JSONObject)array.get(i);
                        AddressBookItem item = new AddressBookItem(
                                jsonObj.get("u").toString(),
                                jsonObj.get("d").toString(),
                                jsonObj.get("dp").toString(),
                                jsonObj.get("n").toString(),
                                jsonObj.get("m").toString(),
                                jsonObj.get("ul").toString(),
                                jsonObj.get("usl").toString(),
                                "");
                        if (mdb.insertAddress(adbBox.getBoxName(), item)) {
                            adbBox.addAdbItem(item);
                        } else {
                            boolean isUpdated = adbBox.updateAdbItem(item);
                            if (isUpdated)
                                mdb.updateAddress(adbBox.getBoxName(), item);
                        }
                        serverList.add(item);
                    }

                    ArrayList<AddressBookItem> localDBList = adbBox.getAddrlist();
                    ObservableList<AddressBookItem> noExistList = FXCollections.observableArrayList();
                    for (AddressBookItem orgItem: localDBList) {
                        boolean isExist = false;
                        for (AddressBookItem serverItem: serverList) {
                            if (serverItem.userIDProperty().getValue().compareToIgnoreCase(orgItem.userIDProperty().getValue()) == 0) {
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist)
                            noExistList.add(orgItem);
                    }

                    if (noExistList.size() > 0) {
                        mdb.removeAddress(adbBox.getBoxName(), noExistList);
                        adbBox.removeItems(noExistList);
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            updateSelectedAdbItem();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                ret = HttpURLConnection.HTTP_BAD_REQUEST;
                if (errBuf != null)
                    errBuf.append(getStackTrace(e));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
            if (errBuf != null)
                errBuf.append(getStackTrace(e));
        }
        return ret;
    }

    /**
     * @author pilot
     * get the security level list from the mail server with the detailed error info
     * @param errBuf
     * @return
     */
    public int getSecurityLevels(StringBuffer errBuf) {

        int ret;

        try {
            String url = Apis.GET_MAIL_SECURITY_LEVEL();
            XMailHttpRequest req = XMailHttpRequest.get(url);
            HttpURLConnection con = req.getConnection();

            ret = con.getResponseCode();

            InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
            BufferedReader in = new BufferedReader( inputStreamReader);

            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();
            try {
                JSONParser parser = new JSONParser();
                JSONArray array = (JSONArray)parser.parse(response.toString());
                GlobalVariables.securityList = FXCollections.observableArrayList();
                for (int i = 0; i < array.size(); i++) {
                    JSONObject jsonObj = (JSONObject)array.get(i);
                    String level = jsonObj.get("sysSecurityLevel").toString();
                    String name = jsonObj.get("sysSecurityLevelName").toString();
                    String color = jsonObj.get("sysSecurityLevelColor").toString();
                    SecurityLevel security = new SecurityLevel(Integer.parseInt(level), name, color);
                    GlobalVariables.securityList.add(security);
                }

                FXCollections.sort(GlobalVariables.securityList);

                secNoLevel = new SecurityLevel(-1, getString("NoLevel"), "black");
                securityLevelComboBox.getItems().clear();
                securityLevelComboBox.getItems().addAll(GlobalVariables.securityList);
                securityLevelComboBox.getItems().add(0, secNoLevel);

                securityLevelComboBox.getSelectionModel().select(0);
                setSecurityLevelCssTo(securityLevelComboBox, secNoLevel);

                securityLevelComboBox.setCellFactory(new Callback<ListView<SecurityLevel>, ListCell<SecurityLevel>>() {
                    @Override public ListCell<SecurityLevel> call(ListView<SecurityLevel> p) {
                        return new ListCell<SecurityLevel>() {
                            protected void updateItem(SecurityLevel item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    setText(item.levelName);
                                    setStyle("-fx-text-fill: " + item.levelColor);
                                }
                            }
                        };
                    }
                });
            } catch (Exception e) {
                System.out.println("HTTP_BAD_REQUEST******************getSecurityLevels failed by ==== " + getStackTrace(e));
                ret = HttpURLConnection.HTTP_BAD_REQUEST;
                if (errBuf != null)
                    errBuf.append(getStackTrace(e));
            }
        } catch (Exception e) {
            System.out.println("HTTP_CLIENT_TIMEOUT******************getSecurityLevels failed by ==== " + getStackTrace(e));
            ret = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
            if (errBuf != null)
                errBuf.append(getStackTrace(e));
        }

        return ret;
    }

    /**
     * get the system security level
     */
    public void getSysSecurityLevel() {
        try {
            String url = Apis.GET_SYS_SECURITY_LEVEL();
            XMailHttpRequest req = XMailHttpRequest.get(url);
            HttpURLConnection con = req.getConnection();

            InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
            BufferedReader in = new BufferedReader( inputStreamReader);

            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject)parser.parse(response.toString());
            String level = jsonObj.get("sysSecurityLevel").toString();
            String name = jsonObj.get("sysSecurityLevelName").toString();
            String color = jsonObj.get("sysSecurityLevelColor").toString();
            GlobalVariables.sysSecurityLevel = new SecurityLevel(Integer.parseInt(level), name, color);
        } catch (Exception e) {
            System.out.println("******************getSysSecurityLevel failed by ==== " + getStackTrace(e));
        }
    }

    public void getUserLevels() {
        try {
            String url = Apis.GET_USER_LEVEL();
            XMailHttpRequest req = XMailHttpRequest.get(url);
            HttpURLConnection con = req.getConnection();

            InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
            BufferedReader in = new BufferedReader( inputStreamReader);

            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray)parser.parse(response.toString());
            GlobalVariables.userLevelList = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonObj = (JSONObject)array.get(i);
                String level = jsonObj.get("userLevel").toString();
                String name = jsonObj.get("userLevelName").toString();
                String color = "#FFF";// jsonObj.getString("userLevelColor").toString();
                SecurityLevel security = new SecurityLevel(Integer.parseInt(level), name, color);
                GlobalVariables.userLevelList.add(security);
            }
        } catch (Exception e) {
            System.out.println("******************getUserLevels failed by ==== " + getStackTrace(e));
        }
    }

    public boolean isReturnedNote(MailItem aItem) {
        return mdb.isMessageReturnNote(aItem.uid);
    }

    public boolean updateReturnNote(MailItem aItem) {
        return mdb.updateMessageReturnNote(aItem.referenceMsg, false);
    }

    /**
     * update the Mail ListView for the new mail list
     * @param msgEntry
     */
    public void insertMailItemToUI(Set<Map.Entry<String, String>> msgEntry) {

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Thread.sleep(500);
                    Platform.runLater(()->{

                        boolean isAdded = false;
                        for (Map.Entry<String, String> eachEntry: msgEntry) {
                            Message msg = mdb.getMessage(eachEntry.getKey());
                            for (MailBox eachBox: mMailBoxList) {
                                int fillingId = mdb.checkNewFilling(eachEntry.getKey(), eachBox.getBoxName());
                                if (fillingId > 0) {
                                    isAdded = addMsgTo(eachBox, msg, fillingId);
                                }
//                                if (eachBox.getBoxName().equalsIgnoreCase(eachEntry.getValue())) {
//                                    isAdded = addMsgTo(eachBox, msg);
//                                }
                            }
                        }

                        if (isAdded) {
                            categoryItemListView.refresh();
                            mainTableView.refresh();
                            updateSelectionStatusLabel();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    /**
     * update the mail item
     * @param aBoxName
     * @param message
     * @return
     */
    public boolean insertMailItem(String aBoxName, Message message, List<EmailSenderService.AttachFile> draftFiles) {
        try {
            String uid = MailItem.getUidFromMsg(message);
            int retVal = insertMailItem(aBoxName, uid, message, false, draftFiles);
            Message msg = mdb.getMessage(uid);

            if (retVal == MailDatabase.STAT_NEW) {
                for (MailBox box: mMailBoxList) {
                    if (box.getBoxName().equalsIgnoreCase(aBoxName)) {
                        int fillingId = mdb.checkNewFilling(uid, aBoxName);
                        int addIndex = box.addMessage(msg, fillingId);
                        if (isSelectedMailCategory() &&
                                mMailBoxList.get(curSelMailCategoryIndex).getBoxName().equalsIgnoreCase(aBoxName)) {
                            MailItem mailItem = convertFrom(box.getBoxName(), msg, uid);
                            mailItem.setFillId(fillingId);
                            mMailList.add(addIndex, mailItem);
                        }
                        break;
                    }
                }
            }

            switch (retVal) {
                case MailDatabase.STAT_NEW:
                case MailDatabase.STAT_UPDATE:
                case MailDatabase.STAT_UPDATE_MOVE:
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            categoryItemListView.refresh();
                            mainTableView.refresh();
                            updateSelectionStatusLabel();
                        }
                    });
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * insert the new message and apply the rules and notify at the bottom notification
     * @param aBoxName
     * @param message
     * @param isRuleCheck
     * @return
     */
    public int insertMailItem(String aBoxName, String uid, Message message, boolean isRuleCheck, List<EmailSenderService.AttachFile> draftFiles) {
        int iRet = STAT_NO_INSERT;
        try {
            if (!isRuleCheck) {
                message.setFlag(Flags.Flag.SEEN, true);
            }
            iRet =  mdb.insertMessage(aBoxName, uid, message, draftFiles, isRuleCheck);
            switch (iRet) {
                case STAT_NEW: //insert
                    break;
                case STAT_UPDATE: //update
                case STAT_UPDATE_MOVE: //update and move to the boxName
                    for (MailBox box: mMailBoxList) {
                        /*if (box.getBoxName().equalsIgnoreCase(boxName)) {
                            box.updateMessage(uid, message);
                        }*/
                        int fillingId = mdb.checkNewFilling(uid, box.getBoxName());
                        if (fillingId > 0) {
                            box.updateMessage(uid, message);
                            if (isSelectedMailCategory() &&
                                    mMailBoxList.get(curSelMailCategoryIndex).getBoxName().equalsIgnoreCase(box.getBoxName())) {
                                int index = 0;
                                int selIndex = -1;
                                for (MailItem item: mMailList) {
                                    if (item.getUid().compareToIgnoreCase(uid) == 0) {
                                        selIndex = index;
                                        break;
                                    }
                                    index++;
                                }
                                if (selIndex >= 0) {
                                    if (iRet != STAT_UPDATE_MOVE)
                                        mMailList.get(selIndex).copyFrom(message);
                                    else
                                        mMailList.remove(selIndex);
                                }
                            }
                        }
                    }
                    break;
                case STAT_FAIL: //fail insert
                case STAT_NO_INSERT: //no insert
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return iRet;
    }

    /**
     * get the inbox index
     * @return
     */
    public int getInboxIndex() {
        int index = 0;
        for (MailBox box: mMailBoxList) {
            if (box.getBoxName().equalsIgnoreCase("inbox")) {
                break;
            }
            index++;
        }
        return index;
    }

    /**
     * get a message from the type box
     * @param uid
     * @return
     */
    public Message getOneMessage(String uid) {
        return mdb.getMessage(uid);
    }

    /**
     * add a msg to the eachBox
     * @param eachBox
     * @param msg
     * @param fillingId
     */
    public boolean addMsgTo(MailBox eachBox, Message msg, int fillingId) {
        int addIndex = eachBox.addMessage(msg, fillingId);
        if (isSelectedMailCategory() &&
                mMailBoxList.get(curSelMailCategoryIndex).getBoxName().equalsIgnoreCase(eachBox.getBoxName())) {
            MailItem mailItem = convertFrom(eachBox.getBoxName(), msg, MailItem.getUidFromMsg(msg));
            mailItem.setFillId(fillingId);
            mMailList.add(addIndex, mailItem);
            return true;
        }
        return false;
    }

    /**
     * update the read status
      * @param aItem
     * @param isSeen
     */
    public void updateMessageReadStatus(MailItem aItem, boolean isSeen) {

        aItem.unread = false;
        try {
            aItem.referenceMsg.setFlag(Flags.Flag.SEEN, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (MailBox box: mMailBoxList) {
            box.updateMessage(aItem.getUid(), aItem.referenceMsg);
        }

        for (MailItem eachItem: mMailList) {
            if (eachItem.uid.compareToIgnoreCase(aItem.uid) == 0) {
                eachItem.unread = false;
                try {
                    eachItem.referenceMsg.setFlag(Flags.Flag.SEEN, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        mdb.updateMessageReadStatus(aItem.uid, isSeen);
        categoryItemListView.refresh();
        mainTableView.refresh();
    }

    /**
     * remove the msg for the uid
     * @param uid
     */
    public void removeMsg(String uid) {

        MailItem delItem = null;

        for (MailItem eachItem: mMailList) {
            if (eachItem.uid.compareToIgnoreCase(uid) == 0) {
                delItem = eachItem;
                break;
            }
        }

        if (delItem != null) {
            mMailList.remove(delItem);
            for (MailBox box: mMailBoxList) {
                if (box.getBoxName().equalsIgnoreCase(delItem.boxName)) {
                    box.removeMsg(delItem);
                }
            }
        }

        mdb.removeDraftMessage(uid);
        categoryItemListView.refresh();
        mainTableView.refresh();
    }

    /**
     * @author pilot
     * set the replied, replied all and forward status
     * @param uid
     * @param stat
     */
    public void updateMessageAnsweredStatus(String uid, int stat) {

        try {
            for (MailItem eachItem: mMailList) {
                if (eachItem.uid.compareToIgnoreCase(uid) == 0) {
                    try {
                        eachItem.referenceMsg.setHeader(MAIL_ANSWER_STAT_HEADER, "" + stat);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mdb.updateMessageAnsweredStatus(uid, stat);
        categoryItemListView.refresh();
        mainTableView.refresh();
    }

//    public void updateMailBox(String boxName) {
//        boolean hasAlready  = false;
//
//        for (int i = 0; i < mMailBoxList.size(); i++) {
//            MailBox box = mMailBoxList.get(i);
//            if (box.getBoxName().equalsIgnoreCase(boxName)) {
//                hasAlready = true;
//                break;
//            }
//        }
//
//        if (!hasAlready) {
//            MailBox newMailBox = new MailBox(boxName, FXCollections.observableArrayList());
//            mMailBoxList.add(newMailBox);
//            mdb.addMailBox(boxName);
//        }
//    }

    public void addNewAddress(String type, AddressBookItem item) {

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                updateMessage(getString("adding"));
                mdb.insertAddress(type, item);
                Platform.runLater(()->{
                    hideWaitingDialog();
                    for (int i = 0; i < mAdbBoxList.size(); i++) {
                        AddressBox box = mAdbBoxList.get(i);
                        if (box.getBoxName().equals(type)) {
                            box.addAdbItem(item);
                            if (isSelectedAdbCategory() && ((AddressBox)categoryItemListView.getSelectionModel().getSelectedItem()).getBoxName().equals(type)) {
                                mAdbItemList.add(item);
                            }
                        }
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

    /**
     * @author pilot
     * update an AddressBookItem from the srcItem to the destItem
     * @param type
     * @param destItem
     * @param srcItem
     */
    public void updateAddress(String type, AddressBookItem destItem, AddressBookItem srcItem) {

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                updateMessage(getString("updating"));
                mdb.updateAddress(type, destItem);
                Platform.runLater(()->{
                    hideWaitingDialog();
                    srcItem.copyFrom(destItem);
                    mainTableView.refresh();
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

    public boolean addRule(RuleItem item) {
        boolean result = mdb.insertRule(item);
        if (result) {
            mRulesList.clear();
            mRulesList.addAll(loadRules());
        }
        return result;
    }

    public boolean updateRule(RuleItem item) {
        boolean result = mdb.updateRule(item);
        if (result) {
            for(RuleItem rule : mRulesList) {
                if (rule.id.get() == item.id.get()) {
                    mRulesList.set(mRulesList.indexOf(rule), item);
                }
            }
        }
        return result;
    }

    public boolean removeRule(RuleItem item) {
        boolean result = mdb.removeRule(item.id.get());
        if (result) {
            mRulesList.clear();
            mRulesList.addAll(loadRules());
        }
        return result;
    }

    public boolean updateCheckDuration(Integer duration) {
        return mdb.updateCheckDuration(duration);
    }

    public boolean setSyncAdb(boolean isSet) {
        return mdb.setSyncAdb(isSet);
    }

    public boolean updateSyncAdbDuration(Integer duration) {
        return mdb.updateSyncAdbDuration(duration);
    }

    public boolean isSetSyncAdb() {
        return mdb.isSetSyncAdb();
    }

    public int getNewMailInterval() {
        return mdb.getNewMailInterval();
    }

    public int getSyncAdbInterval() {
        return mdb.getSyncAdbInterval();
    }

    /**
     * load the drafts files for the uid
     * @param uid
     * @return
     */
    public List<EmailSenderService.AttachFile> loadDraftsFiles(String uid) {
        return mdb.loadDraftFiles(uid);
    }

    public ArrayList<RuleItem> loadRules(){
        return mdb.loadRules();
    }

    /**
     * show notification at the Screen left-bottom
     * @param notifyMsg
     * @param type
     */
    public void showNewApproveTrayNotification(String notifyMsg, NotificationType type) {
        if (isShownNewApproveNotification)
            return;
        isShownNewApproveNotification = true;
        try {
            showTrayNotification(
                    GlobalVariables.TRAY_TYPE_NEW_APPROVE,
                    "",
                    null,
                    "",
                    notifyMsg,
                    NotificationType.NOTICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show the approve notification
     * @param eachMap
     */
    public void showApproveNotification(HashMap<String, Object> eachMap) {
        MailItem item = (MailItem) eachMap.get("item");
        try {
            showTrayNotification(
                    GlobalVariables.TRAY_TYPE_APPROVE,
                    item.boxName,
                    item.referenceMsg,
                    MailItem.getUidFromMsg(item.referenceMsg),
                    String.valueOf(eachMap.get("msg")),
                    NotificationType.NOTICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * hide the sending mail notification
     * @param uid
     */
    public void hideSendingMailNotification(String uid, EventHandler resp) {

        CustomTrayNotification tray = mSendingTrayMap.get(uid);

        if (tray != null) {
            System.out.println("hideSendingMailNotification");
            tray.setOnDismiss(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("setOnDismiss-------handle");
                    resp.handle(null);
                }
            });

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    int iCnt = 0;
                    while (true) {
                        iCnt++;
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (iCnt >= 100 * 3)
                            break;
                    }
                    Platform.runLater(()->{
                        if (tray.isTrayShowing())
                            tray.dismiss(true);
                    });
                    return null;
                }
            };
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        } else
            resp.handle(null);

        mSendingTrayMap.put(uid, null);
    }

    /**
     * send the mail log to the server
     * @param aMessage
     * @param result
     * @param status
     * @param attachmentsWithSec
     */
    public void sendMailLog(Message aMessage,
                            String result,
                            int status,
                            List<EmailSenderService.AttachFile> attachmentsWithSec,
                            EventHandler resp) {

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {

                try {

                    JSONObject jObj = new JSONObject();
                    jObj.put(MAIL_LOG_MUID_HEADER, MailItem.getMuidFromMsg(aMessage));
                    jObj.put(MAIL_LOG_SUBJECT_HEADER, aMessage.getSubject());
                    jObj.put(MAIL_LOG_SEC_LEVEL_HEADER, getSecLevel(aMessage).level);
                    jObj.put(MAIL_LOG_TO_HEADER, getAllToFormattedStringFrom(aMessage, Message.RecipientType.TO));
                    jObj.put(MAIL_LOG_CC_HEADER, getAllToFormattedStringFrom(aMessage, Message.RecipientType.CC));
                    jObj.put(MAIL_LOG_BCC_HEADER, getAllToFormattedStringFrom(aMessage, Message.RecipientType.BCC));
                    jObj.put(MAIL_LOG_IMPORTANT_HEADER, MailItem.isImportantMail(aMessage));

                    if (attachmentsWithSec != null && attachmentsWithSec.size() > 0) {
                        JSONArray jAryAttach = new JSONArray();
                        for (EmailSenderService.AttachFile eachF: attachmentsWithSec) {
                            JSONObject jObjA = new JSONObject();
                            jObjA.put(MAIL_LOG_ATTACH_NAME_HEADER, eachF.getFile().getName());
                            jObjA.put(MAIL_LOG_ATTACH_SIZE_HEADER, eachF.getFile().length());
                            jObjA.put(MAIL_LOG_ATTACH_SEC_LEVEL_HEADER, eachF.getSecLevel().level);
                            jAryAttach.add(jObjA);
                        }
                        jObj.put(MAIL_LOG_ATTACH_HEADER, jAryAttach);
                    }

                    jObj.put(MAIL_LOG_APPROVAL_HEADER, MailItem.isApproveMail(aMessage));
                    if (MailItem.isApproveMail(aMessage)) {
                        JSONArray jAryApprove = new JSONArray();
                        String approvers = getApprovers(aMessage);
                        String[] approverAry = approvers.split(",");
                        for (String eachA: approverAry) {
                            jAryApprove.add(eachA);
                        }
                        jObj.put(MAIL_LOG_APPROVERS_HEADER, jAryApprove);
                    }

                    System.out.print("log result = " + jObj.toString());

                    JSONObject jRet = new JSONObject();
                    jRet.put("status", status);
                    jRet.put("msg", result);

                    String domainAccount = GlobalVariables.mainController.getPCName();

                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("action", "insertMailLog");
                    paramMap.put("domainAccount", domainAccount);
                    paramMap.put("curmailbox", GlobalVariables.account.getAddress());
                    paramMap.put("mail", Base64.getEncoder().encodeToString(jObj.toString().getBytes("utf-8")));
                    paramMap.put("result", Base64.getEncoder().encodeToString(jRet.toString().getBytes("utf-8")));
                    doHttpclientRequest(Apis.GET_MAIL_LOG(), paramMap);

//                    XMailHttpRequest req = XMailHttpRequest.post(Apis.GET_MAIL_LOG());
//                    req.send(String.format(MAIL_LOG_PARAM,
//                            domainAccount,
//                            GlobalVariables.account.getAddress(),
//                            Base64.getEncoder().encodeToString(jObj.toString().getBytes("utf-8")),
//                            Base64.getEncoder().encodeToString(jRet.toString().getBytes("utf-8"))
//                    ));
//
//                    int responseCode = req.code();
//
//                    if (responseCode == 200) {
//                        InputStreamReader inputStreamReader = new InputStreamReader(req.buffer(), Charset.forName("GB18030"));
//
//                        BufferedReader in = new BufferedReader(inputStreamReader);
//
//                        String inputLine;
//                        StringBuffer response = new StringBuffer();
//                        while((inputLine = in.readLine()) != null) {
//                            response.append(inputLine);
//                        }
//
//                        in.close();
//                        req.disconnect();
//                        System.out.print("mail log response = " + response.toString());
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                resp.handle(null);

                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * http post message with the params
     * @param url
     * @param paramMap
     * @throws Exception
     */
    public static void doHttpclientRequest(String url, Map<String, String> paramMap) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        List<NameValuePair> parameters = new ArrayList<>();
        NameValuePair nameValuePair;
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            nameValuePair = new BasicNameValuePair(entry.getKey(),entry.getValue());
            parameters.add(nameValuePair);
        }
        post.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        HttpResponse response = client.execute(post);

        int status = response.getStatusLine().getStatusCode();
        if (status != HttpStatus.SC_OK) {
            throw new Exception("执行请求失败");
        }
        String result = EntityUtils.toString(response.getEntity());
        System.out.println("result:"+result);
    }

    /**
     * show the sending mail notification
     */
    public void showSendingMailNotification(String uid, EventHandler handler) {
        showTrayNotification(
                GlobalVariables.TRAY_TYPE_SENDING_MAIL,
                null,
                null,
                uid,
                getString("sending_mail_tray"),
                NotificationType.INFORMATION,
                handler);
    }

    /**
     * show new app version notification
     */
    public void showNewAppVersionNotification() {
        showTrayNotification(
                GlobalVariables.TRAY_TYPE_UPDATE,
                null,
                null,
                "",
                getString("new_version_exist"),
                NotificationType.INFORMATION);
    }

    /**
     * show new mail notification with the count
     * @param iNewCnt
     */
    public void showNewEmailNotification(String type, int iNewCnt, Message message, String uid) {

        showTrayNotification(
                GlobalVariables.TRAY_TYPE_NEW_MAIL,
                type,
                message,
                uid,
                String.format(getString("notify_new_mail"), iNewCnt),
                NotificationType.INFORMATION);

    }

    /**
     * show notification at the Screen left-bottom
     * @param aMessage
     * @param notifyMsg
     * @param type
     */
    public void showTrayNotification(int trayType,
                                     String msgType,
                                     Message aMessage,
                                     String uid,
                                     String notifyMsg,
                                     NotificationType type) {
        showTrayNotification(trayType,
                msgType,
                aMessage,
                uid,
                notifyMsg,
                type,
                null);
    }
    /**
     * show notification at the Screen left-bottom
     * @param aMessage
     * @param notifyMsg
     * @param type
     */
    public void showTrayNotification(int trayType,
                                     String msgType,
                                     Message aMessage,
                                     String uid,
                                     String notifyMsg,
                                     NotificationType type,
                                     EventHandler handler) {

        CustomTrayNotification tray = new CustomTrayNotification(getString("Notification"), notifyMsg, type);
        tray.setAnimationType(AnimationType.POPUP);

        if (mAryTray == null)
            mAryTray = new LinkedList<>();

        if (mSendingTrayMap == null)
            mSendingTrayMap = new HashMap<>();

        if (handler != null) {
            tray.setOnShown(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    handler.handle(event);
                }
            });
        }

        if (trayType == GlobalVariables.TRAY_TYPE_SENDING_MAIL) {
            mSendingTrayMap.put(uid, tray);
        } else {

            mAryTray.add(tray);

            if (trayWorker == null)
                trayWorker = new WorkerThread("tray");

            if (!trayWorker.isWorking())
                executor.execute(trayWorker);

            tray.setOnDismiss(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (event == null)
                        System.out.println("handle on setOnClicked");
                    else {
                        System.out.println("handle on setOnDismiss = " + event);

                        Task task = new Task() {
                            @Override
                            protected Object call() throws Exception {

                                Thread.sleep(1000);

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        switch (trayType) {
                                            case GlobalVariables.TRAY_TYPE_NEW_MAIL:
                                                GlobalVariables.primaryStage.show();
                                                GlobalVariables.primaryStage.toFront();
                                                if (aMessage != null) {
                                                    showMailDetailScene(convertFrom(msgType, aMessage, uid));
                                                } else {
                                                    categoryListView.getSelectionModel().select(CAT_MAIL);
                                                    Platform.runLater(()->{
                                                        categoryItemListView.getSelectionModel().select(getInboxIndex());
                                                    });
                                                }
                                                break;
                                            case GlobalVariables.TRAY_TYPE_APPROVE:
                                                showMailDetailScene(convertFrom(msgType, aMessage, uid));
                                                break;
                                            case GlobalVariables.TRAY_TYPE_NEW_APPROVE:
                                                GlobalVariables.primaryStage.show();
                                                GlobalVariables.primaryStage.toFront();
                                                categoryListView.getSelectionModel().select(CAT_TODO);
                                                Platform.runLater(()->{
                                                    categoryItemListView.getSelectionModel().select(1);
                                                });
                                                break;
                                            case GlobalVariables.TRAY_TYPE_UPDATE:
                                                updateAndRestart();
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                });
                                return null;
                            }
                        };

                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();
                    }
                }
            });
        }


        tray.showAndWait();
    }

    /**
     * update the app and restart
     */
    public void updateAndRestart() {
        System.out.println("-----------update the app and restart-----------");

        //check the update main.jar
        File jarFile = new File(new File(GlobalVariables.APP_DATA_DIR, GlobalVariables.APP_DOWNLOAD_DIR), GlobalVariables.MAIN_JAR_NAME);
        if (jarFile.isFile() && !jarFile.isHidden() && jarFile.exists()) {
            try {
                File runtimeF1 = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                App.trackLog("current running path====");
                App.trackLog(runtimeF1.getPath());
                File runtimeF2 = new File(runtimeF1.getParentFile(), GlobalVariables.MAIN_JAR_NAME + ".bk");
                App.trackLog("will be changed to---");
                App.trackLog(runtimeF2.getPath());
                boolean isSuccess = runtimeF1.renameTo(runtimeF2);
                App.trackLog("-------success--------" + isSuccess);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * delete the default database
     */
    public void deleteDefaultDb() {
        shutdown(false, new EventHandler() {
            @Override
            public void handle(Event event) {
                File src = new File(APP_DATA_DIR + File.separator + DB_NAME);
                boolean del = src.delete();
                Platform.runLater(()->{
                    if (del) {
                        System.out.println("removed the original database as the result = " + del);
                        mMailBoxList.clear();
                        mAdbBoxList.clear();
                        mToDoBoxList.clear();

                        mMailList.clear();
                        mAdbItemList.clear();

                        mAryCategory.clear();
                        if (mAryTray != null)
                            mAryTray.clear();

                        categoryItemListView.refresh();
                        categoryListView.refresh();
                        mainTableView.refresh();
                        initDataBase();
                    } else {
                        showAlertAndWait(getString("Critical"), getString("access_denied_db_path"), getOwnerStage(), Alert.AlertType.ERROR);
                        App.removeTray();
                        App.killProcess(App.getRuntimePid());
                    }
                });
            }
        });
    }

    /**
     * change the app data path
     * @param path
     */
    public void changeAppDataPath(String path, String name) {
        shutdown(false, new EventHandler() {
            @Override
            public void handle(Event event) {

                Task <Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        updateMessage(getString("move_db_path"));
                        try {
                            File dest = new File(path + File.separator + name);
                            File src = new File(APP_DATA_DIR + File.separator + DB_NAME);
                            if (!dest.exists()) {
                                if (!dest.getParentFile().exists())
                                    dest.getParentFile().mkdirs();
                                System.out.println("copying the database to <" + dest.getAbsolutePath() + "> from <" + src.getAbsolutePath() + ">");
                                MailDatabase.copyFile(src.getAbsolutePath(), dest.getAbsolutePath());
                                Platform.runLater(()->{
                                    hideWaitingExitDialog();
                                    if (dest.exists()) {
                                        boolean del = src.delete();
                                        System.out.println("removed the original database as the result = " + del);
                                        App.setAppPath(path, name);
                                        initDataBase();
                                    } else {
                                        showAlertAndWait(getString("Critical"), getString("access_denied_db_path"), getOwnerStage(), Alert.AlertType.ERROR);
                                        App.removeTray();
                                        App.killProcess(App.getRuntimePid());
                                    }
                                });
                            } else {
                                Platform.runLater(()->{
                                    showAlertAndWait(getString("Critical"), getString("access_denied_db_path"), getOwnerStage(), Alert.AlertType.ERROR);
                                    App.removeTray();
                                    App.killProcess(App.getRuntimePid());
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };

                showWaitingExitDialog(getOwnerStage());
                Thread thread = new Thread(task);
                waitingExitMsgProperty.bind(task.messageProperty());
                thread.setDaemon(true);
                thread.start();
            }
        });
    }

    /**
     * exit the app as the exit flag
     * @param shouldExit
     */
    public void shutdown(boolean shouldExit, EventHandler handler) {

        App.trackLog("----------shutdown....");
        if (adbWorker != null)
            adbWorker.setCancel();
        if (inboxWorker != null)
            inboxWorker.setCancel();
        if (approveWorker != null)
            approveWorker.setCancel();
        if (pendingWorker != null)
            pendingWorker.setCancel();
        if (trayWorker != null)
            trayWorker.setCancel();
        if (fetchFolderWorker != null)
            fetchFolderWorker.setCancel();

        Task <Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                try {

                    if (!shouldExit)
                        updateMessage(getString("stopping_inbox_service"));

                    App.trackLog("----------inboxWorker...stopping...");
                    int iCnt = 0;
                    while (GlobalVariables.IS_WORKING_INBOX) {
                        Thread.sleep(10);
                        if (shouldExit) {
                            iCnt++;
                            if (iCnt >= 100)
                                break;
                        }
                    }

                    if (!shouldExit)
                        updateMessage(getString("stopping_approve_service"));

                    App.trackLog("----------stopped inbox worker....at = " + iCnt);
                    System.out.println("stopped inbox worker");
                    App.trackLog("stopped inbox worker");

                    iCnt = 0;
                    while (GlobalVariables.IS_WORKING_APPROVE) {
                        Thread.sleep(10);
                        if (shouldExit) {
                            iCnt++;
                            if (iCnt >= 100)
                                break;
                        }
                    }

                    if (!shouldExit)
                        updateMessage(getString("stopping_pending_service"));

                    App.trackLog("----------stopped approver worker....at = " + iCnt);
                    System.out.println("stopped approver worker");
                    App.trackLog("stopped approver worker");

                    iCnt = 0;
                    while (GlobalVariables.IS_WORKING_PENDING) {
                        Thread.sleep(10);
                        if (shouldExit) {
                            iCnt++;
                            if (iCnt >= 100)
                                break;
                        }
                    }

                    if (!shouldExit)
                        updateMessage(getString("stopping_tray_service"));

                    App.trackLog("----------stopped pending worker....at = " + iCnt);
                    System.out.println("stopped pending worker");
                    App.trackLog("stopped pending worker");

                    iCnt = 0;
                    while (GlobalVariables.IS_WORKING_TRAY) {
                        Thread.sleep(10);
                        if (shouldExit) {
                            iCnt++;
                            if (iCnt >= 100)
                                break;
                        }
                    }

                    if (!shouldExit)
                        updateMessage(getString("stopping_update_service"));

                    App.trackLog("----------stopped tray worker....at = " + iCnt);
                    System.out.println("stopped tray worker");
                    App.trackLog("stopped tray worker");

                    iCnt = 0;
                    while (GlobalVariables.IS_WORKING_FOLDER_UPDATE) {
                        Thread.sleep(10);
                        if (shouldExit) {
                            iCnt++;
                            if (iCnt >= 100)
                                break;
                        }
                    }

                    App.trackLog("----------stopped update worker....at = " + iCnt);
                    System.out.println("stopped folder update service");
                    App.trackLog("stopped folder update service");

                    if (!shouldExit)
                        updateMessage(getString("disconnecting_database"));

                    App.trackLog("----------disconnectDB....");
                    mdb.disconnectDB();
                    mdb = null;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (shouldExit) {
                    try {
                        System.out.println("remove the app tray on Windows TaskBar");
                        App.trackLog("remove the app tray on Windows TaskBar");
                        App.removeTray();
                        System.out.println("successfully app exit");
                        App.trackLog("successfully app exit --- App.killProcess(App.getRuntimePid())");
                        App.killProcess(App.getRuntimePid());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {

                    adbWorker = null;
                    inboxWorker = null;
                    pendingWorker = null;
                    approveWorker = null;
                    trayWorker = null;
                    updateWorker = null;
                    fetchFolderWorker = null;

                    Platform.runLater(()->{
                        hideWaitingExitDialog();
                        if (handler != null)
                            handler.handle(null);
                    });
                }
                return null;
            }
        };

        if (!shouldExit) {
            showWaitingExitDialog(getOwnerStage());
            waitingExitMsgProperty.bind(task.messageProperty());
        }

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * shutdown the app
     * release the all resources, services and threads
     */
    public void shutdown() {
        shutdown(true, null);
    }
}