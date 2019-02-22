package com.denis.view;

import javax.mail.Message;

import com.denis.controller.*;
import com.denis.controller.persistence.PersistenceAcess;
import com.denis.model.*;

import com.denis.model.GlobalVariables.GlobalVariables;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.json.simple.JSONArray;

import java.util.Locale;
import java.util.ResourceBundle;

public class ViewFactory {

	public static ViewFactory defaultFactory = new ViewFactory();

	public static boolean isLogout = false;

	private final String DEFAULT_CSS = "style.css";
	private final String ADD_USER_EMAIL_FXML = "AddAddressBookUserLayout.fxml";
	private final String PROTOTYPE_FXML = "PrototypeLayout.fxml";
	private final String DRAFT_MAIL_FXML = "ComposeMailLayout.fxml";
	private final String CHOOSE_USER_FXML = "ChooseReceiverLayout.fxml";
	private final String EMAIL_DETAIL_FXML = "EmailDetailLayout.fxml";
	private final String SETTINGS_FXML = "SettingsLayout.fxml";
	private final String ADD_RULE_FXML = "AddRuleLayout.fxml";
	private final String CHOOSE_APPROVER_FXML = "ChooseApproverLayout.fxml";


	private ModelAccess modelAccess = new ModelAccess();
	private PersistenceAcess persistenceAcess = new PersistenceAcess(modelAccess);

	public Scene getChooseUserScene() {
		AbstractController chooseUserController = new ChooseReceiverController(modelAccess);
		return initializeScene(CHOOSE_USER_FXML, chooseUserController);
	}

	public Scene getDraftMailScene(Message message, String type) {
		AbstractController draftMailController = new ComposeMailController(modelAccess, message, type);
		return initializeScene(DRAFT_MAIL_FXML, draftMailController);
	}

	public Scene getComposeMailScene(ComposeMailController controller) {
		return initializeScene(DRAFT_MAIL_FXML, controller);
	}

	public Scene getPrototypeScene() {
		AbstractController prototypeController = new PrototypeController(modelAccess);
		return initializeScene(PROTOTYPE_FXML, prototypeController);
	}

	public Scene getEmailDetailScene(MailItem aItem) {
		AbstractController emailDetailController = new EmailDetailController(modelAccess, aItem);
		return initializeScene(EMAIL_DETAIL_FXML, emailDetailController);
	}

	public Scene getSettingsScene() {
		AbstractController settingsController = new SettingsController(modelAccess);
		return initializeScene(SETTINGS_FXML, settingsController);
	}

	public Scene getAddRuleScene() {
		AddRuleController addRuleController = new AddRuleController(modelAccess, null);
		return initializeScene(ADD_RULE_FXML, addRuleController);
	}

	public Scene getUpdateRuleScene(RuleItem item) {
		AddRuleController addRuleController = new AddRuleController(modelAccess, item);
		return initializeScene(ADD_RULE_FXML, addRuleController);
	}
	
	public Scene getAddUserScene(AddressBookItem addressBookItem, String adbBox, boolean isEdit){
		AbstractController addUserController = new AddAddressBookUserController(modelAccess, addressBookItem, adbBox, isEdit);
		return initializeScene(ADD_USER_EMAIL_FXML, addUserController);
	}

	public Scene getChooseApproverScene(JSONArray aryApprover, ComposeMailController composeController) {
		AbstractController controller = new ChooseApproverController(modelAccess, aryApprover, composeController);
		return initializeScene(CHOOSE_APPROVER_FXML, controller);
	}

	public Node resolveIcon(String treeItemValue) {
		String lowerCaseTreeItemValue = treeItemValue.toLowerCase();
		ImageView returnIcon;
		try {
			if (lowerCaseTreeItemValue.contains("inbox")) {
				returnIcon = new ImageView(new Image(getClass().getResourceAsStream("images/inbox.png")));
			} else if (lowerCaseTreeItemValue.contains("sent")) {
				returnIcon = new ImageView(new Image(getClass().getResourceAsStream("images/sent2.png")));
			} else if (lowerCaseTreeItemValue.contains("spam")) {
				returnIcon = new ImageView(new Image(getClass().getResourceAsStream("images/spam.png")));
			} else if (lowerCaseTreeItemValue.contains("@")) {
				returnIcon = new ImageView(new Image(getClass().getResourceAsStream("images/email.png")));
			} else if (lowerCaseTreeItemValue.isEmpty()) {
				return null;
			} else {
				returnIcon = new ImageView(new Image(getClass().getResourceAsStream("images/folder.png")));
			}
		} catch (NullPointerException e) {
			System.out.println("Invalid image location!!!");
			e.printStackTrace();
			returnIcon = new ImageView();
		}

		returnIcon.setFitHeight(16);
		returnIcon.setFitWidth(16);

		return returnIcon;
	}

