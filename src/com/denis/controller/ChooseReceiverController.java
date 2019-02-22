package com.denis.controller;

import com.denis.model.AddressBookItem;
import com.denis.model.AddressBox;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.view.ViewFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.net.URL;
import java.util.ResourceBundle;

import static com.denis.controller.ComposeMailController.RECEIVER_TYPE_BCC;
import static com.denis.controller.ComposeMailController.RECEIVER_TYPE_CC;
import static com.denis.controller.ComposeMailController.RECEIVER_TYPE_TO;

public class ChooseReceiverController extends AbstractController implements Initializable {

    final ObservableList<AddressBookItem> addressBookData = FXCollections.observableArrayList();
    
    @FXML
    TableView userTableView;

    @FXML
    TextField searchNameTextField;

    @FXML
    TextField toTextField;

    @FXML
    TextField ccTextField;

    @FXML
    TextField bccTextField;

    @FXML
    RadioButton nameOnlyRadioButton;

    @FXML
    RadioButton moreColumnsRadioButton;

    @FXML
    private ListView categoryListView;

    @FXML
    private Label categoryNameLabel1;

    @FXML
    private VBox rootVBox;

    @FXML
    private HBox toHBox;

    @FXML
    private HBox ccHBox;

    @FXML
    private HBox bccHBox;

    final ToggleGroup group = new ToggleGroup();
    private CheckBox selectAllCheckBox;

    ObservableList<AddressBox> mAryCategory = FXCollections.observableArrayList();
    MimeMessage mMessage;
    int mReceiverType;
    private int selectedItemCnt = 0;

