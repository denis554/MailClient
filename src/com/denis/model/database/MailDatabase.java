package com.denis.model.database;

import com.denis.controller.AbstractController;
import com.denis.controller.AddRuleController;
import com.denis.controller.ModelAccess;
import com.denis.controller.services.EmailSenderService;
import com.denis.model.*;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.model.http.Apis;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.sql.*;
import java.util.*;

import static com.denis.controller.AbstractController.*;
import static com.denis.model.GlobalVariables.GlobalVariables.*;
import static com.denis.model.MailItem.APP_STAT_PENDING;
import static com.denis.model.MailItem.NOT_REPLIED;

public class MailDatabase {

    private Connection conn;

    //table names
    final static String TBL_MAIL_BOX = "MailBox";
    final static String TBL_MAIL_CONTENTS = "MailContents";
    final static String TBL_ADDRESSBOOK_BOX = "AddressBookBox";
    final static String TBL_ADDRESSBOOK = "AddressBook";
    final static String TBL_RULES = "Rules";
    final static String TBL_FILLING = "Filling";
    final static String TBL_SETTINGS = "Settings";
    final static String TBL_ACCOUNT = "Account";
    final static String TBL_SERVER_INFO = "ServerInfo";
    final static String TBL_DRAFT_FILE = "DraftFile";

    public static final int STAT_FAIL = -1;
    public static final int STAT_NO_INSERT = STAT_FAIL + 1;
    public static final int STAT_NEW = STAT_NO_INSERT + 1;
    public static final int STAT_UPDATE = STAT_NEW + 1;
    public static final int STAT_UPDATE_MOVE = STAT_UPDATE + 1;
    public static final int STAT_ALREADY = STAT_UPDATE_MOVE + 1;

