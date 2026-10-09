/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.miio.internal;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.BINDING_USERDATA_PATH;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class for common tasks within the Xiaomi vacuum binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public final class Utils {

    /** Maximum number of characters of a response or other (potentially large) payload written to the log */
    static final int MAX_LOG_LENGTH = 2000;

    private static final String SECRET_KEYS = "token|bindkey|bind_key|ssecurity|serviceToken|passToken";
    private static final String LOCATION_KEYS = "longitude|latitude";
    private static final Pattern SECRET_NAME = Pattern.compile(SECRET_KEYS, Pattern.CASE_INSENSITIVE);
    // a Json member with a secret value; the quotes may be escaped when the Json is part of a string
    private static final Pattern SECRET_MEMBER = Pattern
            .compile("(\\\\?\"(?:" + SECRET_KEYS + ")\\\\?\"\\s*:\\s*\\\\?\")([^\"\\\\]*)", Pattern.CASE_INSENSITIVE);
    // a Json member with a location value, which may be a quoted string or a plain number
    private static final Pattern LOCATION_MEMBER = Pattern.compile(
            "(\\\\?\"(?:" + LOCATION_KEYS + ")\\\\?\"\\s*:\\s*\\\\?\"?)([^\"\\\\,}\\]]*)", Pattern.CASE_INSENSITIVE);

    // a Json member with the url the login continues at; the parameters of the url give access to the account
    private static final Pattern LOGIN_URL_MEMBER = Pattern.compile(
            "(\\\\?\"location\\\\?\"\\s*:\\s*\\\\?\"[^\"?]*\\?)((?:[^\"\\\\]|\\\\[^\"])*)", Pattern.CASE_INSENSITIVE);

    /**
     * Convert a string representation of hexadecimal to a byte array.
     *
     * For example: String s = "00010203" returned byte array is {0x00, 0x01, 0x03}
     *
     * @param hex hex input string
     * @return byte array equivalent to hex string
     **/
    public static byte[] hexStringToByteArray(String hex) {
        String s = hex.replace(" ", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static final String HEXES = "0123456789ABCDEF";

    /**
     * Convert a byte array to a string representation of hexadecimals.
     *
     * For example: byte array is {0x00, 0x01, 0x03} returned String s =
     * "00 01 02 03"
     *
     * @param raw byte array
     * @return String equivalent to hex string
     **/
    public static String getSpacedHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(3 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F))).append(" ");
        }
        hex.delete(hex.length() - 1, hex.length());
        return hex.toString();
    }

    public static String getHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static String obfuscateToken(String tokenString) {
        if (tokenString.length() > 4) {
            return tokenString.substring(0, 4)
                    .concat((tokenString.length() < 24) ? tokenString.substring(4).replaceAll(".", "*")
                            : tokenString.substring(4, 24).replaceAll(".", "*").concat(tokenString.substring(24)));
        } else {
            return tokenString;
        }
    }

    /**
     * Masks a value like a cookie when its name shows that it is a token or key, so that it can be written to a log
     * that may be shared.
     *
     * @param name the name of the value
     * @param value the value
     * @return the value, masked when the name is the one of a secret
     */
    public static String maskSecretValue(String name, String value) {
        return SECRET_NAME.matcher(name).find() ? obfuscateToken(value) : value;
    }

    /**
     * Masks the parameters of an url, as the parameters of the Xiaomi login urls give access to the account.
     *
     * @param url the url
     * @return the url with the parameters masked
     */
    public static String maskUrl(String url) {
        int query = url.indexOf('?');
        return query < 0 || query == url.length() - 1 ? url : url.substring(0, query + 1) + "***";
    }

    /**
     * Masks the values of tokens, keys, the location and the parameters of the login url in the Json members of a
     * text, so that it can be written to a log that may be shared. Text without such members is returned unchanged.
     *
     * @param text the text, typically a Json response
     * @return the text with the values of the sensitive members masked
     */
    public static String maskSecrets(String text) {
        String masked = text;
        if (masked.indexOf(':') >= 0) {
            masked = replaceValues(SECRET_MEMBER, masked, true);
            masked = replaceValues(LOCATION_MEMBER, masked, false);
            masked = replaceValues(LOGIN_URL_MEMBER, masked, false);
        }
        return masked;
    }

    private static String replaceValues(Pattern pattern, String text, boolean obfuscate) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String value = obfuscate ? obfuscateToken(matcher.group(2)) : (matcher.group(2).isEmpty() ? "" : "***");
            matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(1) + value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Limits the length of a text to be logged. The end of a longer text is replaced by a note with the original
     * length.
     *
     * @param text the text
     * @param maxLength maximum number of characters to keep
     * @return the (shortened) text
     */
    static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "... [truncated, " + text.length() + " characters in total]";
    }

    /**
     * Prepares a value, like a response of a device or the cloud, to be passed as argument to a debug or higher level
     * log statement: secrets are masked and the length is limited to {@link #MAX_LOG_LENGTH} characters. The text is
     * only created when the log statement is actually written, so there is no cost when the log level is disabled.
     * Trace logging uses {@link #maskSecrets(String)} instead, so that the complete value remains available.
     *
     * @param value the value to log
     * @return an object to be used as log argument
     */
    public static Object sanitizeForLog(@Nullable Object value) {
        return new Object() {
            @Override
            public String toString() {
                return truncate(maskSecrets(String.valueOf(value)), MAX_LOG_LENGTH);
            }
        };
    }

    public static JsonObject convertFileToJSON(URL fileName) throws JsonIOException, JsonSyntaxException,
            JsonParseException, IOException, URISyntaxException, NoSuchFileException {
        JsonObject jsonObject = new JsonObject();
        try (InputStream inputStream = fileName.openStream();
                InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            jsonObject = jsonElement.getAsJsonObject();
            return jsonObject;
        }
    }

    /**
     * Saves string to file in userdata folder
     *
     * @param filename
     * @param data String with content
     * @param logger
     */
    public static void saveToFile(String filename, String data, Logger logger) {
        File folder = new File(BINDING_USERDATA_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File dataFile = new File(folder, filename);
        try (FileWriter writer = new FileWriter(dataFile)) {
            writer.write(data);
            logger.debug("Saved to {}", dataFile.getAbsolutePath());
        } catch (IOException e) {
            logger.debug("Failed to write file '{}': {}", dataFile.getName(), e.getMessage());
        }
    }

    public static String minLengthString(String string, int length) {
        return String.format("%-" + length + "s", string);
    }

    public static String toHEX(String value) {
        try {
            return String.format("%08X", Long.parseUnsignedLong(value));
        } catch (NumberFormatException e) {
            //
        }
        return value;
    }

    public static String fromHEX(String value) {
        try {
            return String.format("%d", Long.parseUnsignedLong(value, 16));
        } catch (NumberFormatException e) {
            //
        }
        return value;
    }

    /**
     * Formats the deviceId to a hex string if possible. Otherwise returns the id unmodified.
     *
     * @param did
     * @return did
     */
    public static String getHexId(String did) {
        if (!did.isBlank() && !did.contains(".")) {
            return toHEX(did);
        }
        return did;
    }
}
