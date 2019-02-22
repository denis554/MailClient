package com.denis.controller;

import com.denis.controller.services.EmailSenderService;
import com.denis.model.AddressBookItem;
import com.denis.model.EmailAccountBean;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.model.MailItem;
import com.denis.model.SecurityLevel;
import com.denis.model.http.Apis;
import com.denis.model.http.XMailHttpRequest;
import com.denis.view.ViewFactory;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.*;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;

import static com.denis.controller.PrototypeController.getPCName;
import static com.denis.model.GlobalVariables.GlobalVariables.*;

public class ComposeMailController extends AbstractController implements Initializable {

    @FXML
    Label errorLabel;

    @FXML
    TextField toTextField;

    @FXML
    TextField ccTextField;

    @FXML
    TextField bccTextField;

    @FXML
    VBox rootVBox;

    @FXML
    HBox bccHBox;

    @FXML
    MenuItem showHideBccMenuItem;

    @FXML
    private HTMLEditor ComposeArea;

    @FXML
    private TextField subjectTextField;

    @FXML
    private Label subjectLabel;

    @FXML
    private HBox subjectHBox;

    @FXML
    VBox attachVBox;

    @FXML
    CheckBox retNoteCheckBox;

    @FXML
    CheckBox importantCheckBox;

    @FXML
    Button toButton;
    @FXML
    Button ccButton;
    @FXML
    Button bccButton;

    @FXML
    HBox toHBox;

    @FXML
    HBox ccHBox;

    @FXML
    HBox secLevelHBox;

    @FXML
    Button attachButton;

    @FXML
    Button toLeftButton;

    @FXML
    Button ccLeftButton;

    @FXML
    Button bccLeftButton;

    private List<EmailSenderService.AttachFile> attachmentsWithSec = new ArrayList<EmailSenderService.AttachFile>();

    final ToggleGroup group = new ToggleGroup();

    EmailSenderService emailSenderService;    private boolean isSent = false;

    JSONObject mJsonCheckSendValue;

    private Message mMessage;
    private String mType;
    private ObservableList<AddressBookItem> mAdbItems;
    private Alert saveAlert = null;
    private List<File> senderAttaches = null;
    int mReceiverType;
    public static final int RECEIVER_TYPE_TO = 0;
    public static final int RECEIVER_TYPE_CC = RECEIVER_TYPE_TO + 1;
    public static final int RECEIVER_TYPE_BCC = RECEIVER_TYPE_CC + 1;

    AutoCompleteTextField toAutoTextField;
    AutoCompleteTextField ccAutoTextField;
    AutoCompleteTextField bccAutoTextField;

    public ComposeMailController(ModelAccess modelAccess, Message message, String type) {
        super(modelAccess);
        mMessage = message;
        mType = type;
    }

    public ComposeMailController(ModelAccess modelAccess, ObservableList<AddressBookItem> adbItems) {
        super(modelAccess);
        this.mAdbItems = adbItems;
        mType = "";
    }

