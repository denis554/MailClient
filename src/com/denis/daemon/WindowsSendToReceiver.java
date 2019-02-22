package com.denis.daemon;

import javafx.application.Platform;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import static com.denis.App.globalPrototypeController;

public class WindowsSendToReceiver {
    private static boolean started = false;
    public static void start() {
        if(started) {
            return;
        }
        new Thread(()-> {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(12600);
                while(true) {
                    Socket s = ss.accept();
                    newConnection(s);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    ss.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void newConnection(Socket s) throws IOException {
        System.out.println("----newConnection-----");
        new Thread(()-> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream(), "utf-8"));
                String filename = reader.readLine();
                System.out.println("filename " + filename);
                List<File> files = new LinkedList<File>();
                if(filename!=null && filename.trim().length()!=0) {
                    if(filename.startsWith("\"") && filename.endsWith("\"")) {
                        files.add(new File(filename.substring(1, filename.length()-1)));
                    } else {
                        files.add(new File(filename));
                    }
                    Platform.runLater(()->globalPrototypeController.showNewMailSceneWithAttachments(files));

                }
            } catch(Throwable t) {
                t.printStackTrace();
            } finally {
                try {
                    s.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



}
