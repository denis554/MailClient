package com.denis.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AddressBookItem {
    private StringProperty userID;
    private StringProperty userDepartment;
    private StringProperty userPath;
    private StringProperty userName;
    private StringProperty mailAddress;
    private StringProperty userLevel;
    private StringProperty userSecurityLevel;
    private StringProperty userNote;
    private BooleanProperty selected;

    public AddressBookItem( String userID, String userDepartment, String userPath, String userName, String mailAddress, String userLevel, String userSecurityLevel, String userNote) {
        this.userID = new SimpleStringProperty(userID);
        this.userDepartment = new SimpleStringProperty(userDepartment);
        this.userPath = new SimpleStringProperty(userPath);
        this.userName = new SimpleStringProperty(userName);
        this.mailAddress = new SimpleStringProperty(mailAddress);
        this.userLevel = new SimpleStringProperty(userLevel);
        this.userSecurityLevel = new SimpleStringProperty(userSecurityLevel);
        this.userNote = new SimpleStringProperty(userNote);
        setSelect(false);
    }

    public AddressBookItem(AddressBookItem aSrc) {
        copyFrom(aSrc);
    }

    public StringProperty userIDProperty() { return userID; }
    public StringProperty userDepartmentProperty() { return userDepartment; }
    public StringProperty userPathProperty() { return userPath; }
    public StringProperty userNameProperty() { return userName; }
    public StringProperty mailAddressProperty() { return mailAddress; }
    public StringProperty userLevelProperty() { return userLevel; }
    public StringProperty userSecurityLevelProperty() { return userSecurityLevel; }
    public StringProperty userNoteProperty() { return userNote; }
    public BooleanProperty selectedProperty() { return selected; }

    public void setSelect(boolean select){
        this.selected = new SimpleBooleanProperty(select);
    }

    public boolean copyFrom(AddressBookItem aSrc) {
        boolean isUpdated = false;

        this.userID = aSrc.userID;

        if (this.userDepartment != null && this.userDepartment.getValue().compareToIgnoreCase(aSrc.userDepartment.getValue()) != 0)
            isUpdated = true;
        this.userDepartment = aSrc.userDepartment;

        if (this.userPath != null && this.userPath.getValue().compareToIgnoreCase(aSrc.userPath.getValue()) != 0)
            isUpdated = true;
        this.userPath = aSrc.userPath;

        if (this.userName != null && this.userName.getValue().compareToIgnoreCase(aSrc.userName.getValue()) != 0)
            isUpdated = true;
        this.userName = aSrc.userName;

        if (this.mailAddress != null && this.mailAddress.getValue().compareToIgnoreCase(aSrc.mailAddress.getValue()) != 0)
            isUpdated = true;
        this.mailAddress = aSrc.mailAddress;

        if (this.userLevel != null && this.userLevel.getValue().compareToIgnoreCase(aSrc.userLevel.getValue()) != 0)
            isUpdated = true;
        this.userLevel = aSrc.userLevel;

        if (this.userSecurityLevel != null && this.userSecurityLevel.getValue().compareToIgnoreCase(aSrc.userSecurityLevel.getValue()) != 0)
            isUpdated = true;
        this.userSecurityLevel = aSrc.userSecurityLevel;

        this.userNote = aSrc.userNote;

        return isUpdated;
    }

    @Override
    public String toString() {
        return "User ID : <" +
                userID.getValue() + ">, User Department : <" +
                userDepartment.getValue() + ">, User Path : " +
                userPath.getValue() + ", User Name : " +
                userName.getValue() + ", Mail Address : " +
                mailAddress.getValue() + ", User Level : " +
                userLevel.getValue() + ", User SecurityLevel Level : " +
                userSecurityLevel.getValue();
    }
}