package com.denis.controller;

import com.denis.controller.services.EmailSenderService;
import com.denis.model.*;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.model.http.Apis;
import com.denis.model.http.XMailHttpRequest;
import com.denis.view.ViewFactory;
import com.sun.mail.util.LineInputStream;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denis.controller.persistence.ValidAccount.getFormattedEmailFrom;
import static com.denis.model.GlobalVariables.GlobalVariables.*;

public abstract class AbstractController {
	
	private ModelAccess modelAccess;
	private EmailValidator emailValidator;
	Alert waitingExitDlg;
	Alert waitingDlg;
	StringProperty waitingExitMsgProperty;
	StringProperty waitingMsgProperty;

	public AbstractController(ModelAccess modelAccess) {
		Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException());
		this.modelAccess = modelAccess;
		this.emailValidator = new EmailValidator();
	}

	public void addComposeController(ComposeMailController controller) {
		GlobalVariables.composeMailControllers.add(controller);
	}

	public void removeComposeController(ComposeMailController controller) {
		GlobalVariables.composeMailControllers.remove(controller);
	}

	/**
	 * get approve status HBox with pairs of key lebel and val label
	 * @param key
	 * @param val
	 * @return
	 */
	public HBox getApproveInfoHBox(String key, String val) {
		HBox appHBox = new HBox();
		javafx.scene.control.Label appKeyLabel = new javafx.scene.control.Label(key);
		appKeyLabel.setFont(new javafx.scene.text.Font(12));
		appKeyLabel.setStyle("-fx-font-weight: bold");

		javafx.scene.control.Label appValLabel = new Label(val);
		appValLabel.setFont(new Font(12));

		appHBox.getChildren().addAll(appKeyLabel, appValLabel);
		return appHBox;
	}

	public static SecurityLevel getSecLevel(Message message) {

		SecurityLevel sec = null;
		try {
			String secLevel[] = message.getHeader(MAIL_SEC_LEVEL_HEADER);
			if (secLevel != null && secLevel.length > 0) {
				return getSecLevel(secLevel[0]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sec;
	}

	public String getStringOfApproveStat(int stat) {
		String appStatString = getString("pending");
		switch (stat) {
			case 0: //no approve
				appStatString = getString("approval_no_approve");
				break;
			case 10: //pending
				appStatString = getString("pending");
				break;
			case 20: //approve
				appStatString = getString("approval_approved");
				break;
			case 30: //reject
				appStatString = getString("approval_rejected");
				break;
			case 40: //cancel123

				appStatString = getString("approval_canceled");
				break;
		}
		return appStatString;
	}

	public JSONObject getResultJSON(StringBuffer response) {
		try {
			if (response != null) {
				JSONParser parser = new JSONParser();
				JSONObject jsonObj = (JSONObject) parser.parse(response.toString());
				return jsonObj;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * get the approvers
	 * @param message
	 * @return
	 */
	public String getApprovers(Message message) {
		try {
			JSONParser jsonParser = new JSONParser();
			String secLevel[] = message.getHeader(MAIL_SEC_LEVEL_HEADER);
			String[] appStatInfo = message.getHeader(MAIL_APPROVE_HEADER_INFO);
			JSONObject jAppStat = (JSONObject) jsonParser.parse(appStatInfo[0]);
			JSONArray jRValue = (JSONArray) jAppStat.get("r");
			String to = message.getHeader(MAIL_APPROVE_HEADER_ORG_TO)[0];
			to = to.replaceAll(",", ";");
			JSONObject jsonObj = getResultJSON(getCheckSendResponse(to, getSecLevel(secLevel[0]).level));
			JSONArray aryAddress = (JSONArray) jsonObj.get("approvers");
			ObservableList<ApproverItem> aryAppovers = getParseApprovers(aryAddress);

			String approvers = "";
			int iLen = jRValue.size();
			for (ApproverItem item: aryAppovers) {
				for (int i = 0 ; i < iLen; i++) {
					if (item.auidProperty().getValue() == Integer.parseInt(jRValue.get(i).toString())) {
						if (i == 0)
							approvers = getUtf8String(item.anameProperty().getValue());
						else
							approvers += "," + getUtf8String(item.anameProperty().getValue());
					}
				}
			}
			return approvers;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public ObservableList<ApproverItem> getParseApprovers(JSONArray ary) {
		ObservableList<ApproverItem> items = FXCollections.observableArrayList();
		int iLen = ary.size();
		for (int i = 0; i < iLen; i++) {
			JSONObject jObj = (JSONObject) ary.get(i);
			ApproverItem item = new ApproverItem(
					jObj.get("aname").toString(),
					jObj.get("auc").toString(),
					Integer.parseInt(jObj.get("auid").toString()));
			items.add(item);
		}
		return items;
	}

	public static SecurityLevel getSecLevel(int aSecLevel) {

		int iCnt = GlobalVariables.securityList.size();
		SecurityLevel sec = null;
		for (int i = 0; i < iCnt; i++) {
			sec = GlobalVariables.securityList.get(i);
			if (sec.level == aSecLevel) {
				break;
			}
		}

		return sec;
	}

	public static SecurityLevel getSecLevel(String aSecLevel) {

		int iCnt = GlobalVariables.securityList.size();
		SecurityLevel sec = null;
		for (int i = 0; i < iCnt; i++) {
			sec = GlobalVariables.securityList.get(i);
			if (sec.level == Integer.parseInt(aSecLevel)) {
				break;
			}
		}

		return sec;
	}

	/**
	 * make the attach file css with a color
	 * @param color
	 * @return
	 */
	public String makeCssFile(String color) {
		PrintWriter fw = null;
		try {
			File f = File.createTempFile("combobox", ".css");
			String fileName = f.getAbsolutePath();
			fw = new PrintWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(".attach-file-button {");
			bw.write("\r\n");
			bw.write("  -fx-background-color: transparent;");
			bw.write("\r\n");
			bw.write("  -fx-border-color: #b8b8b8;");
			bw.write("\r\n");
			bw.write("  -fx-border-width: 1;");
			bw.write("\r\n");
			bw.write("}");
			bw.write("\r\n");

			bw.write(".attach-file-button:hover {");
			bw.write("\r\n");
			bw.write("  -fx-border-color: #ffa81f;");
			bw.write("\r\n");
			bw.write("  -fx-background-color: #ffea6f;");
			bw.write("\r\n");
			bw.write("  -fx-border-radius: 0, 0;");
			bw.write("\r\n");
			bw.write("}");
			bw.write("\r\n");

			bw.write(".attach-file-button:pressed {");
			bw.write("\r\n");
			bw.write("  -fx-background-color: #ffa81f;");
			bw.write("\r\n");
			bw.write("}");
			bw.write("\r\n");

			bw.write(".attach-file-button .list-cell {");
			bw.write("\r\n");
			bw.write("  -fx-text-fill: " + color + ";");
			bw.write("\r\n");
			bw.write("}");
			bw.write("\r\n");
			bw.flush();
			fw.close();
			return fileName;
		} catch (IOException e) {
			e.printStackTrace();
			fw.close();
		}
		return "";
	}

	/**
	 * open file with system default application
	 * @param attachFile
	 */
	public void openFileAsSystemDefaultApp(File attachFile) {
		try {
			Desktop.getDesktop().open(attachFile);
		} catch (Exception e) {
			showAlertAndWait(getString("Warning") + "!", getStackTrace(e), null, Alert.AlertType.WARNING);
			e.printStackTrace();
		}
	}

	public abstract Stage getOwnerStage();

	/**
	 * conver Message to MailItem
	 * @param message
	 * @return
	 */
	public static MailItem convertFrom(String type, Message message, String aUid) {
		String uid = "";
		String to = "";
		String from = "";
		String subject = "";
		String sentDate = "";
		String mailSize = "";
		boolean hasAttach = false;
		String content = "";
		String secLevel = "";
		boolean star = false;
		boolean unread = false;
		String msgContent = "";

		try {

			if (message.getFolder() != null && !message.getFolder().isOpen()) {
				message.getFolder().open(Folder.READ_ONLY);
			}

			from = getFormattedEmailString(getUtf8String(message.getFrom()[0].toString().trim()));
			to = getAllToFormattedStringFrom(message, Message.RecipientType.TO);

			subject = message.getSubject();
//			if (subject.isEmpty()) {
//				subject = getString("no_subject");
//			}
			uid = aUid;
			if (message.getSentDate() != null) {
				sentDate = getFormattedTimeString(message.getSentDate());
//                uid = "" + date.getTime();
			}

			star = message.isSet(Flags.Flag.FLAGGED);
			unread = !message.isSet(Flags.Flag.SEEN);

			//security level
			String secLevelHeader[] = message.getHeader(MAIL_SEC_LEVEL_HEADER);
			SecurityLevel mailSecLevel = null;
			if (secLevelHeader != null && secLevelHeader.length > 0) {
				mailSecLevel = getSecLevel(secLevelHeader[0]);
//				System.out.println("security level = " + mailSecLevel);
				secLevel = mailSecLevel.levelName;
			} else {
				secLevel = GlobalVariables.securityList.get(0).toString();
			}

			System.setProperty("mail.mime.multipart.allowempty", "true");
			System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
			content = GlobalVariables.mainController.getBodyText(uid);
//			System.out.println("content = " + content);

			int size = message.getSize();
			if (!type.equalsIgnoreCase("draft")) {
				hasAttach = GlobalVariables.mainController.hasAttachment(uid);
				size = GlobalVariables.mainController.getMailSize(uid);
			} else {
				hasAttach = GlobalVariables.mainController.hasAttachmentInDraft(uid);
				size += GlobalVariables.mainController.getAttachFilesSizeInDraft(uid);
			}

			mailSize = getFormattedSize(size);

			msgContent = content;

		} catch (Exception e) {
			e.printStackTrace();
		}

		MailItem mailItem = new MailItem(uid, type, secLevel, from, to, subject, sentDate, mailSize, hasAttach, star, msgContent, unread, message);
		return mailItem;
	}

	/**
	 * get the formatted time from Date object
	 * @param date
	 * @return
	 */
	public static String getFormattedTimeString(Date date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd(EEEE) hh:mm a");
		LocalDateTime locTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return locTime.format(formatter);
	}

	/**
	 * get the all To formatted string
	 * @param message
	 * @return
	 */
	public static String getAllToFormattedStringFrom(Message message, Message.RecipientType type) {
		String to = "";
		try {
			if (type == Message.RecipientType.TO) {
				//check approve mail
				if (!MailItem.isApproveMail(message)) {
					Address[] list = message.getRecipients(Message.RecipientType.TO);
					if (list != null && list.length > 0) {
						for (int i = 0; i < list.length; i++) {
							if (i == 0) {
								to = getFormattedEmailString(getUtf8String(list[i].toString()).trim());
							} else {
								to += "; " + getFormattedEmailString(getUtf8String(list[i].toString()).trim());
							}
						}
					}
				} else {
					String[] appInfo = message.getHeader(MAIL_APPROVE_HEADER_ORG_TO);
					to = appInfo[0].trim().replaceAll(" ", "");
					String[] toList = to.split(",");
					for (int i = 0; i < toList.length; i++) {
						if (i == 0) {
							to = getFormattedEmailString(getUtf8String(toList[i].trim()));
						} else {
							to += "; " + getFormattedEmailString(getUtf8String(toList[i].trim()));
						}
					}
				}
			} else {
				Address[] list = message.getRecipients(type);
				if (list != null && list.length > 0) {
					for (int i = 0; i < list.length; i++) {
						if (i == 0) {
							to = getFormattedEmailString(getUtf8String(list[i].toString()).trim());
						} else {
							to += "; " + getFormattedEmailString(getUtf8String(list[i].toString()).trim());
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return to;
	}

	/**
	 * get the formatted email string from a raw email
	 * ex: 235235<123@k.com> ---------> 123@k.com then
	 * ex: 123@k.com ---------> 3456<123@k.com>
	 * @param email
	 * @return
	 */
	public static String getFormattedEmailString(String email) {
		//escape the "<", ">" symbol
		email = email.replaceAll(" ", "");
		email = email.replaceAll(";", ",");
		if (email.indexOf("<") >= 0 && email.indexOf(">") >= 0) {
			email = email.substring(email.indexOf("<") + 1, email.indexOf(">"));
		}
		String ret;
		AddressBookItem adbItem = GlobalVariables.mainController.getAdbItemOf(email);
		if (adbItem != null) {
			ret = adbItem.userNameProperty().getValue() + " <" + email + ">";
		} else {
			ret = getFormattedEmailFrom(email);
		}

		return ret;
	}

	/**
	 * get the formatted date string from the date string
	 * ex: 2018-11-21 13:35:68 ---------> 2018-11-21(Wednesday) 10:35 AM
	 * @param strDate
	 * @return
	 */
	public String formattedDateString(String strDate) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = null;
		try {
			date = format.parse(strDate);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd(EEEE) hh:mm a");
			LocalDateTime locTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			return locTime.format(formatter);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * get the attachment files
	 * @param message
	 * @return
	 */
	public static boolean hasAttachment(Message message) {
		try {
			Multipart multipart = (Multipart) message.getContent();
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) && bodyPart.getFileName() == null) {
					continue; // dealing with attachments only
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * send the reading receipt mail(return note)
	 */
	public void procReturnNoteMail(MailItem aItem) {

		try {

			Message message = aItem.referenceMsg;

			String from = getUtf8String(message.getFrom()[0].toString());
			if (GlobalVariables.account.isSameEmail(from))
				return;

			String returnNote[] = message.getHeader(MAIL_DIPOSITION_TO_HEADER);

			if (returnNote != null && returnNote.length > 0) {
				System.out.println("return note = " + returnNote[0]);
				if (!GlobalVariables.mainController.isReturnedNote(aItem))
					return;
			} else {
				return;
			}

			String subject = getString("auto_reply") + " : " + aItem.subjectProperty().getValue();
			String content = getString("auto_reply_content");
			String to = returnNote[0];

			EmailSenderService emailSenderService =
					new EmailSenderService(getModelAccess().getEmailAccountByName(GlobalVariables.account.getAddress()),
							subject,
							to,
							"",
							"",
							content,
							new ArrayList<>());

			emailSenderService.restart();
			emailSenderService.setOnSucceeded(e-> {
				if(!emailSenderService.getValue().equals("MESSAGE_SENT_OK")){
					System.out.println("failed the return note");
				} else {
					GlobalVariables.mainController.updateReturnNote(aItem);
					System.out.println("succeed the return note");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * change comboBox css with param
	 * @param aComboBox
	 * @param aSecLevel
	 */
	public void setSecurityLevelCssTo(ComboBox aComboBox, SecurityLevel aSecLevel) {
		aComboBox.getStyleClass().remove("attach-file-button");
		File f = new File(makeCssFile(aSecLevel.levelColor));
		aComboBox.getStylesheets().clear();
		aComboBox.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
		aComboBox.getStyleClass().remove("attach-file-button");
		aComboBox.getStyleClass().add("attach-file-button");
	}

	public static String getFormattedSize(int size) {
		String strSize = "";
		if (size < 1024) {
			strSize = "1Kb";
		} else if (size < 1024 * 1024) {
			strSize = String.format("%dKb", size / 1024);
		} else {
			strSize = String.format("%dMb", size / 1024 / 1024);
		}

		return strSize;
	}

	public static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}


	/**
	 * get the system title
	 */
	public void getSystemTitle() {
		try {
			GlobalVariables.sysTitle = "";
			String url = Apis.GET_SYS_TITLE();
			XMailHttpRequest req = XMailHttpRequest.get(url);
			HttpURLConnection con = req.getConnection();

			InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
			BufferedReader in = new BufferedReader( inputStreamReader);

			String inputLine;
			StringBuffer response = new StringBuffer();
			while((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			con.disconnect();
			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject)parser.parse(response.toString());
			GlobalVariables.sysTitle = jsonObj.get("sysTitle").toString();
		} catch (Exception e) {
			System.out.println("******************getSysSecurityLevel failed by ==== " + getStackTrace(e));
		}
	}

	/**
	 * get response according to the check mail send rule
	 * @param to
	 * @param level
	 * @return
	 */
	public StringBuffer getCheckSendResponse(String to, int level) {
		try {
			String domainAccount = GlobalVariables.mainController.getPCName();

			String url = String.format(Apis.CHECK_SEND(), GlobalVariables.account.getAddress(), URLEncoder.encode(to, "utf-8"), level, domainAccount);

			XMailHttpRequest req = XMailHttpRequest.get(url);
			HttpURLConnection con = req.getConnection();
			int responseCode = con.getResponseCode();

			if (responseCode == 200) {
				InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), Charset.forName("GB18030"));
				BufferedReader in = new BufferedReader(inputStreamReader);

				String inputLine;
				StringBuffer response = new StringBuffer();
				while((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
                con.disconnect();
				return response;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isValidIP(String ipString) {
		String IPADDRESS_PATTERN =
				"(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(ipString);
		if (matcher.matches()) {
			return true;
		} else{
			return false;
		}
	}

	public static boolean isValidPort(String val) {
		try {
			int port = Integer.parseInt(val);
			if (port <= 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * show the waiting dialog
	 */
	public void showWaitingExitDialog(Stage owner) {
		if (waitingExitDlg == null) {
			waitingExitDlg = new Alert(Alert.AlertType.INFORMATION);
			Stage stage = (Stage) waitingExitDlg.getDialogPane().getScene().getWindow();
			stage.setOnCloseRequest(e->e.consume());
			stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
			final Button btOk = (Button) waitingExitDlg.getDialogPane().lookupButton(ButtonType.OK);
			btOk.setText(getString("exit"));
			btOk.addEventFilter(ActionEvent.ACTION, event -> {
				GlobalVariables.mainController.shutdown();
			});
			waitingExitDlg.setTitle(getString("waiting"));
			waitingExitMsgProperty = waitingExitDlg.headerTextProperty();
			ProgressIndicator pi = new ProgressIndicator();
			pi.setPrefWidth(300);
			pi.setPrefHeight(100);
			VBox box = new VBox();
			box.setAlignment(Pos.CENTER);
			box.getChildren().add(pi);
			waitingExitDlg.initOwner(owner);
			waitingExitDlg.getDialogPane().setContent(box);
		}

		Platform.runLater(()-> {
			if (!waitingExitDlg.isShowing())
				waitingExitDlg.showAndWait();
		});
	}

	/**
	 * show the waiting dialog
	 */
	public void showWaitingDialog(Stage owner) {
		if (waitingDlg == null) {
			waitingDlg = new Alert(Alert.AlertType.INFORMATION);
			Stage stage = (Stage) waitingDlg.getDialogPane().getScene().getWindow();
			stage.setOnCloseRequest(e->e.consume());
			stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
			final Button btOk = (Button) waitingDlg.getDialogPane().lookupButton(ButtonType.OK);
			btOk.setVisible(false);
			waitingDlg.setTitle(getString("waiting"));
			waitingMsgProperty = waitingDlg.headerTextProperty();
			waitingDlg.setHeaderText("");
			ProgressIndicator pi = new ProgressIndicator();
			pi.setPrefWidth(300);
			pi.setPrefHeight(100);
			VBox box = new VBox();
			box.setAlignment(Pos.CENTER);
			box.getChildren().add(pi);
			waitingDlg.initOwner(owner);
			waitingDlg.getDialogPane().setContent(box);
		}

		Platform.runLater(()-> {
			if (!waitingDlg.isShowing())
				waitingDlg.showAndWait();
		});
	}

	public void hideWaitingDialog() {
		Platform.runLater(()->{
			if (waitingDlg != null && waitingDlg.isShowing())
				waitingDlg.close();
		});
	}

	public void hideWaitingExitDialog() {
		Platform.runLater(()->{
			if (waitingExitDlg != null && waitingExitDlg.isShowing())
				waitingExitDlg.close();
		});
	}

	public void showAlert(String aTitle, String aMsg, Stage owner, Alert.AlertType aType) {
		Alert alert = new Alert(aType);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
		alert.setTitle(aTitle);
		alert.setHeaderText(aMsg);
		alert.initOwner(owner);
		alert.showAndWait();
	}

	public void showAlertOkAndWait(String aTitle, String header, Stage owner, Alert.AlertType aType) {
		Alert alert = new Alert(aType);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
		alert.getDialogPane().getButtonTypes().clear();
		alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
		alert.setTitle(aTitle);
		alert.setHeaderText(header);
		alert.initOwner(owner);
		alert.showAndWait();
	}

	public Optional<ButtonType> showAlertAndWait(String aTitle, String aMsg, Stage owner, Alert.AlertType aType) {
		Alert alert = new Alert(aType);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
		alert.setTitle(aTitle);
		alert.setHeaderText(aMsg);
		alert.initOwner(owner);
		return alert.showAndWait();
	}

	/**
	 * @author pilot
	 * @param aTitle
	 * @param aMsg
	 * @param owner
	 * @param aType
	 */
	public Optional<ButtonType> showAlertCallback(String aTitle, String aMsg, Stage owner, Alert.AlertType aType) {
		Alert alert = new Alert(aType);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
		alert.setTitle(aTitle);
		alert.setHeaderText(aMsg);
		alert.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
		alert.initOwner(owner);
		return alert.showAndWait();
	}

	public ModelAccess getModelAccess() {
		return modelAccess;
	}

	public boolean isValidFileName(String aFileName) {
		Pattern pattern = Pattern.compile("^[^*&%\\s]+$");
		return pattern.matcher(aFileName).matches();
	}

	public boolean isValidateEmail(String aEmail) {
		if (aEmail.indexOf("<") >= 0 && aEmail.indexOf(">") >= 0 && aEmail.endsWith(">")) {
			aEmail = aEmail.substring(aEmail.indexOf("<") + 1, aEmail.indexOf(">"));
		} if (aEmail.indexOf("<") < 0 && aEmail.indexOf(">") < 0) {
			return emailValidator.validate(aEmail);
		} else {
			return false;
		}
	}

	public boolean move(File sourceFile, File destFile)
	{
		if (sourceFile.isDirectory())
		{
			for (File file : sourceFile.listFiles())
			{
				move(file, new File(file.getPath().substring("temp".length()+1)));
			}
		}
		else
		{
			try {
				Files.move(Paths.get(sourceFile.getPath()), Paths.get(destFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * get the validation of email val
	 * @param val
	 * @return
	 */
	public String isInvalidEmail(String val) {
		try {
			StringBuffer sbuf = new StringBuffer();
			val = val.replaceAll(" ", "");
			val = val.replaceAll(";", ",");
			String[] toAry = val.split(",");
			for (String eachMail: toAry) {
				if (eachMail.length() > 0 && !isValidateEmail(eachMail)) {
					sbuf.append(eachMail);
					sbuf.append("\n");
				}
			}
			return sbuf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	public void setEmailToOverwriteField(TextField tf, String val) {
//		tf.setText(val);
//	}

//	public void setReceiversToTextField(TextField tf, String val, boolean shouldParse) {
//		if (!tf.getText().isEmpty()) {
//
//			String[] mailAry = val.split(",");
//
//			if (mailAry.length > 0) {
//				for (String first : mailAry) {
//					if (!isDuplicated(tf, getFormattedEmailString(first)))
//						tf.setText(tf.getText() + ", " + getFormattedEmailString(first));
//				}
//			} else {
//				if (!isDuplicated(tf, val))
//					tf.setText(tf.getText() + ", " + getFormattedEmailString(val));
//			}
//
//		} else
//			if (!shouldParse)
//				tf.setText(val);
//			else
//				tf.setText(getFormattedEmailString(val));
//	}

	/**
	 * get the only reg exp emails from the formatted emails
	 * @param emails
	 * @param isTrimName
	 * @return
	 */
	public static String getEscapeFromEmail(String emails, boolean isTrimName) {
		String ret = emails;
		ret = ret.replaceAll(";", ",");
		ret = ret.replaceAll(" ", "");

		if (isTrimName) {
			String[] mailAry = ret.split(",");
			int iLen = mailAry.length;
			if (iLen > 0) {
				for (int i = 0; i < iLen; i++) {
					String mail = mailAry[i];
					if (mail.indexOf("<") >= 0 && mail.indexOf(">") >= 0) {
						mail = mail.substring(mail.indexOf("<") + 1, mail.indexOf(">"));
					}
					if (i == 0)
						ret = mail;
					else
						ret += ", " + mail;
				}
			}
		}

		return ret;
	}

	/**
	 * get the all escaped strings from the email
	 * ex: 123<123@k.km>;222<234@km.com> ---------> 123@k.km;234@km.com
	 * @param emails
	 * @return
	 */
	public static String getEscapeFromEmail(String emails) {
		return getEscapeFromEmail(emails, false);
	}

	/**
	 * get the sent date string
	 * @param message
	 * @return
	 */
	public String getSentDateFrom(Message message) {
		try {
			Date date = message.getSentDate();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd(EEEE) hh:mm a");
			LocalDateTime locTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			return locTime.format(formatter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	/**
	 * Return the primary text content of the message.
	 */
	static boolean textIsHtml = false;
	public static String getHtmlTextFrom(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			String s = (String)p.getContent();
			textIsHtml = p.isMimeType("text/html");
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			// prefer html text over plain text
			Multipart mp = (Multipart)p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null)
						text = getHtmlTextFrom(bp);
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getHtmlTextFrom(bp);
					if (s != null)
						return s;
				} else {
					return getHtmlTextFrom(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart)p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getHtmlTextFrom(mp.getBodyPart(i));
				if (s != null)
					return s;
			}
		}

		return null;
	}

	/**
	 * get the attach file list for the uid
	 * @param uid
	 * @return
	 */
	public LinkedList<EmailSenderService.AttachFile> getAttachFiles(String uid) {
		LinkedList<EmailSenderService.AttachFile> fList = GlobalVariables.getAttachFiles(uid);
		LinkedList<EmailSenderService.AttachFile> newList = new LinkedList<>();
		if (fList != null) {
			File uidPath = new File(GlobalVariables.APP_DATA_DIR, uid);
			File fwPath = new File(uidPath.getAbsolutePath(), "fw");
			if (!fwPath.exists())
				fwPath.mkdirs();
			for (EmailSenderService.AttachFile eachF: fList) {
				File srcF = eachF.getFile();
				File destF = new File(fwPath.getAbsolutePath(), srcF.getName());
				if (destF.exists())
					destF.delete();
				srcF.renameTo(destF);
				newList.add(new EmailSenderService.AttachFile(destF, eachF.getSecLevel()));
			}
		}
		return newList;
	}

	/**
	 * clear all attach files for the uid
	 * @param uid
	 */
	public void clearAttachFiles(String uid) {
		GlobalVariables.clearAttachFiles(uid);
	}

	/**
	 * add an attach file for the uid
	 * @param uid
	 * @param file
	 */
	public void addAttachFile(String uid, EmailSenderService.AttachFile file) {
		GlobalVariables.addAttachFile(uid, file);
	}

	public File getAttachFile(BodyPart aBodyPart, String uid, boolean isReadOnly) {
		try {
			String fileName = aBodyPart.getFileName();
			DataSource source = aBodyPart.getDataHandler().getDataSource();
			if (source instanceof FileDataSource) {
				FileDataSource fsrc = (FileDataSource) source;
				System.out.println("source name = " + fsrc.getFile().getAbsolutePath());
				return fsrc.getFile();
			}
			fileName = getUtf8String(fileName);
			File dir = new File(GlobalVariables.APP_DATA_DIR, uid);
			if (!dir.exists())
				dir.mkdirs();
			File tempFile = new File(dir.getAbsolutePath(), fileName);
			if (isReadOnly && tempFile.exists())
				tempFile.delete();
			tempFile = getUniqueFilePath(GlobalVariables.APP_DATA_DIR, uid, fileName);
			MimeBodyPart part  = (MimeBodyPart)aBodyPart;
			part.saveFile(tempFile);
			return tempFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * delete the folder
	 * @param f
	 * @throws IOException
	 */
	public void deleteFile(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				deleteFile(c);
		}
		if (!f.delete())
			throw new IOException(getString("error_file_delete") + " " + f);
	}

	/**
	 * get a UTF-8 Decoded string
	 * @param src
	 * @return
	 */
	public static String getUtf8String(String src) {
		try {
			return MimeUtility.decodeText(MimeUtility.unfold(src));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * get the attach file list from the storage file
	 * @return
	 */
	public ArrayList<File> getAttachFiles() {
		ArrayList<File> files = new ArrayList<>();
		try {
			LineInputStream lis = new LineInputStream(new FileInputStream(new File(APP_DATA_DIR, APP_ATTACH_FILE)));
			String fName;
			while ((fName = lis.readLine()) != null) {
				File src = new File(fName);
				if (src.isFile() && !src.isHidden() && src.exists())
					files.add(src);
			}
		} catch (Exception e) {
		}
		return files;
	}

	public File getAttachFile(BodyPart aBodyPart, String uid) {
		return getAttachFile(aBodyPart, uid, true);
	}

	public static File getUniqueFilePath(String parent, String child, String fileName) {
		File dir = new File(parent, child);
		String uniqueName = getUniqueFileName(parent, child, fileName);
//		if (uniqueName.compareToIgnoreCase(fileName) != 0) {
//			File uniquePath = new File(dir, getFileName(uniqueName));
//			uniquePath.mkdirs();
//			return getUniqueFilePath(uniquePath.getPath(), "", fileName);
//		}
		return new File(dir, uniqueName);
	}

	public static String getUniqueFileName(String parent, String child, String fileName) {
		final File dir = new File(parent, child);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		int num = 0;
		final String ext = getFileExtension(fileName);
		final String name = getFileName(fileName);
		File file = new File(dir, fileName);
		while (file.exists()) {
			num++;
			file = new File(dir, name + "-" + num + ext);
		}
		return file.getName();
	}

	public static String getFileExtension(final String path) {
		if (path != null && path.lastIndexOf('.') != -1) {
			return path.substring(path.lastIndexOf('.'));
		}
		return null;
	}

	public static String getFileName(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	public ImageView getFileIcon(File aFile) {
		Image fxImage;

		File selectedFile = aFile;
		String fileName = selectedFile.getName();
		String fileExtension = "";
		if (fileName.indexOf('.') > 0) {
			fileExtension = fileName.substring(fileName.lastIndexOf("."), selectedFile.getName().length());
		}

		if (!selectedFile.exists()) {
			try {
				selectedFile = File.createTempFile("icon", fileExtension);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		FileSystemView view = FileSystemView.getFileSystemView();
		javax.swing.Icon icon = view.getSystemIcon(selectedFile);

		BufferedImage bufferedImage = new BufferedImage(
				icon.getIconWidth(),
				icon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB
		);
		icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);

		fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
		ImageView retV = new javafx.scene.image.ImageView(fxImage);
		retV.setUserData(selectedFile);
		return retV;
	}

	/**
	 * check the duplicate email in the message as the recipient type
	 * @param message
	 * @param type
	 * @param val
	 * @return
	 */
	public boolean isDuplicated(MimeMessage message, Message.RecipientType type, String val) {

		if (message == null || val == null || val.trim().length() == 0)
			return false;

		try {
			Address[] addresses = message.getRecipients(type);
			if (addresses == null || addresses.length == 0)
				return false;
			boolean isExist = false;
			for (Address adb: addresses) {
				isExist |= adb.toString().trim().equalsIgnoreCase(val.trim());
			}
			return isExist;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static final ObjectProperty<Locale> locale;

	static {
		locale = new SimpleObjectProperty<>(getDefaultLocale());
		locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
	}

	/**
	 * getString the supported Locales.
	 *
	 * @return List of Locale objects.
	 */
	public static List<Locale> getSupportedLocales() {
		return new ArrayList<>(Arrays.asList(Locale.ENGLISH, Locale.CHINA));
	}

	public static String getString(final String key, final Object... args) {
		ResourceBundle bundle = ResourceBundle.getBundle("com/denis/resources/lang", getLocale());
		return MessageFormat.format(bundle.getString(key), args);
	}

	/* getString the default locale. This is the systems default if contained in the supported locales, english otherwise.
	 *
	 * @return
	 */
	public static Locale getDefaultLocale() {
		Locale sysDefault = Locale.getDefault();
		//for test
//		sysDefault = Locale.CHINA;
		return getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.ENGLISH;
	}

	public static Locale getLocale() {
		return locale.get();
	}

	public static void setLocale(Locale locale) {
		localeProperty().set(locale);
		Locale.setDefault(locale);
	}

	public static ObjectProperty<Locale> localeProperty() {
		return locale;
	}
}