    public ComposeMailController(ModelAccess modelAccess, List<File> senderAttaches) {
        super(modelAccess);
        mType = "";
        this.senderAttaches = senderAttaches;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("initialize");
        saveAlert = null;
        addComposeController(ComposeMailController.this);
        int iCnt = 0;
        for (SecurityLevel eachLevel: GlobalVariables.securityList) {
            RadioButton secLevelRBtn = new RadioButton();
            secLevelRBtn.setUserData(eachLevel);
            secLevelRBtn.setToggleGroup(group);
            secLevelHBox.getChildren().add(secLevelRBtn);
            if (iCnt == 0)
                secLevelRBtn.selectedProperty().setValue(true);
            secLevelRBtn.setText(eachLevel.levelName);
            secLevelRBtn.setStyle("-fx-text-fill: " + eachLevel.levelColor);
            iCnt++;
        }

//        rootVBox.getChildren().remove(bccHBox);
        subjectHBox.getChildren().remove(subjectLabel);

        checkIfExistsSendToFiles();

        toButton.setText("");
        ccButton.setText("");
        bccButton.setText("");

        toTextField.getStyleClass().clear();
        ccTextField.getStyleClass().clear();
        bccTextField.getStyleClass().clear();
        subjectTextField.getStyleClass().clear();

        toHBox.getChildren().remove(toTextField);
        ccHBox.getChildren().remove(ccTextField);
        bccHBox.getChildren().remove(bccTextField);
//        ccHBox.getChildren().remove(ccHBox);
//        bccHBox.getChildren().remove(bccHBox);
//        subjectHBox.getChildren().remove(subjectHBox);

        toAutoTextField = getTextField();
        ccAutoTextField = getTextField();
        bccAutoTextField = getTextField();

        toHBox.getChildren().add(0, toAutoTextField);
        ccHBox.getChildren().add(0, ccAutoTextField);
        bccHBox.getChildren().add(0, bccAutoTextField);

        toHBox.getStyleClass().addAll("text-field", "text-input");
        ccHBox.getStyleClass().addAll("text-field", "text-input");
        bccHBox.getStyleClass().addAll("text-field", "text-input");
        subjectHBox.getStyleClass().addAll("text-field", "text-input");

        toButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/add_box.png"));
        ccButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/add_box.png"));
        bccButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/add_box.png"));

        switch (mType.toLowerCase()) {

            case "reply":
            {
                Message message = mMessage;
                try {
                    String from = getUtf8String(message.getFrom()[0].toString());
                    toAutoTextField.setText(from);
                    String subject = message.getSubject();
                    subjectTextField.setText(String.format(getString("reply_subject"), subject));

                    String content = GlobalVariables.mainController.getBodyText(MailItem.getUidFromMsg(message));

                    ComposeArea.setHtmlText(getReplyString(content, message));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                break;

            case "reply to all":
            {
                Message message = mMessage;
                try {
                    String from = getUtf8String(message.getFrom()[0].toString());
                    toAutoTextField.setText(from);
                    Address[] addresslist = message.getRecipients(Message.RecipientType.CC);
                    if (addresslist != null) {
                        String cc = "";
                        for (int i = 0; i < addresslist.length; i++) {
                            if (i == 0) {
                                cc = addresslist[i].toString();
                            } else {
                                cc += ", " + addresslist[i].toString();
                            }
                        }
                        ccAutoTextField.setText(cc);
                    }

                    addresslist = message.getRecipients(Message.RecipientType.BCC);
                    if (addresslist != null) {
                        String bcc = "";
                        for (int i = 0; i < addresslist.length; i++) {
                            if (i == 0) {
                                bcc = addresslist[i].toString();
                            } else {
                                bcc += ", " + addresslist[i].toString();
                            }
                        }
                        bccAutoTextField.setText(bcc);
                    }

                    String subject = message.getSubject();
                    subjectTextField.setText(String.format(getString("reply_subject"), subject));

                    String content = GlobalVariables.mainController.getBodyText(MailItem.getUidFromMsg(message));

                    ComposeArea.setHtmlText(getReplyString(content, message));

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
                break;

            case "draft":
            case "forward":
            {
                Message message = mMessage;
                try {

                    String cc = getAllToFormattedStringFrom(message, Message.RecipientType.CC);
                    ccAutoTextField.setText(cc);
                    String to = getAllToFormattedStringFrom(message, Message.RecipientType.TO);
                    toAutoTextField.setText(to);
                    String bcc = getAllToFormattedStringFrom(message, Message.RecipientType.BCC);
                    bccAutoTextField.setText(bcc);

                    String subject = message.getSubject();
                    subjectTextField.setText(subject);
                    if (mType.equalsIgnoreCase("Forward")) {
                        toAutoTextField.setText("");
                        subjectTextField.setText(String.format(getString("forward_subject"), subject));
                    }

                    String content = GlobalVariables.mainController.getBodyText(MailItem.getUidFromMsg(message));

                    ComposeArea.setHtmlText(content);
                    if (mType.equalsIgnoreCase("Forward")) {
                        ComposeArea.setHtmlText(getReplyString(content, message));
                    }

                    //security level
                    String secLevel[] = message.getHeader(MAIL_SEC_LEVEL_HEADER);
                    SecurityLevel mailSecLevel = null;
                    if (secLevel != null && secLevel.length > 0) {
                        mailSecLevel = getSecLevel(secLevel[0]);
                        System.out.println("security level = " + mailSecLevel);
                        for (Toggle eachTogle: group.getToggles()) {
                            SecurityLevel scLevel = (SecurityLevel) eachTogle.getUserData();
                            if (scLevel.level == mailSecLevel.level)
                                eachTogle.selectedProperty().setValue(true);
                        }
                    }

                    //important
                    importantCheckBox.setSelected(MailItem.isImportantMail(message));

                    //return note
                    String returnNote[] = message.getHeader(MAIL_DIPOSITION_TO_HEADER);
                    if (returnNote != null && returnNote.length > 0) {
                        System.out.println("important = " + returnNote[0]);
                        retNoteCheckBox.setSelected(true);
                    }

                    //attach file
                    if (mType.toLowerCase().equalsIgnoreCase("draft")) {
                        List<EmailSenderService.AttachFile> attachFiles = GlobalVariables.mainController.loadDraftsFiles(MailItem.getUidFromMsg(mMessage));
                        setAttachmentsWithSec(attachFiles);
                    } else {
                        List<EmailSenderService.AttachFile> attachFiles = getAttachFiles(MailItem.getUidFromMsg(message));
                        setAttachmentsWithSec(attachFiles);
//                        setAttachmentsWithSec(multipart, secLevel, MailItem.getUidFromMsg(message));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                break;

            default:
                if (mAdbItems != null && mAdbItems.size() > 0) {
                    String toStr = "";
                    for(int i = 0; i < mAdbItems.size(); i++) {
                        if (i == 0)
                            toStr = getFormattedEmailString(mAdbItems.get(i).mailAddressProperty().getValue());
                        else
                            toStr += ", " + getFormattedEmailString(mAdbItems.get(i).mailAddressProperty().getValue());
                    }
                    toAutoTextField.setText(toStr);
                }
                break;
        }

        WebView webView = (WebView) ComposeArea.lookup("WebView");

        webView.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {

                final Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    setAttachments(db.getFiles());
                }
                event.setDropCompleted(true);
                event.consume();
            }
        });

        webView.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                final Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.LINK);
                } else {
                    event.consume();
                }
            }
        });

