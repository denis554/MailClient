package com.denis.controller;

import com.denis.controller.services.EmailSenderService;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.model.MailItem;
import com.denis.model.SecurityLevel;
import com.denis.view.ViewFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.mail.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.*;

import static com.denis.controller.persistence.ValidAccount.getFormattedEmailFrom;
import static com.denis.model.GlobalVariables.GlobalVariables.*;

public class EmailDetailController extends AbstractController implements Initializable {

    @FXML
    VBox rootVBox;

    @FXML
    Button replyInContentButton;

    @FXML
    Button replyAllInContentButton;

    @FXML
    Button forwardInContentButton;

    @FXML
    WebView mailContentWebView;

    @FXML
    Label fromLabel;

    @FXML
    Label subjectLabel;

    @FXML
    Label dateLabel;

    @FXML
    FlowPane toFollowPane;

    @FXML
    FlowPane ccFlowPane;

    @FXML
    FlowPane bccFlowPane;

    @FXML
    Label mailSecurityClassLabel;

    @FXML
    FlowPane attachFlowPane;

    @FXML
    Label statusLabel;

    @FXML
    Label favLabel;

    @FXML
    VBox approveStatVBox;

    @FXML
    HBox headerMenuHBox;

    @FXML
    HBox followContainerHBox;

    @FXML
    HBox followHBox;

    @FXML
    Label importantLabel;

    @FXML
    Label importantLabel1;

    MailItem mMailItem;

    Message message;

