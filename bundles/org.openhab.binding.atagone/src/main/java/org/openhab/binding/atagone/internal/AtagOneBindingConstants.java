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
    public static final String CHANNEL_AVERAGE_OUTSIDE_TEMPERATURE = "heating#average-outside-temperature";
    public static final String CHANNEL_PCB_TEMPERATURE = "device#pcb-temperature";
    public static final String CHANNEL_WIFI_SIGNAL = "device#wifi-signal";
    public static final String CHANNEL_BOILER_TEMPERATURE = "heating#boiler-temperature";
    public static final String CHANNEL_BOILER_RETURN_TEMPERATURE = "heating#boiler-return-temperature";
    public static final String CHANNEL_DHW_FLOW_RATE = "hotwater#flow-rate";
    public static final String CHANNEL_VACATION_REMAINING = "control#vacation-remaining";
    public static final String CHANNEL_EXTEND_REMAINING = "control#extend-remaining";
    public static final String CHANNEL_FIREPLACE_REMAINING = "control#fireplace-remaining";
    public static final String CHANNEL_REPORT_TIME = "device#report-time";
    public static final String CHANNEL_VOLTAGE = "device#voltage";
    public static final String CHANNEL_RESETS = "device#resets";
    public static final String CHANNEL_CH_SCHEDULE_BASE_TEMPERATURE = "heating#schedule-base-temperature";
    public static final String CHANNEL_DHW_SCHEDULE_BASE_TEMPERATURE = "hotwater#schedule-base-temperature";
    public static final String CHANNEL_CH_SCHEDULE = "heating#schedule";
    public static final String CHANNEL_DHW_SCHEDULE = "hotwater#schedule";

    // Settings channels (Phase F) — advanced="true", set-once configuration, not everyday channels.
    public static final String CHANNEL_FROST_PROTECTION = "heating#frost-protection";
    public static final String CHANNEL_FROST_PROTECTION_TEMPERATURE_ROOM = "heating#frost-protection-temperature-room";
    public static final String CHANNEL_FROST_PROTECTION_TEMPERATURE_OUTSIDE = "heating#frost-protection-temperature-outside";
    public static final String CHANNEL_SUMMER_ECO_MODE = "heating#summer-eco-mode";
    public static final String CHANNEL_SUMMER_ECO_TEMPERATURE = "heating#summer-eco-temperature";
    public static final String CHANNEL_HEATING_TYPE = "heating#heating-type";
    public static final String CHANNEL_INSULATION = "heating#insulation";
    public static final String CHANNEL_BUILDING_SIZE = "heating#building-size";
    public static final String CHANNEL_WDR_TEMPERATURE_INFLUENCE = "heating#wdr-temperature-influence";
    public static final String CHANNEL_CLIMATE_ZONE = "heating#climate-zone";
    public static final String CHANNEL_MAX_PREHEAT = "heating#max-preheat";
    public static final String CHANNEL_LEGIONELLA_PROTECTION = "hotwater#legionella-protection";
    public static final String CHANNEL_LEGIONELLA_PROTECTION_DAY = "hotwater#legionella-protection-day";
    public static final String CHANNEL_LEGIONELLA_PROTECTION_TIME = "hotwater#legionella-protection-time";
    public static final String CHANNEL_DISPLAY_BRIGHTNESS = "device#display-brightness";
    public static final String CHANNEL_TIME_ZONE = "device#time-zone";
    public static final String CHANNEL_LANGUAGE = "device#language";

    // Exposure reconciliation channels (Phase I) — values the portal shows that were missing.
    public static final String CHANNEL_DELTA_TEMPERATURE = "heating#delta-temperature";
    public static final String CHANNEL_CH_ACTIVE = "heating#central-heating-active";
    public static final String CHANNEL_DHW_ACTIVE = "hotwater#hot-water-active";
    public static final String CHANNEL_WEATHER_TEMPERATURE = "heating#weather-temperature";
    public static final String CHANNEL_NEXT_SCHEDULE_TIME = "control#next-schedule-time";
    public static final String CHANNEL_NEXT_SCHEDULE_TEMPERATURE = "control#next-schedule-temperature";

    // Thing property key for the persisted client identifier
    public static final String PROPERTY_CLIENT_ID = "clientId";
    // Thing property key for the device's own identifier — also the representation-property, shared
    // with the discovery service so a manually-added Thing and a discovered one populate it the same way.
    public static final String PROPERTY_DEVICE_ID = "deviceId";
    public static final String PROPERTY_INSTALLER_ID = "installerId";

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

    /*
     * Corrected 2026-09-14 — the original values (CH_ACTIVE=0x004, DHW_ACTIVE=0x010, FLAME=0x100)
     * were never independently verified against a real device cycle and were wrong: a genuine DHW
     * heating event was observed (via persistence: DHW tank temperature rising, CH circuit flat,
     * physical display confirming DHW) misclassified as "ch", while the flame channel never once
     * indicated ON despite the burner clearly firing (91-100% modulation, boiler flow temperature
     * spiking). Independently corroborated against
     * https://github.com/kozmoz/atag-one-api/wiki/Thermostat-Protocol: "&512 = dhw_schema, &256 =
     * ch_schema, &8 = boilerHeating, &4 = dhwHeating, &2 = chHeating" — that source's boilerHeating
     * bit is exactly this binding's previously-unused BURNER_ON constant, and its dhwHeating bit is
     * exactly the value this binding had mislabeled CH_ACTIVE, which explains the bug precisely. Not
     * yet re-verified live against this specific device's own CH/DHW cycle.
     */
    public static final int BOILER_STATUS_CH_ACTIVE = 0x002;
    public static final int BOILER_STATUS_DHW_ACTIVE = 0x004;
    public static final int BOILER_STATUS_FLAME = 0x008;
    /**
     * "Which schedule (CH or DHW) is currently governing" — not an activity flag; resolves the previously-unknown 0x200
     * bit. Not currently exposed as a channel.
     */
    public static final int BOILER_STATUS_CH_SCHEMA = 0x100;
    public static final int BOILER_STATUS_DHW_SCHEMA = 0x200;

    public static final Map<Integer, String> CH_MODE_NAMES = Map.of(CH_MODE_MANUAL, "manual", CH_MODE_AUTO, "auto",
            CH_MODE_HOLIDAY, "holiday", CH_MODE_EXTEND, "extend", CH_MODE_FIREPLACE, "fireplace");

    public static final Map<String, Integer> CH_MODE_BY_NAME = Map.of("manual", CH_MODE_MANUAL, "auto", CH_MODE_AUTO,
            "holiday", CH_MODE_HOLIDAY, "extend", CH_MODE_EXTEND, "fireplace", CH_MODE_FIREPLACE);

    public static final Map<Integer, String> CH_CONTROL_MODE_NAMES = Map.of(CH_CONTROL_MODE_ROOM, "thermostat",
            CH_CONTROL_MODE_WEATHER, "weather-dependent");

    public static final Map<String, Integer> CH_CONTROL_MODE_BY_NAME = Map.of("thermostat", CH_CONTROL_MODE_ROOM,
            "weather-dependent", CH_CONTROL_MODE_WEATHER);

    public static final Map<Integer, String> WEATHER_STATUS_NAMES = Map.ofEntries(Map.entry(0, "sunny"),
            Map.entry(1, "clear"), Map.entry(2, "rainy"), Map.entry(3, "snowy"), Map.entry(4, "hail"),
            Map.entry(5, "windy"), Map.entry(6, "fog"), Map.entry(7, "cloudy"), Map.entry(8, "partly-sunny"),
            Map.entry(9, "partly-cloudy"), Map.entry(10, "pouring"), Map.entry(11, "lightning"),
            Map.entry(12, "hurricane"), Map.entry(13, "unknown"));

    public static final Map<Integer, String> FROST_PROTECTION_NAMES = Map.of(0, "off", 1, "outside", 2, "inside", 3,
            "both");
    public static final Map<String, Integer> FROST_PROTECTION_BY_NAME = Map.of("off", 0, "outside", 1, "inside", 2,
            "both", 3);

    public static final Map<Integer, String> HEATING_TYPE_NAMES = Map.of(1, "air-heating", 2, "convector", 3,
            "radiator", 4, "radiator-underfloor", 5, "underfloor", 6, "underfloor-radiator");
    public static final Map<String, Integer> HEATING_TYPE_BY_NAME = Map.of("air-heating", 1, "convector", 2, "radiator",
            3, "radiator-underfloor", 4, "underfloor", 5, "underfloor-radiator", 6);

    public static final Map<Integer, String> INSULATION_NAMES = Map.of(1, "poor", 2, "average", 3, "good");
    public static final Map<String, Integer> INSULATION_BY_NAME = Map.of("poor", 1, "average", 2, "good", 3);

    public static final Map<Integer, String> BUILDING_SIZE_NAMES = Map.of(1, "small", 2, "medium", 3, "large");
    public static final Map<String, Integer> BUILDING_SIZE_BY_NAME = Map.of("small", 1, "medium", 2, "large", 3);

    public static final Map<Integer, String> WDR_TEMPERATURE_INFLUENCE_NAMES = Map.of(0, "off", 1, "less", 2, "medium",
            3, "more", 4, "room-control");
    public static final Map<String, Integer> WDR_TEMPERATURE_INFLUENCE_BY_NAME = Map.of("off", 0, "less", 1, "medium",
            2, "more", 3, "room-control", 4);

    /** Verified live 2026-09-13: Off/1h/2h/3h/Automatic map to 0/60/120/180/1440 minutes. */
    public static final Map<Integer, String> MAX_PREHEAT_NAMES = Map.of(0, "off", 60, "1h", 120, "2h", 180, "3h", 1440,
            "automatic");
    public static final Map<String, Integer> MAX_PREHEAT_BY_NAME = Map.of("off", 0, "1h", 60, "2h", 120, "3h", 180,
            "automatic", 1440);

    public static final Map<Integer, String> WEEKDAY_NAMES = Map.ofEntries(Map.entry(1, "monday"),
            Map.entry(2, "tuesday"), Map.entry(3, "wednesday"), Map.entry(4, "thursday"), Map.entry(5, "friday"),
            Map.entry(6, "saturday"), Map.entry(7, "sunday"));
    public static final Map<String, Integer> WEEKDAY_BY_NAME = Map.ofEntries(Map.entry("monday", 1),
            Map.entry("tuesday", 2), Map.entry("wednesday", 3), Map.entry("thursday", 4), Map.entry("friday", 5),
            Map.entry("saturday", 6), Map.entry("sunday", 7));

    /**
     * Only {@code 1=Berlin} is device-confirmed; the rest follow the app/portal dropdown's order,
     * corroborated but not individually live-tested against this device.
     */
    public static final Map<Integer, String> TIME_ZONE_NAMES = Map.ofEntries(Map.entry(0, "amsterdam"),
            Map.entry(1, "berlin"), Map.entry(2, "brussels"), Map.entry(3, "dublin"), Map.entry(4, "edinburgh"),
            Map.entry(5, "frankfurt"), Map.entry(6, "london"), Map.entry(7, "luxembourg"), Map.entry(8, "paris"),
            Map.entry(9, "rome"));
    public static final Map<String, Integer> TIME_ZONE_BY_NAME = Map.ofEntries(Map.entry("amsterdam", 0),
            Map.entry("berlin", 1), Map.entry("brussels", 2), Map.entry("dublin", 3), Map.entry("edinburgh", 4),
            Map.entry("frankfurt", 5), Map.entry("london", 6), Map.entry("luxembourg", 7), Map.entry("paris", 8),
            Map.entry("rome", 9));

    /** Verified 2026-09-13: the app's language dropdown is 0-indexed; index 4 confirmed German on this device. */
    public static final Map<Integer, String> LANGUAGE_NAMES = Map.of(0, "english", 1, "dutch", 2, "french", 3,
            "italian", 4, "german");
    public static final Map<String, Integer> LANGUAGE_BY_NAME = Map.of("english", 0, "dutch", 1, "french", 2, "italian",
            3, "german", 4);
}
