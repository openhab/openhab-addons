/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import static org.openhab.binding.plugwise.PlugwiseBindingConstants.*;
import static org.openhab.binding.plugwise.internal.protocol.field.DeviceType.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.plugwise.PlugwiseBindingConstants;
import org.openhab.binding.plugwise.internal.protocol.InformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;

/**
 * Utility class for sharing utility methods between objects.
 *
 * @author Wouter Born - Initial contribution
 */
public final class PlugwiseUtils {

    private PlugwiseUtils() {
        // Hidden utility class constructor
    }

    public static DeviceType getDeviceType(ThingTypeUID uid) {
        if (uid.equals(THING_TYPE_CIRCLE)) {
            return CIRCLE;
        } else if (uid.equals(THING_TYPE_CIRCLE_PLUS)) {
            return CIRCLE_PLUS;
        } else if (uid.equals(THING_TYPE_SCAN)) {
            return SCAN;
        } else if (uid.equals(THING_TYPE_SENSE)) {
            return SENSE;
        } else if (uid.equals(THING_TYPE_STEALTH)) {
            return STEALTH;
        } else if (uid.equals(THING_TYPE_SWITCH)) {
            return SWITCH;
        } else {
            return UNKNOWN;
        }
    }

    public static ThingTypeUID getThingTypeUID(DeviceType deviceType) {
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

    public static <T extends Comparable<T>> T minComparable(T first, T second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    public static DateTimeType newDateTimeType(LocalDateTime localDateTime) {
        return new DateTimeType(GregorianCalendar.from(localDateTime.atZone(ZoneId.systemDefault())));
    }

    public static void stopBackgroundThread(Thread thread) {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
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