    public EmailDetailController(ModelAccess modelAccess, MailItem aItem) {
        super(modelAccess);
        mMailItem = aItem;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        message = mMailItem.referenceMsg;

        replyInContentButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/mail_reply.png"));
        replyAllInContentButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/reply_to_all.png"));
        forwardInContentButton.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/forward.png"));

        fromLabel.setText(String.format(getString("mail_from"), getUtf8String(mMailItem.fromProperty().getValue())));
        String subject = mMailItem.subjectProperty().getValue();
        if (subject.isEmpty())
            subject = getString("no_subject");
        subjectLabel.setText(getUtf8String(subject));
        dateLabel.setText(mMailItem.receivedDateProperty().getValue());

        try {

            if (message.getFolder() != null && !message.getFolder().isOpen()) {
                message.getFolder().open(Folder.READ_ONLY);
            }

            //mail security level
            String secLevel[] = message.getHeader(MAIL_SEC_LEVEL_HEADER);

            //check approve mail
            if (mMailItem.isApproveMail()) {

                //approve status header info
                updateApproveStat(mMailItem);

                //to
                String[] appOrgInfo = message.getHeader(MAIL_APPROVE_HEADER_ORG_TO);
                String[] toList = appOrgInfo[0].split(",");

                for (int i = 0; i < toList.length; i++) {
                    Label to1 = new Label(getFormattedEmailString(getUtf8String(toList[i])));
                    to1.setFont(new Font(15));
                    if (i > 0) {
                        to1.setText("; " + getFormattedEmailString(getUtf8String(toList[i])));
                    }
                    toFollowPane.getChildren().add(to1);
                }

            } else {
                Address[] addresslist = message.getRecipients(Message.RecipientType.TO);
                if (addresslist != null && addresslist.length > 0) {
                    for (int i = 0; i < addresslist.length; i++) {
                        Label to1 = new Label(getFormattedEmailString(getUtf8String(addresslist[i].toString())));
                        to1.setFont(new Font(15));
                        if (i  > 0) {
                            to1.setText("; " + getFormattedEmailString(getUtf8String(addresslist[i].toString())));
                        }
                        toFollowPane.getChildren().add(to1);
                    }
                }
            }

            updateImportantStat(mMailItem.isImportantMail());

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

            SecurityLevel mailSecLevel = null;
            if (secLevel != null && secLevel.length > 0) {
                mailSecLevel = getSecLevel(secLevel[0]);
                System.out.println("security level = " + mailSecLevel);
                mailSecurityClassLabel.setText("[" + mailSecLevel.toString() + "]");
                mailSecurityClassLabel.setStyle("-fx-text-fill: " + mailSecLevel.levelColor);
            } else {
                mailSecLevel = GlobalVariables.securityList.get(0);
                mailSecurityClassLabel.setText("[" + mailSecLevel.toString() + "]");
                mailSecurityClassLabel.setStyle("-fx-text-fill: " + mailSecLevel.levelColor);
            }

            //important flag
            String important[] = message.getHeader(MAIL_IMPORTANT_HEADER);
            if (important != null && important.length > 0) {
                System.out.println("important = " + important[0]);
            }

//            String content = GlobalVariables.mainController.getBodyText(MailItem.getUidFromMsg(message));
            String content = mMailItem.content;

            //mail return note
            procReturnNoteMail(mMailItem);

            WebEngine engine = mailContentWebView.getEngine();
            if(content.contains("<body contenteditable=\"true\"")){
                content = content.replaceAll("<body contenteditable=\"true\"", "<body contenteditable=\"false\"");
            }

//            final LongProperty startTime   = new SimpleLongProperty();
//            final LongProperty endTime     = new SimpleLongProperty();
//            final LongProperty elapsedTime = new SimpleLongProperty();
//
//            statusLabel.textProperty().bind(
//                    Bindings.when(elapsedTime.greaterThan(0))
//                            .then(Bindings.concat(getString("loaded_page_in"), elapsedTime.divide(1_000_000), "ms")
//                            ).otherwise(getString("loading"))
//            );
//
//            elapsedTime.bind(Bindings.subtract(endTime, startTime));
//
//            engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
//                @Override
//                public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State state) {
//                    switch (state) {
//                        case RUNNING:
//                            startTime.set(System.nanoTime());
//                            break;
//
//                        case SUCCEEDED:
//                            endTime.set(System.nanoTime());
//                            break;
//                    }
//                }
//            });

            engine.loadContent(content);

            Platform.runLater(()->{
                if (mMailItem.attachProperty().getValue()) {
                    //attach file
                    clearAttachFiles(MailItem.getUidFromMsg(message));
                    Task<Void> task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            updateMessage(getString("loading_attach"));
                            Multipart multipart = (Multipart) GlobalVariables.mainController.getMailContents(MailItem.getUidFromMsg(message)).getContent();
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
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        favLabel.setText("");
        updateStarStat(mMailItem.starProperty().getValue());
    }

    @Override
    public Stage getOwnerStage() {
        return (Stage) toFollowPane.getScene().getWindow();
    }

    /**
     * @author pilot
     * update the approve status
     */
    public void updateApproveStat(MailItem aItem) {

        approveStatVBox.getChildren().clear();
        Message message = aItem.referenceMsg;
        try {
            String secLevel[] = message.getHeader(MAIL_SEC_LEVEL_HEADER);
            String[] appStatInfo = message.getHeader(MailItem.getMuidFromMsg(message) + "-" + MAIL_APPROVE_HEADER_INFO);
            JSONParser jsonParser = new JSONParser();
            if (appStatInfo != null && appStatInfo.length > 0) {
                String res = new String(Base64.getDecoder().decode(appStatInfo[0]), "utf-8");
                JSONObject jAppStat = (JSONObject) jsonParser.parse(res);

                //approve status box
                int appStat = Integer.parseInt(jAppStat.get("status").toString());
                String appStatString = getStringOfApproveStat(appStat);
                HBox approverStatHBox = getApproveInfoHBox(getString("approve_status"), appStatString);
                approveStatVBox.getChildren().add(approverStatHBox);

                //approve date
                HBox approveDateHBox = getApproveInfoHBox(getString("approve_date"), formattedDateString(jAppStat.get("rDate").toString()));
                approveStatVBox.getChildren().add(approveDateHBox);

                HBox approversHBox = getApproveInfoHBox(getString("approver"), getUtf8String(jAppStat.get("approvers").toString()));
                approveStatVBox.getChildren().add(approversHBox);

                String desc = new String(Base64.getDecoder().decode(message.getHeader(MAIL_APPROVE_HEADER_DESC)[0]), "utf-8");
                HBox approveInstructionHBox = getApproveInfoHBox(getString("approve_info"), getUtf8String(desc.trim()));
                approveStatVBox.getChildren().add(approveInstructionHBox);

                HBox applicationInstructionHBox = getApproveInfoHBox(getString("approve_application_info"), getUtf8String(jAppStat.get("rNote").toString()));
                approveStatVBox.getChildren().add(applicationInstructionHBox);
            } else {

                Platform.runLater(()->{
                    //approve status box
                    String appStatString = getStringOfApproveStat(10); //pending
                    HBox approverStatHBox = getApproveInfoHBox(getString("approve_status"), appStatString);
                    approveStatVBox.getChildren().add(approverStatHBox);

                    //approve date
                    HBox approveDateHBox = getApproveInfoHBox(getString("approve_date"), mMailItem.sentDateProperty().getValue());
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
                });

//                appStatInfo = message.getHeader(MAIL_APPROVE_HEADER_INFO);
//                JSONObject jAppStat = (JSONObject) jsonParser.parse(appStatInfo[0]);
//                JSONArray jRValue = (JSONArray) jAppStat.get("r");
//                String to = message.getHeader(MAIL_APPROVE_HEADER_ORG_TO)[0];
//                to = to.replaceAll(",", ";");
//                JSONObject jsonObj = getResultJSON(getCheckSendResponse(to, getSecLevel(secLevel[0]).level));
//                JSONArray aryAddress = (JSONArray) jsonObj.get("approvers");
//                ObservableList<ApproverItem> aryAppovers = getParseApprovers(aryAddress);
//
//                String approvers = "";
//                int iLen = jRValue.size();
//                for (ApproverItem item: aryAppovers) {
//                    for (int i = 0 ; i < iLen; i++) {
//                        if (item.auidProperty().getValue() == Integer.parseInt(jRValue.get(i).toString())) {
//                            if (i == 0)
//                                approvers = getUtf8String(item.anameProperty().getValue());
//                            else
//                                approvers += "," + getUtf8String(item.anameProperty().getValue());
//                        }
//                    }
//                }
//
//                HBox approversHBox = getApproveInfoHBox(getString("approver"), "[" + approvers + "]");
//                approveStatVBox.getChildren().add(approversHBox);
//
//                String desc = new String(Base64.getDecoder().decode(message.getHeader(MAIL_APPROVE_HEADER_DESC)[0]), "utf-8");
//                HBox approveInstructionHBox = getApproveInfoHBox(getString("approve_info"), getUtf8String(desc.trim()));
//                approveStatVBox.getChildren().add(approveInstructionHBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * update important
     */
    public void updateImportantStat(boolean isImportant) {
        mMailItem.setImportant(isImportant);
        if (isImportant) {
            importantLabel1.setText(getString("ImportantMail"));
            importantLabel.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/follow_up_small.png"));
        } else {
            importantLabel1.setText("");
            importantLabel.setGraphic(null);
        }
    }

    /**
     * update flag
     */
    public void updateStarStat(boolean isFlag) {
        mMailItem.setStar(isFlag);
        setStar(mMailItem.starProperty().getValue());
    }

    @FXML
    public void handleClickedOnReplyMenuButton() {

        Stage stage = (Stage)rootVBox.getScene().getWindow();
        stage.close();

        Scene scene = ViewFactory.defaultFactory.getDraftMailScene(message, "Reply");
        stage = new CustomStage();
        stage.setScene(scene);
        stage.setTitle(getString("ReplyMail"));
        ComposeMailController composeMailController = ((FXMLLoader) scene.getUserData()).getController();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                composeMailController.saveDrafts();
            }
        });
        stage.show();
    }

    @FXML
    public void handleClickedOnReplyAllMenuButton() {

        Stage stage = (Stage)rootVBox.getScene().getWindow();
        stage.close();

        Scene scene = ViewFactory.defaultFactory.getDraftMailScene(message, "Reply To All");
        stage = new CustomStage();
        stage.setScene(scene);
        stage.setTitle(getString("ReplyMail"));
        ComposeMailController composeMailController = ((FXMLLoader) scene.getUserData()).getController();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                composeMailController.saveDrafts();
            }
        });
        stage.show();
    }

    @FXML
    public void handleClickedOnForwardMenuButton() {

        Stage stage = (Stage)rootVBox.getScene().getWindow();
        stage.close();

        Scene scene = ViewFactory.defaultFactory.getDraftMailScene(message, "Forward");
        stage = new CustomStage();
        stage.setScene(scene);
        stage.setTitle(getString("ForwardMail"));
        ComposeMailController composeMailController = ((FXMLLoader) scene.getUserData()).getController();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                composeMailController.saveDrafts();
            }
        });
        stage.show();
    }

    @FXML
    public void handleOnFavoriteButton() {
        GlobalVariables.mainController.setStarInUI(mMailItem);
        setStar(mMailItem.starProperty().getValue());
    }

    /**
     * update follow status
     * @param isFollow
     */
    public void setStar(boolean isFollow) {
        if (isFollow) {
            favLabel.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/fav_yes.png"));
        } else {
            favLabel.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/fav_no.png"));
        }
    }
}