	public Node resolveIconWithName(String name, int size) {
		try {
			ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(name)));
			iv.setFitWidth(size);
			iv.setFitHeight(size);
			return iv;
		} catch (Exception e) {
			e.printStackTrace();
			return new ImageView();
		}
	}

	public Node resolveIconWithName(String name) {
		return resolveIconWithName(name, 16);
	}

	public Image resolveIconImageWithName(String name) {
		try {
			return new Image(getClass().getResourceAsStream(name));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Node resolveCategoryIcon(int aIndex) {
		return resolveCategoryIcon(aIndex, 16);
	}

	public Node resolveCategoryIcon(int aIndex, int size) {
		try {
			String iconName = "";
			switch (aIndex) {
				case PrototypeController.CAT_MAIL:
					iconName = "images/mail.png";
					break;
				case PrototypeController.CAT_ADB:
					iconName = "images/contacts.png";
					break;
				case PrototypeController.CAT_TODO:
					iconName = "images/todomail.png";
					break;
			}
			ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(iconName)));
			iv.setFitWidth(size);
			iv.setFitHeight(size);
			return iv;
		} catch (Exception e) {
			e.printStackTrace();
			return new ImageView();
		}
	}

	public Node resolveUserSecurityColor(String name) {
		String color = GlobalVariables.userSecurityLevelList.get(name);

		try {
			Rectangle rect = new Rectangle(0, 0, 10, 15);
			if (color.length() > 0) {
				rect.setFill(Color.valueOf(color));
			} else {
				rect.setFill(Color.TRANSPARENT);
			}
			return rect;
		} catch (Exception e) {
			e.printStackTrace();
			return new ImageView();
		}
	}

	public Node resolveMailSecurityColor(String name) {
		String color = "";
		for (int i = 0; i < GlobalVariables.securityList.size(); i++) {
			SecurityLevel security = GlobalVariables.securityList.get(i);
			if (security.levelName.equalsIgnoreCase(name)) {
				color = security.levelColor;
				break;
			}
		}

		try {
			Rectangle rect = new Rectangle(0, 0, 10, 15);
			if (color.length() > 0) {
				rect.setFill(Color.valueOf(color));
			} else {
				rect.setFill(Color.TRANSPARENT);
			}
			return rect;
		} catch (Exception e) {
			e.printStackTrace();
			return new ImageView();
		}
	}

	public Node resolveMailImportantIcon(String name) {

		try {
			ImageView iv;
			if (name.equalsIgnoreCase("High")) {
				iv = new ImageView(new Image(getClass().getResourceAsStream("images/important_high.png")));
			} else {
				iv = new ImageView();
			}
			iv.setFitWidth(16);
			iv.setFitHeight(16);
			return iv;
		} catch (Exception e) {
			e.printStackTrace();
			return new ImageView();
		}
	}

	public Node resolveMailBoxListItemIcon(String type) {
		try {
			String iconName = "";
			switch (type.toUpperCase()) {
				case "DRAFT":
					iconName = "images/drafts.png";
					break;
				case "OUTBOX":
					iconName = "images/outbox.png";
					break;
				case "JUNK":
					iconName = "images/junk_mail.png";
					break;
				case "INBOX":
					iconName = "images/inbox.png";
					break;
				case "PENDING APPROVAL":
					iconName = "images/pending.png";
					break;
				case "MAIL APPROVAL":
					iconName = "images/approve.png";
					break;
				case "SENT":
					iconName = "images/sent.png";
					break;
				case "DELETE":
				case "TRASH":
					iconName = "images/trash.png";
					break;
				case "SEARCH":
					iconName = "images/search_folder.png";
					break;
				default:
					iconName = "images/folder.png";
					break;
			}
			ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(iconName)));
			iv.setFitWidth(16);
			iv.setFitHeight(16);
			return iv;
		} catch (Exception e) {
			e.printStackTrace();
			return new ImageView();
		}
	}

	public Image emailIcon(String value) {
		
		Image returnIcon;
		
		try {
			switch (value) {
			
			case "qq":
				returnIcon = new Image(getClass().getResourceAsStream("images/qq.jpeg"));
				break;
				
			case "163":
				returnIcon = new Image(getClass().getResourceAsStream("images/163.png"));
				break;
				
			case "126":
				returnIcon = new Image(getClass().getResourceAsStream("images/126.png"));
				break;
				
			case "sina":
				returnIcon = new Image(getClass().getResourceAsStream("images/sina.jpeg"));
				break;
				
			case "hotmail":
				returnIcon = new Image(getClass().getResourceAsStream("images/hotmail.jpeg"));
				break;
				
			case "gmail":
				returnIcon = new Image(getClass().getResourceAsStream("images/gmail.jpeg"));
				break;
				
			case "fox":
				returnIcon = new Image(getClass().getResourceAsStream("images/fox.jpeg"));
				break;
				
			case "139":
				returnIcon = new Image(getClass().getResourceAsStream("images/139.jpeg"));
				break;
				
			case "sohu":
				returnIcon = new Image(getClass().getResourceAsStream("images/sohu.jpeg"));
				break;
				
			case "outlook":
				returnIcon = new Image(getClass().getResourceAsStream("images/outlook.jpeg"));
				break;
				
			default:
				returnIcon = null;
				break;
				
			}
			
		} catch (NullPointerException e) {
			System.out.println("Invalid image location!!!");
			e.printStackTrace();
			returnIcon = null;
		}


		return returnIcon;
	}

	private Scene initializeScene(String fxmlPath, AbstractController controller) {
		FXMLLoader loader;
		Parent parent;
		Scene scene = null;
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("com/denis/resources/lang", Locale.getDefault());
			loader = new FXMLLoader(getClass().getResource(fxmlPath), bundle);
			loader.setController(controller);
			parent = loader.load();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		scene = new Scene(parent);
		scene.setUserData(loader);
		scene.getStylesheets().add(getClass().getResource(DEFAULT_CSS).toExternalForm());
		return scene;
	}

	public PersistenceAcess getPersistenceAcess() {
		return persistenceAcess;
	}
	
	public void didLogin() {
		isLogout = false;
	}
	
	public boolean wasLogout() {
		return isLogout;
	}
	
	public void logout() {
		isLogout = true;
	}

}
