package org.openhab.binding.gmailparadoxparser.gmail.adapter;

import java.util.HashMap;
import java.util.Map;

public class MailParser {

    private static MailParser instance;
    private MyLogger logger = MyLogger.getInstance();

    private MailParser() {

    }

    public static MailParser getInstance() {
        if (instance == null) {
            instance = new MailParser();
        }
        return instance;
    }

    public Map<String, String> parseToMap(String[] lines) {
        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < lines.length; i++) {
            String[] split = lines[i].split(": ");
            if (split.length > 1) {
                result.put(split[0], split[1]);
                logger.logDebug("Key: " + split[0] + "\tValue: " + split[1]);
            } else {
                logger.logDebug("Message cannot be split: " + lines[i]);
            }

        }

        return result;
    }
}
