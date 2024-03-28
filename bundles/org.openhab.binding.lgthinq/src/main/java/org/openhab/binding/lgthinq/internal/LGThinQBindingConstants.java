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
package org.openhab.binding.lgthinq.internal;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LGThinQBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQBindingConstants {

    public static final String BINDING_ID = "lgthinq";
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_AIR_CONDITIONER = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.AIR_CONDITIONER.deviceTypeId());
    public static final ThingTypeUID THING_TYPE_WASHING_MACHINE = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.WASHERDRYER_MACHINE.deviceTypeId());
    public static final ThingTypeUID THING_TYPE_WASHING_TOWER = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.WASHING_TOWER.deviceTypeId());
    public static final ThingTypeUID THING_TYPE_DRYER = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.DRYER.deviceTypeId());

    public static final ThingTypeUID THING_TYPE_HEAT_PUMP = new ThingTypeUID(BINDING_ID,
            DeviceTypes.HEAT_PUMP.deviceTypeId() + "HP");

    public static final ThingTypeUID THING_TYPE_DRYER_TOWER = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.DRYER_TOWER.deviceTypeId());

    public static final ThingTypeUID THING_TYPE_FRIDGE = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.REFRIGERATOR.deviceTypeId());
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AIR_CONDITIONER,
            THING_TYPE_WASHING_MACHINE, THING_TYPE_WASHING_TOWER, THING_TYPE_DRYER_TOWER, THING_TYPE_DRYER,
            THING_TYPE_FRIDGE, THING_TYPE_BRIDGE, THING_TYPE_HEAT_PUMP);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_AIR_CONDITIONER,
            THING_TYPE_WASHING_MACHINE, THING_TYPE_WASHING_TOWER, THING_TYPE_DRYER, THING_TYPE_DRYER_TOWER,
            THING_TYPE_HEAT_PUMP);
    public static final String THING_STATUS_DETAIL_DISCONNECTED = "Device is Disconnected";
    // Max number of retries trying to get the monitor (V1) until consider ERROR in the connection
    public static final Integer MAX_GET_MONITOR_RETRIES = 3;
    public static String THINQ_USER_DATA_FOLDER = OpenHAB.getUserDataFolder() + File.separator + "thinq";
    public static String THINQ_CONNECTION_DATA_FILE = THINQ_USER_DATA_FOLDER + File.separator + "thinqbridge-%s.json";
    public static String BASE_CAP_CONFIG_DATA_FILE = THINQ_USER_DATA_FOLDER + File.separator + "thinq-%s-cap.json";
    public static final String V2_AUTH_PATH = "/oauth/1.0/oauth2/token";
    public static final String V2_USER_INFO = "/users/profile";
    public static final String V2_API_KEY = "VGhpblEyLjAgU0VSVklDRQ==";
    public static final String V2_CLIENT_ID = "65260af7e8e6547b51fdccf930097c51eb9885a508d3fddfa9ee6cdec22ae1bd";
    public static final String V2_SVC_PHASE = "OP";
    public static final String V2_APP_LEVEL = "PRD";
    public static final String V2_APP_OS = "LINUX";
    public static final String V2_APP_TYPE = "NUTS";
    public static final String V2_APP_VER = "3.0.1700";
    public static final String V2_SESSION_LOGIN_PATH = "/emp/v2.0/account/session/";
    public static final String V2_LS_PATH = "/service/application/dashboard";
    public static final String V2_DEVICE_CONFIG_PATH = "service/devices/";
    public static final String V2_CTRL_DEVICE_CONFIG_PATH = "service/devices/%s/%s";
    public static final String V1_START_MON_PATH = "rti/rtiMon";
    public static final String V1_MON_DATA_PATH = "rti/rtiResult";
    public static final String V1_CONTROL_OP = "rti/rtiControl";
    public static final String OAUTH_SEARCH_KEY_PATH = "/searchKey";
    public static final String GATEWAY_SERVICE_PATH_V2 = "/v1/service/application/gateway-uri";
    public static final String GATEWAY_URL_V2 = "https://route.lgthinq.com:46030" + GATEWAY_SERVICE_PATH_V2;
    public static final String PRE_LOGIN_PATH = "/preLogin";
    public static final String SECURITY_KEY = "nuts_securitykey";
    public static final String SVC_CODE = "SVC202";
    public static final String OAUTH_SECRET_KEY = "c053c2a6ddeb7ad97cb0eed0dcb31cf8";
    public static final String OAUTH_CLIENT_KEY = "LGAO722A02";
    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss +0000";
    public static final String APPLICATION_KEY = "6V1V8H2BN5P9ZQGOI5DAQ92YZBDO3EK9";
    public static final String V2_EMP_SESS_PATH = "/emp/oauth2/token/empsession";
    public static final String V2_EMP_SESS_URL = "https://emp-oauth.lgecloud.com" + V2_EMP_SESS_PATH;
    public static final String API_KEY_V2 = "VGhpblEyLjAgU0VSVklDRQ==";

    // the client id is a SHA512 hash of the phone MFR,MODEL,SERIAL,
    // and the build id of the thinq app it can also just be a random
    // string, we use the same client id used for oauth
    public static final String CLIENT_ID = "LGAO221A02";
    public static final String MESSAGE_ID = "wideq";
    public static final String SVC_PHASE = "OP";
    public static final String APP_LEVEL = "PRD";
    public static final String APP_OS = "ANDROID";
    public static final String APP_TYPE = "NUTS";
    public static final String APP_VER = "3.5.1200";

    public static final String DEVICE_ID = "device_id";
    public static final String MODEL_NAME = "model_name";
    public static final String DEVICE_ALIAS = "device_alias";
    public static final String MODEL_URL_INFO = "model_url_info";
    public static final String PLATFORM_TYPE = "platform_type";
    public static final String PLATFORM_TYPE_V1 = "thinq1";
    public static final String PLATFORM_TYPE_V2 = "thinq2";
    static final Set<String> SUPPORTED_LG_PLATFORMS = Set.of(PLATFORM_TYPE_V1, PLATFORM_TYPE_V2);

    public static final int SEARCH_TIME = 20;
    // delay between each devices's scan for state changes (in seconds)
    public static final int DEFAULT_STATE_POLLING_UPDATE_DELAY = 10;

    // ====================== FRIDGE DEVICE CONSTANTS =============================
    // CHANNEL IDS
    // public static final String CHANNEL_MOD_OP_ID = "op_mode";
    // public static final String CHANNEL_FAN_SPEED_ID = "fan_speed";
    // public static final String CHANNEL_TARGET_TEMP_ID = "target_temperature";
    // public static final String CHANNEL_CURRENT_TEMP_ID = "current_temperature";
    // public static final String CHANNEL_COOL_JET_ID = "cool_jet";
    public static final Double FRIDGE_TEMPERATURE_IGNORE_VALUE = 255.0;
    public static final Double FREEZER_TEMPERATURE_IGNORE_VALUE = 255.0;
    public static final String CHANNEL_FRIDGE_TEMP_ID = "fridge-temperature";
    public static final String CHANNEL_FREEZER_TEMP_ID = "freezer-temperature";
    public static final String CHANNEL_REF_TEMP_UNIT = "temp-unit";
    public static final String TEMP_UNIT_CELSIUS = "CELSIUS";
    public static final String TEMP_UNIT_FAHRENHEIT = "FAHRENHEIT";
    public static final String TEMP_UNIT_CELSIUS_SYMBOL = "°C";
    public static final String TEMP_UNIT_FAHRENHEIT_SYMBOL = "°F";
    public static final String FRIDGE_TEMP_NODE_NAME_V2 = "fridgeTemp";
    public static final String FRIDGE_TEMP_NODE_NAME_V1 = "TempRefrigerator";
    public static final String REFRIGERATOR_SNAPSHOT_NODE_V2 = "refState";

    // ====================== AIR CONDITIONER DEVICE CONSTANTS =============================
    // CHANNEL IDS
    public static final String CHANNEL_MOD_OP_ID = "op_mode";
    public static final String CHANNEL_FAN_SPEED_ID = "fan_speed";
    public static final String CHANNEL_POWER_ID = "power";
    public static final String CHANNEL_TARGET_TEMP_ID = "target_temperature";
    public static final String CHANNEL_CURRENT_TEMP_ID = "current_temperature";
    public static final String CHANNEL_COOL_JET_ID = "cool_jet";
    public static final String CHANNEL_AIR_CLEAN_ID = "air_clean";
    public static final String CHANNEL_AUTO_DRY_ID = "auto_dry";
    public static final String CHANNEL_ENERGY_SAVING_ID = "energy_saving";

    public static final Map<String, String> CAP_AC_OP_MODE = Map.of("@AC_MAIN_OPERATION_MODE_COOL_W", "Cool",
            "@AC_MAIN_OPERATION_MODE_DRY_W", "Dry", "@AC_MAIN_OPERATION_MODE_FAN_W", "Fan",
            "@AC_MAIN_OPERATION_MODE_HEAT_W", "Heat", "@AC_MAIN_OPERATION_MODE_AIRCLEAN_W", "Air Clean",
            "@AC_MAIN_OPERATION_MODE_ACO_W", "Auto", "@AC_MAIN_OPERATION_MODE_AI_W", "AI",
            "@AC_MAIN_OPERATION_MODE_ENERGY_SAVING_W", "Eco", "@AC_MAIN_OPERATION_MODE_AROMA_W", "Aroma",
            "@AC_MAIN_OPERATION_MODE_ANTIBUGS_W", "Anti Bugs");

    public static final Map<String, String> CAP_AC_FAN_SPEED = Map.ofEntries(
            Map.entry("@AC_MAIN_WIND_STRENGTH_SLOW_W", "Slow"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_SLOW_LOW_W", "Slower"), Map.entry("@AC_MAIN_WIND_STRENGTH_LOW_W", "Low"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_LOW_MID_W", "Low Mid"), Map.entry("@AC_MAIN_WIND_STRENGTH_MID_W", "Mid"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_MID_HIGH_W", "Mid High"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_HIGH_W", "High"), Map.entry("@AC_MAIN_WIND_STRENGTH_POWER_W", "Power"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_AUTO_W", "Auto"), Map.entry("@AC_MAIN_WIND_STRENGTH_NATURE_W", "Auto"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_LOW_RIGHT_W", "Right Low"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_MID_RIGHT_W", "Right Mid"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_HIGH_RIGHT_W", "Right High"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_LOW_LEFT_W", "Left Low"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_MID_LEFT_W", "Left Mid"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_HIGH_LEFT_W", "Left High"));

    public static final Map<String, String> CAP_AC_COOL_JET = Map.of("@COOL_JET", "Cool Jet");
    // ======= RAC MODES
    public static final String CAP_AC_AUTODRY = "@AUTODRY";
    public static final String CAP_AC_AUTODRY_NODE = "AutoDry";
    public static final String CAP_AC_ENERGYSAVING = "@ENERGYSAVING";
    public static final String CAP_AC_AIRCLEAN = "@AIRCLEAN";
    // ====================
    public static final String CAP_AC_COMMAND_OFF = "@OFF";
    public static final String CAP_AC_COMMAND_ON = "@ON";

    public static final String CAP_AC_AIR_CLEAN_COMMAND_ON = "@AC_MAIN_AIRCLEAN_ON_W";
    public static final String CAP_AC_AIR_CLEAN_COMMAND_OFF = "@AC_MAIN_AIRCLEAN_OFF_W";

    // ====================== WASHING MACHINE CONSTANTS =============================
    public static final String WM_POWER_OFF_VALUE = "POWEROFF";
    public static final String WM_SNAPSHOT_WASHER_DRYER_NODE_V2 = "washerDryer";
    public static final String WM_CHANNEL_STATE_ID = "state";
    public static final String WM_CHANNEL_COURSE_ID = "course";
    public static final String WM_CHANNEL_SMART_COURSE_ID = "smart-course";
    public static final String WM_CHANNEL_DOWNLOADED_COURSE_ID = "downloaded-course";
    public static final String WM_CHANNEL_TEMP_LEVEL_ID = "temperature-level";
    public static final String WM_CHANNEL_DOOR_LOCK_ID = "door-lock";

    public static final String WM_CHANNEL_RINSE_ID = "rinse";

    public static final String WM_CHANNEL_SPIN_ID = "spin";

    public static final String WM_CHANNEL_LAUNCH_REMOTE_START_ID = "launch-remote-start";
    public static final String WM_CHANNEL_REMOTE_START_ID = "washer-remote-start";
    public static final String WM_CHANNEL_STAND_BY_ID = "washer-stand-by";
    public static final String WM_CHANNEL_REMAIN_TIME_ID = "remain-time";
    public static final String WM_CHANNEL_DELAY_TIME_ID = "delay-time";

    public static final Map<String, String> CAP_WP_STATE = Map.ofEntries(Map.entry("@WM_STATE_POWER_OFF_W", "Off"),
            Map.entry("@WM_STATE_INITIAL_W", "Initial"), Map.entry("@WM_STATE_PAUSE_W", "Pause"),
            Map.entry("@WM_STATE_RESERVE_W", "Reserved"), Map.entry("@WM_STATE_DETECTING_W", "Detecting"),
            Map.entry("@WM_STATE_RUNNING_W", "Running"), Map.entry("@WM_STATE_RINSING_W", "Rinsing"),
            Map.entry("@WM_STATE_SPINNING_W", "Spinning"), Map.entry("@WM_STATE_COOLDOWN_W", "Cool Down"),
            Map.entry("@WM_STATE_RINSEHOLD_W", "Rinse Hold"), Map.entry("@WM_STATE_WASH_REFRESHING_W", "Refreshing"),
            Map.entry("@WM_STATE_STEAMSOFTENING_W", "Steam Softening"), Map.entry("@WM_STATE_END_W", "End"),
            Map.entry("@WM_STATE_DRYING_W", "Drying"), Map.entry("@WM_STATE_DEMO_W", "Demonstration"),
            Map.entry("@WM_STATE_ERROR_W", "Error"));

    public static final Map<String, String> CAP_WP_STATE_V1 = Map.ofEntries(Map.entry("@WM_STATE_POWER_OFF_W", "Off"),
            Map.entry("@WM_STATE_INITIAL_W", "Initial"), Map.entry("@WM_STATE_PAUSE_W", "Pause"),
            Map.entry("@WM_STATE_RESERVE_W", "Reserved"), Map.entry("@WM_STATE_DETECTING_W", "Detecting"),
            Map.entry("@WM_STATE_RUNNING_W", "Running"), Map.entry("@WM_STATE_RINSING_W", "Rinsing"),
            Map.entry("@WM_STATE_SPINNING_W", "Spinning"), Map.entry("@WM_STATE_COOLDOWN_W", "Cool Down"),
            Map.entry("@WM_STATE_RINSEHOLD_W", "Rinse Hold"), Map.entry("@WM_STATE_WASH_REFRESHING_W", "Refreshing"),
            Map.entry("@WM_STATE_STEAMSOFTENING_W", "Steam Softening"), Map.entry("@WM_STATE_END_W", "End"),
            Map.entry("@WM_STATE_DRYING_W", "Drying"), Map.entry("@WM_STATE_DEMO_W", "Demonstration"),
            Map.entry("@WM_STATE_ERROR_W", "Error"));

    public static final Map<String, String> CAP_WP_TEMPERATURE = Map.ofEntries(
            Map.entry("@WM_TERM_NO_SELECT_W", "Not Selected"), Map.entry("@WM_TITAN2_OPTION_TEMP_20_W", "20"),
            Map.entry("@WM_TITAN2_OPTION_TEMP_COLD_W", "Cold"), Map.entry("@WM_TITAN2_OPTION_TEMP_30_W", "30"),
            Map.entry("@WM_TITAN2_OPTION_TEMP_40_W", "40"), Map.entry("@WM_TITAN2_OPTION_TEMP_50_W", "50"),
            Map.entry("@WM_TITAN2_OPTION_TEMP_60_W", "60"), Map.entry("@WM_TITAN2_OPTION_TEMP_95_W", "95"));

    public static final Map<String, String> CAP_WP_SPIN = Map.ofEntries(
            Map.entry("@WM_TERM_NO_SELECT_W", "Not Selected"), Map.entry("@M_TITAN2_OPTION_SPIN_NO_SPIN_W", "No Spin"),
            Map.entry("@WM_TITAN2_OPTION_SPIN_400_W", "400"), Map.entry("@WM_TITAN2_OPTION_SPIN_600_W", "600"),
            Map.entry("@WM_TITAN2_OPTION_SPIN_700_W", "700"), Map.entry("@WM_TITAN2_OPTION_SPIN_800_W", "800"),
            Map.entry("@WM_TITAN2_OPTION_SPIN_900_W", "900"), Map.entry("@WM_TITAN2_OPTION_SPIN_1000_W", "1000"),
            Map.entry("@WM_TITAN2_OPTION_SPIN_1100_W", "1100"), Map.entry("@WM_TITAN2_OPTION_SPIN_1200_W", "1200"),
            Map.entry("@WM_TITAN2_OPTION_SPIN_1400_W", "1400"), Map.entry("@WM_TITAN2_OPTION_SPIN_1600_W", "1600"),
            Map.entry("@WM_TITAN2_OPTION_SPIN_MAX_W", "Max Spin"));

    public static final Map<String, String> CAP_WP_RINSE = Map.ofEntries(
            Map.entry("@WM_TERM_NO_SELECT_W", "Not Selected"), Map.entry("@WM_TITAN2_OPTION_RINSE_NORMAL_W", "Normal"),
            Map.entry("@WM_TITAN2_OPTION_RINSE_RINSE+_W", "Plus"),
            Map.entry("@WM_TITAN2_OPTION_RINSE_RINSE++_W", "Plus +"),
            Map.entry("@WM_TITAN2_OPTION_RINSE_NORMALHOLD_W", "Normal Hold"),
            Map.entry("@WM_TITAN2_OPTION_SPIN_800_W", "800"),
            Map.entry("@WM_TITAN2_OPTION_RINSE_RINSE+HOLD_W", "Plus Hold"));
    public static final String WM_COMMAND_REMOTE_START_V2 = "WMStart";
    // ==============================================================================

    // ======================== DRYER CONSTANTS ============================

    public static final String DR_CHANNEL_STATE_ID = "state";
    public static final String DR_CHANNEL_COURSE_ID = "course";
    public static final String DR_CHANNEL_SMART_COURSE_ID = "smart-course";
    public static final String DR_CHANNEL_DRY_LEVEL_ID = "dry-level";
    public static final String DR_CHANNEL_PROCESS_STATE_ID = "process-state";
    public static final String DR_CHANNEL_CHILD_LOCK_ID = "child-lock";
    public static final String DR_CHANNEL_REMAIN_TIME_ID = "remain-time";
    public static final String DR_CHANNEL_ERROR_ID = "error";

    public static final Map<String, String> CAP_DR_PROCESS_STATE = Map.ofEntries(
            Map.entry("@WM_STATE_DETECTING_W", "Detecting"), Map.entry("@WM_STATE_STEAM_W", "Steam"),
            Map.entry("@WM_STATE_DRY_W", "Drying"), Map.entry("@WM_STATE_COOLING_W", "Cool"),
            Map.entry("@WM_STATE_ANTI_CREASE_W", "Anti Crease"), Map.entry("@WM_STATE_END_W", "End"));

    public static final Map<String, String> CAP_DR_STATE = Map.ofEntries(Map.entry("@WM_STATE_POWER_OFF_W", "Off"),
            Map.entry("@WM_STATE_INITIAL_W", "Initial"), Map.entry("@WM_STATE_PAUSE_W", "Pause"),
            Map.entry("@WM_STATE_RUNNING_W", "Running"), Map.entry("@WM_STATE_RESERVE_W", "Reserved"),
            Map.entry("@WM_STATE_ERROR_W", "Error"), Map.entry("@WM_STATE_SMART_DIAGNOSIS_W", "Smart Diagnosis"));

    public static final Map<String, String> CAP_DR_DRY_LEVEL = Map.ofEntries(Map.entry("-", "-"),
            Map.entry("@WM_DRY24_DRY_LEVEL_IRON_W", "Iron"), Map.entry("@WM_DRY24_DRY_LEVEL_CUPBOARD_W", "Cup Board"),
            Map.entry("@WM_DRY24_DRY_LEVEL_EXTRA_W", "Extra"));
}
