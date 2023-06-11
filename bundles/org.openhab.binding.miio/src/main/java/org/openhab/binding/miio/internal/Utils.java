/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
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
        if (tokenString.length() > 8) {
            String tokenText = tokenString.substring(0, 8)
                    .concat((tokenString.length() < 24) ? tokenString.substring(8).replaceAll(".", "X")
                            : tokenString.substring(8, 24).replaceAll(".", "X").concat(tokenString.substring(24)));
            return tokenText;
        } else {
            return tokenString;
        }
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
     * @param string with content
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
