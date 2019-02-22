package com.denis.controller.services;

import com.denis.controller.AbstractController;
import com.denis.model.EmailAccountBean;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.model.SecurityLevel;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.denis.model.GlobalVariables.GlobalVariables.*;

public class EmailSenderService extends Service<String> {

	private String result;

	public static class AttachFile {
		private File file;
		private SecurityLevel secLevel;
		public AttachFile(File aFile, SecurityLevel aSecLevel) {
			this.file = aFile;
			this.secLevel = aSecLevel;
		}
		public File getFile() {
			return file;
		}
		public SecurityLevel getSecLevel() {
			return secLevel;
		}
	}

	private EmailAccountBean emailAccountBean;
	private String subject;
	private String recipient;
	private String cc;
	private String bcc;
	private String content;
	private SecurityLevel secLevel;
	private List<File> attachments = new ArrayList<File>();
	private List<AttachFile> attachmentsWithSec = new ArrayList<AttachFile>();
	private boolean important;
	private boolean returnNote;
	private Message mMessage;
	private boolean isSaved;

	public EmailSenderService(EmailAccountBean emailAccountBean,
							  String subject,
							  String recipient,
							  String cc,
							  String bcc,
							  String content,
							  List<File> attachments) {

		this.emailAccountBean = emailAccountBean;
		this.subject = subject;
		this.recipient = recipient;
		this.cc = cc;
		this.bcc = bcc;
		this.content = content;
		this.attachments = attachments;
		isSaved = false;
	}

	public EmailSenderService(EmailAccountBean emailAccountBean,
							  String subject,
							  String recipient,
							  String cc,
							  String bcc,
							  String content,
							  SecurityLevel secLevel,
							  boolean returnNote,
							  boolean important,
							  List<AttachFile> attachmentsWithSec) {

		this.emailAccountBean = emailAccountBean;
		this.subject = subject;
		this.recipient = recipient;
		this.cc = cc;
		this.bcc = bcc;
		this.content = content;
		this.secLevel = secLevel;
		this.returnNote = returnNote;
		this.important = important;
		this.attachmentsWithSec = attachmentsWithSec;
		makePreparedMessage();
		isSaved = true;
	}

	public void makePreparedMessage() {
		this.mMessage = getMakeAndPrepareMessage();
	}

	public Message getPreparedMessage() {
		return this.mMessage;
	}

	/**
	 * get a will sent prepare message
	 * @return
	 */
	private Message getMakeAndPrepareMessage() {

		try {
			//make message
			Session session = emailAccountBean.getSession();
			MimeMessage message = new MimeMessage(session);
			String sender = GlobalVariables.accountInfo.getUserName() + "<" + emailAccountBean.getEmailAdress() + ">";
			System.out.println("sender = " + sender);
			message.setFrom(sender);
			message.addRecipients(Message.RecipientType.TO, recipient);

			if (cc != null) {
				message.addRecipients(Message.RecipientType.CC, cc);
			}
			if (bcc != null) {
				message.addRecipients(Message.RecipientType.BCC, bcc);
			}
			//security level
			if (secLevel != null)
				message.setHeader(MAIL_SEC_LEVEL_HEADER, "" + secLevel.level);
			else {
				message.setHeader(MAIL_SEC_LEVEL_HEADER, "" + GlobalVariables.securityList.get(0).level);
			}

			if (important)
				message.setHeader(MAIL_IMPORTANT_HEADER, "true");

			if (returnNote)
				message.setHeader(MAIL_DIPOSITION_TO_HEADER, emailAccountBean.getEmailAdress());

			message.setSubject(subject);

			// Setting the content:
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content, "text/html; charset=UTF-8");
			multipart.addBodyPart(messageBodyPart);

			// adding attachments:
			if (attachments != null && attachments.size() > 0) {
				for (File file : attachments) {
					MimeBodyPart messageBodyPartAttach = new MimeBodyPart();
					DataSource source = new FileDataSource(file.getAbsolutePath());
					messageBodyPartAttach.setDataHandler(new DataHandler(source));
					messageBodyPartAttach.setFileName(file.getName());
					multipart.addBodyPart(messageBodyPartAttach);
				}
			}

			if (attachmentsWithSec != null && attachmentsWithSec.size() > 0) {
				for (AttachFile file : attachmentsWithSec) {
					MimeBodyPart messageBodyPartAttach = new MimeBodyPart();
					DataSource source = new FileDataSource(file.getFile().getAbsolutePath());
					messageBodyPartAttach.setDataHandler(new DataHandler(source));
					messageBodyPartAttach.setFileName(file.getFile().getName());
					//file security level
					messageBodyPartAttach.setHeader(MAIL_ATTACH_SEC_LEVEL_HEADER, "" + file.getSecLevel().level);
					multipart.addBodyPart(messageBodyPartAttach);
				}
			}

			message.setContent(multipart);

			message.setSentDate(new Date());

			message.setHeader(MAIL_CUSTOM_UUID_HEADER, "" + message.getSentDate().getTime());
			message.setHeader(MAIL_MUID_HEADER, UUID.randomUUID().toString());

			return message;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected Task<String> createTask() {
		return new Task<String>() {
			@Override
			protected String call() throws Exception {
				try {

					if (mMessage == null) {
						mMessage = getMakeAndPrepareMessage();
					}
					mMessage.saveChanges();

					Transport.send(mMessage, mMessage.getAllRecipients());
					result = "MESSAGE_SENT_OK";

					if (isSaved)
						GlobalVariables.mainController.insertMailItem("SENT", mMessage, null);

				} catch (SendFailedException e) {
					e.printStackTrace();
					if (e.getNextException() != null) {
						Exception nextEp = e.getNextException();
						result = AbstractController.getStackTrace(nextEp);
					} else {
						result = AbstractController.getStackTrace(e);
					}
				} catch (Exception e) {
					result = AbstractController.getStackTrace(e);
				}
				return result;
			}
		};
	}
}