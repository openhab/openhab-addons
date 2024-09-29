/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.miele.internal;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miele.internal.api.dto.DeviceMetaData;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link DeviceUtil} class contains utility methods for extracting
 * and parsing device information, for example from ExtendedDeviceState.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DeviceUtil {
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    private static final String TEMPERATURE_UNDEFINED = "32768";
    private static final String TEMPERATURE_COLD = "-32760";
    private static final String TEXT_PREFIX = "miele.";

    private static final Map<String, String> STATES = Map.ofEntries(Map.entry("1", "off"), Map.entry("2", "stand-by"),
            Map.entry("3", "programmed"), Map.entry("4", "waiting-to-start"), Map.entry("5", "running"),
            Map.entry("6", "paused"), Map.entry("7", "end"), Map.entry("8", "failure"), Map.entry("9", "abort"),
            Map.entry("10", "idle"), Map.entry("11", "rinse-hold"), Map.entry("12", "service"),
            Map.entry("13", "super-freezing"), Map.entry("14", "super-cooling"), Map.entry("15", "super-heating"),
            Map.entry("144", "default"), Map.entry("145", "locked"), Map.entry("255", "not-connected"));

    /**
     * Convert byte array to hex representation.
     */
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars, StandardCharsets.UTF_8);
    }

    /**
     * Convert string consisting of 8 bit characters to byte array.
     * Note: This simple operation has been extracted and put here to document
     * and ensure correct behavior for 8 bit characters that should be turned
     * into single bytes without any UTF-8 encoding.
     */
    public static byte[] stringToBytes(String input) {
        return input.getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * Convert string to Number:Temperature state with unit Celcius
     */
    public static State getTemperatureState(String s) throws NumberFormatException {
        if (TEMPERATURE_UNDEFINED.equals(s)) {
            return UnDefType.UNDEF;
        }
        if (TEMPERATURE_COLD.equals(s)) {
            return new QuantityType<>(10, SIUnits.CELSIUS);
        }
        int temperature = Integer.parseInt(s);
        return new QuantityType<>(temperature, SIUnits.CELSIUS);
    }

    /**
     * Get state text for provided string taking into consideration {@link DeviceMetaData}
     * as well as built-in/translated strings.
     */
    public static State getStateTextState(String s, @Nullable DeviceMetaData dmd,
            MieleTranslationProvider translationProvider) {
        return getTextState(s, dmd, translationProvider, STATES, MISSING_STATE_TEXT_PREFIX, "");
    }

    /**
     * Get text for provided string taking into consideration {@link DeviceMetaData}
     * as well as built-in/translated strings.
     * 
     * @param s Raw string to be processed
     * @param dmd {@link DeviceMetaData} possibly containing LocalizedValue and/or enum from gateway
     * @param translationProvider {@link MieleTranslationProvider} for localization support
     * @param valueMap Map of numeric values with corresponding text keys
     * @param propertyPrefix Property prefix appended to text key (including dot)
     * @param appliancePrefix Appliance prefix appended to text key (including dot)
     * @return Text string as State
     */
    public static State getTextState(String s, @Nullable DeviceMetaData dmd,
            MieleTranslationProvider translationProvider, Map<String, String> valueMap, String propertyPrefix,
            String appliancePrefix) {
        if ("0".equals(s)) {
            return UnDefType.UNDEF;
        }

        String gatewayText = null;
        if (dmd != null) {
            if (dmd.LocalizedValue != null && !dmd.LocalizedValue.isEmpty()) {
                gatewayText = dmd.LocalizedValue;
            } else {
                gatewayText = dmd.getMieleEnum(s);
            }
        }

        String value = valueMap.get(s);
        if (value != null) {
            String key = TEXT_PREFIX + propertyPrefix + appliancePrefix + value;
            return new StringType(
                    translationProvider.getText(key, gatewayText != null ? gatewayText : propertyPrefix + s));
        }

        if (gatewayText != null) {
            return new StringType(gatewayText);
        }

        return new StringType(propertyPrefix + s);
    }
}
