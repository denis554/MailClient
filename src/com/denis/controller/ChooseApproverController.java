package com.denis.controller;

import com.denis.model.ApproverItem;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.view.ViewFactory;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import static com.denis.model.GlobalVariables.GlobalVariables.*;

public class ChooseApproverController extends AbstractController implements Initializable {

    @FXML
    private TextField seachTextField;
    @FXML
    private ListView optApproversListView;
    @FXML
    private ListView appApproversListView;
    @FXML
    private TextArea instructionTextArea;
    @FXML
    private Label instructionEnterdWordStatLabel;

    static final int MAX_INPUT_WORDS = 100;

    ObservableList<ApproverItem> mAryOptAppovers;
    ObservableList<ApproverItem> mAryAppAppovers = FXCollections.observableArrayList();

    ComposeMailController mComposeController;

    public ChooseApproverController(ModelAccess modelAccess, JSONArray aryApprover, ComposeMailController composeController) {
        super(modelAccess);
        mComposeController = composeController;
        mAryOptAppovers = getParseApprovers(aryApprover);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        instructionEnterdWordStatLabel.setText(String.format(getString("app_instructions_word"), 0, MAX_INPUT_WORDS));
        instructionTextArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int iLen = newValue.length();
                instructionEnterdWordStatLabel.setText(String.format(getString("app_instructions_word"), iLen, MAX_INPUT_WORDS - iLen));
                if (instructionTextArea.getText().length() > MAX_INPUT_WORDS) {
                    String s = instructionTextArea.getText().substring(0, MAX_INPUT_WORDS);
                    instructionTextArea.setText(s);
                }
            }
        });

        FilteredList<ApproverItem> filteredData = new FilteredList<>(mAryOptAppovers, p -> true);

        seachTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String filter = seachTextField.getText();
                if(filter == null || filter.length() == 0) {
                    filteredData.setPredicate(s -> true);
                } else {
                    filteredData.setPredicate(s -> s.anameProperty().getValue().contains(filter));
                }
            }
        });

        SortedList<ApproverItem> sortedData = new SortedList<>(filteredData);
        sortedData.setComparator(new Comparator<ApproverItem>() {
            @Override
            public int compare(ApproverItem o1, ApproverItem o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        optApproversListView.setItems(sortedData);
        optApproversListView.setCellFactory(new Callback<ListView<Object>, ListCell<Object>>() {
            @Override
            public ListCell<Object> call(ListView<Object> param) {
                // TODO Auto-generated method stub
                return new OptApproverCell();
            }
        });

        appApproversListView.setItems(mAryAppAppovers);
        appApproversListView.setCellFactory(new Callback<ListView<Object>, ListCell<Object>>() {
            @Override
            public ListCell<Object> call(ListView<Object> param) {
                // TODO Auto-generated method stub
                return new AppApproverCell();
            }
        });
    }

    private class OptApproverCell extends ListCell<Object> {
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {

                HBox box = new HBox();
                box.setSpacing(10);
                HBox.setMargin(box, new Insets(0, 10, 0, 10));

                Pane pan = new Pane();
                HBox.setHgrow(pan, Priority.ALWAYS);

                VBox vbox = new VBox();
                vbox.getChildren().add(new Label(item.toString() + ""));

                Node imageview = ViewFactory.defaultFactory.resolveIconWithName("images/add.png");
                imageview.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //add user
                        System.out.println("clicked on an add approver");
                        ApproverItem appItem = null;
                        if (mAryAppAppovers.size() > 0)
                            appItem = mAryAppAppovers.get(0);
                        mAryAppAppovers.clear();
                        mAryAppAppovers.add((ApproverItem) item);
                        for (ApproverItem eachItem: mAryOptAppovers) {
                            if (eachItem.auidProperty().getValue() == ((ApproverItem)item).auidProperty().getValue()) {
                                mAryOptAppovers.remove(item);
                                break;
                            }
                        }
                        optApproversListView.getSelectionModel().clearSelection();
                        if (appItem != null) {
                            mAryOptAppovers.add(appItem);
                        }
                    }
                });

                box.getChildren().addAll(vbox, pan, imageview);
                setGraphic(box);
            } else {
                setGraphic(null);
            }
        }
    }

    private class AppApproverCell extends ListCell<Object> {
        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {

                HBox box = new HBox();
                box.setSpacing(10);
                HBox.setMargin(box, new Insets(0, 10, 0, 10));

                Pane pan = new Pane();
                HBox.setHgrow(pan, Priority.ALWAYS);

                VBox vbox = new VBox();
                vbox.getChildren().add(new Label(item.toString() + ""));

                Node imageview = ViewFactory.defaultFactory.resolveIconWithName("images/close.png");
                imageview.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //add user
                        System.out.println("clicked on a close approver");
                        mAryOptAppovers.add((ApproverItem) item);
                        mAryAppAppovers.clear();
                        optApproversListView.getSelectionModel().clearSelection();
                    }
                });

                box.getChildren().addAll(vbox, pan, imageview);
                setGraphic(box);
            } else {
                setGraphic(null);
            }
        }
    }

    public boolean isValidation() {
        return mAryAppAppovers.size() > 0;
    }

    @Override
    public Stage getOwnerStage() {
        return (Stage) optApproversListView.getScene().getWindow();
    }

    @FXML
    private void handleClickedOnOkButton() {
        System.out.println("handleClickedOnOkButton");
        if (!isValidation()) {
            showAlertAndWait(getString("Warning") + "!", getString("plz_sel_approver"), getOwnerStage(), Alert.AlertType.WARNING);
            return;
        }

        MimeMessage msg = (MimeMessage) mComposeController.getApproveMessage();
        try {

            ApproverItem appItem = mAryAppAppovers.get(0);
            JSONObject jObj = mComposeController.getCheckSendJSON();

            //To
            String to = "";
            Address[] list = msg.getRecipients(Message.RecipientType.TO);
            if (list != null && list.length > 0) {
                for (int i = 0; i < list.length; i++) {
                    if (i != 0) {
                        to += "," + list[i].toString();
                    } else {
                        to = list[i].toString();
                    }
                }
                System.out.println("TO = " + to);
            }
            msg.setRecipients(Message.RecipientType.TO, GlobalVariables.account.getAddress());

            //X-KM-ORIG-TO
            System.out.println("X-KM-ORIG-TO = " + to);
            msg.setHeader(MAIL_APPROVE_HEADER_ORG_TO, to);

            //X-KM-APPROVE-FLAG
            msg.setHeader(MAIL_APPROVE_HEADER_FLAG, "true");

            //X-KM-APPROVE-INFO
            JSONObject jInfo = new JSONObject();
            //a value
            jInfo.put("a", jObj.get("approveRuleIds"));

            //l value
            jInfo.put("l", jObj.get("approveLevel"));

            //r value
            JSONArray jRValue = new JSONArray();
            jRValue.add(appItem.auidProperty().getValue());
            jInfo.put("r", jRValue);

            System.out.println("X-KM-APPROVE-INFO = " + jInfo.toString());
            msg.setHeader(MAIL_APPROVE_HEADER_INFO, jInfo.toString());

            //X-KM-APPLICATION-DESC
            String base64 = Base64.encode(instructionTextArea.getText().getBytes("utf-8"));
            System.out.println("X-KM-APPLICATION-DESC = " + base64);
            msg.setHeader(MAIL_APPROVE_HEADER_DESC, base64);

            //X-KM-APPROVE-EXCLUDE
            JSONArray jExclude = new JSONArray();
            JSONArray jAryAddr = (JSONArray) jObj.get("errorDatas");
            int iLen = jAryAddr.size();
            boolean isExist = false;
            for (int i = 0; i < iLen; i++) {
                JSONObject jOneObj = (JSONObject) jAryAddr.get(i);
                String cmp1 = jOneObj.get("address").toString();
                for (Address eachAddr : list) {
                    if (eachAddr.toString().equalsIgnoreCase(cmp1)) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist)
                    jExclude.add(cmp1);
            }

            System.out.println("X-KM-APPROVE-EXCLUDE = " + jExclude.toString());
            msg.setHeader(MAIL_APPROVE_HEADER_EXC, jExclude.toString());
//            mComposeController.setApproveMessage(msg);
            mComposeController.sendMailTo();
            handleClickedOnCancelButton();

        } catch (Exception e) {
            mComposeController.sentFailedNotify(e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

    @FXML
    private void handleClickedOnCancelButton() {
        System.out.println("handleClickedOnCancelButton");
        Stage stage = (Stage)optApproversListView.getScene().getWindow();
        stage.close();
    }
}
