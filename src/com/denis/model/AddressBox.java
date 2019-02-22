package com.denis.model;

import com.denis.controller.AbstractController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Locale;

public class AddressBox {

    String type;
    private String zhName;
    ArrayList<AddressBookItem> addrlist = new ArrayList<>();
    boolean isCustom;

    public AddressBox(String name, ArrayList<AddressBookItem> addrlist) {
        this.type = name;
        this.addrlist = addrlist;
        this.isCustom = true;
    }

    public AddressBox(String name, String zhName, ArrayList<AddressBookItem> addrlist, int custom) {
        this.type = name;
        this.zhName = zhName;
        this.addrlist = addrlist;
        this.isCustom = custom == 1?true:false;
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
            System.out.println("no exist resource = " + AbstractController.getStackTrace(e));
        }
        return type;
    }

    public String getBoxName() {
        return type;
    }

    public void changeBoxName(String name) {
        this.type = name;
    }

    public ArrayList<AddressBookItem> getAddrlist() {
        return addrlist;
    }

    public ArrayList<String> getAdblist() {
        ArrayList<String> aryAdb = new ArrayList<>();
        for (AddressBookItem item: addrlist) {
            aryAdb.add(AbstractController.getFormattedEmailString(item.mailAddressProperty().getValue()));
        }
        return aryAdb;
    }

    public void addAdbItem(AddressBookItem item) {
        boolean isExist = false;
        for (AddressBookItem cmpItem: addrlist) {
            if (item.mailAddressProperty().getValue().toLowerCase().trim().compareToIgnoreCase(cmpItem.mailAddressProperty().getValue().toLowerCase().trim()) == 0) {
                isExist = true;
                break;
            }
        }
        if (!isExist)
            addrlist.add(item);
    }

    public boolean isCustomBox() {
        return isCustom;
    }

    public ObservableList<AddressBookItem> adbCopyFrom(ObservableList<AddressBookItem> addressItems) {
        ObservableList<AddressBookItem> copiedItems = FXCollections.observableArrayList();
        try {
            for (AddressBookItem item: addressItems) {
                boolean isExist = false;
                for (AddressBookItem cmpItem: addrlist) {
                    if (item == null)
                        break;
                    String cmp1 = item.mailAddressProperty().getValue().toLowerCase().trim();
                    String cmp2 = cmpItem.mailAddressProperty().getValue().toLowerCase().trim();
                    if (cmp1.compareToIgnoreCase(cmp2) == 0) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist && item != null) {
                    AddressBookItem newItem = new AddressBookItem(item);
                    addrlist.add(newItem);
                    copiedItems.add(newItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return copiedItems;
    }

    public void addAdbItem(ObservableList<AddressBookItem> selectedAdbItems) {
        addrlist.addAll(selectedAdbItems);
    }

    /**
     * update a address book item in address book list
     * @param aItem
     */
    public boolean updateAdbItem(AddressBookItem aItem) {
        if (aItem == null || addrlist.size() == 0)
            return false;
        boolean isUpdated = false;
        for (AddressBookItem item: addrlist) {
            if (item.userIDProperty().getValue().compareToIgnoreCase(aItem.userIDProperty().getValue()) == 0) {
                isUpdated = item.copyFrom(aItem);
                break;
            }
        }
        return isUpdated;
    }

    /**
     * remove the address book items in address book list
     * @param noExistList
     */
    public void removeItems(ObservableList<AddressBookItem> noExistList) {
        if (noExistList == null || noExistList.size() == 0 || addrlist.size() == 0)
            return;
        for (AddressBookItem noItem: noExistList) {
            boolean isExist = false;
            int index = 0;
            for (AddressBookItem orgItem: addrlist) {
                if (noItem.userIDProperty().getValue().compareToIgnoreCase(orgItem.userIDProperty().getValue()) == 0) {
                    isExist = true;
                    break;
                }
                index++;
            }
            if (isExist)
                addrlist.remove(index);
        }
    }

    public boolean isPublic() {
        return  type.equalsIgnoreCase("public");
    }

    public void clearSelection() {
        for (AddressBookItem orgItem: addrlist) {
            orgItem.setSelect(false);
        }
    }
}