    public MailDatabase(String dbName) {
        try {
            File file = new File(dbName);
            if (!file.exists()) {
                System.out.println("db not exist!");
                copyFile("base.db", dbName);
            }
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void copyFile(String source, String dest) {

        InputStream is = null;
        OutputStream os = null;
        try {
            File srcFile = new File(source);
            File destFile = new File(dest);
            is = new FileInputStream(srcFile);
            os = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            is.close();
            os.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertAccount(String userId, String password) {
        try {

            String sql = "DELETE FROM " + TBL_ACCOUNT;
            PreparedStatement preStmt = conn.prepareStatement(sql);
            preStmt.execute();

            sql = "INSERT INTO " + TBL_ACCOUNT + "(uid, password) VALUES(?, ?)";
            PreparedStatement preStmt1 = conn.prepareStatement(sql);
            preStmt1.setString(1, userId);
            preStmt1.setString(2, password);
            preStmt1.execute();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * check the attachment status
     * @param uid
     * @return
     */
    public String getBodyText(String uid) {
        try {
            String selectSql = "SELECT body_text FROM " + TBL_MAIL_CONTENTS + " WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            ResultSet rs = preStmt.executeQuery();
            String ret = "";
            while (rs.next()) {
                ret =  rs.getString("body_text");
                break;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * check the attachment status
     * @param uid
     * @return
     */
    public boolean hasAttach(String uid) {
        try {
            String selectSql = "SELECT has_attach FROM " + TBL_MAIL_CONTENTS + " WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            ResultSet rs = preStmt.executeQuery();
            boolean ret = false;
            while (rs.next()) {
                ret =  rs.getInt("has_attach")==1?true:false;
                break;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * check the attachment status
     * @param uid
     * @return
     */
    public boolean hasAttachInDraft(String uid) {
        try {
            String selectSql = "SELECT * FROM " + TBL_DRAFT_FILE + " WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            ResultSet rs = preStmt.executeQuery();
            boolean ret = false;
            while (rs.next()) {
                ret = true;
                break;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * check the attachment status
     * @param uid
     * @return
     */
    public int getMailSize(String uid) {
        try {
            String selectSql = "SELECT size FROM " + TBL_MAIL_CONTENTS + " WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            ResultSet rs = preStmt.executeQuery();
            long totalSize = 0;
            while (rs.next()) {
                totalSize = rs.getInt("size");
                break;
            }
            if (!rs.isClosed()) {
                rs.close();
            }
            if (!preStmt.isClosed()) {
                preStmt.close();
            }
            return (int) totalSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * check the attachment status
     * @param uid
     * @return
     */
    public int getAttachSizeInDraft(String uid) {
        try {
            String selectSql = "SELECT * FROM " + TBL_DRAFT_FILE + " WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            ResultSet rs = preStmt.executeQuery();
            long totalSize = 0;
            while (rs.next()) {
                String filePath = rs.getString("fullpath");
                File f = new File(filePath);
                totalSize += f.length();
            }
            if (!rs.isClosed()) {
                rs.close();
            }
            if (!preStmt.isClosed()) {
                preStmt.close();
            }
            return (int) totalSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * remote the server info
     * @return
     */
    public boolean removeServerInfo() {
        boolean isSuccess = false;
        try {
            String insertSql = "DELETE FROM " + TBL_SERVER_INFO;
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.execute();
            isSuccess = true;
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isSuccess;
    }

    /**
     * insert the mail server ip and port
     * @param ip
     * @param port
     * @return
     */
    public boolean insertServerInfo(String ip, String port) {

        boolean isInsert = false;
        try {
            String insertSql = "INSERT INTO " + TBL_SERVER_INFO + "(ip_addr, port) VALUES(?, ?)";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setString(1, ip);
            preStmt.setString(2, port);
            preStmt.execute();
            isInsert = true;
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isInsert;
    }

    /**
     * get check of the not bind account
     * @return
     */
    public boolean isSetServer() {
        try {
            String selectSql = "SELECT * FROM " + TBL_SERVER_INFO;
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            ResultSet rs = preStmt.executeQuery();
            boolean isSet = false;
            while (rs.next()) {
                isSet = true;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return isSet;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @author pilot
     * get the server ip and port
     * @return
     */
    public Pair<String, String> getServerInfo() {
        Pair<String, String> pair = null;
        try {
            String selectSql = "SELECT * FROM " + TBL_SERVER_INFO;
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                pair = new Pair<>(rs.getString("ip_addr"), rs.getString("port"));
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return pair;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pair;
    }

    /**
     * get check of the not bind account
     * @return
     */
    public boolean isNotBindedAccount() {
        try {
            String selectSql = "SELECT * FROM " + TBL_ACCOUNT;
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            ResultSet rs = preStmt.executeQuery();
            boolean isBind = true;
            while (rs.next()) {
                isBind = false;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return isBind;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isValidAccount(String aUserName) {
        try {
            String selectSql = "SELECT * FROM " + TBL_ACCOUNT;
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            ResultSet rs = preStmt.executeQuery();
            boolean val = true;
            while (rs.next()) {
                String uid = rs.getString("uid");
                if (uid.compareTo(aUserName) != 0) {
                    val = false;
                    break;
                }
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return val;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * get the mail box list
     * @return
     */
    public ObservableList<MailBox> getMailBoxList() {
        ObservableList<MailBox> list = FXCollections.observableArrayList();
        try {
            //get box list
            String selectSql = "SELECT * FROM " + TBL_MAIL_BOX;
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("type");
                int custom = rs.getInt("custom");
                if (type.equalsIgnoreCase("pending") || type.equalsIgnoreCase("junk"))
                    continue;
                String zhDesc = rs.getString("zh_desc");
//                MailBox box = new MailBox(id, type, zhDesc, loadMailBox(type), custom);
                MailBox box = new MailBox(id, type, zhDesc, loadMailsOfBox(type), custom);
                list.add(box);
            }

            //get box message list
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addMailBox(String name) {
        try {
            String insertSql = "INSERT INTO " + TBL_MAIL_BOX + "(type, custom) VALUES(?, ?)";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setString(1, name);
            preStmt.setInt(2, 1);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isMessageReturnNote (String uid) {
        boolean isExist = false;
        try {
            String selectSql = "SELECT return_note FROM " + TBL_MAIL_CONTENTS + " WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                int ret = rs.getInt("return_note");
                isExist = ret == 1?true:false;
                break;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isExist;
    }

    public boolean checkNewMailBoxName(String name) {
        boolean isNew = true;
        try {
            String selectSql = "SELECT * FROM " + TBL_MAIL_BOX + " WHERE type = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, name);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                isNew = false;
                break;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isNew;
    }

    /**
     * get the new connect with new id
     * @param uid
     * @param draftFiles
     */
    public void connectStore(String aBoxName, String uid, List<EmailSenderService.AttachFile> draftFiles) {

        ModelAccess modelAccess = new ModelAccess();
        modelAccess.setMailType("other");
        EmailAccountBean emailAccount = new EmailAccountBean(
                GlobalVariables.account.getAddress(),
                GlobalVariables.account.getPassword(),
                modelAccess.getMailType());

        if (emailAccount.getLoginState() == EmailConstants.LOGIN_STATE_SUCCEDED) {

            try {

                Folder[] folders = emailAccount.getStore().getDefaultFolder().list();
                Folder inboxFolder = null;
                for(Folder folder: folders) {
                    if (folder.getName().equalsIgnoreCase("inbox")) {
                        inboxFolder = folder;
                        break;
                    }
                }

                if(inboxFolder.getType() != Folder.HOLDS_FOLDERS && !inboxFolder.isOpen()){
                    inboxFolder.open(Folder.READ_WRITE);
                }

                int msgCnt = inboxFolder.getMessageCount();

                Message message = null;
                for(int i = msgCnt; i > 0 ; i--) {
                    POP3Folder pop3Folder = (POP3Folder) inboxFolder;
                    Message currentMessage = inboxFolder.getMessage(i);
                    String cmpId = pop3Folder.getUID(currentMessage);
                    if (cmpId.compareToIgnoreCase(uid) == 0) {
                        message = currentMessage;
                        break;
                    }
                }

                if (message != null) {

                    try {

                        System.out.println("attach mail insertting = " + uid);
                        int ret = insertMessageAsStream(aBoxName,
                                uid,
                                message,
                                draftFiles,
                                true);

                        if (ret == STAT_NEW) {
                            HashMap<String, String> msgMap = new HashMap<>();
                            msgMap.put(uid, aBoxName);
                            Message oneMsg = MailDatabase.this.getMessage(uid);
                            Platform.runLater(()->{
                                System.out.println("notify new mail uid = " + uid);
                                GlobalVariables.mainController.showNewEmailNotification(aBoxName, 1, oneMsg, MailItem.getUidFromMsg(oneMsg));
                                GlobalVariables.mainController.insertMailItemToUI(msgMap.entrySet());
                            });
                        }

                        message.setFlag(Flags.Flag.DELETED, true);
                    } catch (Exception exx) {
                        System.out.println("can't read the message num = " + message.getMessageNumber());
                    }
                }

                inboxFolder.close(true);

            } catch (Exception ex) {
                System.out.println("---background download fail---" + AbstractController.getStackTrace(ex));
            }
        }
    }

    /**
     * insert the message as the byte stream
     * @param aBoxName
     * @param uid
     * @param message
     * @param draftFiles
     * @param isRuleCheck
     * @return
     */
    int insertMessageAsStream(String aBoxName,
                             String uid,
                             Message message,
                             List<EmailSenderService.AttachFile> draftFiles,
                             boolean isRuleCheck) {
        try {

            //insert the filling
            if (isRuleCheck) {
                String boxName = fillingMail(message, uid);
                if (boxName == null || boxName.trim().length() == 0) {
                    insertFilling(uid, "INBOX");
                }
            } else {
                insertFilling(uid, aBoxName);
            }

            System.setProperty("mail.mime.multipart.allowempty", "true");
            System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");

            ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
            ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();

            int hasSeen = message.isSet(Flags.Flag.SEEN)?0:1;
            int hasAttach = AbstractController.hasAttachment(message)?1:0;
            int note = 0;
            int size = message.getSize();
            String returnNote[] = message.getHeader(MAIL_DIPOSITION_TO_HEADER);
            if (returnNote != null && returnNote.length > 0) {
                note = 1;
            }

            MimeMessage headerMsg = new MimeMessage(Session.getDefaultInstance(new Properties()));

            Enumeration enumH = message.getAllHeaders();
            while (enumH.hasMoreElements()) {
                Header header = (Header) enumH.nextElement();
                headerMsg.setHeader(header.getName(), header.getValue());
            }

            headerMsg.setText("header");

            String text = AbstractController.getHtmlTextFrom(message);

            headerMsg.writeTo(headerStream);
            message.writeTo(bodyStream);
            if (size == -1)
                size = bodyStream.size();

            byte[] headerData = headerStream.toByteArray();
            byte[] contentsData = bodyStream.toByteArray();

            String insertSql = "INSERT INTO " + TBL_MAIL_CONTENTS +
                    "(reference, uid, type, unread, return_note, answer_stat, contents, has_attach, size, body_text)" +
                    " VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setBytes(1, headerData);
            preStmt.setString(2, uid);
            preStmt.setString(3, aBoxName);
            preStmt.setInt(4, hasSeen); //unread
            preStmt.setInt(5, note);
            preStmt.setInt(6, NOT_REPLIED);
            preStmt.setBytes(7, contentsData);
            preStmt.setInt(8, hasAttach);
            preStmt.setInt(9, size);
            preStmt.setString(10, text);

            preStmt.execute();
            headerStream.flush();
            headerStream.close();
            bodyStream.flush();
            bodyStream.close();
            if (!preStmt.isClosed())
                preStmt.close();

            //draft file
            if (aBoxName.equalsIgnoreCase("draft") && draftFiles != null && draftFiles.size() > 0) {
                updateDraft(uid, draftFiles);
            }

            return STAT_NEW;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return STAT_NO_INSERT;
    }

    /**
     * insert message or update if uid exists
     * @param aBoxName
     * @param uid
     * @param message
     * @param draftFiles
     * @param isRuleCheck
     * @return
     */
    public int insertMessage(String aBoxName,
                             String uid,
                             Message message,
                             List<EmailSenderService.AttachFile> draftFiles,
                             boolean isRuleCheck) {

        try {

            if (checkNewMail(uid)) {

//                int size = message.getSize();

                //if the POP3Message size > 5M, connect to the store
//                if (size > 1024 * 1024 * 5 && (message instanceof POP3Message)) {
                Message msg = HandlingPop3Msg.get(uid);
                System.out.println("---------------check-------------attach----------download----uid = " + uid);
                if ((message instanceof POP3Message) && msg == null && AbstractController.hasAttachment(message)) {
                    //new connection
                    if (msg != null) {
                        return STAT_NO_INSERT;
                    }

                    System.out.println("---------------will-------------attach----------download----uid = " + uid);
                    HandlingPop3Msg.put(uid, message);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("---------------connect-------------attach----------download----uid = " + uid);
                            connectStore(aBoxName, uid, draftFiles);
                            HandlingPop3Msg.put(uid, null);
                            System.out.println("---------------finish-------------attach----------download----uid = " + uid);
                        }
                    }).start();

                    return STAT_NO_INSERT;
                }

                if (msg != null)
                    return STAT_NO_INSERT;


                return insertMessageAsStream(aBoxName,
                        uid,
                        message,
                        draftFiles,
                        isRuleCheck);

            } else {
                int ret = STAT_ALREADY;
                switch (aBoxName.toLowerCase()) {
                    case "draft":
                        updateMessage(uid, message);
                        if (draftFiles != null && draftFiles.size() > 0)
                            updateDraft(uid, draftFiles);
                        ret = STAT_UPDATE;
                        break;
                    case "inbox":
                        break;
                    case "sent"://if draft mail has sent, it will be moved to the sent box automatically
                        updateDraftMailToSentBox(aBoxName, uid);
                        ret = STAT_UPDATE_MOVE;
                        break;
                    default:
                        break;
                }
                return ret;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return STAT_FAIL;
    }


    /**
     * get the matched box with rules
     * @param message
     * @return
     */
    public String fillingMail(Message message, String uid) {

        //parse message
        try {

            String subject = getUtf8String(message.getSubject()).toLowerCase();
            Address[] senderList = message.getFrom();
            String mailSender = getEscapeFromEmail(getUtf8String(senderList[0].toString()).toLowerCase(), true);
            Address[] mailReceivers = message.getAllRecipients();

            String result = "";
            for (RuleItem ruleItem: GlobalVariables.mainController.mRulesList) {

                //check sender
                boolean isInS = ((AddRuleController.RuleInclude)ruleItem.includeSenders.getValue()).rule.equalsIgnoreCase("include");
                boolean isInT = ((AddRuleController.RuleInclude)ruleItem.includeTitles.getValue()).rule.equalsIgnoreCase("include");
                boolean isInR = ((AddRuleController.RuleInclude)ruleItem.includeReceivers.getValue()).rule.equalsIgnoreCase("include");

                boolean isExistS = false;
                boolean isExistT = false;
                boolean isExistR = false;
                boolean isMatchedS = false;
                boolean isMatchedT = false;
                boolean isMatchedR = false;

                String senders = getUtf8String(getEscapeFromEmail(ruleItem.senders.getValue())).toLowerCase();
                String retBox = ((MailBox) ruleItem.moveTo.getValue()).getBoxName();
                String ruleT = getUtf8String(ruleItem.titles.getValue()).toLowerCase();
                String receivers = getUtf8String(getEscapeFromEmail(ruleItem.receivers.getValue())).toLowerCase();

                String[] ruleUsers = senders.split(",");
                if (senders.indexOf(",") > 0) {
                    for (String ruleSender: ruleUsers) {
                        String cmp1 = getEscapeFromEmail(ruleSender, true);
                        String cmp2 = getEscapeFromEmail(mailSender, true);
                        if (cmp1.contains(cmp2)) {
                            isExistS = true;
                            break;
                        }
                    }
                } else {
                    if (senders.contains(mailSender)) {
                        isExistS = true;
                    }
                }

                if (isExistS && isInS) {
                    isMatchedS = true;
                } else if (!isExistS && !isInS){
                    isMatchedS = true;
                }

                //check title
                if (ruleT != null &&
                        ruleT.trim().length() > 0) {
                    if (subject.contains(ruleT))
                        isExistT = true;

                    if (isExistT && isInT) {
                        isMatchedT = true;
                    } else if (!isExistT && !isInT) {
                        isMatchedT = true;
                    }
                }

                //check receiver
                ruleUsers = receivers.split(",");
                if (receivers.indexOf(",") > 0) {
                    for (Address receiver: mailReceivers) {
                        for (String ruleReceiver: ruleUsers) {
                            String cmp1 = getEscapeFromEmail(getUtf8String(receiver.toString()), true).toLowerCase();
                            String cmp2 = getEscapeFromEmail(ruleReceiver, true);
                            if (cmp1.contains(cmp2)) {
                                isExistR = true;
                                break;
                            }
                        }
                    }
                } else {
                    for (Address receiver: mailReceivers) {
                        String cmp1 = getEscapeFromEmail(getUtf8String(receiver.toString()), true).toLowerCase();
                        String cmp2 = getEscapeFromEmail(receivers, true);
                        if (cmp1.contains(cmp2)) {
                            isExistR = true;
                            break;
                        }
                    }
                }

                if (isExistR && isInR) {
                    isMatchedR = true;
                } else if (!isExistR && !isInR) {
                    isMatchedR = true;
                }

                boolean isExist = checkNewMailBoxName(retBox);
                if (isExist)
                    retBox = "INBOX";

                if (isMatchedS)
                    result = retBox;

                if (isMatchedS && isMatchedT && isMatchedR) {
                    insertFilling(uid, retBox);
                    continue;
                }
                if (isMatchedS && isMatchedT) {
                    insertFilling(uid, retBox);
                    continue;
                }
                if (isMatchedS) {
                    insertFilling(uid, retBox);
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * @author pilot
     * update the draft attached files for the message
     * @param uid
     * @param draftFiles
     * @return
     */
    public boolean updateDraft(String uid, List<EmailSenderService.AttachFile> draftFiles) {

        if (draftFiles == null || draftFiles.size() == 0)
            return true;

        String sql = "DELETE FROM " + TBL_DRAFT_FILE + " WHERE uid = ?";

        try {

            PreparedStatement preStmt = conn.prepareStatement(sql);

            preStmt.setString(1, uid);
            preStmt.execute();

            if (!preStmt.isClosed())
                preStmt.close();

            sql = "INSERT INTO " + TBL_DRAFT_FILE + "(fullpath, uid, sec_level) VALUES(?, ?, ?)";

            conn.setAutoCommit(false);
            preStmt = conn.prepareStatement(sql);

            for (EmailSenderService.AttachFile eachF: draftFiles) {
                preStmt.setString(1, eachF.getFile().getAbsolutePath());
                preStmt.setString(2, uid);
                preStmt.setInt(3, eachF.getSecLevel().level);
                preStmt.addBatch();
            }

            preStmt.executeBatch();

            if (!preStmt.isClosed())
                preStmt.close();

            conn.commit();
            conn.setAutoCommit(true);

            return true;

        } catch (Exception e) {
            System.out.println("------update of the draft failed = " + AbstractController.getStackTrace(e));
        }

        return false;
    }

    /**
     * @author pilot
     * update a message with the status
     * @param uid
     * @param status
     */
    public void updateMessageAnsweredStatus(String uid, int status) {
        try {
            String insertSql = "UPDATE " + TBL_MAIL_CONTENTS + " SET answer_stat = ? WHERE  uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setInt(1, status);
            preStmt.setString(2, uid);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @author pilot
     * update a message with the isSeen
     * @param uid
     * @param isSeen
     */
    public void updateMessageReadStatus(String uid, boolean isSeen) {
        try {
            String insertSql = "UPDATE " + TBL_MAIL_CONTENTS + " SET unread = ? WHERE  uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setInt(1, isSeen?0:1);
            preStmt.setString(2, uid);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * set important status for the mailItems
     * @param mailItems
     */
    public void setStartedMessage(ObservableList<MailItem> mailItems) {
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE " + TBL_MAIL_CONTENTS + " SET reference = ? WHERE  uid = ?";
            System.out.println("UPDATE query = " + sql);
            PreparedStatement preStmt = conn.prepareStatement(sql);
            for (MailItem eachL: mailItems) {
                String uid = MailItem.getUidFromMsg(eachL.referenceMsg);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                eachL.referenceMsg.writeTo(out);
                byte[] msgData = out.toByteArray();
                preStmt.setBytes(1, msgData);
                preStmt.setString(2, uid);
                preStmt.addBatch();
            }
            preStmt.executeBatch();
            if (!preStmt.isClosed())
                preStmt.close();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * set important status for the mailItems
     * @param mailItems
     */
    public void setImportantMessage(ObservableList<MailItem> mailItems) {
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE " + TBL_MAIL_CONTENTS + " SET reference = ? WHERE  uid = ?";
            System.out.println("UPDATE query = " + sql);
            PreparedStatement preStmt = conn.prepareStatement(sql);
            for (MailItem eachL: mailItems) {
                String uid = MailItem.getUidFromMsg(eachL.referenceMsg);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                eachL.referenceMsg.writeTo(out);
                byte[] msgData = out.toByteArray();
                preStmt.setBytes(1, msgData);
                preStmt.setString(2, uid);
                preStmt.addBatch();
            }
            preStmt.executeBatch();
            if (!preStmt.isClosed())
                preStmt.close();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * move some mail items to the name box
     * @param destBox
     * @param aItems
     * @return
     */
    public boolean moveMailToBox(String destBox, ObservableList<MailItem> aItems) {

        boolean movedTo = false;
        try {

//            String sql = "UPDATE " + TBL_MAIL_CONTENTS + " SET type = ? WHERE uid = ?";
            String sql = "UPDATE " + TBL_FILLING + " SET box = ? WHERE uid = ? AND id = ?";

            System.out.println("UPDATE query = " + sql);

            conn.setAutoCommit(false);
            PreparedStatement preStmt = conn.prepareStatement(sql);

            ArrayList<Integer> aryFillings = new ArrayList<>();
            for (MailItem eachL: aItems) {
                int id = checkNewFilling(eachL.getUid(), destBox);
                if (id > 0) {
                    aryFillings.add(id);
                }
                preStmt.setString(1, destBox);
                preStmt.setString(2, eachL.uid);
                //new
                preStmt.setInt(3, eachL.fillingId);
                preStmt.addBatch();
            }

            preStmt.executeBatch();
            if (!preStmt.isClosed())
                preStmt.close();

            //remove the duplicate filling items
            if (aryFillings.size() > 0) {
                sql = "DELETE FROM " + TBL_FILLING + " WHERE id = ?";
                preStmt = conn.prepareStatement(sql);

                System.out.println("UPDATE query = " + sql);

                for (Integer eachF: aryFillings) {
                    preStmt.setInt(1, eachF);
                    preStmt.addBatch();
                }

                preStmt.executeBatch();
                if (!preStmt.isClosed())
                    preStmt.close();
            }

            conn.commit();
            conn.setAutoCommit(true);
            movedTo = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return movedTo;
    }

    /**
     * @author pilot
     * update the message list status with the isSeen
     * @param aItems
     * @param isSeen
     */
    public void updateMessageReadStatus(ObservableList<MailItem> aItems, boolean isSeen) {
        try {

            String sql = "UPDATE " + TBL_MAIL_CONTENTS + " SET unread = ? WHERE uid = ?";

            System.out.println("UPDATE query = " + sql);

            conn.setAutoCommit(false);
            PreparedStatement preStmt = conn.prepareStatement(sql);

            for (MailItem eachL: aItems) {
                preStmt.setInt(1, isSeen?0:1);
                preStmt.setString(2, eachL.uid);
                preStmt.addBatch();
            }

            preStmt.executeBatch();
            if (!preStmt.isClosed())
                preStmt.close();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean updateMessageReturnNote(Message aMsg, boolean isNote) {
        try {
            String uid = MailItem.getUidFromMsg(aMsg);

            String insertSql = "UPDATE " + TBL_MAIL_CONTENTS + " SET return_note = ? WHERE  uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setInt(1, isNote?1:0);
            preStmt.setString(2, uid);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public void updateMessageFlag(Message msg, boolean flag) {
        try {
            String uid = MailItem.getUidFromMsg(msg);

            String insertSql = "UPDATE " + TBL_MAIL_CONTENTS + " SET flag = ? WHERE  uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            if (flag) {
                preStmt.setInt(1, 1);
            } else {
                preStmt.setInt(1, 0);
            }
            preStmt.setString(2, uid);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * remove one message for the uid
     * @param uid
     * @return
     */
    public boolean removeDraftMessage(String uid) {
        boolean removed = false;
//        String sql = "UPDATE " + TBL_MAIL_CONTENTS + " SET type = ? WHERE uid = ?";
        String sql = "DELETE " + TBL_MAIL_CONTENTS + " SET WHERE uid = ?";
        System.out.println("UPDATE query = " + sql);

        try {

            PreparedStatement preStmt = conn.prepareStatement(sql);
            preStmt.setString(1, uid);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();

            //remove draft file
            sql = "DELETE " + TBL_DRAFT_FILE + " SET WHERE uid = ?";
            preStmt = conn.prepareStatement(sql);
            preStmt.setString(1, uid);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();

            //remove filling
            sql = "DELETE " + TBL_FILLING + " SET WHERE uid = ?";
            preStmt = conn.prepareStatement(sql);
            preStmt.setString(1, uid);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();

            removed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return removed;
    }

    /**
     * remove some mail messages
     * @param aItems
     * @param mailBox
     * @return
     */
    public boolean removeMessage(ObservableList<MailItem> aItems, MailBox mailBox) {
        boolean removed = false;
        try {
            conn.setAutoCommit(false);

            if (mailBox.getBoxName().equalsIgnoreCase("trash") || mailBox.getBoxName().toLowerCase().contains("pending")) {
                String sql = "DELETE FROM " + TBL_MAIL_CONTENTS + " WHERE uid = ?";
                System.out.println("DELETE query = " + sql);
                PreparedStatement preStmt = conn.prepareStatement(sql);
                for (MailItem eachL: aItems) {
                    preStmt.setLong(1, Long.parseLong(eachL.uid));
                    preStmt.addBatch();
                }
                preStmt.executeBatch();
                if (!preStmt.isClosed())
                    preStmt.close();

                sql = "DELETE FROM " + TBL_FILLING + " WHERE uid = ?";
                System.out.println("DELETE query = " + sql);
                preStmt = conn.prepareStatement(sql);
                for (MailItem eachL: aItems) {
                    preStmt.setString(1, eachL.uid);
                    preStmt.addBatch();
                }
                preStmt.executeBatch();
                if (!preStmt.isClosed())
                    preStmt.close();

                /*String sql = "UPDATE " + TBL_MAIL_CONTENTS + " SET type = ? WHERE uid = ?";
                System.out.println("UPDATE query = " + sql);
                PreparedStatement preStmt = conn.prepareStatement(sql);
                for (MailItem eachL: aItems) {
                    preStmt.setString(1, "DELETED");
                    preStmt.setString(2, eachL.uid);
                    preStmt.addBatch();
                }
                preStmt.executeBatch();
                if (!preStmt.isClosed())
                    preStmt.close();*/
            } else {
//                String sql = "UPDATE " + TBL_MAIL_CONTENTS + " SET type = ? WHERE uid = ?";
                String sql = "UPDATE " + TBL_FILLING + " SET box = ? WHERE uid = ? AND id = ?";
                System.out.println("UPDATE query = " + sql);
                ArrayList<Integer> aryFillings = new ArrayList<>();
                PreparedStatement preStmt = conn.prepareStatement(sql);
                for (MailItem eachL: aItems) {
                    int id = checkNewFilling(eachL.uid, "TRASH");
                    if (id > 0)
                        aryFillings.add(eachL.fillingId);
                    preStmt.setString(1, "TRASH");
                    preStmt.setString(2, eachL.uid);
                    preStmt.setInt(3, eachL.fillingId);
                    preStmt.addBatch();
                }
                preStmt.executeBatch();
                if (!preStmt.isClosed())
                    preStmt.close();

                //remove the duplicate filling items
                if (aryFillings.size() > 0) {
                    sql = "DELETE FROM " + TBL_FILLING + " WHERE id = ?";
                    preStmt = conn.prepareStatement(sql);

                    System.out.println("UPDATE query = " + sql);

                    for (Integer eachF: aryFillings) {
                        preStmt.setInt(1, eachF);
                        preStmt.addBatch();
                    }

                    preStmt.executeBatch();
                    if (!preStmt.isClosed())
                        preStmt.close();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            removed = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return removed;
    }

    /**
     * move a mail to the name box
     * @param name
     * @param uid
     * @return
     */
    public boolean updateDraftMailToSentBox(String name, String uid) {
        boolean movedTo = false;
        try {
//            String updateSql = "UPDATE " + TBL_MAIL_CONTENTS + " SET type = ? WHERE uid = ?";
            String updateSql = "UPDATE " + TBL_FILLING + " SET box = ? WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(updateSql);
            preStmt.setString(1, name);
            preStmt.setString(2, uid);
            preStmt.execute();
            movedTo = true;
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return movedTo;
    }

    public boolean updateMessage(String uid, Message aMessage) {
        boolean movedTo = false;
        try {
            String updateSql = "UPDATE " + TBL_MAIL_CONTENTS + " SET reference = ? WHERE uid = ?";

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            aMessage.writeTo(out);

            byte[] msgData = out.toByteArray();

            PreparedStatement preStmt = conn.prepareStatement(updateSql);

            preStmt.setBytes(1, msgData);
            preStmt.setString(2, uid);

            preStmt.execute();
            movedTo = true;
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return movedTo;
    }


    public boolean changeMailBox(String name, String newName) {
        boolean removed = false;
        try {
            String updateSql = "UPDATE " + TBL_MAIL_BOX + " SET type = ? WHERE type = ?";
            PreparedStatement preStmt = conn.prepareStatement(updateSql);
            preStmt.setString(1, newName);
            preStmt.setString(2, name);
            preStmt.execute();

            updateSql = "UPDATE " + TBL_MAIL_CONTENTS + " SET type = ? WHERE type = ?";
            PreparedStatement preStmt1 = conn.prepareStatement(updateSql);
            preStmt1.setString(1, newName);
            preStmt1.setString(2, name);
            preStmt1.execute();

            if (!preStmt.isClosed())
                preStmt.close();
            if (!preStmt1.isClosed())
                preStmt1.close();

            removed = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return removed;
    }

    /**
     * remove a mail box
     * @param name
     * @return
     */
    public boolean removeMailBox(String name) {
        boolean removed = false;

        try {

            String removeSql = "DELETE FROM " + TBL_MAIL_BOX + " WHERE type = ?";
            PreparedStatement preStmt = conn.prepareStatement(removeSql);
            preStmt.setString(1, name);
            preStmt.execute();

            if (!preStmt.isClosed())
                preStmt.close();

//            String deleteSql = "UPDATE " + TBL_MAIL_CONTENTS + " SET type = ? WHERE type = ?";
            String deleteSql = "UPDATE " + TBL_FILLING + " SET box = ? WHERE box = ?";
            preStmt = conn.prepareStatement(deleteSql);
            preStmt.setString(1, "TRASH");
            preStmt.setString(2, name);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();

            removed = true;

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return removed;
    }

    /**
     * check the new filter box for the uid
     * @param uid
     * @param filterBox
     * @return
     */
    public int checkNewFilling (String uid, String filterBox) {
        int isNew = -1;
        try {
            String selectSql = "SELECT * FROM " + TBL_FILLING + " WHERE uid = ? AND box = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            preStmt.setString(2, filterBox);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                isNew = rs.getInt("id");
                break;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isNew;
    }

    public boolean checkNewMail (String uid) {
        boolean isNew = true;
        try {
            String selectSql = "SELECT * FROM " + TBL_MAIL_CONTENTS + " WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                isNew = false;
                break;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isNew;
    }

    public boolean checkNewAddressBoxName(String name) {
        boolean isNew = true;
        try {
            String selectSql = "SELECT * FROM " + TBL_ADDRESSBOOK_BOX + " WHERE type = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, name);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                isNew = false;
                break;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isNew;
    }

    /**
     * get a message
     * @param uid
     * @return
     */
    public Message getMailContents(String uid) {
        return getMessage(uid, false);
    }

    public Message getMessage(String uid) {
        return getMessage(uid, true);
    }

    /**
     * get a message
     * @param uid
     * @return
     */
    Message getMessage(String uid, boolean isHeader) {
        try {
            String selectSql = "SELECT * FROM " + TBL_MAIL_CONTENTS + " WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            ResultSet rs = preStmt.executeQuery();
            Message msg = null;
            while (rs.next()) {
                int flag = rs.getInt("flag");
                int unread = rs.getInt("unread");
                InputStream input = rs.getBinaryStream(isHeader?"reference":"contents");
                Properties properties = new Properties();
                properties.put("mail.transport.protocol", "smtps");
                properties.put("mail.smtp.host", Apis.HTTP_HOST);
                properties.put("mail.smtp.port", "25");

                Session mailSession = Session.getDefaultInstance(properties, null);
                msg = new MimeMessage(mailSession, input);
                msg.setHeader(MAIL_CUSTOM_UUID_HEADER, uid);
                input.close();
                msg.setFlag(Flags.Flag.FLAGGED, flag == 1?true:false);
                msg.setFlag(Flags.Flag.SEEN, unread == 0?true:false);
                break;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * load the attached files for the draft mail
     * @param uid
     * @return
     */
    public List<EmailSenderService.AttachFile> loadDraftFiles(String uid) {
        ObservableList<EmailSenderService.AttachFile> ret = FXCollections.observableArrayList();
        try {
            String selectSql = "SELECT * FROM " + TBL_DRAFT_FILE + " WHERE uid = ?";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, uid);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                String path = rs.getString("fullpath");
                int secLevel = rs.getInt("sec_level");
                EmailSenderService.AttachFile attachFile = new EmailSenderService.AttachFile(
                        new File(path),
                        getSecLevel(secLevel)
                );
                ret.add(attachFile);
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * load the approve mail list
     * @return
     */
    public ObservableList<MailItem> loadApproveMails() {
        ObservableList<MailItem> msgList = FXCollections.observableArrayList();
        try {
            String selectSql = "SELECT * FROM " + TBL_MAIL_CONTENTS + " ORDER By uid DESC";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                String uid = rs.getString("uid");
                String type = rs.getString("type");
                int unread = rs.getInt("unread");

                InputStream input = rs.getBinaryStream("reference");
                Properties properties = new Properties();
                properties.put("mail.transport.protocol", "smtps");
                properties.put("mail.smtp.host", Apis.HTTP_HOST);
                properties.put("mail.smtp.port", "25");

                Session mailSession = Session.getDefaultInstance(properties, null);
                Message msg = new MimeMessage(mailSession, input);
                msg.setHeader(MAIL_CUSTOM_UUID_HEADER, uid);
                input.close();
                msg.setFlag(Flags.Flag.SEEN, unread == 0?true:false);

                MailItem item = new MailItem(uid, msg, type);
                if (!item.isApproveMail())
                    continue;

                if (item.isHandledOfApprove() && !item.getApproveStatus(APP_STAT_PENDING))
                    continue;

                msgList.add(item);
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  msgList;
    }

    /**
     * load the mail items for the box
     * @param boxName
     * @return
     */
    public ObservableList<MailItem> loadMailsOfBox(String boxName) {
        ObservableList<MailItem> msgList = FXCollections.observableArrayList();
        try {
//            String selectSql = "SELECT * FROM " + TBL_MAIL_CONTENTS + " WHERE type = ? ORDER By uid DESC";

            String selectSql = "SELECT " +
                    TBL_FILLING + ".id, " +
                    TBL_MAIL_CONTENTS + ".flag as flag, " +
                    TBL_MAIL_CONTENTS + ".unread as unread, " +
                    TBL_MAIL_CONTENTS + ".answer_stat as answer_stat, " +
                    TBL_MAIL_CONTENTS + ".reference as reference, " +
                    TBL_MAIL_CONTENTS + ".uid FROM " +
                    TBL_FILLING +
                    " INNER JOIN " +
                    TBL_MAIL_CONTENTS +
                    " ON " +
                    TBL_MAIL_CONTENTS + ".uid = " + TBL_FILLING + ".uid" +
                    " WHERE box = ?";

            System.out.println("selectSql = " + selectSql);

            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, boxName);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int flag = rs.getInt("flag");
                int unread = rs.getInt("unread");
                int answer_stat = rs.getInt("answer_stat");

                InputStream input = rs.getBinaryStream("reference");
                Properties properties = new Properties();
                properties.put("mail.transport.protocol", "smtps");
                properties.put("mail.smtp.host", Apis.HTTP_HOST);
                properties.put("mail.smtp.port", "25");

                Session mailSession = Session.getDefaultInstance(properties, null);
                Message msg = new MimeMessage(mailSession, input);
                input.close();

                msg.setHeader(MAIL_CUSTOM_UUID_HEADER, rs.getString("uid"));
                msg.setHeader(MAIL_ANSWER_STAT_HEADER, "" + answer_stat);

                msg.setFlag(Flags.Flag.FLAGGED, flag == 1?true:false);
                msg.setFlag(Flags.Flag.SEEN, unread == 0?true:false);
                MailItem item = convertFrom(boxName, msg, MailItem.getUidFromMsg(msg));
                item.setFillId(id);
                msgList.add(item);
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  msgList;
    }

    public void addAdbBox(String name) {
        try {
            String insertSql = "INSERT INTO " + TBL_ADDRESSBOOK_BOX + "(type, custom) VALUES(?,?)";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setString(1, name);
            preStmt.setInt(2, 1);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ObservableList<AddressBox> getAdbBoxs(){
        ObservableList<AddressBox> list = FXCollections.observableArrayList();
        try {
            String selectSql = "SELECT * FROM " + TBL_ADDRESSBOOK_BOX + "";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                String type = rs.getString("type");
                String zhName = rs.getString("zh_desc");
                int custom = rs.getInt("custom");
                AddressBox box = new AddressBox(type, zhName, loadAdbListOf(type), custom);
                list.add(box);
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    ArrayList<AddressBookItem> loadAdbListOf(String name) {
        ArrayList<AddressBookItem> addrList = new ArrayList<>();
        try {
            String selectSql = "SELECT * FROM " + TBL_ADDRESSBOOK + " WHERE type = ?  ORDER By id ASC";
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            preStmt.setString(1, name);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                String uid = rs.getString("uid");
                String department = rs.getString("department");
                String path = rs.getString("path");
                String username = rs.getString("username");
                String mail = rs.getString("mail");
                String userlevel = rs.getString("userlevel");
                String securitylevel = rs.getString("securitylevel");
                String note = rs.getString("note");

                AddressBookItem item = new AddressBookItem(
                        uid,
                        department,
                        path,
                        username,
                        mail,
                        userlevel,
                        securitylevel,
                        note);

                addrList.add(item);
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  addrList;
    }

    public void removeAddress(String boxName, ObservableList<AddressBookItem> items) {
        try {
            conn.setAutoCommit(false);
            String sql = "DELETE FROM " + TBL_ADDRESSBOOK + " WHERE  uid = ? AND type = ?";
            System.out.println("DELETE query = " + sql);
            PreparedStatement preStmt = conn.prepareStatement(sql);
            for (AddressBookItem bookItem: items) {
                preStmt.setString(1, bookItem.userIDProperty().getValue());
                preStmt.setString(2, boxName);
                preStmt.addBatch();
            }
            preStmt.executeBatch();
            if (!preStmt.isClosed())
                preStmt.close();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

//        boolean removed = false;
//        AddressBookItem item;
//        try {
//            String insertSql = "DELETE FROM " + TBL_ADDRESSBOOK + " WHERE  uid = ?";
//            PreparedStatement preStmt = conn.prepareStatement(insertSql);
//            preStmt.setString(1, item.userIDProperty().getValue());
//            preStmt.execute();
//            removed = true;
//            if (!preStmt.isClosed())
//                preStmt.close();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        return removed;
    }

    /**
     * update AddressBookItem from the srcBox to the destBox
     * @param destBox
     * @param srcBox
     * @param bookItems
     */
    public void updateAdbItem(String destBox, String srcBox, ObservableList<AddressBookItem> bookItems) {

        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE " + TBL_ADDRESSBOOK + " SET type = ? WHERE  uid = ? AND type = ?";
            System.out.println("UPDATE query = " + sql);
            PreparedStatement preStmt = conn.prepareStatement(sql);
            for (AddressBookItem bookItem: bookItems) {
                preStmt.setString(1, destBox);
                preStmt.setString(2, bookItem.userIDProperty().getValue());
                preStmt.setString(3, srcBox);
                preStmt.addBatch();
            }
            preStmt.executeBatch();
            if (!preStmt.isClosed())
                preStmt.close();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

//        boolean isInsert = false;
//        try {
//
//            String uid = bookItem.userIDProperty().getValue();
//            String insertSql = "UPDATE " + TBL_ADDRESSBOOK + " SET boxName = ? WHERE  uid = ?";
//            PreparedStatement preStmt = conn.prepareStatement(insertSql);
//            preStmt.setString(1, name);
//            preStmt.setString(2, uid);
//            preStmt.execute();
//            if (!preStmt.isClosed())
//                preStmt.close();
//            isInsert = true;
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        return isInsert;
    }

    public void insertAddress(String name, ObservableList<AddressBookItem> copiedItems) {
        try {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO " + TBL_ADDRESSBOOK + "(uid, department, path, username, mail, userlevel, securitylevel, type) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            System.out.println("UPDATE query = " + sql);
            PreparedStatement preStmt = conn.prepareStatement(sql);
            for (AddressBookItem bookItem: copiedItems) {
                preStmt.setString(1, bookItem.userIDProperty().getValue());
                preStmt.setString(2, bookItem.userDepartmentProperty().getValue());
                preStmt.setString(3, bookItem.userPathProperty().getValue());
                preStmt.setString(4, bookItem.userNameProperty().getValue());
                preStmt.setString(5, bookItem.mailAddressProperty().getValue());
                preStmt.setString(6, bookItem.userLevelProperty().getValue());
                preStmt.setString(7, bookItem.userSecurityLevelProperty().getValue());
                preStmt.setString(8, name);
                preStmt.addBatch();
            }
            preStmt.executeBatch();
            if (!preStmt.isClosed())
                preStmt.close();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean insertAddress(String boxType, AddressBookItem bookItem) {

        boolean isInsert = false;
        try {
            String uid = bookItem.userIDProperty().getValue();

            if (checkNewUidFromAdb(uid)) {

                String insertSql = "INSERT INTO " + TBL_ADDRESSBOOK +
                        "(uid, department, path, username, mail, userlevel, securitylevel, note, type)" +
                        " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement preStmt = conn.prepareStatement(insertSql);
                preStmt.setString(1, uid);
                preStmt.setString(2, bookItem.userDepartmentProperty().getValue());
                preStmt.setString(3, bookItem.userPathProperty().getValue());
                preStmt.setString(4, bookItem.userNameProperty().getValue());
                preStmt.setString(5, bookItem.mailAddressProperty().getValue());
                preStmt.setString(6, bookItem.userLevelProperty().getValue());
                preStmt.setString(7, bookItem.userSecurityLevelProperty().getValue());
                preStmt.setString(8, bookItem.userNoteProperty().getValue());
                preStmt.setString(9, boxType);
                preStmt.execute();

                isInsert = true;
                if (!preStmt.isClosed())
                    preStmt.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isInsert;
    }

    /**
     * @author pilot
     * update an AddressBookItem with the boxName and source aItem
     * @param boxName
     * @param aItem
     * @return
     */
    public boolean updateAddress(String boxName, AddressBookItem aItem) {

        boolean isInsert = false;
        try {
            String uid = aItem.userIDProperty().getValue();

            String insertSql = "UPDATE " + TBL_ADDRESSBOOK +
                    " SET " +
                    "department = ?, " +
                    "path = ?, " +
                    "username = ?, " +
                    "mail = ?, " +
                    "userlevel = ?, " +
                    "securitylevel =? , " +
                    "note = ? " +
                    "WHERE  uid = ? AND type = ?";

            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setString(1, aItem.userDepartmentProperty().getValue());
            preStmt.setString(2, aItem.userPathProperty().getValue());
            preStmt.setString(3, aItem.userNameProperty().getValue());
            preStmt.setString(4, aItem.mailAddressProperty().getValue());
            preStmt.setString(5, aItem.userLevelProperty().getValue());
            preStmt.setString(6, aItem.userSecurityLevelProperty().getValue());
            preStmt.setString(7, aItem.userNoteProperty().getValue());
            preStmt.setString(8, uid);
            preStmt.setString(9, boxName);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();
            isInsert = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isInsert;
    }

    public boolean checkNewUidFromAdb(String aUid) {

        boolean isExist = false;
        try {
            String selectSql = "SELECT uid FROM " + TBL_ADDRESSBOOK;
            PreparedStatement preStmt = conn.prepareStatement(selectSql);
            ResultSet rs = preStmt.executeQuery();
            while (rs.next()) {
                String uid = rs.getString("uid");
                if (uid.compareToIgnoreCase(aUid) == 0) {
                    isExist = true;
                    break;
                }
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return !isExist;
    }

    public boolean changeAddressBox(String name, String newName) {
        boolean removed = false;
        try {
            String updateSql = "UPDATE " + TBL_ADDRESSBOOK_BOX + " SET type = ? WHERE type = ?";
            PreparedStatement preStmt = conn.prepareStatement(updateSql);
            preStmt.setString(1, newName);
            preStmt.setString(2, name);
            preStmt.execute();

            updateSql = "UPDATE " + TBL_ADDRESSBOOK + " SET type = ? WHERE type = ?";
            PreparedStatement preStmt1 = conn.prepareStatement(updateSql);
            preStmt1.setString(1, newName);
            preStmt1.setString(2, name);
            preStmt1.execute();

            if (!preStmt.isClosed())
                preStmt.close();
            if (!preStmt1.isClosed())
                preStmt1.close();
            removed = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return removed;
    }

    /**
     * remove an Address box
     * @param name
     * @return
     */
    public boolean removeAddressBox(String name) {
        boolean removed = false;
        try {
            String removeSql = "DELETE FROM " + TBL_ADDRESSBOOK_BOX + " WHERE  type = ?";
            PreparedStatement preStmt = conn.prepareStatement(removeSql);
            preStmt.setString(1, name);
            preStmt.execute();

            String updateSql = "UPDATE " + TBL_ADDRESSBOOK + " SET type = ? WHERE type = ?";
            PreparedStatement preStmt1 = conn.prepareStatement(updateSql);
            preStmt1.setString(1, name);
            preStmt1.setString(2, name);
            preStmt1.execute();
            if (!preStmt.isClosed())
                preStmt.close();
            if (!preStmt1.isClosed())
                preStmt1.close();
            removed = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return removed;
    }

    /**
     * filling for the message uid
     * @param uid
     * @param filterBox
     * @return
     */
    public int insertFilling(String uid, String filterBox) {
        int ret = -1;
        int id = checkNewFilling(uid, filterBox);
        if (id < 0) {
            try {
                String insertSql = "INSERT INTO " + TBL_FILLING + "(uid, box) VALUES (?, ?)";
                PreparedStatement preStmt = conn.prepareStatement(insertSql);
                preStmt.setString(1, uid);
                preStmt.setString(2, filterBox);
                ret = preStmt.executeUpdate();
                if (!preStmt.isClosed())
                    preStmt.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return ret;
    }

    public boolean insertRule(RuleItem item) {

        boolean isInsert = false;
        try {
            String insertSql = "INSERT INTO " + TBL_RULES + "(senders, bsenders_include, receivers, breceivers_include, titles, btitles_include, move_to) VALUES(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setString(1, item.senders.get());
            preStmt.setString(2, ((AddRuleController.RuleInclude)item.includeSenders.get()).rule);
            preStmt.setString(3, item.receivers.get());
            preStmt.setString(4, ((AddRuleController.RuleInclude)item.includeReceivers.get()).rule);
            preStmt.setString(5, item.titles.get());
            preStmt.setString(6, ((AddRuleController.RuleInclude)item.includeTitles.get()).rule);
            preStmt.setString(7, ((MailBox)item.moveTo.get()).getBoxName());
            preStmt.execute();
            isInsert = true;
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isInsert;
    }

    public boolean updateRule(RuleItem item) {

        boolean isInsert = false;
        try {
            String insertSql = "UPDATE " + TBL_RULES + " SET senders = ?, bsenders_include = ?, receivers = ?, breceivers_include = ?, titles = ?, btitles_include =?, move_to = ? WHERE  id = ?";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setString(1, item.senders.get());
            preStmt.setString(2, ((AddRuleController.RuleInclude)item.includeSenders.get()).rule);
            preStmt.setString(3, item.receivers.get());
            preStmt.setString(4, ((AddRuleController.RuleInclude)item.includeReceivers.get()).rule);
            preStmt.setString(5, item.titles.get());
            preStmt.setString(6, ((AddRuleController.RuleInclude)item.includeTitles.get()).rule);
            preStmt.setString(7, ((MailBox)item.moveTo.get()).getBoxName());
            preStmt.setInt(8, item.id.get());
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();
            isInsert = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isInsert;
    }

    public boolean removeRule(int id) {
        boolean removed = false;
        try {
            String removeSql = "DELETE FROM " + TBL_RULES + " WHERE  id = ?";
            PreparedStatement preStmt = conn.prepareStatement(removeSql);
            preStmt.setInt(1, id);
            preStmt.execute();
            removed = true;
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return removed;
    }

    /**
     * load the app Rulues of Settings
     * @return
     */
    public ArrayList<RuleItem> loadRules() {

        ArrayList<RuleItem> rulesList = new ArrayList<>();
        try {
            String selectSql = "SELECT * FROM " + TBL_RULES + " ORDER By id ASC";
            Statement preStmt = conn.createStatement();
            ResultSet rs = preStmt.executeQuery(selectSql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String senders = rs.getString("senders");
                String includeSenders = rs.getString("bsenders_include");
                String receivers = rs.getString("receivers");
                String includeReceivers = rs.getString("breceivers_include");
                String titles = rs.getString("titles");
                String includeTitles = rs.getString("btitles_include");
                String moveTo = rs.getString("move_to");
                ObservableList<MailBox> mailBoxes = GlobalVariables.mainController.mMailBoxList;
                MailBox moveToBox = new MailBox();
                for (MailBox box: mailBoxes) {
                    if (moveTo.equalsIgnoreCase(box.getBoxName())) {
                        moveToBox = box;
                        break;
                    }
                }

                RuleItem item = new RuleItem(
                        senders,
                        new AddRuleController.RuleInclude(includeSenders),
                        receivers,
                        new AddRuleController.RuleInclude(includeReceivers),
                        titles == null?"":titles,
                        new AddRuleController.RuleInclude(includeTitles),
                        moveToBox,
                        id);

                rulesList.add(item);
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  rulesList;
    }

    public boolean updateCheckDuration(int duration) {
        System.out.println(duration);

        boolean isInsert = false;
        try {
            String insertSql = "UPDATE " + TBL_SETTINGS + " SET value = ? WHERE  id = 1";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setInt(1, duration);
            preStmt.execute();
            isInsert = true;
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isInsert;
    }

    public boolean updateSyncAdbDuration(int duration) {
        System.out.println(duration);

        boolean isInsert = false;
        try {
            String insertSql = "UPDATE " + TBL_SETTINGS + " SET value = ? WHERE  id = 2";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setInt(1, duration);
            preStmt.execute();
            isInsert = true;
            if (!preStmt.isClosed())
                preStmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return isInsert;
    }

    public int getNewMailInterval() {
        try {
            String selectSql = "SELECT * FROM " + TBL_SETTINGS + " WHERE id = 1";
            Statement preStmt = conn.createStatement();
            ResultSet rs = preStmt.executeQuery(selectSql);
            int ret = 0;
            while (rs.next()) {
                ret = rs.getInt("value");
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return ret;
        } catch (Exception e) {
            System.out.println("-----------database connection closed-------------");
        }
        return 0;
    }

    public int getSyncAdbInterval() {
        try {
            String selectSql = "SELECT * FROM " + TBL_SETTINGS + " WHERE id = 2";
            Statement preStmt = conn.createStatement();
            ResultSet rs = preStmt.executeQuery(selectSql);
            int val = 0;
            while (rs.next()) {
                val = rs.getInt("value");
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return val;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isSetSyncAdb() {
        try {
            String selectSql = "SELECT * FROM " + TBL_SETTINGS + " WHERE id = 3";
            Statement preStmt = conn.createStatement();
            ResultSet rs = preStmt.executeQuery(selectSql);
            boolean val = false;
            while (rs.next()) {
                val = rs.getInt("value") == 1;
            }
            if (!rs.isClosed())
                rs.close();
            if (!preStmt.isClosed())
                preStmt.close();
            return val;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setSyncAdb(boolean isSet) {
        System.out.println("set sync = " + isSet);

        try {
            String insertSql = "UPDATE " + TBL_SETTINGS + " SET value = ? WHERE  id = 3";
            PreparedStatement preStmt = conn.prepareStatement(insertSql);
            preStmt.setInt(1, isSet == true?1:0);
            preStmt.execute();
            if (!preStmt.isClosed())
                preStmt.close();
            return false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

    public void disconnectDB() {
        try {
            if (!conn.isClosed())
                conn.close();
            conn = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
