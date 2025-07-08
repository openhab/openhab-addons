package org.openhab.binding.evcc.internal.discovery;

public class Utils {
    /**
     * 
     */
    public static String sanatizeName(String name) {
        if (!name.matches("[a-zA-Z0-9_-]+")) {
            return name.replaceAll("[^a-zA-Z0-9_-]", "-");
        } else {
            return name;
        }
    }
}
