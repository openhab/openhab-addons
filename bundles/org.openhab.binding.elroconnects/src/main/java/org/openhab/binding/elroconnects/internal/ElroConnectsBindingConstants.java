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
package org.openhab.binding.elroconnects.internal;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ElroConnectsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsBindingConstants {

    static final String BINDING_ID = "elroconnects";

    // List of all Thing Type UIDs
    public static final String TYPE_ACCOUNT = "account";
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, TYPE_ACCOUNT);

    public static final String TYPE_CONNECTOR = "connector";
    public static final ThingTypeUID THING_TYPE_CONNECTOR = new ThingTypeUID(BINDING_ID, TYPE_CONNECTOR);

    public static final String TYPE_SMOKEALARM = "smokealarm";
    public static final String TYPE_COALARM = "coalarm";
    public static final String TYPE_HEATALARM = "heatalarm";
    public static final String TYPE_WATERALARM = "wateralarm";
    public static final String TYPE_ENTRYSENSOR = "entrysensor";
    public static final String TYPE_MOTIONSENSOR = "motionsensor";
    public static final String TYPE_THSENSOR = "temperaturesensor";
    public static final String TYPE_POWERSOCKET = "powersocket";
    public static final ThingTypeUID THING_TYPE_SMOKEALARM = new ThingTypeUID(BINDING_ID, TYPE_SMOKEALARM);
    public static final ThingTypeUID THING_TYPE_COALARM = new ThingTypeUID(BINDING_ID, TYPE_COALARM);
    public static final ThingTypeUID THING_TYPE_HEATALARM = new ThingTypeUID(BINDING_ID, TYPE_HEATALARM);
    public static final ThingTypeUID THING_TYPE_WATERALARM = new ThingTypeUID(BINDING_ID, TYPE_WATERALARM);
    public static final ThingTypeUID THING_TYPE_ENTRYSENSOR = new ThingTypeUID(BINDING_ID, TYPE_ENTRYSENSOR);
    public static final ThingTypeUID THING_TYPE_MOTIONSENSOR = new ThingTypeUID(BINDING_ID, TYPE_MOTIONSENSOR);
    public static final ThingTypeUID THING_TYPE_THSENSOR = new ThingTypeUID(BINDING_ID, TYPE_THSENSOR);
    public static final ThingTypeUID THING_TYPE_POWERSOCKET = new ThingTypeUID(BINDING_ID, TYPE_POWERSOCKET);

    public static final Set<ThingTypeUID> SUPPORTED_ACCOUNT_TYPE_UIDS = Set.of(THING_TYPE_ACCOUNT);
    public static final Set<ThingTypeUID> SUPPORTED_CONNECTOR_TYPES_UIDS = Set.of(THING_TYPE_CONNECTOR);
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_TYPES_UIDS = Set.of(THING_TYPE_SMOKEALARM,
            THING_TYPE_COALARM, THING_TYPE_HEATALARM, THING_TYPE_WATERALARM, THING_TYPE_ENTRYSENSOR,
            THING_TYPE_MOTIONSENSOR, THING_TYPE_THSENSOR, THING_TYPE_POWERSOCKET);
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = Stream
            .concat(SUPPORTED_ACCOUNT_TYPE_UIDS.stream(), SUPPORTED_CONNECTOR_TYPES_UIDS.stream())
            .collect(Collectors.toSet());
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(SUPPORTED_BRIDGE_TYPES_UIDS.stream(), SUPPORTED_DEVICE_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    // List of all Channel ids
    public static final String SCENE = "scene";

    public static final String SIGNAL_STRENGTH = "signal";
    public static final String BATTERY_LEVEL = "battery";
    public static final String LOW_BATTERY = "lowBattery";
    public static final String MUTE_ALARM = "muteAlarm";
    public static final String TEST_ALARM = "testAlarm";

    public static final String ENTRY = "entry";
    public static final String MOTION = "motion";

    public static final String POWER_STATE = "powerState";

    public static final String TEMPERATURE = "temperature";
    public static final String HUMIDITY = "humidity";

    public static final String ALARM = "alarm";
    public static final String SMOKE_ALARM = "smokeAlarm";
    public static final String CO_ALARM = "coAlarm";
    public static final String HEAT_ALARM = "heatAlarm";
    public static final String WATER_ALARM = "waterAlarm";
    public static final String ENTRY_ALARM = "entryAlarm";
    public static final String MOTION_ALARM = "motionAlarm";

    // Config properties
    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_CONNECTOR_ID = "connectorId";
    public static final String CONFIG_IP_ADDRESS = "ipAddress";
    public static final String CONFIG_REFRESH_INTERVAL_S = "refreshInterval";
    public static final String CONFIG_LEGACY_FIRMWARE = "legacyFirmware";
    public static final String CONFIG_DEVICE_ID = "deviceId";
    public static final String CONFIG_DEVICE_TYPE = "deviceType";

    // ELRO cmd constants
    public static final int ELRO_DEVICE_CONTROL = 101;
    public static final int ELRO_GET_DEVICE_NAME = 14;
    public static final int ELRO_GET_DEVICE_STATUSES = 15;
    public static final int ELRO_REC_DEVICE_NAME = 17;
    public static final int ELRO_REC_DEVICE_STATUS = 119;
    public static final int ELRO_SYNC_DEVICES = 29;

    public static final int ELRO_DEVICE_JOIN = 2;
    public static final int ELRO_DEVICE_CANCEL_JOIN = 7;
    public static final int ELRO_DEVICE_REPLACE = 103;
    public static final int ELRO_DEVICE_REMOVE = 104;
    public static final int ELRO_DEVICE_RENAME = 105;

    public static final int ELRO_SELECT_SCENE = 106;
    public static final int ELRO_GET_SCENE = 18;
    public static final int ELRO_REC_SCENE = 128;
    public static final int ELRO_REC_SCENE_NAME = 126;
    public static final int ELRO_REC_SCENE_TYPE = 127;
    public static final int ELRO_SYNC_SCENES = 131;

    public static final int ELRO_REC_ALARM = 25;

    public static final int ELRO_IGNORE_YES_NO = 11;

    // Older firmware uses different cmd message codes
    public static final Map<Integer, Integer> ELRO_LEGACY_MESSAGES = Map.ofEntries(Map.entry(ELRO_DEVICE_CONTROL, 1),
            Map.entry(ELRO_DEVICE_REPLACE, 3), Map.entry(ELRO_DEVICE_REMOVE, 4), Map.entry(ELRO_DEVICE_RENAME, 5),
            Map.entry(ELRO_SELECT_SCENE, 6), Map.entry(ELRO_SYNC_SCENES, 31), Map.entry(ELRO_REC_DEVICE_STATUS, 19),
            Map.entry(ELRO_REC_SCENE, 28), Map.entry(ELRO_REC_SCENE_NAME, 26), Map.entry(ELRO_REC_SCENE_TYPE, 27));
    public static final Map<Integer, Integer> ELRO_NEW_MESSAGES = ELRO_LEGACY_MESSAGES.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));

    // ELRO device types
    public static enum ElroDeviceType {
        ENTRY_SENSOR,
        CO_ALARM,
        CXSM_ALARM,
        MOTION_SENSOR,
        SM_ALARM,
        POWERSOCKET,
        THERMAL_ALARM,
        TH_SENSOR,
        WT_ALARM,
        DEFAULT
    }

    public static final Map<ElroDeviceType, ThingTypeUID> THING_TYPE_MAP = Map.ofEntries(
            Map.entry(ElroDeviceType.ENTRY_SENSOR, THING_TYPE_ENTRYSENSOR),
            Map.entry(ElroDeviceType.CO_ALARM, THING_TYPE_COALARM),
            Map.entry(ElroDeviceType.CXSM_ALARM, THING_TYPE_SMOKEALARM),
            Map.entry(ElroDeviceType.MOTION_SENSOR, THING_TYPE_MOTIONSENSOR),
            Map.entry(ElroDeviceType.SM_ALARM, THING_TYPE_SMOKEALARM),
            Map.entry(ElroDeviceType.THERMAL_ALARM, THING_TYPE_HEATALARM),
            Map.entry(ElroDeviceType.WT_ALARM, THING_TYPE_WATERALARM),
            Map.entry(ElroDeviceType.TH_SENSOR, THING_TYPE_THSENSOR),
            Map.entry(ElroDeviceType.POWERSOCKET, THING_TYPE_POWERSOCKET));

    public static final Map<ElroDeviceType, String> TYPE_NAMES = Map.ofEntries(
            Map.entry(ElroDeviceType.ENTRY_SENSOR, TYPE_ENTRYSENSOR), Map.entry(ElroDeviceType.CO_ALARM, TYPE_COALARM),
            Map.entry(ElroDeviceType.CXSM_ALARM, TYPE_SMOKEALARM),
            Map.entry(ElroDeviceType.MOTION_SENSOR, TYPE_MOTIONSENSOR),
            Map.entry(ElroDeviceType.SM_ALARM, TYPE_SMOKEALARM),
            Map.entry(ElroDeviceType.THERMAL_ALARM, TYPE_HEATALARM),
            Map.entry(ElroDeviceType.WT_ALARM, TYPE_WATERALARM), Map.entry(ElroDeviceType.TH_SENSOR, TYPE_THSENSOR),
            Map.entry(ElroDeviceType.POWERSOCKET, TYPE_POWERSOCKET));

    public static final Set<String> T_ENTRY_SENSOR = Set.of("0101", "1101", "2101");
    public static final Set<String> T_POWERSOCKET = Set.of("0200", "1200", "2200");
    public static final Set<String> T_MOTION_SENSOR = Set.of("0100", "1100", "2100");
    public static final Set<String> T_CO_ALARM = Set.of("0000", "1000", "2000", "0008", "1008", "2008", "000E", "100E",
            "200E");
    public static final Set<String> T_SM_ALARM = Set.of("0001", "1001", "2001", "0009", "1009", "2009", "000F", "100F",
            "200F");
    public static final Set<String> T_WT_ALARM = Set.of("0004", "1004", "2004", "000C", "100C", "200C", "0012", "1012",
            "2012");
    public static final Set<String> T_TH_SENSOR = Set.of("0102", "1102", "2102");
    public static final Set<String> T_CXSM_ALARM = Set.of("0005", "1109", "2109", "000D", "100D", "200D", "0013",
            "1013", "2013");
    public static final Set<String> T_THERMAL_ALARM = Set.of("0003", "1003", "2003", "000B", "100B", "200B", "0011",
            "1011", "2011");

    public static final Map<ElroDeviceType, Set<String>> DEVICE_TYPE_MAP = Map.ofEntries(
            Map.entry(ElroDeviceType.ENTRY_SENSOR, T_ENTRY_SENSOR), Map.entry(ElroDeviceType.CO_ALARM, T_CO_ALARM),
            Map.entry(ElroDeviceType.CXSM_ALARM, T_CXSM_ALARM),
            Map.entry(ElroDeviceType.MOTION_SENSOR, T_MOTION_SENSOR), Map.entry(ElroDeviceType.SM_ALARM, T_SM_ALARM),
            Map.entry(ElroDeviceType.POWERSOCKET, T_POWERSOCKET),
            Map.entry(ElroDeviceType.THERMAL_ALARM, T_THERMAL_ALARM), Map.entry(ElroDeviceType.TH_SENSOR, T_TH_SENSOR),
            Map.entry(ElroDeviceType.WT_ALARM, T_WT_ALARM));

    public static final Map<String, ElroDeviceType> TYPE_MAP = DEVICE_TYPE_MAP.entrySet().stream()
            .flatMap(e -> e.getValue().stream().map(v -> Map.entry(v, e.getKey())))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    // ELRO device status
    public static enum ElroDeviceStatus {
        NORMAL,
        TRIGGERED,
        TEST,
        SILENCE,
        OPEN,
        FAULT,
        UNDEF
    }

    // Listener threadname prefix
    public static final String THREAD_NAME_PREFIX = "binding-";
}
