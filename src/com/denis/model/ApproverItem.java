package com.denis.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ApproverItem implements Comparable<ApproverItem> {
    private StringProperty aname;
    private StringProperty auc;
    private IntegerProperty auid;

    public ApproverItem(String aname, String auc, int auid) {
        this.aname = new SimpleStringProperty(aname);
        this.auc = new SimpleStringProperty(auc);
        this.auid = new SimpleIntegerProperty(auid);
    }

    public StringProperty anameProperty() { return aname; }
    public StringProperty aucProperty() { return auc; }
    public IntegerProperty auidProperty() { return auid; }

    @Override
    public String toString() {
        return anameProperty().getValue();
    }

    @Override
    public int compareTo(ApproverItem cmp) {
        return this.anameProperty().getValue().compareTo(cmp.anameProperty().getValue());
    }
}