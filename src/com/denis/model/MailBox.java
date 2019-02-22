package com.denis.model;

import com.denis.controller.AbstractController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.mail.Flags;
import javax.mail.Message;
import java.util.Locale;

import static com.denis.controller.AbstractController.convertFrom;

public class MailBox {

    private int id;
    private String type;
    private String zhName;
//    private ObservableList<Pair<Integer, Message>> msglist;
    private ObservableList<MailItem> msglist;
    private boolean isCustom;

    public MailBox() {
        this.type = "";
        this.isCustom = true;
    }

//    public MailBox(String name, ObservableList<Pair<Integer, Message>> msglist) {
    public MailBox(String name, ObservableList<MailItem> msglist) {
        this.type = name;
        this.msglist = msglist;
        this.isCustom = true;
    }

//    public MailBox(int id, String name, String zhName, ObservableList<Pair<Integer, Message>> msglist, int custom) {
    public MailBox(int id, String name, String zhName, ObservableList<MailItem> msglist, int custom) {
        this.id = id;
        this.type = name;
        this.zhName = zhName;
        this.msglist = msglist;
        this.isCustom = custom == 1?true:false;
    }

    public int getUnreadCount() {
        int iCnt = 0;
//        for (Pair<Integer, Message> item: msglist) {
        for (MailItem item: msglist) {
            try {
                if (!item.referenceMsg.isSet(Flags.Flag.SEEN))
                    iCnt++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return iCnt;
    }

    @Override
    public String toString() {
        Locale locale = AbstractController.getLocale();
        try {
            String ret = AbstractController.getString(type);
            if (locale.equals(Locale.CHINA))
                return (ret == null || ret.isEmpty())?type:ret;
            else
                return ret;

        } catch (Exception e) {
            System.out.println("no exist resource = ");
        }
        return type;
    }

    /**
     * clear selection status
     */
    public void clearSelection() {
        if (this.msglist == null || this.msglist.size() == 0)
            return;
        for (MailItem item: msglist) {
            item.setSelect(false);
        }
    }

    public ObservableList<MailItem> getMsgList() {
        return msglist;
    }

    /**
     * add a message with filling id
     * @param msg
     * @param fillingId
     * @return
     */
    public int addMessage (Message msg, int fillingId) {

        if (this.msglist == null)
            this.msglist = FXCollections.observableArrayList();

        int addIndex = -1;
        try {
            long sendDate = msg.getSentDate().getTime();
            for (int i = 0; i < msglist.size(); i ++) {
                if (sendDate > msglist.get(i).referenceMsg.getSentDate().getTime() ) {
                    MailItem item = convertFrom(getBoxName(), msg, MailItem.getUidFromMsg(msg));
                    item.setFillId(fillingId);
                    msglist.add(i, item);
                    addIndex = i;
                    break;
                }
            }
            if (addIndex == -1) {
                addIndex = msglist.size();
                MailItem item = convertFrom(getBoxName(), msg, MailItem.getUidFromMsg(msg));
                item.setFillId(fillingId);
                msglist.add(item);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return addIndex;
    }

    public String getBoxName() {
        return type;
    }

    public void changeBoxName (String name) {
        this.type = name;
    }

    /**
     * remove a message from the message list
     * @param item
     */
    public void removeMsg(MailItem item) {

        if (this.msglist == null || this.msglist.size() == 0)
            return;

        int i = 0;
        boolean isExist = false;
        for (MailItem cmp2: msglist) {
            String uid = MailItem.getUidFromMsg(cmp2.referenceMsg);
            if (item.getUid().compareToIgnoreCase(uid) == 0) {
                isExist = true;
                break;
            }
            i++;
        }
        if (isExist)
            this.msglist.remove(i);
    }

    /**
     * remove all items from message list
     * @param mailItems
     */
    public void removeAllMsg(ObservableList<MailItem> mailItems) {

        if (this.msglist == null)
            this.msglist = FXCollections.observableArrayList();

        for (MailItem cmp1: mailItems) {
            int i = 0;
            boolean isExist = false;
            for (MailItem cmp2: msglist) {
                String uid = MailItem.getUidFromMsg(cmp2.referenceMsg);
                if (cmp1.getUid().compareToIgnoreCase(uid) == 0) {
                    isExist = true;
                    break;
                }
                i++;
            }
            if (isExist)
                this.msglist.remove(i);
        }

    }

    /**
     * add all the messages
     * @param aMailItems
     */
    public void addAllMessage(ObservableList<MailItem> aMailItems) {
        if (this.msglist == null)
            this.msglist = FXCollections.observableArrayList();

        ObservableList<MailItem> newItems = FXCollections.observableArrayList();
        for (MailItem item1: aMailItems) {
            item1.setSelect(false);
            boolean isExist = false;
            for (MailItem item2: msglist) {
                if (item1.getUid().compareToIgnoreCase(item2.getUid()) == 0) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist)
                newItems.add(item1);
        }
        this.msglist.addAll(newItems);
    }

    public static MailBox getTrashBox(ObservableList<MailBox> mailBoxes) {

        for (MailBox box: mailBoxes) {
            if (box.getBoxName().equalsIgnoreCase("trash"))
                return box;
        }

        return null;

    }

    public boolean isCustomBox() {
        return isCustom;
    }

    /**
     * update the message for the uid
     * @param uid
     * @param message
     */
    public void updateMessage(String uid, Message message) {

        if (this.msglist == null || this.msglist.size() == 0)
            return;

        try {
            int index = 0;
            int curSel = -1;
            for (MailItem item: msglist) {
                String cmpUid = MailItem.getUidFromMsg(item.referenceMsg);
                if (Long.parseLong(uid) == Long.parseLong(cmpUid)) {
                    curSel = index;
                    break;
                }
                index++;
            }
            if (curSel >= 0) {
//                msglist.remove(curSel);
                msglist.get(curSel).copyFrom(message);
//                item = convertFrom(getBoxName(), message, uid);
//                item.setFillId(fillingId);
//                msglist.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTrash() {
        return type.equalsIgnoreCase("trash");
    }

}