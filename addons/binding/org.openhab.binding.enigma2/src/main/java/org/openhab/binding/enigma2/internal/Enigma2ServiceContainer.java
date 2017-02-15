package org.openhab.binding.enigma2.internal;

import java.util.ArrayList;

public class Enigma2ServiceContainer {

    private ArrayList<String> listOfServiceNames = new ArrayList<String>();
    private ArrayList<String> listOfServiceReference = new ArrayList<String>();

    public void add(String serviceName, String serviceReference) {
        serviceName = Enigma2ServiceContainer.cleanString(serviceName);
        if (!contains(serviceName)) {
            listOfServiceNames.add(serviceName);
            listOfServiceReference.add(serviceReference);
        }
    }

    public String get(String serviceName) {
        serviceName = Enigma2ServiceContainer.cleanString(serviceName);
        int index = findIndex(serviceName);
        if (index != -1) {
            return listOfServiceReference.get(findIndex(serviceName));
        } else {
            return null;
        }
    }

    private boolean contains(String name) {
        return (findIndex(name) != -1);
    }

    private int findIndex(String name) {
        for (int i = 0; i < listOfServiceNames.size(); i++) {
            String compareName = listOfServiceNames.get(i);
            if (name.equals(compareName)) {
                return i;
            }
        }
        return -1;
    }

    public static String cleanString(String string) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(string);
        for (int i = buffer.length() - 1; i >= 0; i--) {
            if (!isValidChar(buffer.charAt(i))) {
                buffer.deleteCharAt(i);
            }
        }
        return buffer.toString();
    }

    private static boolean isValidChar(char c) {
        int castChar = c;
        if ((castChar >= 32) && (castChar <= 125)) {
            return true;
        }
        return false;
    }
}
