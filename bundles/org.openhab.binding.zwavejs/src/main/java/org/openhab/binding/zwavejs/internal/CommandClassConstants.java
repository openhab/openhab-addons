/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Equipment;

/**
 * Contains the Z-Wave predefined command class constants.
 * Used for semantic tagging and differentiation of control functions.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class CommandClassConstants {

    public static final Integer COMMAND_CLASS_ALARM = 0x71;
    public static final Integer COMMAND_CLASS_ANTITHEFT = 0x5D;
    public static final Integer COMMAND_CLASS_ANTITHEFT_UNLOCK = 0x7E;
    public static final Integer COMMAND_CLASS_APPLICATION_STATUS = 0x22;
    public static final Integer COMMAND_CLASS_ASSOCIATION = 0x85;
    public static final Integer COMMAND_CLASS_ASSOCIATION_COMMAND_CONFIGURATION = 0x9B;
    public static final Integer COMMAND_CLASS_ASSOCIATION_GRP_INFO = 0x59;
    public static final Integer COMMAND_CLASS_AUTHENTICATION = 0xA1;
    public static final Integer COMMAND_CLASS_AUTHENTICATION_MEDIA_WRITE = 0xA2;
    public static final Integer COMMAND_CLASS_BARRIER_OPERATOR = 0x66;
    public static final Integer COMMAND_CLASS_BASIC = 0x20;
    public static final Integer COMMAND_CLASS_BASIC_TARIFF_INFO = 0x36;
    public static final Integer COMMAND_CLASS_BATTERY = 0x80;
    public static final Integer COMMAND_CLASS_CENTRAL_SCENE = 0x5B;
    public static final Integer COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE = 0x46;
    public static final Integer COMMAND_CLASS_CLOCK = 0x81;
    public static final Integer COMMAND_CLASS_CONFIGURATION = 0x70;
    public static final Integer COMMAND_CLASS_CONTROLLER_REPLICATION = 0x21;
    public static final Integer COMMAND_CLASS_CRC_16_ENCAP = 0x56;
    public static final Integer COMMAND_CLASS_DCP_CONFIG = 0x3A;
    public static final Integer COMMAND_CLASS_DCP_MONITOR = 0x3B;
    public static final Integer COMMAND_CLASS_DEVICE_RESET_LOCALLY = 0x5A;
    public static final Integer COMMAND_CLASS_DOOR_LOCK = 0x62;
    public static final Integer COMMAND_CLASS_DOOR_LOCK_LOGGING = 0x4C;
    public static final Integer COMMAND_CLASS_ENERGY_PRODUCTION = 0x90;
    public static final Integer COMMAND_CLASS_ENTRY_CONTROL = 0x6F;
    public static final Integer COMMAND_CLASS_FIRMWARE_UPDATE_MD = 0x7A;
    public static final Integer COMMAND_CLASS_GENERIC_SCHEDULE = 0xA3;
    public static final Integer COMMAND_CLASS_GEOGRAPHIC_LOCATION = 0x8C;
    public static final Integer COMMAND_CLASS_GROUPING_NAME = 0x7B;
    public static final Integer COMMAND_CLASS_HRV_CONTROL = 0x39;
    public static final Integer COMMAND_CLASS_HRV_STATUS = 0x37;
    public static final Integer COMMAND_CLASS_HUMIDITY_CONTROL_MODE = 0x6D;
    public static final Integer COMMAND_CLASS_HUMIDITY_CONTROL_OPERATING_STATE = 0x6E;
    public static final Integer COMMAND_CLASS_HUMIDITY_CONTROL_SETPOINT = 0x64;
    public static final Integer COMMAND_CLASS_INCLUSION_CONTROLLER = 0x74;
    public static final Integer COMMAND_CLASS_INDICATOR = 0x87;
    public static final Integer COMMAND_CLASS_IP_ASSOCIATION = 0x5C;
    public static final Integer COMMAND_CLASS_IR_REPEATER = 0xA0;
    public static final Integer COMMAND_CLASS_IRRIGATION = 0x6B;
    public static final Integer COMMAND_CLASS_LANGUAGE = 0x89;
    public static final Integer COMMAND_CLASS_LOCK = 0x76;
    public static final Integer COMMAND_CLASS_MAILBOX = 0x69;
    public static final Integer COMMAND_CLASS_MANUFACTURER_PROPRIETARY = 0x91;
    public static final Integer COMMAND_CLASS_MANUFACTURER_SPECIFIC = 0x72;
    public static final Integer COMMAND_CLASS_MARK = 0xEF;
    public static final Integer COMMAND_CLASS_METER = 0x32;
    public static final Integer COMMAND_CLASS_METER_PULSE = 0x35;
    public static final Integer COMMAND_CLASS_METER_TBL_CONFIG = 0x3C;
    public static final Integer COMMAND_CLASS_METER_TBL_MONITOR = 0x3D;
    public static final Integer COMMAND_CLASS_METER_TBL_PUSH = 0x3E;
    public static final Integer COMMAND_CLASS_MULTI_CHANNEL = 0x60;
    public static final Integer COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION = 0x8E;
    public static final Integer COMMAND_CLASS_MULTI_CMD = 0x8F;
    public static final Integer COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC = 0x4D;
    public static final Integer COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION = 0x34;
    public static final Integer NETWORK_MANAGEMENT_INSTALLATION_MAINTENANCE = 0x67;
    public static final Integer COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY = 0x52;
    public static final Integer COMMAND_CLASS_NO_OPERATION = 0x00;
    public static final Integer COMMAND_CLASS_NODE_NAMING = 0x77;
    public static final Integer COMMAND_CLASS_NODE_PROVISIONING = 0x78;
    public static final Integer COMMAND_CLASS_POWERLEVEL = 0x73;
    public static final Integer COMMAND_CLASS_PREPAYMENT = 0x3F;
    public static final Integer COMMAND_CLASS_PREPAYMENT_ENCAPSULATION = 0x41;
    public static final Integer COMMAND_CLASS_PROPRIETARY = 0x88;
    public static final Integer COMMAND_CLASS_PROTECTION = 0x75;
    public static final Integer COMMAND_CLASS_RATE_TBL_CONFIG = 0x48;
    public static final Integer COMMAND_CLASS_RATE_TBL_MONITOR = 0x49;
    public static final Integer COMMAND_CLASS_SCENE_ACTIVATION = 0x2B;
    public static final Integer COMMAND_CLASS_SCENE_ACTUATOR_CONF = 0x2C;
    public static final Integer COMMAND_CLASS_SCENE_CONTROLLER_CONF = 0x2D;
    public static final Integer COMMAND_CLASS_SCHEDULE = 0x53;
    public static final Integer COMMAND_CLASS_SCHEDULE_ENTRY_LOCK = 0x4E;
    public static final Integer COMMAND_CLASS_SCREEN_ATTRIBUTES = 0x93;
    public static final Integer COMMAND_CLASS_SCREEN_MD = 0x92;
    public static final Integer COMMAND_CLASS_SECURITY = 0x98;
    public static final Integer COMMAND_CLASS_SECURITY_2 = 0x9F;
    public static final Integer COMMAND_CLASS_SECURITY_SCHEME0_MARK = 0xF100;
    public static final Integer COMMAND_CLASS_SENSOR_ALARM = 0x9C;
    public static final Integer COMMAND_CLASS_SENSOR_BINARY = 0x30;
    public static final Integer COMMAND_CLASS_SENSOR_MULTILEVEL = 0x31;
    public static final Integer COMMAND_CLASS_SILENCE_ALARM = 0x9D;
    public static final Integer COMMAND_CLASS_SIMPLE_AV_CONTROL = 0x94;
    public static final Integer COMMAND_CLASS_SOUND_SWITCH = 0x79;
    public static final Integer COMMAND_CLASS_SUPERVISION = 0x6C;
    public static final Integer COMMAND_CLASS_SWITCH_BINARY = 0x25;
    public static final Integer COMMAND_CLASS_SWITCH_COLOR = 0x33;
    public static final Integer COMMAND_CLASS_SWITCH_MULTILEVEL = 0x26;
    public static final Integer COMMAND_CLASS_SWITCH_TOGGLE_MULTILEVEL = 0x29;
    public static final Integer COMMAND_CLASS_TARIFF_CONFIG = 0x4A;
    public static final Integer COMMAND_CLASS_TARIFF_TBL_MONITOR = 0x4B;
    public static final Integer COMMAND_CLASS_THERMOSTAT_FAN_MODE = 0x44;
    public static final Integer COMMAND_CLASS_THERMOSTAT_FAN_STATE = 0x45;
    public static final Integer COMMAND_CLASS_THERMOSTAT_MODE = 0x40;
    public static final Integer COMMAND_CLASS_THERMOSTAT_OPERATING_STATE = 0x42;
    public static final Integer COMMAND_CLASS_THERMOSTAT_SETBACK = 0x47;
    public static final Integer COMMAND_CLASS_THERMOSTAT_SETPOINT = 0x43;
    public static final Integer COMMAND_CLASS_TIME = 0x8A;
    public static final Integer COMMAND_CLASS_TIME_PARAMETERS = 0x8B;
    public static final Integer COMMAND_CLASS_TRANSPORT_SERVICE = 0x55;
    public static final Integer COMMAND_CLASS_USER_CODE = 0x63;
    public static final Integer COMMAND_CLASS_VERSION = 0x86;
    public static final Integer COMMAND_CLASS_WAKE_UP = 0x84;
    public static final Integer COMMAND_CLASS_WINDOW_COVERING = 0x6A;
    public static final Integer COMMAND_CLASS_ZIP = 0x23;
    public static final Integer COMMAND_CLASS_ZIP_6LOWPAN = 0x4F;
    public static final Integer COMMAND_CLASS_ZIP_GATEWAY = 0x5F;
    public static final Integer COMMAND_CLASS_ZIP_NAMING = 0x68;
    public static final Integer COMMAND_CLASS_ZIP_ND = 0x58;
    public static final Integer COMMAND_CLASS_ZIP_PORTAL = 0x61;
    public static final Integer COMMAND_CLASS_ZWAVEPLUS_INFO = 0x5E;

    //@formatter:off
    public static final Set<Integer> COMMAND_SET_ALARM_DEVICE_EQUIPMENT = Set.of(
        COMMAND_CLASS_ALARM,
        COMMAND_CLASS_ANTITHEFT,
        COMMAND_CLASS_ANTITHEFT_UNLOCK,
        COMMAND_CLASS_SECURITY,
        COMMAND_CLASS_SECURITY_2,
        COMMAND_CLASS_SECURITY_SCHEME0_MARK,
        COMMAND_CLASS_SENSOR_ALARM,
        COMMAND_CLASS_SILENCE_ALARM);

    public static final Set<Integer> COMMAND_SET_AUDIO_VISUAL_EQUIPMENT = Set.of(
        COMMAND_CLASS_SOUND_SWITCH);

    public static final Set<Integer> COMMAND_SET_BATTERY_EQUIPMENT = Set.of(
        COMMAND_CLASS_BATTERY);

    public static final Set<Integer> COMMAND_SET_CONTROL_DEVICE_EQUIPMENT = Set.of(
        COMMAND_CLASS_SWITCH_BINARY);

    public static final Set<Integer> COMMAND_SET_DISPLAY_EQUIPMENT = Set.of(
        COMMAND_CLASS_INDICATOR);

    public static final Set<Integer> COMMAND_SET_GATE_EQUIPMENT = Set.of(
        COMMAND_CLASS_BARRIER_OPERATOR);

    public static final Set<Integer> COMMAND_SET_HUMIDIFIER_EQUIPMENT = Set.of(
        COMMAND_CLASS_HUMIDITY_CONTROL_MODE,
        COMMAND_CLASS_HUMIDITY_CONTROL_OPERATING_STATE,
        COMMAND_CLASS_HUMIDITY_CONTROL_SETPOINT);

    public static final Set<Integer> COMMAND_SET_HVAC_EQUIPMENT = Set.of(
        COMMAND_CLASS_HRV_STATUS,
        COMMAND_CLASS_HRV_CONTROL);

    public static final Set<Integer> COMMAND_SET_IRRIGATION_EQUIPMENT = Set.of(
        COMMAND_CLASS_IRRIGATION);

    public static final Set<Integer> COMMAND_SET_LIGHT_SOURCE_EQUIPMENT = Set.of(
        COMMAND_CLASS_SWITCH_COLOR,
        COMMAND_CLASS_SWITCH_MULTILEVEL,
        COMMAND_CLASS_SWITCH_TOGGLE_MULTILEVEL);

    public static final Set<Integer> COMMAND_SET_LOCK_EQUIPMENT = Set.of(
        COMMAND_CLASS_DOOR_LOCK,
        COMMAND_CLASS_DOOR_LOCK_LOGGING,
        COMMAND_CLASS_ENTRY_CONTROL);

    public static final Set<Integer> COMMAND_SET_METER_EQUIPMENT = Set.of(
        COMMAND_CLASS_BASIC_TARIFF_INFO,
        COMMAND_CLASS_METER,
        COMMAND_CLASS_METER_PULSE,
        COMMAND_CLASS_METER_TBL_CONFIG,
        COMMAND_CLASS_METER_TBL_MONITOR,
        COMMAND_CLASS_METER_TBL_PUSH,
        COMMAND_CLASS_POWERLEVEL,
        COMMAND_CLASS_TARIFF_CONFIG,
        COMMAND_CLASS_TARIFF_TBL_MONITOR);

    public static final Set<Integer> COMMAND_SET_POWER_SUPPLY_EQUIPMENT = Set.of(
        COMMAND_CLASS_ENERGY_PRODUCTION);

    public static final Set<Integer> COMMAND_SET_SENSOR_EQUIPMENT = Set.of(
        COMMAND_CLASS_SENSOR_BINARY,
        COMMAND_CLASS_SENSOR_MULTILEVEL);

    public static final Set<Integer> COMMAND_SET_THERMOSTAT_EQUIPMENT = Set.of(
        COMMAND_CLASS_THERMOSTAT_FAN_MODE,
        COMMAND_CLASS_THERMOSTAT_FAN_STATE,
        COMMAND_CLASS_THERMOSTAT_MODE,
        COMMAND_CLASS_THERMOSTAT_OPERATING_STATE,
        COMMAND_CLASS_THERMOSTAT_SETBACK,
        COMMAND_CLASS_THERMOSTAT_SETPOINT);

    public static final Set<Integer> COMMAND_SET_WINDOW_COVERING_EQUIPMENT = Set.of(
        COMMAND_CLASS_WINDOW_COVERING);

    public static final Set<Integer> COMMAND_SET_ZONE_EQUIPMENT = Set.of(
        COMMAND_CLASS_SCENE_ACTIVATION);

    //@formatter:on`

    // Map of command class sets to their corresponding equipment tags
    public static final Map<Set<Integer>, SemanticTag> EQUIPMENT_MAP = Map.ofEntries(
            Map.entry(COMMAND_SET_LIGHT_SOURCE_EQUIPMENT, Equipment.LIGHT_SOURCE),
            Map.entry(COMMAND_SET_ALARM_DEVICE_EQUIPMENT, Equipment.ALARM_DEVICE),
            Map.entry(COMMAND_SET_AUDIO_VISUAL_EQUIPMENT, Equipment.AUDIO_VISUAL),
            Map.entry(COMMAND_SET_BATTERY_EQUIPMENT, Equipment.BATTERY),
            Map.entry(COMMAND_SET_CONTROL_DEVICE_EQUIPMENT, Equipment.CONTROL_DEVICE),
            Map.entry(COMMAND_SET_DISPLAY_EQUIPMENT, Equipment.DISPLAY),
            Map.entry(COMMAND_SET_GATE_EQUIPMENT, Equipment.GATE),
            Map.entry(COMMAND_SET_HUMIDIFIER_EQUIPMENT, Equipment.HUMIDIFIER),
            Map.entry(COMMAND_SET_HVAC_EQUIPMENT, Equipment.HVAC),
            Map.entry(COMMAND_SET_IRRIGATION_EQUIPMENT, Equipment.IRRIGATION),
            Map.entry(COMMAND_SET_LOCK_EQUIPMENT, Equipment.LOCK),
            Map.entry(COMMAND_SET_METER_EQUIPMENT, Equipment.ELECTRIC_METER),
            Map.entry(COMMAND_SET_POWER_SUPPLY_EQUIPMENT, Equipment.POWER_SUPPLY),
            Map.entry(COMMAND_SET_SENSOR_EQUIPMENT, Equipment.SENSOR),
            Map.entry(COMMAND_SET_THERMOSTAT_EQUIPMENT, Equipment.THERMOSTAT),
            Map.entry(COMMAND_SET_WINDOW_COVERING_EQUIPMENT, Equipment.WINDOW_COVERING),
            Map.entry(COMMAND_SET_ZONE_EQUIPMENT, Equipment.ZONE));
}
