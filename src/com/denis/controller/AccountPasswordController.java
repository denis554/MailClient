package com.denis.controller;

import com.denis.model.GlobalVariables.GlobalVariables;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountPasswordController  extends AbstractController implements Initializable {

    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private TextField apAccountTextfield;

    @FXML
    private TextField bindPasswordTextField;

    @FXML
    private PasswordField apPasswordTextfield;

    @FXML
    private Label bindNotifyLabel;

    private String account;
    private String password;

    public AccountPasswordController(ModelAccess modelAccess) {
        super(modelAccess);
    }

    public AccountPasswordController(ModelAccess modelAccess, String account, String password) {
        super(modelAccess);
        this.account = account;
        this.password = password;
    }

    @Override
    public Stage getOwnerStage() {
        return (Stage) rootAnchorPane.getScene().getWindow();
    }

    public boolean isValidation() {
        if (GlobalVariables.mainController.isNotBindedAccount()) {
            if (bindPasswordTextField.getText().isEmpty()) {
                showAlertAndWait(getString("Warning") + "!", getString("input_pwd"), null, Alert.AlertType.WARNING);
                return false;
            }
        } else {
            if (apPasswordTextfield.getText().isEmpty()) {
                showAlertAndWait(getString("Warning") + "!", getString("input_pwd"), null, Alert.AlertType.WARNING);
                return false;
            }
            if (apPasswordTextfield.getText().compareTo(this.password) != 0) {
                showAlertAndWait(getString("Warning") + "!", getString("no_matching_pwd"), null, Alert.AlertType.WARNING);
                return false;
            }
        }
        return true;
    }

    @FXML
    void handleClickedOnOk() {
        if (!isValidation()) {
            return;
        }
        if (GlobalVariables.mainController.isNotBindedAccount())
            GlobalVariables.mainController.setBindAccount(bindPasswordTextField.getText());
        GlobalVariables.mainController.startInitMailSystem();
        Stage stage = (Stage) apAccountTextfield.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleClickedOnCancel() {
        GlobalVariables.mainController.finishApp();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apAccountTextfield.setText(this.account);

        if (GlobalVariables.mainController.isNotBindedAccount()) {
            bindPasswordTextField.setText(this.password);
            rootAnchorPane.getChildren().remove(apPasswordTextfield);
        } else {
            rootAnchorPane.getChildren().remove(bindPasswordTextField);
            rootAnchorPane.getChildren().remove(bindNotifyLabel);
        }

        bindPasswordTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER)
                    handleClickedOnOk();
            }
        });

        apPasswordTextfield.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER)
                    handleClickedOnOk();
            }
        });
    }
}
