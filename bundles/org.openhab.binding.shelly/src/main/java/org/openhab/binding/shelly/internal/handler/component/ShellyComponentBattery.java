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
package org.openhab.binding.shelly.internal.handler.component;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorBat;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusPower;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusPower.Shelly2DeviceStatusBattery;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link ShellyComponentBattery} implements the battery channels of the device itself and of an attached
 * external sensor (e.g. an H&amp;T paired with a Wall Display, reported as devicepower:1).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyComponentBattery {
    private static final String[] EXT_CHANNELS = { CHANNEL_SENSOR_BAT_LEVEL, CHANNEL_SENSOR_BAT_LOW };
    private static final Set<String> EXT_CHANNEL_IDS = Set.of(
            CHANNEL_GROUP_SENSOR + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_SENSOR_BAT_LEVEL,
            CHANNEL_GROUP_SENSOR + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_SENSOR_BAT_LOW);

    /**
     * Only a full GetStatus may clear the attached sensor's battery (e.g. the sensor was unpaired), a NotifyStatus
     * just doesn't report devicepower:1. A devicepower:1 without a battery doesn't create one, since bat1 drives
     * the channel creation.
     */
    public static void fillExtStatus(ShellyStatusSensor sdata, @Nullable Shelly2DeviceStatusPower power,
            boolean fullStatus) {
        Shelly2DeviceStatusBattery battery = power != null ? power.battery : null;
        if (battery == null) {
            if (fullStatus) {
                sdata.bat1 = null;
            }
            return;
        }
        ShellySensorBat bat = sdata.bat1;
        if (bat == null) {
            bat = sdata.bat1 = new ShellySensorBat();
        }
        bat.voltage = getDouble(battery.volt);
        bat.value = getDouble(battery.percent);
    }

    /**
     * The external sensor can be paired after the Thing already exists, so its channels are created/removed on
     * every cycle instead of only at Thing creation.
     */
    public static boolean updateExtChannels(ShellyThingInterface thingHandler, ShellyStatusSensor sdata) {
        ShellySensorBat bat = sdata.bat1;
        if (bat == null) {
            thingHandler.removeChannels(EXT_CHANNEL_IDS);
            return false;
        }

        Map<String, Channel> channels = new LinkedHashMap<>();
        for (String channel : EXT_CHANNELS) {
            ShellyChannelDefinitions.addChannel(thingHandler.getThing(), channels, true, CHANNEL_GROUP_SENSOR, channel);
        }
        thingHandler.updateThingChannels(Map.of(), channels);
        return updateChannels(thingHandler, CHANNEL_GROUP_SENSOR, bat, false);
    }

    /**
     * Publish battery level and low-battery state, a device-reported low-battery flag overrides the configured
     * threshold, which doesn't apply while charging.
     */
    public static boolean updateChannels(ShellyThingInterface thingHandler, String group, ShellySensorBat bat,
            boolean charging) {
        boolean updated = false;
        if (bat.value != null) {
            updated |= thingHandler.updateChannel(group, CHANNEL_SENSOR_BAT_LEVEL,
                    toQuantityType(getDouble(bat.value), 0, Units.PERCENT));
        }

        int lowBattery = thingHandler.getThingConfig().getLowBattery();
        Boolean batteryLowFlag = bat.batteryLow;
        boolean isLow = batteryLowFlag != null ? batteryLowFlag.booleanValue()
                : (bat.value != null && !charging && getDouble(bat.value) < lowBattery);
        boolean changed = thingHandler.updateChannel(group, CHANNEL_SENSOR_BAT_LOW, getOnOff(isLow));
        if (changed && isLow) {
            thingHandler.postEvent(ALARM_TYPE_LOW_BATTERY, false);
        }
        return updated | changed;
    }
}
