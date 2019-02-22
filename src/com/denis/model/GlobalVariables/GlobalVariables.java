package com.denis.model.GlobalVariables;

import com.denis.controller.ComposeMailController;
import com.denis.controller.PrototypeController;
import com.denis.controller.persistence.ValidAccount;
import com.denis.controller.services.EmailSenderService;
import com.denis.model.SecurityLevel;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import javax.mail.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class GlobalVariables {

    public final static String APP_NAME = "KMailClient";
    public final static String APP_REG_KEY = "SOFTWARE\\KMailClient";
    public final static String APP_DB_PATH_KEY = "Default_Database_Path";
    public final static String APP_DB_NAME_KEY = "Default_Database_Name";
    public final static String BASE_DB_NAME = "base.db";
    public static String DB_NAME = "xmail.db";

    public static ValidAccount account;
    public static ValidAccount accountInfo;
    public static ObservableList<SecurityLevel> securityList;
    public static ArrayList<SecurityLevel> userLevelList;
    public static LinkedList<ComposeMailController> composeMailControllers = new LinkedList<>();
    public static LinkedList<ComposeMailController> needSaveDrafts = new LinkedList<>();
    public static HashMap<String, LinkedList<EmailSenderService.AttachFile>> attachFiles = new HashMap<>();
    public static HashMap<String, Message> HandlingPop3Msg = new HashMap<>();

    public static HashMap<String, String> userSecurityLevelList = new HashMap<String, String>(){{
        put("普通", "#00FF36");
        put("一般", "#31A5FF");
        put("重要", "#222EFF");
        put("核心", "#00067D");
    }};

    public static String sysTitle;
    public static SecurityLevel sysSecurityLevel;
    public static PrototypeController mainController;
    public static Stage primaryStage;
    public static String MAIL_CUSTOM_UUID_HEADER = "custom-uuid";
    public static String MAIL_MUID_HEADER = "mail-traced-identifier";
    public static String MAIL_ATTACH_SEC_LEVEL_HEADER = "mail_attachment_security_level_identifier";
    public static String MAIL_SEC_LEVEL_HEADER = "mail_security_level_identifier";
    public static String MAIL_IMPORTANT_HEADER = "mail-important-identifier";
    public static String MAIL_DIPOSITION_TO_HEADER = "Disposition-Notification-To";

    //mail log headers
    public static String MAIL_LOG_MUID_HEADER = "muid";
    public static String MAIL_LOG_SUBJECT_HEADER = "mailSubject";
    public static String MAIL_LOG_SEC_LEVEL_HEADER = "msl";
    public static String MAIL_LOG_TO_HEADER = "to";
    public static String MAIL_LOG_CC_HEADER = "cc";
    public static String MAIL_LOG_BCC_HEADER = "bcc";
    public static String MAIL_LOG_IMPORTANT_HEADER = "importantMail";
    public static String MAIL_LOG_ATTACH_HEADER = "attachments";
    public static String MAIL_LOG_ATTACH_NAME_HEADER = "aname";
    public static String MAIL_LOG_ATTACH_SIZE_HEADER = "asize";
    public static String MAIL_LOG_ATTACH_SEC_LEVEL_HEADER = "asl";
    public static String MAIL_LOG_APPROVAL_HEADER = "approvalMail";
    public static String MAIL_LOG_APPROVERS_HEADER = "approvers";

    public static String MAIL_APPROVE_HEADER_ORG_TO = "X-KM-ORIG-TO";
    public static String MAIL_APPROVE_HEADER_FLAG = "X-KM-APPROVE-FLAG";
    public static String MAIL_APPROVE_HEADER_INFO = "X-KM-APPROVE-INFO";
    public static String MAIL_APPROVE_HEADER_DESC = "X-KM-APPLICATION-DESC";
    public static String MAIL_APPROVE_HEADER_EXC = "X-KM-APPROVE-EXCLUDE";

    public static String MAIL_ANSWER_STAT_HEADER = "answer-stat";
    public static String APP_DATA_DIR = "";
    public static String APP_DOWNLOAD_DIR = "download";
    public static String MAIN_JAR_NAME = "main.jar";
    public static String APP_ATTACH_FILE = "attach.lock";
    public static String LOG_FILE = "log.txt";
    public static String CONF_FILE = "config.txt";
    public static String APP_PID_DIR = "";
    public static boolean isRegApp = false;

    public static final int TRAY_TYPE_NEW_MAIL = 0;
    public static final int TRAY_TYPE_APPROVE = TRAY_TYPE_NEW_MAIL + 1;
    public static final int TRAY_TYPE_UPDATE = TRAY_TYPE_APPROVE + 1;
    public static final int TRAY_TYPE_NEW_APPROVE = TRAY_TYPE_UPDATE + 1;
    public static final int TRAY_TYPE_SENDING_MAIL = TRAY_TYPE_NEW_APPROVE + 1;

    public static boolean IS_WORKING_INBOX = false;
    public static boolean IS_WORKING_APPROVE = false;
    public static boolean IS_WORKING_PENDING = false;
    public static boolean IS_WORKING_TRAY = false;
    public static boolean IS_WORKING_ADB = false;
    public static boolean IS_WORKING_APP_UPDATE = false;
    public static boolean IS_WORKING_FOLDER_UPDATE = false;

    public static boolean isExistDrafts() {
        return needSaveDrafts.size() > 0;
    }

    /**
     * remove all no needed drafts
     */
    public static void removeAllNoNeedDrafts() {
        if (composeMailControllers.size() == 0)
            return;
        ComposeMailController ctl = composeMailControllers.poll();
        if (!ctl.isCloseableWithoutSave()) {
            needSaveDrafts.add(ctl);
            removeAllNoNeedDrafts();
        } else {
            removeAllNoNeedDrafts();
            Platform.runLater(()->{
                ctl.closeComplete();
            });
        }
    }

    /**
     * get the attach file list for the uid
     * @param uid
     * @return
     */
    public static LinkedList<EmailSenderService.AttachFile> getAttachFiles(String uid) {
        return attachFiles.get(uid);
    }

    /**
     * close the draft
     */
    public static void askSaveDraft() {
        ComposeMailController ctl = needSaveDrafts.poll();
        if (ctl != null) {
            ctl.close();
        }
    }

    /**
     * attach a file for the uid
     * @param uid
     * @param file
     */
    public static void addAttachFile(String uid, EmailSenderService.AttachFile file) {
        LinkedList<EmailSenderService.AttachFile> list = attachFiles.get(uid);
        if (list == null)
            list = new LinkedList<>();
        list.add(file);
        attachFiles.put(uid, list);
    }

    /**
     * clear the attach file list for the uid
     * @param uid
     */
    public static void clearAttachFiles(String uid) {
        LinkedList<EmailSenderService.AttachFile> list = attachFiles.get(uid);
        if (list == null)
            return;
        list.clear();
        attachFiles.put(uid, list);
    }
}
