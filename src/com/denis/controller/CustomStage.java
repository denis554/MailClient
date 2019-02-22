package com.denis.controller;

import com.denis.view.ViewFactory;
import javafx.stage.Stage;

public class CustomStage extends Stage {
    public CustomStage() {
        getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
    }
}
