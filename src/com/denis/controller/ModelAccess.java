package com.denis.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Folder;

import com.denis.controller.persistence.ValidAccount;
import com.denis.controller.persistence.ValidAddressBook;
import com.denis.model.EmailAccountBean;
import com.denis.model.folder.EmailFolderBean;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;

public class ModelAccess {

	private Map<String, EmailAccountBean> emailAccounts = new HashMap<String, EmailAccountBean>();
	private ObservableList<String> emailAccountsNames = FXCollections.observableArrayList();
	private List<ValidAccount> validAccountList = new ArrayList<ValidAccount>();
	private ObservableList<ValidAddressBook> validAddressBookList = FXCollections.observableArrayList();
	private EmailFolderBean<String> root = new EmailFolderBean<String>("", new ImageView());
	
	// needed for updater service
	private List<Folder> folderList = new ArrayList<Folder>();

	//mail boxName
	private String mailType;

	public void clearModel() {
		emailAccounts.clear();
		emailAccountsNames.clear();
		validAccountList.clear();
		validAddressBookList.clear();
		root.getChildren().clear();
		folderList.clear();
	}

	public EmailAccountBean getEmailAccountByName(String name) {
		return emailAccounts.get(name);
	}

	public void addAccount(EmailAccountBean account) {
		emailAccounts.put(account.getEmailAdress(), account);
		emailAccountsNames.add(account.getEmailAdress());
		validAccountList.add(new ValidAccount(account.getEmailAdress(), account.getPassword(), mailType));
	}

	public List<Folder> getFolderList() {
		return folderList;
	}

	public void addFolder(Folder folder) {
		folderList.add(folder);
	}

	public EmailFolderBean<String> getRoot() {
		return root;
	}
	
	public List<ValidAccount> getValidAccountList(){
		return validAccountList;
	}
	
	public ObservableList<ValidAddressBook> getValidAddressBookList(){
		return validAddressBookList;
	}

	public String getMailType() {
		return mailType;
	}
	
	public void setMailType(String aType) {
		mailType = aType;
	}
	
}
