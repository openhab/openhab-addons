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

    // Standard channels
    public static final String CHANNEL_ROOM_TEMPERATURE = "room-temperature";
    public static final String CHANNEL_TARGET_TEMPERATURE = "target-temperature";
    public static final String CHANNEL_HVAC_MODE = "hvac-mode";
    public static final String CHANNEL_PRESET_MODE = "preset-mode";
    public static final String CHANNEL_PRESET_MODE_DURATION = "preset-mode-duration";
    public static final String CHANNEL_CH_WATER_TEMPERATURE = "ch-water-temperature";
    public static final String CHANNEL_CH_RETURN_TEMPERATURE = "ch-return-temperature";
    public static final String CHANNEL_CH_WATER_PRESSURE = "ch-water-pressure";
    public static final String CHANNEL_CH_SETPOINT = "ch-setpoint";
    public static final String CHANNEL_DHW_TEMPERATURE = "dhw-temperature";
    public static final String CHANNEL_DHW_TARGET_TEMPERATURE = "dhw-target-temperature";
    public static final String CHANNEL_DHW_MODE = "dhw-mode";
    public static final String CHANNEL_OUTSIDE_TEMPERATURE = "outside-temperature";
    public static final String CHANNEL_FLAME = "flame";
    public static final String CHANNEL_MODULATION_LEVEL = "modulation-level";
    public static final String CHANNEL_BURNING_HOURS = "burning-hours";
    public static final String CHANNEL_BURNER_TARGET = "burner-target";
    public static final String CHANNEL_VACATION_DURATION = "vacation-duration";
    public static final String CHANNEL_VACATION_START = "vacation-start";
    public static final String CHANNEL_VACATION_END = "vacation-end";
    public static final String CHANNEL_VACATION_TEMPERATURE = "vacation-temperature";
    public static final String CHANNEL_EXTEND_DURATION = "extend-duration";
    public static final String CHANNEL_FIREPLACE_DURATION = "fireplace-duration";
    public static final String CHANNEL_WEATHER_STATUS = "weather-status";
    public static final String CHANNEL_DEVICE_ERRORS = "device-errors";
    public static final String CHANNEL_BOILER_ERRORS = "boiler-errors";
    public static final String CHANNEL_TIME_TO_TARGET = "time-to-target";

    // Advanced diagnostic channels
    public static final String CHANNEL_SHOWN_SET_TEMPERATURE = "shown-set-temperature";
    public static final String CHANNEL_AVERAGE_OUTSIDE_TEMPERATURE = "average-outside-temperature";
    public static final String CHANNEL_PCB_TEMPERATURE = "pcb-temperature";
    public static final String CHANNEL_WIFI_SIGNAL = "wifi-signal";
    public static final String CHANNEL_MODULATION_MIN = "min-modulation-level";
    public static final String CHANNEL_BOILER_TEMPERATURE = "boiler-temperature";
    public static final String CHANNEL_BOILER_RETURN_TEMPERATURE = "boiler-return-temperature";
    public static final String CHANNEL_DHW_FLOW_RATE = "dhw-flow-rate";
    public static final String CHANNEL_MAX_BOILER_TEMPERATURE = "max-boiler-temperature";
    public static final String CHANNEL_VACATION_REMAINING = "vacation-remaining";
    public static final String CHANNEL_EXTEND_REMAINING = "extend-remaining";
    public static final String CHANNEL_FIREPLACE_REMAINING = "fireplace-remaining";
    public static final String CHANNEL_REPORT_TIME = "report-time";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_POWER_CONSUMPTION = "power-consumption";
    public static final String CHANNEL_RESETS = "resets";
    public static final String CHANNEL_MEMORY_ALLOCATION = "memory-allocation";

    // Advanced writable device-configuration channels
    public static final String CHANNEL_FROST_PROTECTION = "frost-protection";
    public static final String CHANNEL_FROST_PROTECTION_TEMPERATURE = "frost-protection-temperature";
    public static final String CHANNEL_LEGIONELLA_PROTECTION = "legionella-protection";
    public static final String CHANNEL_SUMMER_ECO_MODE = "summer-eco-mode";
    public static final String CHANNEL_SUMMER_ECO_TEMPERATURE = "summer-eco-temperature";

    // Thing property key for the persisted client identifier
    public static final String PROPERTY_CLIENT_ID = "clientId";

    // ── Protocol enum constants ───────────────────────────────────────────────

    public static final int CH_MODE_MANUAL = 1;
    public static final int CH_MODE_AUTO = 2;
    public static final int CH_MODE_HOLIDAY = 3;
    public static final int CH_MODE_EXTEND = 4;
    public static final int CH_MODE_FIREPLACE = 5;

    public static final int CH_CONTROL_MODE_HEAT = 0;
    public static final int CH_CONTROL_MODE_AUTO = 1;

    public static final int BOILER_STATUS_CH_ACTIVE = 0x004;
    public static final int BOILER_STATUS_BURNER_ON = 0x008;
    public static final int BOILER_STATUS_DHW_ACTIVE = 0x010;
    public static final int BOILER_STATUS_FLAME = 0x100;

    public static final Map<Integer, String> CH_MODE_NAMES = Map.of(CH_MODE_MANUAL, "manual", CH_MODE_AUTO, "auto",
            CH_MODE_HOLIDAY, "holiday", CH_MODE_EXTEND, "extend", CH_MODE_FIREPLACE, "fireplace");

    public static final Map<String, Integer> CH_MODE_BY_NAME = Map.of("manual", CH_MODE_MANUAL, "auto", CH_MODE_AUTO,
            "holiday", CH_MODE_HOLIDAY, "extend", CH_MODE_EXTEND, "fireplace", CH_MODE_FIREPLACE);

    public static final Map<Integer, String> CH_CONTROL_MODE_NAMES = Map.of(CH_CONTROL_MODE_HEAT, "heat",
            CH_CONTROL_MODE_AUTO, "auto");

    public static final Map<Integer, String> WEATHER_STATUS_NAMES = Map.ofEntries(Map.entry(0, "sunny"),
            Map.entry(1, "clear"), Map.entry(2, "rainy"), Map.entry(3, "snowy"), Map.entry(4, "hail"),
            Map.entry(5, "windy"), Map.entry(6, "fog"), Map.entry(7, "cloudy"), Map.entry(8, "partly-sunny"),
            Map.entry(9, "partly-cloudy"), Map.entry(10, "pouring"), Map.entry(11, "lightning"),
            Map.entry(12, "hurricane"), Map.entry(13, "unknown"));
}
