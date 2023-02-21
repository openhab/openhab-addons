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
package org.openhab.binding.plugwise.internal;

import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.*;
import static org.openhab.binding.plugwise.internal.protocol.field.DeviceType.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.protocol.InformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Utility class for sharing utility methods between objects.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public final class PlugwiseUtils {

    private PlugwiseUtils() {
        // Hidden utility class constructor
    }

    public static DeviceType getDeviceType(ThingTypeUID uid) {
        if (THING_TYPE_CIRCLE.equals(uid)) {
            return CIRCLE;
        } else if (THING_TYPE_CIRCLE_PLUS.equals(uid)) {
            return CIRCLE_PLUS;
        } else if (THING_TYPE_SCAN.equals(uid)) {
            return SCAN;
        } else if (THING_TYPE_SENSE.equals(uid)) {
            return SENSE;
        } else if (THING_TYPE_STEALTH.equals(uid)) {
            return STEALTH;
        } else if (THING_TYPE_SWITCH.equals(uid)) {
            return SWITCH;
        } else {
            return UNKNOWN;
        }
    }

    public static @Nullable ThingTypeUID getThingTypeUID(DeviceType deviceType) {
        if (deviceType == CIRCLE) {
            return THING_TYPE_CIRCLE;
        } else if (deviceType == CIRCLE_PLUS) {
            return THING_TYPE_CIRCLE_PLUS;
        } else if (deviceType == SCAN) {
            return THING_TYPE_SCAN;
        } else if (deviceType == SENSE) {
            return THING_TYPE_SENSE;
        } else if (deviceType == STEALTH) {
            return THING_TYPE_STEALTH;
        } else if (deviceType == SWITCH) {
            return THING_TYPE_SWITCH;
        } else {
            return null;
        }
    }

    public static String lowerCamelToUpperUnderscore(String text) {
        return text.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
    }

    public static <T extends Comparable<T>> T minComparable(T first, T second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    public static DateTimeType newDateTimeType(LocalDateTime localDateTime) {
        return new DateTimeType(localDateTime.atZone(ZoneId.systemDefault()));
    }

    public static void stopBackgroundThread(@Nullable Thread thread) {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    public static String upperUnderscoreToLowerCamel(String text) {
        final String delimiter = "_";
        StringBuilder upperCamelBuilder = new StringBuilder(text.length());
        for (String str : text.split(delimiter)) {
            if (upperCamelBuilder.isEmpty() && str.length() > 0) {
                upperCamelBuilder.append(str.substring(0, 1).toLowerCase());
            } else if (str.length() > 0) {
                upperCamelBuilder.append(str.substring(0, 1).toUpperCase());
            }
            if (str.length() > 1) {
                upperCamelBuilder.append(str.substring(1).toLowerCase());
            }
        }
        return upperCamelBuilder.toString();
    }

    public static boolean updateProperties(Map<String, String> properties, InformationResponseMessage message) {
        boolean update = false;

        // Update firmware version property
        String oldFirmware = properties.get(Thing.PROPERTY_FIRMWARE_VERSION);
        String newFirmware = DateTimeFormatter.ISO_LOCAL_DATE.format(message.getFirmwareVersion());
        if (oldFirmware == null || !oldFirmware.equals(newFirmware)) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, newFirmware);
            update = true;
        }

        // Update hardware version property
        String oldHardware = properties.get(Thing.PROPERTY_HARDWARE_VERSION);
        String newHardware = message.getHardwareVersion();
        if (oldHardware == null || !oldHardware.equals(newHardware)) {
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, newHardware);
            update = true;
        }

        // Update hertz property for devices with a relay
        if (message.getDeviceType().isRelayDevice()) {
            String oldHertz = properties.get(PlugwiseBindingConstants.PROPERTY_HERTZ);
            String newHertz = Integer.toString(message.getHertz());
            if (oldHertz == null || !oldHertz.equals(newHertz)) {
                properties.put(PlugwiseBindingConstants.PROPERTY_HERTZ, newHertz);
                update = true;
            }
        }

        // Update MAC address property
        String oldMACAddress = properties.get(PlugwiseBindingConstants.PROPERTY_MAC_ADDRESS);
        String newMACAddress = message.getMACAddress().toString();
        if (oldMACAddress == null || !oldMACAddress.equals(newMACAddress)) {
            properties.put(PlugwiseBindingConstants.PROPERTY_MAC_ADDRESS, newMACAddress);
            update = true;
        }

        return update;
    }
}
