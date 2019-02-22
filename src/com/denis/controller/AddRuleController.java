package com.denis.controller;

import com.denis.model.*;
import com.denis.model.GlobalVariables.GlobalVariables;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class AddRuleController extends AbstractController implements Initializable {

    @FXML
    private TextField RuleIdText;

    @FXML
    private ChoiceBox<RuleInclude> SendersIncludeChoice;

    @FXML
    private TextField SendersText;

    @FXML
    private ChoiceBox<RuleInclude> TitlesIncludeChoice;

    @FXML
    private TextField TitlesText;

    @FXML
    private ChoiceBox<RuleInclude> ReceiversIncludeChoice;

    @FXML
    private TextField ReceiversText;

    @FXML
    private ChoiceBox<MailBox> MoveToChoice;


    private RuleItem selectedItem = null;
    private Boolean isAdd = true;
    public AddRuleController(ModelAccess modelAccess, RuleItem item) {
        super(modelAccess);
        selectedItem = item;
    }

    public static class RuleInclude {
        public String rule;
        public RuleInclude(String rule) {
            this.rule = rule;
        }

        @Override
        public String toString() {
            try {
                return AbstractController.getString(rule);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rule;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<RuleInclude> checkNewMessageDurations = FXCollections.observableArrayList();
        checkNewMessageDurations.addAll(new RuleInclude("Include"), new RuleInclude("Exclude"));
        SendersIncludeChoice.setItems(checkNewMessageDurations);
        TitlesIncludeChoice.setItems(checkNewMessageDurations);
        ReceiversIncludeChoice.setItems(checkNewMessageDurations);
        SendersIncludeChoice.setValue(checkNewMessageDurations.get(0));
        TitlesIncludeChoice.setValue(checkNewMessageDurations.get(0));
        ReceiversIncludeChoice.setValue(checkNewMessageDurations.get(0));

//        ObservableList<String> AddressBoxs = FXCollections.observableArrayList();
        ObservableList<MailBox> addrBoxlist = GlobalVariables.mainController.mMailBoxList;
//        for (int i = 0; i < addrBoxlist.size(); i++ ) {
//            MailBox box = addrBoxlist.get(i);
//            AddressBoxs.add(box.toString());
//        }
        MoveToChoice.setItems(addrBoxlist);
        MoveToChoice.setValue(addrBoxlist.get(0));

        if (selectedItem != null) {
            SendersText.setText(selectedItem.senders.get());
            ReceiversText.setText(selectedItem.receivers.get());
            TitlesText.setText(selectedItem.titles.get());
            SendersIncludeChoice.setValue((RuleInclude) selectedItem.includeSenders.get());
            TitlesIncludeChoice.setValue((RuleInclude)selectedItem.includeTitles.get());
            ReceiversIncludeChoice.setValue((RuleInclude)selectedItem.includeReceivers.get());
            MoveToChoice.setValue((MailBox) selectedItem.moveTo.get());
            isAdd = false;
        }
    }

    @FXML
    void SaveBtnAction() {
        RuleInclude includeSenders = SendersIncludeChoice.getSelectionModel().getSelectedItem();
        String senders = SendersText.getText();
        RuleInclude includeTitles = TitlesIncludeChoice.getSelectionModel().getSelectedItem();
        String titles = TitlesText.getText();
        RuleInclude includeReceivers = ReceiversIncludeChoice.getSelectionModel().getSelectedItem();
        String receivers = ReceiversText.getText();
        MailBox moveTo = MoveToChoice.getSelectionModel().getSelectedItem();

        Integer id = 0;

        if (!isAdd){
            id = selectedItem.id.get();
        }

        RuleItem updated_item = new RuleItem(senders, includeSenders, receivers, includeReceivers, titles, includeTitles, moveTo, id);
        if (!updated_item.validate()) {
            showAlertAndWait(getString("Warning"), updated_item.errorMsg, getOwnerStage(), Alert.AlertType.WARNING);
            return;
        }

        boolean success = false;
        if (!isAdd) {
            success = GlobalVariables.mainController.updateRule(updated_item);
            if (!success) {
                showAlertAndWait(getString("Warning"), getString("cant_update"), getOwnerStage(), Alert.AlertType.WARNING);
            }
        } else {

            success = GlobalVariables.mainController.addRule(updated_item);
            if (!success) {
                showAlertAndWait(getString("Warning"), getString("cant_update"), getOwnerStage(), Alert.AlertType.WARNING);
            }
        }
        if (success) {
            Stage stage = (Stage)SendersText.getScene().getWindow();
            stage.close();
        }
    }

    @Override
    public Stage getOwnerStage() {
        return (Stage) SendersText.getScene().getWindow();
    }
    
    @FXML
    void CancelBtnAction() {
        Stage stage = (Stage) SendersText.getScene().getWindow();
        stage.close();
    }

}