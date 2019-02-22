package com.denis.model;

import javafx.beans.property.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.mail.Address;
import javax.mail.Message;

import java.util.Base64;

import static com.denis.controller.AbstractController.convertFrom;
import static com.denis.model.GlobalVariables.GlobalVariables.*;

public class MailItem implements Comparable<MailItem> {
    private StringProperty secLevel;
    private StringProperty from;
    private StringProperty to;
    private StringProperty subject;
    private StringProperty receivedDate;
    private StringProperty sentDate;
    private StringProperty size;
    private BooleanProperty attach;
    private BooleanProperty star;
    private BooleanProperty selected;

    public String uid = "";
    public String boxName = "";
    public boolean unread = false;
    public String content = "";
    public Message referenceMsg;
    public int fillingId = -1;

    public static final int NOT_REPLIED = 0;
    public static final int REPLIED = NOT_REPLIED + 1;
    public static final int FORWARDED = REPLIED + 1;
    public static final int REPLY_AND_FORWARD = FORWARDED + 1;

    public static final int APP_STAT_NO_APPROVE = 0; //no approver
    public static final int APP_STAT_PENDING = 10; //pending
    public static final int APP_STAT_APPROVED = 20; //approved
    public static final int APP_STAT_REJECTED = 30; //rejected
    public static final int APP_STAT_CANCELED = 40; //canceled

    /**
     * set the filtering id
     * @param id
     */
    public void setFillId(int id) {
        fillingId = id;
    }

    public boolean isHandledOfApprove() {
        try {
            String[] traceId = referenceMsg.getHeader(getMuidFromMsg(referenceMsg) + "-" + MAIL_APPROVE_HEADER_INFO);
            if (traceId != null && traceId.length > 0) {
                String res = new String(Base64.getDecoder().decode(traceId[0]));
                JSONParser jsonParser = new JSONParser();
                JSONObject jobj = (JSONObject) jsonParser.parse(res);
                return Integer.parseInt(jobj.get("status").toString()) >= APP_STAT_NO_APPROVE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getApproveStatus(int aStat) {
        try {
            String[] traceId = referenceMsg.getHeader(MAIL_MUID_HEADER + "-" + MAIL_APPROVE_HEADER_INFO);
            if (traceId != null && traceId.length > 0) {
                String res = traceId[0];
                JSONParser jsonParser = new JSONParser();
                JSONObject jobj = (JSONObject) jsonParser.parse(res);
                return Integer.parseInt(jobj.get("status").toString()) == aStat;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isApproveMail() {
        try {
            String[] approveFlag = referenceMsg.getHeader(MAIL_APPROVE_HEADER_FLAG);
            return approveFlag != null && approveFlag.length > 0 && Boolean.parseBoolean(approveFlag[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isApproveMail(Message msg) {
        try {
            String[] approveFlag = msg.getHeader(MAIL_APPROVE_HEADER_FLAG);
            return approveFlag != null && approveFlag.length > 0 && Boolean.parseBoolean(approveFlag[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isImportantMail(Message message) {
        try {
            String[] impFlag = message.getHeader(MAIL_IMPORTANT_HEADER);
            return impFlag != null && impFlag.length > 0 && Boolean.parseBoolean(impFlag[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean getStatOf(Message message, int stat) {
        try {
            String[] header = message.getHeader(MAIL_ANSWER_STAT_HEADER);
            if (header != null && header.length > 0)
                return Integer.parseInt(header[0]) == stat;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isImportantMail() {
        try {
            String[] impFlag = referenceMsg.getHeader(MAIL_IMPORTANT_HEADER);
            return impFlag != null && impFlag.length > 0 && Boolean.parseBoolean(impFlag[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUid() {
        return this.uid;
    }

    public static String getUidFromMsg(Message msg) {
        try {
            return  msg.getHeader(MAIL_CUSTOM_UUID_HEADER)[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getMuidFromMsg(Message msg) {
        try {
            return  msg.getHeader(MAIL_MUID_HEADER)[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getAllToString() {
        String to = "";
        try {
            Address[] addresslist = referenceMsg.getRecipients(Message.RecipientType.TO);
            if (addresslist != null && addresslist.length > 0) {
                for (int i = 0; i < addresslist.length; i++) {
                    to = addresslist[i].toString();
                    if (i > 0) {
                        to += ", " + addresslist[i].toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return to;
    }

    public MailItem(String uid, Message msg, String boxName) {
        this.uid = uid;
        this.boxName = boxName;
        this.referenceMsg = msg;
    }

    public MailItem(String uid, String boxName, String secLevel, String from, String to, String subject, String receivedDate, String size, Boolean attach, Boolean star, String content, boolean unread, Message referenceMsg) {
        this.secLevel = new SimpleStringProperty(secLevel);
        this.from = new SimpleStringProperty(from);
        this.to = new SimpleStringProperty(to);
        this.subject = new SimpleStringProperty(subject);
        this.receivedDate = new SimpleStringProperty(receivedDate);
        this.sentDate = new SimpleStringProperty(receivedDate);
        this.size = new SimpleStringProperty(size);
        this.attach = new SimpleBooleanProperty(attach);
        this.star = new SimpleBooleanProperty(star);
        this.unread = unread;
        this.content = content;
        this.uid = uid;
        this.boxName = boxName;
        this.referenceMsg = referenceMsg;
        setSelect(false);
    }

    public void copyFrom(Message message) {
        MailItem item = convertFrom(boxName, message, uid);
        this.secLevel = item.secLevel;
        this.from = item.from;
        this.to = item.to;
        this.subject = item.subject;
        this.receivedDate = item.receivedDate;
        this.sentDate = item.sentDate;
        this.size = item.size;
        this.attach = item.attach;
        this.star = item.star;
        this.content = item.content;
        this.referenceMsg = item.referenceMsg;
        this.unread = item.unread;
        setSelect(false);
    }

    public StringProperty secLevelProperty() { return secLevel; }
    public StringProperty fromProperty() { return from; }
    public StringProperty toProperty() { return to; }
    public StringProperty subjectProperty() { return subject; }
    public StringProperty receivedDateProperty() { return receivedDate; }
    public StringProperty sentDateProperty() { return sentDate; }
    public StringProperty sizeProperty() { return size; }
    public BooleanProperty attachProperty() { return attach; }
    public BooleanProperty starProperty() { return star; }
    public BooleanProperty selectedProperty() { return selected; }

    public void setSelect(boolean select){
        this.selected = new SimpleBooleanProperty(select);
    }

    public void setStar(boolean follow){
        this.star = new SimpleBooleanProperty(follow);
    }

    @Override
    public String toString() {
        return "from : <" +
                from.getValue() + ">, to : <" +
                to.getValue() + ">, subject : " +
                subject.getValue() + ", received date : " +
                receivedDate.getValue() + ", sent date : " +
                sentDate.getValue() + ", secLevel : " +
                secLevel.getValue();
    }

    public void setImportant(boolean isImportant) {
        try {
            referenceMsg.setHeader(MAIL_IMPORTANT_HEADER, "" + isImportant);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(MailItem cmp1) {
        try {
            return cmp1.referenceMsg.getSentDate().compareTo(this.referenceMsg.getSentDate());
        } catch (Exception e) {
        }
        return 0;
    }
}