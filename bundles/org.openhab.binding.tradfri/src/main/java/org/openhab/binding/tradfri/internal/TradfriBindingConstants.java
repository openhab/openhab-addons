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
package org.openhab.binding.tradfri.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TradfriBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Christoph Weitkamp - Added support for remote controller and motion sensor devices (read-only battery level)
 * @author Manuel Raffel - Added support for blinds
 * @author Vivien Boistuaud - Added support for air purifier
 */
@NonNullByDefault
public class TradfriBindingConstants {

    private static final String BINDING_ID = "tradfri";

    // List of all Thing Type UIDs
    public static final ThingTypeUID GATEWAY_TYPE_UID = new ThingTypeUID(BINDING_ID, "gateway");

    public static final ThingTypeUID THING_TYPE_ONOFF_PLUG = new ThingTypeUID(BINDING_ID, "0010");
    public static final ThingTypeUID THING_TYPE_DIMMABLE_LIGHT = new ThingTypeUID(BINDING_ID, "0100");
    public static final ThingTypeUID THING_TYPE_COLOR_TEMP_LIGHT = new ThingTypeUID(BINDING_ID, "0220");
    public static final ThingTypeUID THING_TYPE_COLOR_LIGHT = new ThingTypeUID(BINDING_ID, "0210");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "0820");
    public static final ThingTypeUID THING_TYPE_REMOTE_CONTROL = new ThingTypeUID(BINDING_ID, "0830");
    public static final ThingTypeUID THING_TYPE_MOTION_SENSOR = new ThingTypeUID(BINDING_ID, "0107");
    public static final ThingTypeUID THING_TYPE_BLINDS = new ThingTypeUID(BINDING_ID, "0202");
    public static final ThingTypeUID THING_TYPE_OPEN_CLOSE_REMOTE_CONTROL = new ThingTypeUID(BINDING_ID, "0203");
    public static final ThingTypeUID THING_TYPE_AIR_PURIFIER = new ThingTypeUID(BINDING_ID, "0007");

    public static final Set<ThingTypeUID> SUPPORTED_LIGHT_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_COLOR_TEMP_LIGHT, THING_TYPE_COLOR_LIGHT)
                    .collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_PLUG_TYPES_UIDS = Set.of(THING_TYPE_ONOFF_PLUG);

    public static final Set<ThingTypeUID> SUPPORTED_BLINDS_TYPES_UIDS = Set.of(THING_TYPE_BLINDS);

    public static final Set<ThingTypeUID> SUPPORTED_AIR_PURIFIER_TYPES_UIDS = Set.of(THING_TYPE_AIR_PURIFIER);

    // List of all Gateway Configuration Properties
    public static final String GATEWAY_CONFIG_HOST = "host";
    public static final String GATEWAY_CONFIG_PORT = "port";
    public static final String GATEWAY_CONFIG_CODE = "code";
    public static final String GATEWAY_CONFIG_IDENTITY = "identity";
    public static final String GATEWAY_CONFIG_PRE_SHARED_KEY = "preSharedKey";

    // Not yet used - included for future support
    public static final Set<ThingTypeUID> SUPPORTED_CONTROLLER_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_DIMMER, THING_TYPE_REMOTE_CONTROL,
                    THING_TYPE_OPEN_CLOSE_REMOTE_CONTROL, THING_TYPE_MOTION_SENSOR).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = Set.of(GATEWAY_TYPE_UID);

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(SUPPORTED_LIGHT_TYPES_UIDS.stream(), SUPPORTED_CONTROLLER_TYPES_UIDS.stream(),
                    SUPPORTED_PLUG_TYPES_UIDS.stream(), SUPPORTED_BLINDS_TYPES_UIDS.stream(),
                    SUPPORTED_AIR_PURIFIER_TYPES_UIDS.stream())
            .reduce(Stream::concat).orElseGet(Stream::empty).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.concat(SUPPORTED_BRIDGE_TYPES_UIDS.stream(), SUPPORTED_DEVICE_TYPES_UIDS.stream())
                    .collect(Collectors.toSet()));

    // List of all Channel IDs
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR_TEMPERATURE = "color_temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_BATTERY_LEVEL = "battery_level";
    public static final String CHANNEL_BATTERY_LOW = "battery_low";
    public static final String CHANNEL_FAN_MODE = "fan_mode";
    public static final String CHANNEL_FAN_SPEED = "fan_speed";
    public static final String CHANNEL_DISABLE_LED = "disable_led";
    public static final String CHANNEL_LOCK_BUTTON = "lock_button";
    public static final String CHANNEL_AIR_QUALITY_PM25 = "air_quality_pm25";
    public static final String CHANNEL_AIR_QUALITY_RATING = "air_quality_rating";
    public static final String CHANNEL_FILTER_CHECK_NEXT = "filter_check_next";
    public static final String CHANNEL_FILTER_CHECK_ALARM = "filter_check_alarm";
    public static final String CHANNEL_FILTER_UPTIME = "filter_uptime";

    // IPSO Objects
    public static final String DEVICES = "15001";
    public static final String AIR_PURIFIER = "15025";
    public static final String AIR_QUALITY = "5907";
    public static final String AUTH_PATH = "9063";
    public static final String BLINDS = "15015";
    public static final String CLIENT_IDENTITY_PROPOSED = "9090";
    public static final String COLOR = "5706";
    public static final String COLOR_X = "5709";
    public static final String COLOR_Y = "5710";
    public static final String COMMISSIONING_MODE = "9061";
    public static final String CREATED_AT = "9002";
    public static final String CUM_ACTIVE_POWER = "5805";
    public static final String CURRENT_TIMESTAMP = "9059";
    public static final int DEFAULT_DIMMER_TRANSITION_TIME = 5;
    public static final String DEVICE = "3";
    public static final String DIMMER = "5851";
    public static final int DIMMER_MAX = 254;
    public static final int DIMMER_MIN = 0;
    public static final String END_ACTION = "9043";
    public static final String END_TIME_HR = "9048";
    public static final String END_TIME_MN = "9049";
    public static final String ERROR_TAG = "errorcode";
    public static final String FAN_MODE = "5900";
    public static final String FAN_SPEED = "5908";
    public static final String FILTER_UPTIME = "5902";
    public static final String FORCE_CHECK_OTA_UPDATE = "9032";
    public static final String GATEWAY = "15011";
    public static final String GATEWAY_DETAILS = "15012";
    public static final String GATEWAY_NAME = "9035";
    public static final int GATEWAY_REBOOT_NOTIFICATION = 1003;
    public static final String GATEWAY_REBOOT_NOTIFICATION_TYPE = "9052";
    public static final String GATEWAY_TIME_SOURCE = "9071";
    public static final String GATEWAY_UPDATE_DETAILS_URL = "9056";
    public static final String GATEWAY_UPDATE_PROGRESS = "9055";
    public static final String GROUPS = "15004";
    public static final String GROUP_ID = "9038";
    public static final String GROUP_LINK_ARRAY = "9995";
    public static final String GROUP_SETTINGS = "9045";
    public static final String HS_ACCESSORY_LINK = "9018";
    public static final String HS_LINK = "15002";
    public static final String IKEA_MOODS = "9068";
    public static final String INSTANCE_ID = "9003";
    public static final String LAST_SEEN = "9020";
    public static final String LED_DISABLE = "5906";
    public static final String LIGHT = "3311";
    public static final int LIGHTS_OFF_SMART_TASK = 2;
    public static final String LIGHT_SETTING = "15013";
    public static final String LOCK_PHYSICAL_BUTTON = "5905";
    public static final int LOSS_OF_INTERNET_CONNECTIVITY = 5001;
    public static final String MASTER_TOKEN_TAG = "9036";
    public static final String MAX_MSR_VALUE = "5602";
    public static final String MAX_RNG_VALUE = "5604";
    public static final String MIN_MSR_VALUE = "5601";
    public static final String MIN_RNG_VALUE = "5603";
    public static final String NAME = "9001";
    public static final int NEW_FIRMWARE_AVAILABLE = 1001;
    public static final String NEW_PSK_BY_GW = "9091";
    public static final String NEXT_FILTER_CHECK = "5910";
    public static final String NOTIFICATION_EVENT = "9015";
    public static final String NOTIFICATION_NVPAIR = "9017";
    public static final String NOTIFICATION_STATE = "9014";
    public static final int NOT_AT_HOME_SMART_TASK = 1;
    public static final String NTP_SERVER = "9023";
    public static final String ONOFF = "5850";
    public static final String ON_TIME = "5852";
    public static final String OPEN = "1";
    public static final int OPTION_APP_TOKEN = 2051;
    public static final int OTA_CRITICAL = 1;
    public static final int OTA_FORCED = 5;
    public static final int OTA_NORMAL = 0;
    public static final int OTA_REQUIRED = 2;
    public static final String OTA_TYPE = "9066";
    public static final String OTA_UPDATE = "9037";
    public static final String OTA_UPDATE_STATE = "9054";
    public static final String PLUG = "3312";
    public static final String POWER_FACTOR = "5820";
    public static final String POSITION = "5536";
    public static final String REACHABILITY_STATE = "9019";
    public static final String REBOOT = "9030";
    public static final String REPEAT_DAYS = "9041";
    public static final String REPEATER = "15014";
    public static final String RESET = "9031";
    public static final String RESET_MIN_MAX_MSR = "5605";
    public static final String SCENE = "15005";
    public static final String SCENE_ACTIVATE_FLAG = "9058";
    public static final String SCENE_ID = "9039";
    public static final String SCENE_INDEX = "9057";
    public static final String SCENE_LINK = "9009";
    public static final String SENSOR = "3300";
    public static final String SENSOR_TYPE = "5751";
    public static final String SENSOR_VALUE = "5700";
    public static final String SESSION_ID = "9033";
    public static final String SESSION_LENGTH = "9064";
    public static final String SHORTCUT_ICON_REFERENCE_TYPE = "9051";
    public static final String SMART_TASK_ACTION = "9050";
    public static final String SMART_TASK_TEMPLATE = "9016";
    public static final int SMART_TASK_TRIGGERED_EVENT = 1002;
    public static final String SMART_TASK_TYPE = "9040";
    public static final String START_ACTION = "9042";
    public static final String START_TIME_HR = "9046";
    public static final String START_TIME_MN = "9047";
    public static final String STOP_TRIGGER = "5523";
    public static final String SWITCH = "15009";
    public static final String TIME_ARRAY = "9994";
    public static final String TIME_REMAINING_IN_SECONDS = "9024";
    public static final String TRANSITION_TIME = "5712";
    public static final String TRIGGER_TIME_INTERVAL = "9044";
    public static final String TYPE = "5750";
    public static final String UNIT = "5701";
    public static final String UPDATE_ACCEPTED_TIMESTAMP = "9069";
    public static final String UPDATE_FIRMWARE = "9034";
    public static final String USE_CURRENT_LIGHT_SETTINGS = "9070";
    public static final String VERSION = "9029";
    public static final int WAKE_UP_SMART_TASK = 3;

    public static final String TYPE_SWITCH = "0";
    public static final String TYPE_REMOTE = "1";
    public static final String TYPE_LIGHT = "2";
    public static final String TYPE_PLUG = "3";
    public static final String TYPE_SENSOR = "4";
    public static final String TYPE_REPEATER = "6";
    public static final String TYPE_BLINDS = "7";
    public static final String TYPE_AIR_PURIFIER = "10";

    public static final String DEVICE_VENDOR = "0";
    public static final String DEVICE_MODEL = "1";
    public static final String DEVICE_FIRMWARE = "3";
    public static final String DEVICE_BATTERY_LEVEL = "9";

    // List of Air Purifier Constants
    public static final int FAN_MODE_OFF = 0;
    public static final int FAN_MODE_AUTO = 1;
    public static final int FAN_MODE_SPEED1 = 10;
    public static final int FAN_MODE_SPEED2 = 20;
    public static final int FAN_MODE_SPEED3 = 30;
    public static final int FAN_MODE_SPEED4 = 40;
    public static final int FAN_MODE_SPEED5 = 50;

    public static final Set<Integer> AIR_PURIFIER_FANMODE = Set.of(FAN_MODE_OFF, FAN_MODE_AUTO, FAN_MODE_SPEED1,
            FAN_MODE_SPEED2, FAN_MODE_SPEED3, FAN_MODE_SPEED4, FAN_MODE_SPEED5);

    public static final int AIR_PURIFIER_AIR_QUALITY_OK = 36;
    public static final int AIR_PURIFIER_AIR_QUALITY_BAD = 86;

    public static final int AIR_PURIFIER_AIR_QUALITY_UNDEFINED = 65535;
}
