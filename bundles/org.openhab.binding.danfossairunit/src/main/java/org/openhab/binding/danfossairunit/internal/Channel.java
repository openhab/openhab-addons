/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This enum holds the available channels with their properties (name, ...) and read/write accessors to access
 * the corresponding values on the air unit.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public enum Channel {

    // Main Channels

    CHANNEL_CURRENT_TIME("current_time", ChannelGroup.MAIN, DanfossAirUnit::getCurrentTime),
    CHANNEL_MODE("mode", ChannelGroup.MAIN, DanfossAirUnit::getMode, DanfossAirUnit::setMode),
    CHANNEL_MANUAL_FAN_STEP("manual_fan_step", ChannelGroup.MAIN, DanfossAirUnit::getManualFanStep,
            DanfossAirUnit::setManualFanStep),
    CHANNEL_EXTRACT_FAN_SPEED("extract_fan_speed", ChannelGroup.MAIN, DanfossAirUnit::getExtractFanSpeed),
    CHANNEL_SUPPLY_FAN_SPEED("supply_fan_speed", ChannelGroup.MAIN, DanfossAirUnit::getSupplyFanSpeed),
    CHANNEL_EXTRACT_FAN_STEP("extract_fan_step", ChannelGroup.MAIN, DanfossAirUnit::getExtractFanStep),
    CHANNEL_SUPPLY_FAN_STEP("supply_fan_step", ChannelGroup.MAIN, DanfossAirUnit::getSupplyFanStep),

    CHANNEL_BOOST("boost", ChannelGroup.MAIN, DanfossAirUnit::getBoost, DanfossAirUnit::setBoost),
    CHANNEL_NIGHT_COOLING("night_cooling", ChannelGroup.MAIN, DanfossAirUnit::getNightCooling,
            DanfossAirUnit::setNightCooling),

    // Main Temperature Channels
    CHANNEL_ROOM_TEMP("room_temp", ChannelGroup.TEMPS, DanfossAirUnit::getRoomTemperature),
    CHANNEL_ROOM_TEMP_CALCULATED("room_temp_calculated", ChannelGroup.TEMPS,
            DanfossAirUnit::getRoomTemperatureCalculated),
    CHANNEL_OUTDOOR_TEMP("outdoor_temp", ChannelGroup.TEMPS, DanfossAirUnit::getOutdoorTemperature),

    // Humidity Channel
    CHANNEL_HUMIDITY("humidity", ChannelGroup.HUMIDITY, DanfossAirUnit::getHumidity),

    // recuperator channels
    CHANNEL_BYPASS("bypass", ChannelGroup.RECUPERATOR, DanfossAirUnit::getBypass, DanfossAirUnit::setBypass),
    CHANNEL_SUPPLY_TEMP("supply_temp", ChannelGroup.RECUPERATOR, DanfossAirUnit::getSupplyTemperature),
    CHANNEL_EXTRACT_TEMP("extract_temp", ChannelGroup.RECUPERATOR, DanfossAirUnit::getExtractTemperature),
    CHANNEL_EXHAUST_TEMP("exhaust_temp", ChannelGroup.RECUPERATOR, DanfossAirUnit::getExhaustTemperature),

    // service channels
    CHANNEL_BATTERY_LIFE("battery_life", ChannelGroup.SERVICE, DanfossAirUnit::getBatteryLife),
    CHANNEL_FILTER_LIFE("filter_life", ChannelGroup.SERVICE, DanfossAirUnit::getFilterLife),
    CHANNEL_FILTER_PERIOD("filter_period", ChannelGroup.SERVICE, DanfossAirUnit::getFilterPeriod,
            DanfossAirUnit::setFilterPeriod);

    private final String channelName;
    private final ChannelGroup group;
    private final DanfossAirUnitReadAccessor readAccessor;
    @Nullable
    private final DanfossAirUnitWriteAccessor writeAccessor;

    public static Channel getByName(String name) {
        for (Channel channel : values()) {
            if (channel.getChannelName().equals(name)) {
                return channel;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown channel name: %s", name));
    }

    Channel(String channelName, ChannelGroup group, DanfossAirUnitReadAccessor readAccessor) {
        this(channelName, group, readAccessor, null);
    }

    Channel(String channelName, ChannelGroup group, DanfossAirUnitReadAccessor readAccessor,
            @Nullable DanfossAirUnitWriteAccessor writeAccessor) {
        this.channelName = channelName;
        this.group = group;
        this.readAccessor = readAccessor;
        this.writeAccessor = writeAccessor;
    }

    public String getChannelName() {
        return channelName;
    }

    public ChannelGroup getGroup() {
        return group;
    }

    public DanfossAirUnitReadAccessor getReadAccessor() {
        return readAccessor;
    }

    @Nullable
    public DanfossAirUnitWriteAccessor getWriteAccessor() {
        return writeAccessor;
    }
}
