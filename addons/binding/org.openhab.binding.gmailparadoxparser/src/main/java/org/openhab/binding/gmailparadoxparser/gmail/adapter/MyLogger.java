package org.openhab.binding.gmailparadoxparser.gmail.adapter;

public class MyLogger {
    private static MyLogger logger;

    private MyLogger() {

    }

    public static MyLogger getInstance() {
        if (logger == null) {
            logger = new MyLogger();
        }
        return logger;
    }

    public void logDebug(String message) {
        System.out.println("DEBUG: " + message);
    }
}
