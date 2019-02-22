package com.denis.model;

import com.denis.controller.AbstractController;
import com.denis.controller.AddRuleController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class RuleItem {
    public SimpleIntegerProperty id;
    public SimpleStringProperty senders;
    public SimpleObjectProperty includeSenders;
    public SimpleStringProperty receivers;
    public SimpleObjectProperty includeReceivers;
    public SimpleStringProperty titles;
    public SimpleObjectProperty includeTitles;
    public SimpleObjectProperty moveTo;
    public String errorMsg;

    public RuleItem(String senders, AddRuleController.RuleInclude includeSenders, String receivers, AddRuleController.RuleInclude includeReceivers, String titles, AddRuleController.RuleInclude includeTitles, Object moveTo, int id) {

        this.senders = new SimpleStringProperty(senders);
        this.includeSenders = new SimpleObjectProperty(includeSenders);
        this.receivers = new SimpleStringProperty(receivers);
        this.includeReceivers = new SimpleObjectProperty(includeReceivers);
        this.titles = new SimpleStringProperty(titles);
        this.includeTitles = new SimpleObjectProperty(includeTitles);
        this.moveTo = new SimpleObjectProperty(moveTo);
        this.id = new SimpleIntegerProperty(id);
    }

    public Integer getId() {
        return id.get();
    }
    public String getDescription() {
        return AbstractController.getString("Senders") + ": " + senders.get() + ", " +
                AbstractController.getString("Receivers") + ": " + receivers.get() + ", " +
                AbstractController.getString("Titles") + ": " + titles.get() + ", " +
                AbstractController.getString("Move_To") + ": " + moveTo.get();
    }

    public boolean validate() {
        if (!validateMultipleEmails(senders.get())) {
            errorMsg = AbstractController.getString("no_senders");
            return false;
        }
        if (!validateMultipleEmails(receivers.get())) {
            errorMsg = AbstractController.getString("no_receivers");
            return false;
        }
        if (senders.get().trim().isEmpty() && receivers.get().trim().isEmpty() && titles.get().trim().isEmpty()) {
            errorMsg = AbstractController.getString("no_either");
            return false;
        }
        return  true;
    }

    public boolean validateMultipleEmails(String emails){
        EmailValidator validator = new EmailValidator();
        emails = emails.trim();
        if (emails.isEmpty()) {
            return true;
        }
        String result[] = emails.trim().split("\\s*,\\s*");
        for (String o : result) {
            if (!validator.validate(o)){
                return false;
            }
        }
        return  true;
    }
}