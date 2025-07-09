package org.openhab.binding.evcc.internal.discovery;

public class Utils {
    /**
     * 
     */
    public static String sanatizeName(String name) {
        if (!name.matches("[a-zA-Z0-9_-]+")) {
            return name.replaceAll("ß", "ss").replaceAll("ä", "ae").replaceAll("ü", "ue").replaceAll("ö", "oe")
                    .replaceAll("[^a-zA-Z0-9_-]", "-");
        } else {
            return name;
        }
    }
}
