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
package org.openhab.binding.tapocontrol.internal.helpers.utils;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.discovery.dto.TapoDiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;

/**
 * {@link TapoUtils} TapoUtils -
 * Utility Helper Functions
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class TapoUtils {

    /************************************
     * CALCULATION UTILS
     ***********************************/
    /**
     * Limit Value between limits
     * 
     * @param value Integer
     * @param lowerLimit
     * @param upperLimit
     * @return
     */
    public static Integer limitVal(@Nullable Integer value, Integer lowerLimit, Integer upperLimit) {
        if (value == null || value < lowerLimit) {
            return lowerLimit;
        } else if (value > upperLimit) {
            return upperLimit;
        }
        return value;
    }

    /************************************
     * FORMAT UTILS
     ***********************************/
    /**
     * return value or default val if it's null
     * 
     * @param <T> Type of value
     * @param value value
     * @param defaultValue defaut value
     * @return
     */
    public static <T> T getValueOrDefault(@Nullable T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * compare tow values against an comparator and return the other one
     * if both are null, comparator will be returned - if both have values val2 will be returned
     * 
     * @param <T> Type of return value
     * @param val1 fist value to campare - will be returned if val2 is null or matches comparator
     * @param val2 second value to compare - will be returned if val1 is null or matches comparator
     * @param comparator compared values with this
     * @return
     */
    public static <T> T compareValuesAgainstComparator(@Nullable T val1, @Nullable T val2, T comparator) {
        if (val1 == null && val2 == null) {
            return comparator;
        } else if (val1 != null && (val2 == null || val2.equals(comparator))) {
            return Objects.requireNonNull(val1);
        } else if (val1 == null || val1.equals(comparator)) {
            return Objects.requireNonNull(val2);
        } else {
            return Objects.requireNonNull(val2);
        }
    }

    /**
     * Format MAC-Address replacing old division chars and add new one
     * 
     * @param mac unformated mac-Address
     * @param newDivisionChar new division char (e.g. ":","-" )
     * @return new formated mac-Address
     */
    public static String formatMac(String mac, char newDivisionChar) {
        String unformatedMac = unformatMac(mac);
        return unformatedMac.replaceAll("(.{2})", "$1" + newDivisionChar).substring(0, 17);
    }

    /**
     * unformat MAC-Address replace all division chars
     * 
     * @param mac string with mac address
     * @return mac address without any division chars
     */
    public static String unformatMac(String mac) {
        mac = mac.replace("-", "");
        mac = mac.replace(":", "");
        mac = mac.replace(".", "");
        return mac;
    }

    /**
     * Get DeviceModel from String - Formats different spellings in model-strings
     * 
     * @param device JsonObject with deviceData
     * @return String with DeviceModel
     */
    public static String getDeviceModel(TapoDiscoveryResult device) {
        return getDeviceModel(device.deviceModel());
    }

    /**
     * Get DeviceModel from String - Formats different spellings in model-strings
     * 
     * @param deviceModel String to find model from
     * @return String with DeviceModel
     */
    public static String getDeviceModel(String deviceModel) {
        try {
            deviceModel = deviceModel.replaceAll("\\(.*\\)", ""); // replace (DE)
            deviceModel = deviceModel.replace("Tapo", "");
            deviceModel = deviceModel.replace("Series", "");
            deviceModel = deviceModel.trim();
            deviceModel = deviceModel.replace(" ", "_");
            deviceModel = deviceModel.substring(0, 4);
            return deviceModel;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * GET DEVICE LABEL
     * 
     * @param device JsonObject with deviceData
     * @return String with DeviceLabel
     */
    public static String getDeviceLabel(TapoDiscoveryResult device) {
        try {
            String deviceLabel = "";
            String deviceModel = getDeviceModel(device);
            String alias = device.alias();
            ThingTypeUID deviceUID = new ThingTypeUID(BINDING_ID, deviceModel);

            if (SUPPORTED_HUB_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_HUB;
            } else if (SUPPORTED_SOCKET_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_SOCKET;
            } else if (SUPPORTED_SOCKET_STRIP_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_SOCKET_STRIP;
            } else if (SUPPORTED_WHITE_BULB_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_WHITE_BULB;
            } else if (SUPPORTED_COLOR_BULB_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_COLOR_BULB;
            } else if (SUPPORTED_LIGHT_STRIP_UIDS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_LIGHTSTRIP;
            } else if (SUPPORTED_SMART_CONTACTS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_SMART_CONTACT;
            } else if (SUPPORTED_MOTION_SENSORS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_MOTION_SENSOR;
            } else if (SUPPORTED_WHEATHER_SENSORS.contains(deviceUID)) {
                deviceLabel = DEVICE_DESCRIPTION_TEMP_SENSOR;
            }
            if (alias.length() > 0) {
                return String.format("%s %s %s (%s)", DEVICE_VENDOR, deviceModel, deviceLabel, alias);
            }
            return String.format("%s %s %s", DEVICE_VENDOR, deviceModel, deviceLabel);
        } catch (Exception e) {
            return "";
        }
    }
}
