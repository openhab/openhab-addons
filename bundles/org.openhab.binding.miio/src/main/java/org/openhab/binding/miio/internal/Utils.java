/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.miio.internal.MiIoBindingConstants.BINDING_ID;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class for common tasks within the Xiaomi vacuum binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */

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
        if (raw == null) {
            return "";
        }
        final StringBuilder hex = new StringBuilder(3 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F))).append(" ");
        }
        hex.delete(hex.length() - 1, hex.length());
        return hex.toString();
    }

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return "";
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static JsonObject convertFileToJSON(URL fileName) throws JsonIOException, JsonSyntaxException, IOException {
        JsonObject jsonObject = new JsonObject();
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(IOUtils.toString(fileName));
        jsonObject = jsonElement.getAsJsonObject();
        return jsonObject;
    }

    @NonNullByDefault
    public List<URL> findDatabaseFiles(Logger logger) {
        List<URL> urlEntries = new ArrayList<>();
        try {
            File[] userDbFiles = new File(ConfigConstants.getUserDataFolder() + File.separator + BINDING_ID)
                    .listFiles((dir, name) -> name.endsWith(".json"));
            if (userDbFiles != null) {
                for (File f : userDbFiles) {
                    urlEntries.add(f.toURI().toURL());
                    logger.debug("Adding local json db file: {}, {}", f.getName(), f.toURI().toURL());
                }
            }
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            urlEntries
                    .addAll(Collections.list(bundle.findEntries(MiIoBindingConstants.DATABASE_PATH, "*.json", false)));
        } catch (Exception e) {
            logger.debug("Error while searching for database files: {}", e.getMessage());
        }
        return urlEntries;
    }
}