    public ChooseReceiverController(ModelAccess modelAccess) {
        super(modelAccess);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        selectAllCheckBox = new CheckBox();
        nameOnlyRadioButton.setToggleGroup(group);
        moreColumnsRadioButton.setToggleGroup(group);

        nameOnlyRadioButton.setUserData("Name Only");
        moreColumnsRadioButton.setUserData("More");

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {
                    System.out.println(group.getSelectedToggle().getUserData().toString());
                    String key = searchNameTextField.getText();
                    searchNameTextField.setText("");
                    searchNameTextField.setText(key);
                }
            }
        });

        categoryNameLabel1.setGraphic(ViewFactory.defaultFactory.resolveIconWithName("/com/denis/view/images/contacts.png"));

        categoryListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null) {
                    addressBookData.clear();
                    AddressBox item = (AddressBox) categoryListView.getSelectionModel().getSelectedItem();
                    addressBookData.addAll(item.getAddrlist());
                    initAddressBookTable(item.getBoxName());
                }
            }
        });

        categoryListView.setCellFactory(new Callback<ListView<AddressBox>, ListCell<AddressBox>>() {

            @Override
            public ListCell<AddressBox> call(ListView<AddressBox> param) {
                // TODO Auto-generated method stub
                return new CategoryListCell();
            }
        });

        mAryCategory = GlobalVariables.mainController.getAddrBoxList();
        categoryListView.getItems().addAll(mAryCategory);
        categoryListView.getSelectionModel().select(0);

        userTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectAllCheckBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (selectAllCheckBox.isSelected()) {
                    userTableView.getSelectionModel().selectAll();
                } else {
                    userTableView.getSelectionModel().clearSelection();
                }
                handleSelectionItems();
            }
        });

        KeyCombination keyCtrlA = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_ANY);
        userTableView.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keyCtrlA.match(event)) {
                    handleSelectionItems();
                }
            }
        });

        userTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null) {
                }
            }
        });

        userTableView.setRowFactory(tv -> {
            TableRow row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Object rowData = row.getItem();
                    if (rowData instanceof AddressBookItem) {
                        setAdbItemToUI((AddressBookItem) rowData);
                        getOwnerStage().close();
                    }
                }
            });
            return row ;
        });

        userTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            handleSelectionItems();
        });

        rootVBox.getChildren().remove(toHBox);
        rootVBox.getChildren().remove(ccHBox);
        rootVBox.getChildren().remove(bccHBox);
    }

    @FXML
    public void handleClickedOnTableView(MouseEvent event) {
        if(event != null && event.isControlDown()) {
            handleSelectionItems();
        }
    }

    public void initAddressBookTable(String type) {

        for (AddressBookItem item: addressBookData) {
            item.setSelect(false);
        }

        userTableView.getColumns().clear();
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

        selectAll.setPrefWidth(26);
        userNameCol.setPrefWidth(100);
        userDepartmentCol.setPrefWidth(100);
        mailAddressCol.setPrefWidth(150);
        userLevelCol.setPrefWidth(100);
        userSecurityLevelCol.setPrefWidth(100);

        userDepartmentCol.setText(getString("User_Department"));
        userNameCol.setText(getString("User_Name"));
        mailAddressCol.setText(getString("Mail_Address"));
        userSecurityLevelCol.setText(getString("User_Security_Level"));
        userLevelCol.setText(getString("User_Level"));

        userDepartmentCol.setCellValueFactory(new PropertyValueFactory("userDepartment"));
        userNameCol.setCellValueFactory(new PropertyValueFactory("userName"));
        mailAddressCol.setCellValueFactory(new PropertyValueFactory("mailAddress"));
        userLevelCol.setCellValueFactory(new PropertyValueFactory("userLevel"));
        userSecurityLevelCol.setCellValueFactory(new PropertyValueFactory("userSecurityLevel"));

        switch (type.toLowerCase()) {
            case "public":
                break;
            default:
                userDepartmentCol.setVisible(false);
                userSecurityLevelCol.setVisible(false);
                userLevelCol.setText(getString("note"));
                userLevelCol.setPrefWidth(200);
                userLevelCol.setCellValueFactory(new PropertyValueFactory("userNote"));
                break;
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
                                        userTableView.getSelectionModel().select(index);
                                    else
                                        userTableView.getSelectionModel().clearSelection(index);

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

        FilteredList<AddressBookItem> filteredData = new FilteredList<>(addressBookData, p -> true);
        searchNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            System.out.println("textfield changed from " + oldValue + " to " + newValue);

            filteredData.setPredicate(addressBookItem -> {

                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                String condition = group.getSelectedToggle().getUserData().toString();
                if (condition.equalsIgnoreCase("Name Only")) {
                    if (addressBookItem.userIDProperty().toString().toLowerCase().contains(lowerCaseFilter))
                        return true; // Filter matches first name.
                } else {
                    if (addressBookItem.userDepartmentProperty().toString().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    }
                    if (addressBookItem.userPathProperty().toString().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    }
                    if (addressBookItem.userNameProperty().toString().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    }
                    if (addressBookItem.mailAddressProperty().toString().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    }
                    if (addressBookItem.userLevelProperty().toString().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    }
                    if (addressBookItem.userSecurityLevelProperty().toString().toLowerCase().contains(lowerCaseFilter)) {
                        return true; // Filter matches last name.
                    }
                }
                return false;

            });
        });

        // 3. Wrap the FilteredList in a SortedList.
        SortedList<AddressBookItem> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(userTableView.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        userTableView.setItems(sortedData);

        userTableView.getColumns().add(selectAll);
        userTableView.getColumns().add(userNameCol);
        userTableView.getColumns().add(userDepartmentCol);
        userTableView.getColumns().add(mailAddressCol);
        userTableView.getColumns().add(userSecurityLevelCol);
        userTableView.getColumns().add(userLevelCol);
    }

    /**
     * handles the selection of mail items
     */
    public void handleSelectionItems() {

        ObservableList selItems = userTableView.getSelectionModel().getSelectedItems();
        ObservableList allItems = userTableView.getItems();

        selectedItemCnt = 0;
        for (Object org: allItems) {
            if (org == null)
                continue;
            if (org instanceof AddressBookItem)
                ((AddressBookItem)org).setSelect(false);
        }

        for (Object org: selItems) {
            if (org == null)
                continue;
            selectedItemCnt++;
            if (org instanceof AddressBookItem)
                ((AddressBookItem)org).setSelect(true);
        }

        if (selectedItemCnt == userTableView.getItems().size()) {
            selectAllCheckBox.setIndeterminate(false);
            selectAllCheckBox.setSelected(true);
        } else {
            if (selectedItemCnt <=0) {
                selectAllCheckBox.setIndeterminate(false);
                selectAllCheckBox.setSelected(false);
            } else
                selectAllCheckBox.setIndeterminate(true);
        }

        userTableView.refresh();
    }

    //for test
    private class CategoryListCell extends ListCell<AddressBox> {
        @Override
        public void updateItem(AddressBox item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(ViewFactory.defaultFactory.resolveIconWithName("images/user.png"));
                setText(item.toString());
            }
        }
    }

    @FXML
    public void actionOnToButton() {

        if (!isValidateSelection())
            return;

        AddressBookItem person = (AddressBookItem) userTableView.getSelectionModel().getSelectedItem();
        setAdbItemToUI(person);
    }

    @FXML
    public void actionOnCcButton() {

        if (!isValidateSelection())
            return;

        AddressBookItem person = (AddressBookItem) userTableView.getSelectionModel().getSelectedItem();
        setAdbItemToUI(person);
    }

    @FXML
    public void actionOnBccButton() {

        if (!isValidateSelection())
            return;

        AddressBookItem person = (AddressBookItem) userTableView.getSelectionModel().getSelectedItem();
        setAdbItemToUI(person);
    }

    /**
     * set the AddressBookItem to the associated UI TextField
     * @param person
     */
    public void setAdbItemToUI(AddressBookItem person) {

        String val = person.mailAddressProperty().getValue();
        System.out.println(val);
        try {

//            String to;
            switch (mReceiverType) {
                case RECEIVER_TYPE_TO:
//                    to = toTextField.getText();
//                    System.out.println("all To raw receivers = " + to);
//                    to = getEscapeFromEmail(to);
//                    System.out.println("all To escaped receivers = " + to);
//                    mMessage.setRecipients(Message.RecipientType.TO, to);

                    if (!isDuplicated(mMessage, Message.RecipientType.TO, val)) {
                        mMessage.addRecipients(Message.RecipientType.TO, val);
//                        updateTextFields();
                    } else {
//                        showAlertAndWait(getString("Warning"),
//                                String.format(getString("dup_email_receivers"),
//                                        getFormattedEmailString(val), getString("ToStrim")),
//                                getOwnerStage(), Alert.AlertType.WARNING);
                    }
                    break;
                case RECEIVER_TYPE_CC:
//                    to = ccTextField.getText();
//                    System.out.println("all Cc raw receivers = " + to);
//                    to = getEscapeFromEmail(to);
//                    System.out.println("all Cc escaped receivers = " + to);
//                    mMessage.setRecipients(Message.RecipientType.CC, to);

                    if (!isDuplicated(mMessage, Message.RecipientType.CC, val)) {
                        mMessage.addRecipients(Message.RecipientType.CC, val);
//                        updateTextFields();
                    } else {
//                        showAlertAndWait(getString("Warning"),
//                                String.format(getString("dup_email_receivers"), getFormattedEmailString(val), getString("CcStrim")),
//                                getOwnerStage(), Alert.AlertType.WARNING);
                    }
                    break;
                case RECEIVER_TYPE_BCC:
//                    to = bccTextField.getText();
//                    System.out.println("all Bcc raw receivers = " + to);
//                    to = getEscapeFromEmail(to);
//                    System.out.println("all Bcc escaped receivers = " + to);
//                    mMessage.setRecipients(Message.RecipientType.BCC, to);

                    if (!isDuplicated(mMessage, Message.RecipientType.BCC, val)) {
                        mMessage.addRecipients(Message.RecipientType.BCC, val);
//                        updateTextFields();
                    } else {
//                        showAlertAndWait(getString("Warning"),
//                                String.format(getString("dup_email_receivers"), getFormattedEmailString(val), getString("BccStrim")),
//                                getOwnerStage(), Alert.AlertType.WARNING);
                    }
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Stage getOwnerStage() {
        return (Stage) rootVBox.getScene().getWindow();
    }

    /**
     * check the validate selection
     * @return
     */
    public boolean isValidateSelection() {
        AddressBookItem person = (AddressBookItem) userTableView.getSelectionModel().getSelectedItem();
        if (person == null) {
            showAlertAndWait(getString("Warning") + "!", getString("plz_sel_user"), getOwnerStage(), Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML
    public void actionOnOkButton() {

//        if (!isMailValidation())
//            return;

        ObservableList<AddressBookItem> adbList = userTableView.getSelectionModel().getSelectedItems();
        if (adbList.size() == 0)
            return;

        for (AddressBookItem item: adbList) {

            try {

//                String to;
                switch (mReceiverType) {
                    case RECEIVER_TYPE_TO:
//                    to = toTextField.getText();
//                    System.out.println("all Bcc raw receivers = " + to);
//                    to = getEscapeFromEmail(to);
//                    System.out.println("all Bcc escaped receivers = " + to);
                        if (!isDuplicated(mMessage, Message.RecipientType.TO, item.mailAddressProperty().getValue()))
                            mMessage.addRecipients(Message.RecipientType.TO, item.mailAddressProperty().getValue());
                        break;
                    case RECEIVER_TYPE_CC:
//                    to = ccTextField.getText();
//                    System.out.println("all Cc raw receivers = " + to);
//                    to = getEscapeFromEmail(to);
//                    System.out.println("all Cc escaped receivers = " + to);
                        if (!isDuplicated(mMessage, Message.RecipientType.CC, item.mailAddressProperty().getValue()))
                            mMessage.addRecipients(Message.RecipientType.CC, item.mailAddressProperty().getValue());
                        break;
                    case RECEIVER_TYPE_BCC:
//                    to = bccTextField.getText();
//                    System.out.println("all Bcc raw receivers = " + to);
//                    to = getEscapeFromEmail(to);
//                    System.out.println("all Bcc escaped receivers = " + to);
                        if (!isDuplicated(mMessage, Message.RecipientType.BCC, item.mailAddressProperty().getValue()))
                            mMessage.addRecipients(Message.RecipientType.BCC, item.mailAddressProperty().getValue());
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Stage stage = (Stage) userTableView.getScene().getWindow();
        stage.close();
    }

    /**
     * get the only email validation
     * @return
     */
//    public boolean isMailValidation() {
//        String val;
//
//        switch (mReceiverType) {
//            case RECEIVER_TYPE_TO:
//                val = toTextField.getText();
//                String invalid = isInvalidEmail(val);
//                if (invalid != null && invalid.length() > 0) {
//                    showAlertAndWait(getString("Critical") + "!",
//                            String.format(getString("invalid_email_receivers"), invalid, getString("ToStrim")),
//                            getOwnerStage(), Alert.AlertType.ERROR);
//                    return false;
//                }
//                break;
//            case RECEIVER_TYPE_CC:
//                val = ccTextField.getText();
//                invalid = isInvalidEmail(val);
//                if (invalid != null && invalid.length() > 0) {
//                    showAlertAndWait(getString("Critical") + "!",
//                            String.format(getString("invalid_email_receivers"), invalid, getString("CcStrim")),
//                            getOwnerStage(), Alert.AlertType.ERROR);
//                    return false;
//                }
//                break;
//            case RECEIVER_TYPE_BCC:
//                val = bccTextField.getText();
//                invalid = isInvalidEmail(val);
//                if (invalid != null && invalid.length() > 0) {
//                    showAlertAndWait(getString("Critical") + "!",
//                            String.format(getString("invalid_email_receivers"), invalid, getString("BccStrim")),
//                            getOwnerStage(), Alert.AlertType.ERROR);
//                    return false;
//                }
//                break;
//        }
//
//        return true;
//    }

    @FXML
    public void actionOnCancelButton() {
        toTextField.setText("");
        ccTextField.setText("");
        bccTextField.setText("");

        mMessage = null;

        Stage stage = (Stage) userTableView.getScene().getWindow();
        stage.close();
    }

    /**
     * set all receivers to the TextField controller
     * @param message
     * @param type
     */
    public void setAllReceivers(MimeMessage message, int type) {
        mMessage = message;
        mReceiverType = type;
//        updateTextFields();
    }

    /**
     * update the all receiver TextFields
     */
//    public void updateTextFields() {
//
//        String to;
//
//        switch (mReceiverType) {
//            case RECEIVER_TYPE_TO:
//                to = getAllToFormattedStringFrom(mMessage, Message.RecipientType.TO);
//                toTextField.setText(to.replaceAll(";", ","));
//                rootVBox.getChildren().remove(ccHBox);
//                rootVBox.getChildren().remove(bccHBox);
//                break;
//            case RECEIVER_TYPE_CC:
//                to = getAllToFormattedStringFrom(mMessage, Message.RecipientType.CC);
//                ccTextField.setText(to.replaceAll(";", ","));
//                rootVBox.getChildren().remove(toHBox);
//                rootVBox.getChildren().remove(bccHBox);
//                break;
//            case RECEIVER_TYPE_BCC:
//                to = getAllToFormattedStringFrom(mMessage, Message.RecipientType.BCC);
//                bccTextField.setText(to.replaceAll(";", ","));
//                rootVBox.getChildren().remove(toHBox);
//                rootVBox.getChildren().remove(ccHBox);
//                break;
//        }
//
//    }

    /**
     * get the all receivers
     * @return
     */
    public Message getAllReceivers() {
        return mMessage;
    }

}
