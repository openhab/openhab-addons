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
package org.openhab.binding.asuswrt.internal.helpers;

import java.util.regex.Pattern;

import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The {@link AsuswrtUtils} contains utility helper functions.
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class AsuswrtUtils {
    private static final Pattern PATTERN_MAC_PAIRS = Pattern.compile("^([a-fA-F0-9]{2}[:\\.-]?){5}[a-fA-F0-9]{2}$");
    private static final Pattern PATTERN_MAC_TRIPLES = Pattern.compile("^([a-fA-F0-9]{3}[:\\.-]?){3}[a-fA-F0-9]{3}$");

    /*
     * Calculation utility methods
     */

    /**
     * Limits a value between limits.
     *
     * @param value the value that should be limited
     * @param lowerLimit will be returned if value is below
     * @param upperLimit will be returned if value is higher
     */
    public static int limitVal(@Nullable Integer value, int lowerLimit, int upperLimit) {
        if (value == null || value < lowerLimit) {
            return lowerLimit;
        } else if (value > upperLimit) {
            return upperLimit;
        }
        return value;
    }

    /*
     * Formatting utility methods
     */

    /**
     * Returns a value or default value when the value is <code>null</code>.
     *
     * @param <T> Type of value
     * @param value the value which should be checked
     * @param defaultValue the default value that will be returned when <code>value</code> is <code>null</code>
     */
    public static <T> T getValueOrDefault(@Nullable T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Formats a MAC address by replacing old separator characters and adding new ones.
     *
     * @param mac unformatted MAC address
     * @param newSeparatorChar new separator characters (e.g. ":","-" )
     */
    public static String formatMac(String mac, char newSeparatorChar) {
        String unformatedMac = unformatMac(mac);
        String formatedMac = "";
        try {
            formatedMac = unformatedMac.replaceAll("(.{2})", "$1" + newSeparatorChar).substring(0, 17);
            return formatedMac;
        } catch (Exception e) {
            return mac;
        }
    }

    /**
     * Unformats a MAC address. Removes all separator characters.
     */
    public static String unformatMac(String rawMac) {
        String mac = rawMac;
        mac = mac.replace("-", "");
        mac = mac.replace(":", "");
        mac = mac.replace(".", "");
        mac = mac.replace(" ", "");
        return mac;
    }

    /**
     * Checks if a MAC address is valid.
     */
    public static boolean isValidMacAddress(String mac) {
        // MAC-Addresses usually are 6 * 2 hex nibbles separated by colons,
        // but apparently it is legal to have 4 * 3 hex nibbles as well,
        // and the separators can be any of : or - or . or nothing.
        return (PATTERN_MAC_PAIRS.matcher(mac).find() || PATTERN_MAC_TRIPLES.matcher(mac).find());
    }

    /**
     * Converts a hexadecimal String to a byte array.
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            }
        } catch (Exception e) {
        }
        return data;
    }

    /**
     * Converts a {@link String} to a <code>boolean</code>.
     *
     * @param s the string to be converted ('0', '1', '-1', 'true', 'false')
     * @param defVal default value if no match was found
     */
    public static boolean stringToBool(@Nullable String s, boolean defVal) {
        if (s == null) {
            return defVal;
        } else if ("1".equals(s) || "-1".equals(s)) {
            return true;
        } else if ("0".equals(s)) {
            return false;
        } else {
            try {
                return Boolean.parseBoolean(s);
            } catch (Exception e) {
                return defVal;
            }
        }
    }

    /**
     * Converts a {@link String} to an <code>int</code>.
     *
     * @param s the string to be converted
     * @param defVal the default value if the string is not a number
     */
    public static int stringToInteger(@Nullable String s, int defVal) {
        if (s == null) {
            return defVal;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defVal;
        }
    }

    /**
     * Returns the provided string if it is not <code>null</code>, empty or blank. Otherwise the default value is
     * returned.
     *
     * @param s the string to check
     * @param defVal the default value
     * @return the string or the default value
     */
    public static String stringOrDefault(@Nullable String s, String defVal) {
        if (s == null || s.isEmpty() || s.isBlank()) {
            return defVal;
        }
        return s;
    }

    /*
     * JSON formatting
     */

    /**
     * Checks if a String is valid JSON.
     */
    public static boolean isValidJson(String json) {
        try {
            Gson gson = new Gson();
            JsonObject jsnObject = gson.fromJson(json, JsonObject.class);
            return jsnObject != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets a {@link String} value from a {@link JsonObject}.
     *
     * @param jsonObject the object that will be searched for the key
     * @param name the name of the key containing the value
     * @param defVal the default value if the key does not exist
     */
    public static String jsonObjectToString(@Nullable JsonObject jsonObject, String name, String defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            return jsonObject.get(name).getAsString();
        } else {
            return defVal;
        }
    }

    /**
     * Gets a {@link String} value from a {@link JsonObject} using an empty String as default value.
     *
     * @param jsonObject the object that will be searched for the key
     * @param name the name of the key containing the value
     */
    public static String jsonObjectToString(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToString(jsonObject, name, "");
    }

    /**
     * Gets a <code>boolean</code> value from a {@link JsonObject}.
     *
     * @param jsonObject the object that will be searched for the key
     * @param name the name of the key containing the value
     * @param defVal the default value if the key does not exist
     */
    public static boolean jsonObjectToBool(@Nullable JsonObject jsonObject, String name, boolean defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            JsonPrimitive o = jsonObject.getAsJsonPrimitive(name);
            if (o.isBoolean()) {
                return jsonObject.get(name).getAsBoolean();
            } else if (o.isNumber()) {
                Integer iVal = jsonObject.get(name).getAsInt();
                return (iVal.equals(1) || iVal.equals(-1));
            } else if (o.isString()) {
                String val = jsonObject.get(name).getAsString();
                return stringToBool(val, defVal);
            }
        }
        return defVal;
    }

    /**
     * Gets a <code>boolean</code> value from a {@link JsonObject} using <code>false</code> as default value.
     *
     * @param jsonObject the object that will be searched for the key
     * @param name the name of the key containing the value
     */
    public static boolean jsonObjectToBool(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToBool(jsonObject, name, false);
    }

    /**
     * Gets an <code>int</code> value from a {@link JsonObject}.
     *
     * @param jsonObject the object that will be searched for the key
     * @param name the name of the key containing the value
     * @param defVal the default value if the key does not exist
     */
    public static int jsonObjectToInt(@Nullable JsonObject jsonObject, String name, int defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            JsonPrimitive o = jsonObject.getAsJsonPrimitive(name);
            if (o.isNumber()) {
                return jsonObject.get(name).getAsInt();
            } else if (o.isString()) {
                String val = jsonObject.get(name).getAsString();
                return stringToInteger(val, defVal);
            }
        }
        return defVal;
    }

    /**
     * Gets an <code>int</code> value from a {@link JsonObject} using <code>0</code> as default value.
     *
     * @param jsonObject the object that will be searched for the key
     * @param name the name of the key containing the value
     */
    public static int jsonObjectToInt(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToInt(jsonObject, name, 0);
    }

    /**
     * Gets a {@link Number} value from a {@link JsonObject}.
     *
     * @param jsonObject the object that will be searched for the key
     * @param name the name of the key containing the value
     * @param defVal the default value if the key does not exist
     */
    public static Number jsonObjectToNumber(@Nullable JsonObject jsonObject, String name, Number defVal) {
        if (jsonObject != null && jsonObject.has(name)) {
            return jsonObject.get(name).getAsNumber();
        } else {
            return defVal;
        }
    }

    /**
     * Gets a {@link Number} value from a {@link JsonObject} using <code>0</code> as default value.
     *
     * @param jsonObject the object that will be searched for the key
     * @param name the name of the key containing the value
     */
    public static Number jsonObjectToNumber(@Nullable JsonObject jsonObject, String name) {
        return jsonObjectToNumber(jsonObject, name, 0);
    }

    /*
     * Type utility methods
     */

    /**
     * Returns an {@link OnOffType} from a {@link Boolean}.
     */
    public static OnOffType getOnOffType(@Nullable Boolean boolVal) {
        return (boolVal != null ? boolVal ? OnOffType.ON : OnOffType.OFF : OnOffType.OFF);
    }

    /**
     * Returns an {@link OnOffType} from an {@link Integer}.
     */
    public static OnOffType getOnOffType(Integer intVal) {
        return intVal == 0 ? OnOffType.OFF : OnOffType.ON;
    }

    /**
     * Returns a {@link StringType} from a {@link String}.
     */
    public static StringType getStringType(@Nullable String strVal) {
        return new StringType(strVal != null ? strVal : "");
    }

    /**
     * Returns a {@link DecimalType} from a {@link Double}.
     */
    public static DecimalType getDecimalType(@Nullable Double numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * Returns a {@link DecimalType} from an {@link Integer}.
     */
    public static DecimalType getDecimalType(@Nullable Integer numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * Returns a {@link DecimalType} from a {@link Long}.
     */
    public static DecimalType getDecimalType(@Nullable Long numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * Returns a {@link PercentType} from an {@link Integer}.
     */
    public static PercentType getPercentType(@Nullable Integer numVal) {
        Integer val = limitVal(numVal, 0, 100);
        return new PercentType(val);
    }

    /**
     * Returns a {@link HSBType} from {@link Integer}s.
     *
     * @param hue the hue color
     * @param saturation the saturation (0-100)
     * @param brightness the brightness (0-100)
     */
    public static HSBType getHSBType(Integer hue, Integer saturation, Integer brightness) {
        DecimalType h = new DecimalType(hue);
        PercentType s = new PercentType(saturation);
        PercentType b = new PercentType(brightness);
        return new HSBType(h, s, b);
    }

    /**
     * Returns a {@link QuantityType} with the {@link Time} unit.
     */
    public static QuantityType<Time> getTimeType(@Nullable Number numVal, Unit<Time> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /**
     * Returns a {@link QuantityType} with the {@link Power} unit.
     */
    public static QuantityType<Power> getPowerType(@Nullable Number numVal, Unit<Power> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /**
     * Returns a {@link QuantityType} with the {@link Energy} unit.
     */
    public static QuantityType<Energy> getEnergyType(@Nullable Number numVal, Unit<Energy> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }

    /**
     * Returns a {@link QuantityType} with the provided unit.
     */
    public static State getQuantityType(@Nullable Number numVal, Unit<?> unit) {
        return new QuantityType<>((numVal != null ? numVal : 0), unit);
    }
}
