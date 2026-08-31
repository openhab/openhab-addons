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
package org.openhab.binding.atagone.internal;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Constants shared across the ATAG ONE binding.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneBindingConstants {

    public static final String BINDING_ID = "atagone";

    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_THERMOSTAT);

    /*
     * Standard channels.
     *
     * Values are group-qualified ("group#channel-id") to match ChannelUID.getId(), which — once a
     * thing-type uses channel groups — returns the full grouped id, not the bare channel id. Getting
     * this wrong is a silent failure, not a compile or startup error: handleCommand()'s switch on the
     * bare id would match nothing for every writable channel, and updateChannels()'s updateState()
     * calls would construct ChannelUIDs for channels that don't exist on the Thing. Keep these values
     * in sync with the <channel-groups> mapping in thing-types.xml — a channel moved to a different
     * group there must be moved here too.
     */
    public static final String CHANNEL_ROOM_TEMPERATURE = "heating#room-temperature";
    public static final String CHANNEL_TARGET_TEMPERATURE = "heating#target-temperature";
    public static final String CHANNEL_CH_CONTROL_MODE = "heating#control-mode";
    public static final String CHANNEL_PRESET_MODE = "control#preset-mode";
    public static final String CHANNEL_PRESET_MODE_DURATION = "control#preset-mode-duration";
    public static final String CHANNEL_CH_WATER_TEMPERATURE = "heating#water-temperature";
    public static final String CHANNEL_CH_RETURN_TEMPERATURE = "heating#return-temperature";
    public static final String CHANNEL_CH_WATER_PRESSURE = "heating#water-pressure";
    public static final String CHANNEL_CH_SETPOINT = "heating#water-setpoint";
    public static final String CHANNEL_DHW_TEMPERATURE = "hotwater#temperature";
    public static final String CHANNEL_DHW_TARGET_TEMPERATURE = "hotwater#target-temperature";
    public static final String CHANNEL_OUTSIDE_TEMPERATURE = "heating#outside-temperature";
    public static final String CHANNEL_FLAME = "heating#flame";
    public static final String CHANNEL_MODULATION_LEVEL = "heating#modulation-level";
    public static final String CHANNEL_BURNING_HOURS = "heating#burning-hours";
    public static final String CHANNEL_BURNER_TARGET = "heating#burner-target";
    public static final String CHANNEL_VACATION_DURATION = "control#vacation-duration";
    public static final String CHANNEL_VACATION_START = "control#vacation-start";
    public static final String CHANNEL_VACATION_END = "control#vacation-end";
    public static final String CHANNEL_VACATION_TEMPERATURE = "control#vacation-temperature";
    public static final String CHANNEL_EXTEND_DURATION = "control#extend-duration";
    public static final String CHANNEL_FIREPLACE_DURATION = "control#fireplace-duration";
    public static final String CHANNEL_WEATHER_STATUS = "heating#weather-status";
    public static final String CHANNEL_DEVICE_ERRORS = "alerts#device-errors";
    public static final String CHANNEL_BOILER_ERRORS = "alerts#boiler-errors";
    public static final String CHANNEL_TIME_TO_TARGET = "heating#time-to-target";

    // Advanced diagnostic channels
    public static final String CHANNEL_SHOWN_SET_TEMPERATURE = "heating#shown-set-temperature";
    public static final String CHANNEL_AVERAGE_OUTSIDE_TEMPERATURE = "heating#average-outside-temperature";
    public static final String CHANNEL_PCB_TEMPERATURE = "device#pcb-temperature";
    public static final String CHANNEL_WIFI_SIGNAL = "device#wifi-signal";
    public static final String CHANNEL_MODULATION_MIN = "heating#min-modulation-level";
    public static final String CHANNEL_BOILER_TEMPERATURE = "heating#boiler-temperature";
    public static final String CHANNEL_BOILER_RETURN_TEMPERATURE = "heating#boiler-return-temperature";
    public static final String CHANNEL_DHW_FLOW_RATE = "hotwater#flow-rate";
    public static final String CHANNEL_MAX_BOILER_TEMPERATURE = "heating#max-boiler-temperature";
    public static final String CHANNEL_VACATION_REMAINING = "control#vacation-remaining";
    public static final String CHANNEL_EXTEND_REMAINING = "control#extend-remaining";
    public static final String CHANNEL_FIREPLACE_REMAINING = "control#fireplace-remaining";
    public static final String CHANNEL_REPORT_TIME = "device#report-time";
    public static final String CHANNEL_VOLTAGE = "device#voltage";
    public static final String CHANNEL_RESETS = "device#resets";
    public static final String CHANNEL_MEMORY_ALLOCATION = "device#memory-allocation";

    /*
     * Advanced writable device-configuration channels — declared but not yet wired to any
     * channel-type in thing-types.xml or any read/write logic in the handler (planned for a later
     * phase). Grouped by subsystem, not under a dedicated settings group — see the channel-group
     * placement rule in thing-types.xml's channel-groups comment, and DEVELOPERS.md's gap analysis
     * for the full list of planned fields per group.
     */
    public static final String CHANNEL_FROST_PROTECTION = "heating#frost-protection";
    public static final String CHANNEL_FROST_PROTECTION_TEMPERATURE = "heating#frost-protection-temperature";
    public static final String CHANNEL_LEGIONELLA_PROTECTION = "hotwater#legionella-protection";
    public static final String CHANNEL_SUMMER_ECO_MODE = "heating#summer-eco-mode";
    public static final String CHANNEL_SUMMER_ECO_TEMPERATURE = "heating#summer-eco-temperature";

    // Thing property key for the persisted client identifier
    public static final String PROPERTY_CLIENT_ID = "clientId";

    // ── Protocol enum constants ───────────────────────────────────────────────

    public static final int CH_MODE_MANUAL = 1;
    public static final int CH_MODE_AUTO = 2;
    public static final int CH_MODE_HOLIDAY = 3;
    public static final int CH_MODE_EXTEND = 4;
    public static final int CH_MODE_FIREPLACE = 5;

    /*
     * Room-setpoint control vs. weather-compensated (outdoor-temperature-driven heating curve)
     * control. Independent of ch_mode (the manual/auto/holiday/extend/fireplace preset) — this is a
     * separate "Weather control" setting on the thermostat, not a manual/auto or heat/auto toggle,
     * and stays fixed regardless of which ch_mode preset is active.
     */
    public static final int CH_CONTROL_MODE_ROOM = 0;
    public static final int CH_CONTROL_MODE_WEATHER = 1;

    public static final int BOILER_STATUS_CH_ACTIVE = 0x004;
    public static final int BOILER_STATUS_BURNER_ON = 0x008;
    public static final int BOILER_STATUS_DHW_ACTIVE = 0x010;
    public static final int BOILER_STATUS_FLAME = 0x100;

    public static final Map<Integer, String> CH_MODE_NAMES = Map.of(CH_MODE_MANUAL, "manual", CH_MODE_AUTO, "auto",
            CH_MODE_HOLIDAY, "holiday", CH_MODE_EXTEND, "extend", CH_MODE_FIREPLACE, "fireplace");

    public static final Map<String, Integer> CH_MODE_BY_NAME = Map.of("manual", CH_MODE_MANUAL, "auto", CH_MODE_AUTO,
            "holiday", CH_MODE_HOLIDAY, "extend", CH_MODE_EXTEND, "fireplace", CH_MODE_FIREPLACE);

    public static final Map<Integer, String> CH_CONTROL_MODE_NAMES = Map.of(CH_CONTROL_MODE_ROOM, "room",
            CH_CONTROL_MODE_WEATHER, "weather");

    public static final Map<Integer, String> WEATHER_STATUS_NAMES = Map.ofEntries(Map.entry(0, "sunny"),
            Map.entry(1, "clear"), Map.entry(2, "rainy"), Map.entry(3, "snowy"), Map.entry(4, "hail"),
            Map.entry(5, "windy"), Map.entry(6, "fog"), Map.entry(7, "cloudy"), Map.entry(8, "partly-sunny"),
            Map.entry(9, "partly-cloudy"), Map.entry(10, "pouring"), Map.entry(11, "lightning"),
            Map.entry(12, "hurricane"), Map.entry(13, "unknown"));
}