        rootVBox.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {

                final Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    setAttachments(db.getFiles());
                }
                event.setDropCompleted(true);
                event.consume();
            }
        });

        rootVBox.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                final Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.LINK);
                } else {
                    event.consume();
                }
            }
        });

        if(senderAttaches!=null) {
            setAttachments(senderAttaches);
        }

        Platform.runLater(()->{
            toLeftButton.setPrefHeight(toHBox.getHeight());
            ccLeftButton.setPrefHeight(toHBox.getHeight());
            bccLeftButton.setPrefHeight(toHBox.getHeight());
        });
    }

    /**
     * get the AutoCompleteTextField
     * @return
     */
    public AutoCompleteTextField getTextField() {
        AutoCompleteTextField ret = new AutoCompleteTextField();
        ret.getStyleClass().clear();
        HBox.setHgrow(ret, Priority.ALWAYS);

        ArrayList<String> allAdb = GlobalVariables.mainController.getAllAddress();
        ret.getEntries().addAll(allAdb);

        return ret;
    }

    /**
     * get the reply contents string when user replies
     * @param content
     * @param aMsg
     * @return
     */
    public String getReplyString(String content, Message aMsg) {

        String newContent = "";
        try {

            MimeMessage message = (MimeMessage) aMsg;
            String from = getFormattedEmailString(getUtf8String(message.getFrom()[0].toString()));
            String sentDate = getSentDateFrom(message);
            String to = getAllToFormattedStringFrom(message, Message.RecipientType.TO);
            String subject = message.getSubject();

            Document doc = Jsoup.parse(content);
            String body = doc.body().html();

            from = from.replaceAll("<", "&lt;");
            from = from.replaceAll(">", "&gt;");

            to = to.replaceAll("<", "&lt;");
            to = to.replaceAll(">", "&gt;");

            subject = subject.replaceAll("<", "&lt;");
            subject = subject.replaceAll(">", "&gt;");

            newContent =
                    "<html dir=\"ltr\">" +
                    "<head></head>" +
                    "<body contenteditable=\"true\">" +
                    "<br>" +
                    "<br>" +
                    "<div style = 'margin: 3px;'>=====================" + getString("org_msg") + "===================</div>" +
                    "<div style ='background-color:#EEE;'>" +
                    "&nbsp;&nbsp;" + String.format(getString("mail_from"), from) + "<br>" +
                    "&nbsp;&nbsp;" + String.format(getString("SentDateColon"), sentDate) + "<br>" +
                    "&nbsp;&nbsp;" + String.format(getString("ToColon1"), to) + "<br>" +
                    "&nbsp;&nbsp;" + String.format(getString("SubjectColon2"), subject) + "<br>" +
                    "</div>" +
                    "<div style = 'margin: 10px;'>" + body + "</div>" +
                    "</body>" +
                    "</html>";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newContent;
    }

    /**
     * get the all receivers info
     * @param scene
     */
    public void fetchAllRecipients(Scene scene) {
        FXMLLoader loader = (FXMLLoader) scene.getUserData();
        ChooseReceiverController ctl = loader.getController();

        //parse
        Message message = ctl.getAllReceivers();
        if (message == null)
            return;

        String to;
        switch (mReceiverType) {
            case RECEIVER_TYPE_TO:
                to = getAllToFormattedStringFrom(message, Message.RecipientType.TO);
                toAutoTextField.setText(to.replaceAll(";", ","));
                break;
            case RECEIVER_TYPE_CC:
                to = getAllToFormattedStringFrom(message, Message.RecipientType.CC);
                ccAutoTextField.setText(to.replaceAll(";", ","));
                break;
            case RECEIVER_TYPE_BCC:
                to = getAllToFormattedStringFrom(message, Message.RecipientType.BCC);
                bccAutoTextField.setText(to.replaceAll(";", ","));
                break;
        }

    }

    /**
     * set the all receivers as its type to the ChooseReceiverController
     * @param scene
     * @param type
     */
    public void setAllRecipientsTo(Scene scene, int type) {

        mReceiverType = type;
        FXMLLoader loader = (FXMLLoader) scene.getUserData();
        ChooseReceiverController ctl = loader.getController();

        //add
        EmailAccountBean emailAccountBean = getModelAccess().getEmailAccountByName(GlobalVariables.account.getAddress());
        Session session = emailAccountBean.getSession();
        MimeMessage message = new MimeMessage(session);
        try {

            String to;

            switch (mReceiverType) {
                case RECEIVER_TYPE_TO:
                    to = toAutoTextField.getText();
                    System.out.println("all To raw receivers = " + to);
                    to = getEscapeFromEmail(to);
                    System.out.println("all To escaped receivers = " + to);
                    message.setRecipients(Message.RecipientType.TO, to);
                    break;
                case RECEIVER_TYPE_CC:
                    to = ccAutoTextField.getText();
                    System.out.println("all Cc raw receivers = " + to);
                    to = getEscapeFromEmail(to);
                    System.out.println("all Cc escaped receivers = " + to);
                    message.setRecipients(Message.RecipientType.CC, to);
                    break;
                case RECEIVER_TYPE_BCC:
                    to = bccAutoTextField.getText();
                    System.out.println("all Bcc raw receivers = " + to);
                    to = getEscapeFromEmail(to);
                    System.out.println("all Bcc escaped receivers = " + to);
                    message.setRecipients(Message.RecipientType.BCC, to);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ctl.setAllReceivers(message, type);
    }

    @Override
    public Stage getOwnerStage() {
        return (Stage) toAutoTextField.getScene().getWindow();
    }
    
    @FXML
    public void actionOnToButton() {

        if (!isMailValidation())
            return;

        Scene scene = ViewFactory.defaultFactory.getChooseUserScene();
        CustomStage stage = new CustomStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle(getString("tb.addressBook"));
        stage.initOwner(getOwnerStage());
        setAllRecipientsTo(scene, RECEIVER_TYPE_TO);
        stage.showAndWait();
        fetchAllRecipients(scene);
    }

    @FXML
    public void actionOnCcButton() {

        if (!isMailValidation())
            return;

        Scene scene = ViewFactory.defaultFactory.getChooseUserScene();
        CustomStage stage = new CustomStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle(getString("tb.addressBook"));
        stage.initOwner(getOwnerStage());
        setAllRecipientsTo(scene, RECEIVER_TYPE_CC);
        stage.showAndWait();
        fetchAllRecipients(scene);
    }

    @FXML
    public void actionOnBccButton() {

        if (!isMailValidation())
            return;

        Scene scene = ViewFactory.defaultFactory.getChooseUserScene();
        CustomStage stage = new CustomStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle(getString("tb.addressBook"));
        stage.initOwner(getOwnerStage());
        setAllRecipientsTo(scene, RECEIVER_TYPE_BCC);
        stage.showAndWait();
        fetchAllRecipients(scene);
    }

    @FXML
    public void actionOnShowHideBccMenuItem() {
        System.out.println("actionOnShowHideBccMenuItem");
        int index = rootVBox.getChildren().indexOf(bccHBox);
        if (index == -1) {
            rootVBox.getChildren().add(3, bccHBox);
            showHideBccMenuItem.setText(getString("hide_bcc"));
        } else {
            rootVBox.getChildren().remove(bccHBox);
            showHideBccMenuItem.setText(getString("show_bcc"));
        }
    }

    @FXML
    public void actionOnAttachButton() {
        System.out.println("Clicked on actionOnAttachButton");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(getString("open_res_file"));

        String home = System.getProperty("user.home");
        if (home != null && home.trim().length() > 0) {
            File file = new File(home + "/Downloads/");
            if(!file.exists()) file = new File(home);
            fileChooser.setInitialDirectory(file);
        }

        //multiple file dialog
        List<File> fileList = fileChooser.showOpenMultipleDialog(attachVBox.getScene().getWindow());
        setAttachments(fileList);
    }

    /**
     * set attachment files to UI
     * @param fileList
     */
    public void setAttachments(List<File> fileList) {
        if (fileList == null || fileList.size() == 0)
            return;

        for (File eachFile: fileList) {
            System.out.println("processing " + eachFile);
            //check a duplication file
            int iCnt = attachVBox.getChildren().size();
            boolean isExist = false;
            for (int i = 0; i  < iCnt; i++) {
                Node child = attachVBox.getChildren().get(i);
                File f = (File) child.getUserData();
                if (f.getAbsolutePath().compareToIgnoreCase(eachFile.getAbsolutePath()) == 0) {
                    isExist = true;
                    break;
                }
            }
            if (isExist)
                continue;
            System.out.println("selected file = " + eachFile.getAbsolutePath());
            attachVBox.setPadding(new Insets(5, 0, 5,0));
            attachVBox.getChildren().add(getAttachFile(eachFile, null, null));
        }
    }

    /**
     * set attachment files to UI with multi part
     * @param multipart
     * @param secLevel
     * @param uid
     */
    public void setAttachmentsWithSec(Multipart multipart, String[] secLevel, String uid) {
        try {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                System.out.println(bodyPart.getContentType());
                if(bodyPart.getFileName() != null && bodyPart.getFileName().length() > 0) {
                    File attachFile = getAttachFile(bodyPart, uid);

                    String[] attachLevel = bodyPart.getHeader(MAIL_ATTACH_SEC_LEVEL_HEADER);
                    SecurityLevel attachFileLevel = null;
                    if (secLevel != null && secLevel.length > 0) {
                        System.out.println("attach security level = " + attachLevel[0]);
                        attachFileLevel = getSecLevel(attachLevel[0]);
                    }
                    attachVBox.setPadding(new Insets(5, 0, 5,0));
                    attachVBox.getChildren().add(getAttachFile(attachFile, bodyPart.getFileName(), attachFileLevel));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set attachment files to UI
     * @param fileList
     */
    public void setAttachmentsWithSec(List<EmailSenderService.AttachFile> fileList) {
        if (fileList != null && fileList.size() > 0) {
            for (EmailSenderService.AttachFile eachFile: fileList) {
                System.out.println("selected file = " + eachFile.getFile().getAbsolutePath());
                attachVBox.setPadding(new Insets(5, 0, 5,0));
                attachVBox.getChildren().add(getAttachFile(eachFile.getFile(), null, eachFile.getSecLevel()));
            }
        }
    }

    /**
     * check the Send To File list from windows
     */
    public void checkIfExistsSendToFiles() {
        setAttachments(getAttachFiles());
    }

    /**
     * @author pilot
     * remove the attach file
     * @param aFile
     */
    public void removeAttachFile(File aFile) {
        int iSize = attachVBox.getChildren().size();
        for (int i = 0; i < iSize; i++) {
            Node eachNode = attachVBox.getChildren().get(i);
            if (((File)eachNode.getUserData()).getAbsolutePath().equalsIgnoreCase(aFile.getAbsolutePath())) {
                attachVBox.getChildren().remove(i);
                break;
            }
        }
    }

    /**
     * get attach file node
     * @param aFile
     * @param aFileName
     * @param aSecLevel
     * @return
     */
    public Node getAttachFile(File aFile, String aFileName, SecurityLevel aSecLevel) {
        SplitMenuButton attachButton = new SplitMenuButton();

        String fileName = aFile.getName();
        attachButton.setGraphic(getFileIcon(aFile));

        attachButton.getStyleClass().add("attach-menu-button");

        if (aFileName != null)
            fileName = aFileName;

        attachButton.setText(fileName + "(" + getFormattedSize((int)aFile.length()) + ")");

        attachButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("clicked on");
                try {
                    Desktop.getDesktop().open(aFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        MenuItem actionItemView = new MenuItem();
        MenuItem actionItemOpenFolder = new MenuItem();
        MenuItem actionItemRemove = new MenuItem();

        actionItemRemove.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                System.out.println("Delete action on the [" + aFile.getName() + "] file");

                removeAttachFile(aFile);
            }
        });

        actionItemView.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                System.out.println("Delete action on the [" + aFile.getName() + "] file");
                try {
                    Desktop.getDesktop().open(aFile);
                } catch ( Exception e) {
                    e.printStackTrace();
                }
            }
        });

        actionItemOpenFolder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                System.out.println("Open action on the [" + aFile.getName() + "] file");
                try {
                    Desktop.getDesktop().open(aFile.getParentFile());
                } catch ( Exception e) {
                    e.printStackTrace();
                }
            }
        });

        actionItemView.setText(getString("Open"));
        actionItemOpenFolder.setText(getString("Open_Folder"));
        actionItemRemove.setText(getString("Remove"));

        attachButton.getItems().add(actionItemView);
        attachButton.getItems().add(actionItemOpenFolder);
        attachButton.getItems().add(actionItemRemove);

        HBox box= new HBox();
        box.setSpacing(5);
        box.setAlignment(Pos.CENTER_LEFT);

        int index = 0;

        if (aSecLevel == null) {
            String regName = fileName.split("\\.")[0];

            int attachLength = GlobalVariables.securityList.size();

            float[] ratios = new float[attachLength];

            for (int i = 0; i < attachLength; i++){
                int appear_count = 0;
                SecurityLevel key = GlobalVariables.securityList.get(i);
                for (int j = 0; j < key.toString().toCharArray().length; j++){
                    char elem_char = key.toString().toCharArray()[j];
                    if (regName.indexOf(elem_char) >= 0) {
                        appear_count++;
                    }
                }
                ratios[i] = (float) appear_count / (float) key.toString().toCharArray().length;
            }

            float max = ratios[0];

            for (int i = 0; i < attachLength; i++) {
                if (max < ratios[i]){
                    max = ratios[i];
                    index = i;
                }
            }
        }

        SecurityLevel security = GlobalVariables.securityList.get(index);
        if (aSecLevel != null)
            security = aSecLevel;

        ComboBox attachClassComboBox = new ComboBox();
        attachClassComboBox.getItems().addAll(GlobalVariables.securityList);
        attachClassComboBox.getSelectionModel().select(security);

        setSecurityLevelCssTo(attachClassComboBox, security);

        attachClassComboBox.setCellFactory(new Callback<ListView<SecurityLevel>, ListCell<SecurityLevel>>() {
            @Override public ListCell<SecurityLevel> call(ListView<SecurityLevel> p) {
                return new ListCell<SecurityLevel>() {
                    @Override
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

        attachClassComboBox.valueProperty().addListener((obs, oldItem, newItem) -> {
            setSecurityLevelCssTo(attachClassComboBox, ((SecurityLevel)newItem));
        });

        box.setUserData(aFile);
        box.getChildren().addAll(attachButton, attachClassComboBox);

        return box;
    }

    /**
     * get the only email validation
     * @return
     */
    public boolean isMailValidation() {
        String val = toAutoTextField.getText();
        String invalid = isInvalidEmail(val);
        if (invalid != null && invalid.length() > 0) {
            showAlertAndWait(getString("Critical") + "!",
                    String.format(getString("invalid_email_receivers"), invalid, getString("ToStrim")),
                    getOwnerStage(), Alert.AlertType.ERROR);
            return false;
        }

        val = ccAutoTextField.getText();
        invalid = isInvalidEmail(val);
        if (invalid != null && invalid.length() > 0) {
            showAlertAndWait(getString("Critical") + "!",
                    String.format(getString("invalid_email_receivers"), invalid, getString("CcStrim")),
                    getOwnerStage(), Alert.AlertType.ERROR);
            return false;
        }

        val = bccAutoTextField.getText();
        invalid = isInvalidEmail(val);
        if (invalid != null && invalid.length() > 0) {
            showAlertAndWait(getString("Critical") + "!",
                    String.format(getString("invalid_email_receivers"), invalid, getString("BccStrim")),
                    getOwnerStage(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    /**
     * get the sending mail validation
     * @return
     */
    public boolean isValidation() {

        //sender
        String val = toAutoTextField.getText();
        if (val.isEmpty()) {
            showAlertAndWait(getString("Warning") + "!", getString("no_sender"), getOwnerStage(), Alert.AlertType.WARNING);
            return false;
        }

        if (!isMailValidation())
            return false;

        //check attach file level and mail security level
        SecurityLevel secLevel = (SecurityLevel) group.getSelectedToggle().getUserData();
        for (EmailSenderService.AttachFile eachFile: attachmentsWithSec) {
            if (eachFile.getSecLevel().level > secLevel.level) {
                showAlertAndWait(getString("Warning") + "!", getString("attach_file_error"), getOwnerStage(), Alert.AlertType.WARNING);
                return false;
            }
        }

        //mail password check
        try {
            String url = String.format(Apis.GET_MAILBOX_INFO(), getPCName());
            XMailHttpRequest req = XMailHttpRequest.get(url);
            HttpURLConnection con = req.getConnection();
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
            JSONObject jsonObj = (JSONObject)parser.parse(response.toString());
            String pwd = jsonObj.get("mailboxPwd").toString();
            if (GlobalVariables.account.getPassword().compareTo(pwd) != 0) {
                showAlertAndWait(getString("Warning"), getString("pwd_changed"), getOwnerStage(), Alert.AlertType.WARNING);
                saveDrafts();
                GlobalVariables.mainController.shutdown();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return true;
    }

    @FXML
    public void actionOnSaveDraftButton() {
        if (!isCloseableWithoutSave())
            saveDraftsComplete();
        removeComposeController(ComposeMailController.this);
        getOwnerStage().close();
    }

    @FXML
    public void actionOnSendButton() {

        setAttachmentFromUI();

        if (!isValidation()) {
            return;
        }

        //check subject
        String val = subjectTextField.getText();
        if (val.isEmpty()) {
            Optional<ButtonType> ret = showAlertCallback(getString("Warning") + "!", getString("no_subject1"), getOwnerStage(), Alert.AlertType.WARNING);
            if (ret.get() == ButtonType.CANCEL)
                return;
        }

        final String[] to = {toAutoTextField.getText()};
        to[0] = getEscapeFromEmail(to[0], true);
        to[0] = to[0].replaceAll(",", ";");
        int level = ((SecurityLevel)group.getSelectedToggle().getUserData()).level;

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {

                updateMessage(getString("check_send"));

                try {

                    StringBuffer checkResponse = getCheckSendResponse(to[0], level);

                    JSONObject jsonObj = getResultJSON(checkResponse);
                    boolean isApproved = getResponseParameter(jsonObj, "approveMail");
                    boolean isPass = getResponseParameter(jsonObj, "pass");

                    // check the pass field.
                    if (!isPass) {
                        String errInfo = getErrorDataString(jsonObj);
                        Platform.runLater(()->{
                            showAlertAndWait(getString("send_forbidden"), getString("cant_send_mail") + "\n" + errInfo, getOwnerStage(), Alert.AlertType.WARNING);
                        });
                        return null;
                    }

                    Platform.runLater(()->{

                        hideWaitingDialog();

                        SecurityLevel secLevel = (SecurityLevel) group.getSelectedToggle().getUserData();
                        String content = ComposeArea.getHtmlText();

                        if (isApproved)
                            to[0] = getDirectEmails(jsonObj, false);
                        else
                            to[0] = getEscapeFromEmail(toAutoTextField.getText(), true);

                        emailSenderService =
                                new EmailSenderService(getModelAccess().getEmailAccountByName(GlobalVariables.account.getAddress()),
                                        subjectTextField.getText(),
                                        to[0],
                                        getEscapeFromEmail(ccAutoTextField.getText(), true),
                                        getEscapeFromEmail(bccAutoTextField.getText(), true),
                                        content,
                                        secLevel,
                                        retNoteCheckBox.isSelected(),
                                        importantCheckBox.isSelected(),
                                        attachmentsWithSec);

                        //check approve mail
                        if (isApproved) {
                            showNeedApproveMail(jsonObj);
                            return;
                        }

                        sendMail(emailSenderService);

                    });

                } catch (Exception e) {
                    e.printStackTrace();
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

    /**
     * get prepare message for the approve mail
     * @return
     */
    public Message getApproveMessage() {
        return emailSenderService.getPreparedMessage();
    }

    /**
     * send the email
     */
    public void sendMailTo() {
        sendMail(emailSenderService);
    }

    /**
     * send the mail by using the SMTP transport
     */
    public void sendMail(EmailSenderService service) {
        errorLabel.setText(getString("sending_mail"));

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(()->getOwnerStage().hide());
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        Platform.runLater(()->{

            GlobalVariables.mainController.showSendingMailNotification(
                    MailItem.getUidFromMsg(service.getPreparedMessage()),
                    new EventHandler() {

                @Override
                public void handle(Event event) {
                    service.restart();
                    service.setOnSucceeded(e-> {

                        GlobalVariables.mainController.hideSendingMailNotification(
                                MailItem.getUidFromMsg(service.getPreparedMessage()),
                                new EventHandler() {

                            @Override
                            public void handle(Event event) {

                                int status;
                                if(!service.getValue().equals("MESSAGE_SENT_OK")){
                                    status = -1;
                                } else {
                                    isSent = true;
                                    status = 0;
                                }

                                GlobalVariables.mainController.sendMailLog(
                                        service.getPreparedMessage(),
                                        service.getValue(),
                                        status,
                                        attachmentsWithSec,
                                        new EventHandler() {

                                    @Override
                                    public void handle(Event event) {

                                        System.out.println("sendMailLog-------handle");
                                        if (status == -1) {
                                            getOwnerStage().show();
                                            getOwnerStage().toFront();
                                            Platform.runLater(()->{
                                                sentFailedNotify(service.getValue());
                                            });
                                        } else {
                                            System.out.println("close-------handle");
                                            updateMessage();
                                            getOwnerStage().close();
                                        }
                                    }
                                });
                            }
                        });
                    });
                }
            });
        });
//        Platform.runLater(()-> showWaitingDlg());
    }

    /**
     * This function will be sent the mail to some persons who exclude the person who needs the approval mail
     * @param aJSON
     */
    public void sendMailByDirect(JSONObject aJSON) {
        String receivers = getDirectEmails(aJSON, true);
        if (receivers == null || receivers.length() == 0)
            return;
        SecurityLevel secLevel = (SecurityLevel) group.getSelectedToggle().getUserData();
        String content = ComposeArea.getHtmlText();
        EmailSenderService sender = new EmailSenderService(
                getModelAccess().getEmailAccountByName(GlobalVariables.account.getAddress()),
                subjectTextField.getText(),
                receivers,
                getEscapeFromEmail(ccAutoTextField.getText()),
                getEscapeFromEmail(bccAutoTextField.getText()),
                content,
                secLevel,
                retNoteCheckBox.isSelected(),
                importantCheckBox.isSelected(),
                attachmentsWithSec
        );
        sendMail(sender);
    }

    /**
     * hide waiting dialog
     */
    public void hideWaitingDlg() {
        if (waitingExitDlg != null && waitingExitDlg.isShowing())
            waitingExitDlg.close();
    }

    private boolean getResponseParameter(JSONObject jsonObj, String parameter) {
        try {
            if (jsonObj != null) {
                return  ((Boolean) jsonObj.get(parameter)).booleanValue();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONObject getCheckSendJSON() {
        return mJsonCheckSendValue;
    }

    /**
     * get an error string from JSONArray
     * @param aJSON
     * @return
     */
    public String getErrorDataString(JSONObject aJSON) {
        String msgContents = new String("");
        try {
            JSONArray aryError = (JSONArray) aJSON.get("errorDatas");
            int size = aryError.size();
            String addressTitle = new String(getString("AddressColon"));
            String infoTitle = new String(getString("InfoColon"));
            String newLine = new String("\n");
            for (int i = 0; i < size; i++) {
                JSONObject obj = (JSONObject) aryError.get(i);
                String address = obj.get("address").toString();
                String info = obj.get("info").toString();
                msgContents += addressTitle + address + infoTitle + info + newLine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgContents;
    }

    /**
     * get the exclude receivers in JSONObject
     * if the isExclude = false, get the all receivers of errorDatas
     * else then, get the some receivers of all receivers that exclude the all receivers of errorDatas
     * @param aJSON
     * @param isExclude
     * @return
     */
    public String getDirectEmails(JSONObject aJSON, boolean isExclude) {
        String ret = "";
        try {
            JSONArray aryError = (JSONArray) aJSON.get("errorDatas");
            int size = aryError.size();
            if (!isExclude) {
                for (int i = 0; i < size; i++) {
                    JSONObject obj = (JSONObject) aryError.get(i);
                    String cmp2 = obj.get("address").toString();
                    if (ret.length() == 0)
                        ret = cmp2;
                    else
                        ret += "," + cmp2;
                }
                return ret;
            }
            String[] mails = getEscapeFromEmail(toAutoTextField.getText(), true).split(",");
            if (mails != null && mails.length > 0) {
                for (String cmp1: mails) {
                    boolean isExist = false;
                    for (int i = 0; i < size; i++) {
                        JSONObject obj = (JSONObject) aryError.get(i);
                        String cmp2 = getEscapeFromEmail(obj.get("address").toString(), true);
                        if (cmp1.trim().compareToIgnoreCase(cmp2.trim()) == 0) {
                            isExist = true;
                            break;
                        }
                    }
                    if (!isExist) {
                        if (ret.length() == 0)
                            ret = cmp1;
                        else
                            ret += "," + cmp1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * show the approve dialog
     * @param aJSON
     */
    private void showNeedApproveMail(JSONObject aJSON) {

        //send the mail directly
        sendMailByDirect(aJSON);

        mJsonCheckSendValue = aJSON;
        String msg = aJSON.get("msg").toString();
        JSONArray aryApprovers = (JSONArray) aJSON.get("approvers");

        String msgContents = getErrorDataString(aJSON);

        Label infoLabel = new Label();
        infoLabel.setText(msgContents);
        infoLabel.setPadding(new Insets(0,5,0,5));

        FlowPane exceptionFlowPane = new FlowPane();
        exceptionFlowPane.getChildren().addAll(infoLabel);

        Alert infoAlertDlg = new Alert(Alert.AlertType.INFORMATION);
        Stage stage = (Stage) infoAlertDlg.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
        infoAlertDlg.setTitle(getString("approve_mail"));
        infoAlertDlg.setHeaderText(msg);
        infoAlertDlg.setContentText(msgContents);
        infoAlertDlg.getButtonTypes().setAll(ButtonType.OK);
        ((Button) infoAlertDlg.getDialogPane().lookupButton(ButtonType.OK)).setText(getString("confirm"));
        infoAlertDlg.getDialogPane().setContent(exceptionFlowPane);
        infoAlertDlg.showAndWait().ifPresent(bt -> {
            //show approver select list dialog
            if (bt == ButtonType.CANCEL){
            } else if (bt == ButtonType.OK) {
                showApproversDialog(aryApprovers);
            }
        });
    }

    /**
     * show and send approve mail to the selected chooser
     * @param aryApprovers
     */
    public void showApproversDialog(JSONArray aryApprovers) {
        Scene scene = ViewFactory.defaultFactory.getChooseApproverScene(aryApprovers, this);
        CustomStage stage = new CustomStage();
        stage.setScene(scene);
        stage.initOwner(ComposeArea.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(getString("select_approver"));
        stage.show();
    }

    /**
     * notify the failed reason
     * @param value
     */
    public void sentFailedNotify(String value) {
        errorLabel.setText(getString("fail_mail_sent"));
        showAlertAndWait(getString("Critical"), value, getOwnerStage(), Alert.AlertType.ERROR);
    }

    /**
     * show the successful message and close this stage
     */
    public void updateMessage() {
        errorLabel.setText(getString("success_mail_sent"));

        try {
            if (mMessage != null) {
                String uid = MailItem.getUidFromMsg(mMessage);
                switch (mType.toLowerCase()) {
                    case "reply":
                    case "reply to all":
                        if (MailItem.getStatOf(mMessage, MailItem.REPLY_AND_FORWARD))
                            break;
                        if (MailItem.getStatOf(mMessage, MailItem.FORWARDED))
                            GlobalVariables.mainController.updateMessageAnsweredStatus(uid, MailItem.REPLY_AND_FORWARD);
                        else
                            GlobalVariables.mainController.updateMessageAnsweredStatus(uid, MailItem.REPLIED);
                        break;
                    case "forward":
                        if (MailItem.getStatOf(mMessage, MailItem.REPLY_AND_FORWARD))
                            break;
                        if (MailItem.getStatOf(mMessage, MailItem.REPLIED))
                            GlobalVariables.mainController.updateMessageAnsweredStatus(uid, MailItem.REPLY_AND_FORWARD);
                        else
                            GlobalVariables.mainController.updateMessageAnsweredStatus(uid, MailItem.FORWARDED);
                        break;
                    case "draft":
                        GlobalVariables.mainController.removeMsg(uid);
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("-----------update exception as follow = " + getStackTrace(e));
        }

//        new Thread( new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            getOwnerStage().close();
//                        }
//                    });
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    /**
     * close after check the draft status
     */
    public void close() {
        if (saveAlert != null) {
            try {
                Stage stage = (Stage) saveAlert.getDialogPane().getScene().getWindow();
                stage.show();
                stage.toFront();
                return;
            } catch (Exception e) {
            }
        }
        saveDrafts();
        closeComplete();
    }

    /**
     * get the closeable contents
     * @return
     */
    public boolean isCloseableWithoutSave() {
        if (isSent) {
            return true;
        }
        if (subjectTextField.getText().length() == 0 && ComposeArea.getHtmlText().contains("<body contenteditable=\"true\"></body>")) {
            return true;
        }
        return false;
    }

    /**
     * close this controller without save
     */
    public void closeComplete() {
        getOwnerStage().close();
    }

    /**
     * save draft
     */
    public void saveDraftsComplete() {
        SecurityLevel secLevel = (SecurityLevel) group.getSelectedToggle().getUserData();

        String content = ComposeArea.getHtmlText();

        try {

            EmailAccountBean emailAccountBean = getModelAccess().getEmailAccountByName(GlobalVariables.account.getAddress());
            Session session = emailAccountBean.getSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(emailAccountBean.getEmailAdress());
            String to = getEscapeFromEmail(toAutoTextField.getText());
            message.addRecipients(Message.RecipientType.TO, to);
            to = getEscapeFromEmail(ccAutoTextField.getText());
            message.addRecipients(Message.RecipientType.CC, to);
            to = getEscapeFromEmail(bccAutoTextField.getText());
            message.addRecipients(Message.RecipientType.BCC, to);
            message.setSubject(subjectTextField.getText());

            //security level
            if (secLevel != null)
                message.setHeader(MAIL_SEC_LEVEL_HEADER, "" + secLevel.level);

            //important
            if (importantCheckBox.isSelected())
                message.setHeader(MAIL_IMPORTANT_HEADER, "true");

            //return note
            if (retNoteCheckBox.isSelected())
                message.setHeader(MAIL_DIPOSITION_TO_HEADER, getModelAccess().getEmailAccountByName(GlobalVariables.account.getAddress()).getEmailAdress());

            // Setting the content:
            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(content, "text/html; charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);

            setAttachmentFromUI();

            message.setContent(multipart);
            message.setSentDate(new Date());
            message.setHeader(MAIL_CUSTOM_UUID_HEADER, "" + message.getSentDate().getTime());

            if (mType.toLowerCase().equalsIgnoreCase("draft"))
                message.setHeader(MAIL_CUSTOM_UUID_HEADER, MailItem.getUidFromMsg(mMessage));
            else
                message.setHeader(MAIL_CUSTOM_UUID_HEADER, "" + message.getSentDate().getTime());

            message.saveChanges();
            GlobalVariables.mainController.insertMailItem("DRAFT", message, attachmentsWithSec);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * save the draft in the app database when it is finished
     */
    public void saveDrafts() {

        if (isCloseableWithoutSave()) {
            return;
        }

        getOwnerStage().show();
        getOwnerStage().toFront();

        saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
        Stage stage = (Stage) saveAlert.getDialogPane().getScene().getWindow();
        stage.initOwner(getOwnerStage());
        stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
        saveAlert.setHeaderText(getString("question_save_draft"));
        Optional<ButtonType> result = saveAlert.showAndWait();
        if (result.get() != ButtonType.OK) {
            removeComposeController(ComposeMailController.this);
            return;
        }

        saveDraftsComplete();
    }

    /**
     * @author pilot
     * set attachments item from UI
     */
    public void setAttachmentFromUI() {
        attachmentsWithSec.clear();
        for (int i = 0; i < attachVBox.getChildren().size(); i ++) {
            HBox item = (HBox) attachVBox.getChildren().get(i);
            File attach  = (File) item.getUserData();
            ComboBox attachClassLabel = (ComboBox) item.getChildren().get(1);
            attachmentsWithSec.add(new EmailSenderService.AttachFile(attach, (SecurityLevel) attachClassLabel.getSelectionModel().getSelectedItem()));
        }
    }
}