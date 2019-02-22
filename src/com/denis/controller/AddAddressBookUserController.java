package com.denis.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

import com.denis.model.AddressBookItem;
import com.denis.model.AddressBox;
import com.denis.model.GlobalVariables.GlobalVariables;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AddAddressBookUserController extends AbstractController implements Initializable{

	@FXML
	private TextField uidField;
	@FXML
	private TextField noteField;
	@FXML
	private TextField nameField;
    @FXML
    private TextField addressField;
	@FXML
	private ComboBox userLevelBox;
	@FXML
  	private Button addUserBtn;
	@FXML
  	private Label addUserLabel;
	@FXML
  	private Label depLabel;
	@FXML
  	private HBox userIdHBox;
	@FXML
  	private HBox userLevelHBox;
	@FXML
  	private VBox rootVBox;

	String adbBoxType = "";
	boolean isEditMode = false;

	ArrayList<String> userLevelList = new ArrayList<>();
	ArrayList<String> securityLevelList = new ArrayList<>();

	AddressBookItem address = null;

	public AddAddressBookUserController(ModelAccess modelAccess) {
		super(modelAccess);
	}

	public AddAddressBookUserController(ModelAccess modelAccess, AddressBookItem addressBookItem, String adbBox, boolean isEdit) {
		super(modelAccess);
		address = addressBookItem;
		adbBoxType = adbBox;
		isEditMode = isEdit;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

//		for (int i = 0; i < GlobalVariables.securityList.size(); i++) {
//			SecurityLevel security = GlobalVariables.securityList.get(i);
//			securityLevelList.add(security.levelName);
//		}
//
//		for (int i = 0; i < GlobalVariables.userLevelList.size(); i++) {
//			SecurityLevel security = GlobalVariables.userLevelList.get(i);
//			userLevelList.add(security.levelName);
//		}
//
//		userLevelBox.getItems().addAll(userLevelList);
//		userLevelBox.getSelectionModel().select(0);

		rootVBox.getChildren().remove(userIdHBox);
		if (!isNeededDepartment()) {
			rootVBox.getChildren().remove(userLevelHBox);
		}

		addUserLabel.setText(getString("AddUser"));

		if (isEditMode) {

			addUserLabel.setText(getString("EditUser"));
//			uidField.setText(address.userIDProperty().getValue());
//			uidField.setDisable(true);
			nameField.setText(address.userNameProperty().getValue());
			addressField.setText(address.mailAddressProperty().getValue());
			noteField.setText(address.userNoteProperty().getValue());
//			String userlevel = address.userLevelProperty().getValue();
//
//			for (int i = 0; i < userLevelList.size(); i++) {
//				if (userLevelList.get(i).equals(userlevel)) {
//					userLevelBox.getSelectionModel().select(i);
//					break;
//				}
//			}

			addUserBtn.setText(getString("edit"));
		}
	}

	@FXML
    void addBtnAction() {

    	//TODO add validation

//		if (uidField.getText().isEmpty()) {
//			showAlert(getString("input_email"));
//			return;
//		} else if(address == null && !GlobalVariables.mainController.checkNewuser(uidField.getText())) {
//			showAlert(getString("duplicated_user_id"));
//			return;
//		}

		if (nameField.getText().trim().isEmpty()) {
			showAlert(getString("input_user_name"));
			return;
		}

		if (addressField.getText().trim().isEmpty()) {
    		showAlert(getString("input_user_email"));
    		return;
    	}
    	
    	if (!isValidateEmail(addressField.getText().trim())) {
			showAlert(getString("invalid_email"));
    		return;
    	}

    	//duplicate mail address
		AddressBox adbBox = GlobalVariables.mainController.getAdbBoxOf(addressField.getText());
    	if (adbBox != null) {
    		showAlertAndWait(getString("Warning"), String.format(getString("duplicated_email"), adbBox.getBoxName()), getOwnerStage(), Alert.AlertType.WARNING);
    		return;
		}

//    	String userLevel = userLevelBox.getSelectionModel().getSelectedItem().toString();
		AddressBookItem item = new AddressBookItem(
				"",
				"",
				"",
				nameField.getText(),
				addressField.getText(),
				"",
				"",
				noteField.getText());

    	if (address != null) {
			item.userIDProperty().setValue(address.userIDProperty().getValue());
			GlobalVariables.mainController.updateAddress(adbBoxType, item, address);
		} else {
			item.userIDProperty().setValue(new Random().nextInt() + "");
			GlobalVariables.mainController.addNewAddress(adbBoxType, item);
		}

     	Stage stage = (Stage)addressField.getScene().getWindow();
    	stage.close();
    }

    public void showAlert(String content) {
		showAlertAndWait(getString("Warning"), content, getOwnerStage(), Alert.AlertType.INFORMATION);
	}

	public boolean isNeededDepartment() {
		return adbBoxType.equalsIgnoreCase("public");
	}

    @Override
    public Stage getOwnerStage() {
        return (Stage) nameField.getScene().getWindow();
    }
}
