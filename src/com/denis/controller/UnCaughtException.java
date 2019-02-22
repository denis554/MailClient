package com.denis.controller;

import com.denis.App;

public class UnCaughtException extends Throwable implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        App.trackLog("UnCaughtException Thread = " + t.getName(), true);
        App.trackLog(AbstractController.getStackTrace(e), true);
    }
}
