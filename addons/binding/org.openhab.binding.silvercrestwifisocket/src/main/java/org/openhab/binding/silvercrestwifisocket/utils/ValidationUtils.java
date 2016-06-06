package org.openhab.binding.silvercrestwifisocket.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitary static class to perform some validations.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // avoid instantiation.
    }

    public static final String MAC_PATTERN = "^([0-9A-Fa-f]{2}[:-]*){5}([0-9A-Fa-f]{2})$";

    /**
     * Validates if one Mac address is valid.
     *
     * @param mac the mac, with or without :
     * @return true if is valid.
     */
    public static boolean isMacValid(final String mac) {
        Pattern pattern = Pattern.compile(ValidationUtils.MAC_PATTERN);
        Matcher matcher = pattern.matcher(mac);
        return matcher.matches();
    }

    /**
     * Validates if one Mac address is not valid.
     *
     * @param mac the mac, with or without :
     * @return true if is not valid.
     */
    public static boolean isMacNotValid(final String macAddress) {
        return !isMacValid(macAddress);
    }

}
