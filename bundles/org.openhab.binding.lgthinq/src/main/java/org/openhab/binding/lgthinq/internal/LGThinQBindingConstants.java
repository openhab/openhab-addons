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
package org.openhab.binding.lgthinq.internal;

import static java.util.Map.entry;

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

    public static final String CONFIG_DESCRIPTION_URI_CHANNEL = "channel-type:thinq:config";
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final String PROPERTY_VENDOR_NAME = "LG Thinq";
    public static final ThingTypeUID THING_TYPE_AIR_CONDITIONER = new ThingTypeUID(BINDING_ID,
            String.valueOf(DeviceTypes.AIR_CONDITIONER.deviceTypeId()));
    public static final ThingTypeUID THING_TYPE_WASHING_MACHINE = new ThingTypeUID(BINDING_ID, "201");
    public static final String WM_CHANNEL_REMOTE_START_GRP_ID = "remote-start-grp";
    public static final String CHANNEL_DASHBOARD_GRP_ID = "dashboard";
    public static final String CHANNEL_EXTENDED_INFO_GRP_ID = "extended-information";
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
    public static final int DEFAULT_ENERGY_COLLECTOR_POLLING_UPDATE_DELAY = 60;

    // ====================== FRIDGE DEVICE CONSTANTS =============================
    // CHANNEL IDS
    // public static final String CHANNEL_MOD_OP_ID = "op_mode";
    // public static final String CHANNEL_FAN_SPEED_ID = "fan_speed";
    // public static final String CHANNEL_TARGET_TEMP_ID = "target_temperature";
    // public static final String CHANNEL_CURRENT_TEMP_ID = "current_temperature";
    // public static final String CHANNEL_COOL_JET_ID = "cool_jet";
    public static final Double FRIDGE_TEMPERATURE_IGNORE_VALUE = 255.0;
    public static final Double FREEZER_TEMPERATURE_IGNORE_VALUE = 255.0;
    public static final String FR_CHANNEL_FRIDGE_TEMP_ID = "fridge-temperature";
    public static final String FR_CHANNEL_FREEZER_TEMP_ID = "freezer-temperature";
    public static final String FR_CHANNEL_REF_TEMP_UNIT = "temp-unit";
    public static final String TEMP_UNIT_CELSIUS = "CELSIUS";
    public static final String TEMP_UNIT_FAHRENHEIT = "FAHRENHEIT";
    public static final String TEMP_UNIT_CELSIUS_SYMBOL = "°C";
    public static final String TEMP_UNIT_FAHRENHEIT_SYMBOL = "°F";

    public static final String FR_CHANNEL_ICE_PLUS = "fr-ice-plus";
    public static final String FR_CHANNEL_EXPRESS_MODE = "fr-express-mode";
    public static final String FR_CHANNEL_SMART_SAVING_MODE_V2 = "fr-smart-saving-mode";
    public static final String FR_CHANNEL_SMART_SAVING_SWITCH_V1 = "fr-smart-saving-switch";
    public static final String FR_CHANNEL_ACTIVE_SAVING = "fr-active-saving";
    public static final String FR_CHANNEL_FRESH_AIR_FILTER = "fr_fresh_air_filter";
    public static final String FR_CHANNEL_WATER_FILTER = "fr_water_filter";
    public static final Set<String> CELSIUS_UNIT_VALUES = Set.of("01", "1", "C", "CELSIUS", TEMP_UNIT_CELSIUS_SYMBOL);
    public static final Set<String> FAHRENHEIT_UNIT_VALUES = Set.of("02", "2", "F", "FAHRENHEIT",
            TEMP_UNIT_FAHRENHEIT_SYMBOL);
    public static final Set<String> DOOR_OPEN_FR_VALUES = Set.of("1", "01", "OPEN");
    public static final Set<String> DOOR_CLOSE_FR_VALUES = Set.of("0", "00", "CLOSE");
    public static final String REFRIGERATOR_SNAPSHOT_NODE_V2 = "refState";
    public static final String FR_SET_CONTROL_COMMAND_NAME_V1 = "SetControl";
    public static final Map<String, String> CAP_FR_SMART_SAVING_MODE = Map.of("@CP_TERM_USE_NOT_W", "Disabled",
            "@RE_SMARTSAVING_MODE_NIGHT_W", "Night Mode", "@RE_SMARTSAVING_MODE_CUSTOM_W", "Custom Mode");
    public static final Map<String, String> CAP_FR_ON_OFF = Map.of("@CP_OFF_EN_W", "Off", "@CP_ON_EN_W", "On");
    public static final Map<String, String> CAP_FR_LABEL_ON_OFF = Map.of("OFF", "Off", "ON", "On", "IGNORE",
            "Not Available");

    public static final Map<String, String> CAP_FR_LABEL_CLOSE_OPEN = Map.of("CLOSE", "Closed", "OPEN", "Open",
            "IGNORE", "Not Available");

    public static final Map<String, String> CAP_FR_EXPRESS_MODES = Map.of("@CP_OFF_EN_W", "Express Mode Off",
            "@CP_ON_EN_W", "Express Fridge/Freezer On", "@RE_MAIN_SPEED_FREEZE_TERM_W", "Rapid Freeze On");

    public static final Map<String, String> CAP_FR_FRESH_AIR_FILTER_MAP = Map.ofEntries(/* v1 */ entry("1", "Off"),
            entry("2", "Auto Mode"), entry("3", "Power Mode"), entry("4", "Replace Filter"),
            /* v2 */ entry("OFF", "Off"), entry("AUTO", "Auto Mode"), entry("POWER", "Power Mode"),
            entry("REPLACE", "Replace Filter"), entry("SMART_STORAGE_POWER", "Smart Storage Power"),
            entry("SMART_STORAGE_OFF", "Smart Storage Off"), entry("SMART_STORAGE_ON", "Smart Storage On"),
            entry("IGNORE", "Not Available"));

    public static final Map<String, String> CAP_FR_SMART_SAVING_V2_MODE = Map.of("OFF", "Off", "NIGHT_ON", "Night Mode",
            "CUSTOM_ON", "Custom Mode", "SMARTGRID_DR_ON", "Demand Response", "SMARTGRID_DD_ON", "Delay Defrost",
            "IGNORE", "Not Available");

    public static final Map<String, String> CAP_FR_WATER_FILTER = Map.ofEntries(entry("0_MONTH", "0 Month Used"),
            entry("0", "0 Month Used"), entry("1_MONTH", "1 Month Used"), entry("1", "1 Month Used"),
            entry("2_MONTH", "2 Month Used"), entry("2", "2 Month Used"), entry("3_MONTH", "3 Month Used"),
            entry("3", "3 Month Used"), entry("4_MONTH", "4 Month Used"), entry("4", "4 Month Used"),
            entry("5_MONTH", "5 Month Used"), entry("5", "5 Month Used"), entry("6_MONTH", "6 Month Used"),
            entry("6", "6 Month Used"), entry("7_MONTH", "7 Month Used"), entry("8_MONTH", "8 Month Used"),
            entry("9_MONTH", "9 Month Used"), entry("10_MONTH", "10 Month Used"), entry("11_MONTH", "11 Month Used"),
            entry("12_MONTH", "12 Month Used"), entry("IGNORE", "Not Available"));
    public static final String CAP_FR_WATER_FILTER_USED_POSTFIX = "Month(s) Used";
    public static final Map<String, String> CAP_FR_TEMP_UNIT_V2_MAP = Map.of(TEMP_UNIT_CELSIUS,
            TEMP_UNIT_CELSIUS_SYMBOL, TEMP_UNIT_FAHRENHEIT, TEMP_UNIT_FAHRENHEIT_SYMBOL);

    // ====================== AIR CONDITIONER DEVICE CONSTANTS =============================
    // CHANNEL IDS
    public static final String CHANNEL_MOD_OP_ID = "op_mode";
    public static final String CHANNEL_AIR_WATER_SWITCH_ID = "hp_air_water_switch";
    public static final String CHANNEL_FAN_SPEED_ID = "fan_speed";
    public static final String CHANNEL_POWER_ID = "power";
    public static final String CHANNEL_EXTENDED_INFO_COLLECTOR_ID = "extended_info_collector";
    public static final String CHANNEL_CURRENT_POWER_ID = "current_power";
    public static final String CHANNEL_REMAINING_FILTER_ID = "remaining_filter";
    public static final String CHANNEL_TARGET_TEMP_ID = "target_temperature";
    public static final String CHANNEL_MIN_TEMP_ID = "min_temperature";
    public static final String CHANNEL_MAX_TEMP_ID = "max_temperature";
    public static final String CHANNEL_CURRENT_TEMP_ID = "current_temperature";
    public static final String CHANNEL_COOL_JET_ID = "cool_jet";
    public static final String CHANNEL_AIR_CLEAN_ID = "air_clean";
    public static final String CHANNEL_AUTO_DRY_ID = "auto_dry";
    public static final String CHANNEL_ENERGY_SAVING_ID = "energy_saving";

    public static final String CAP_ACHP_OP_MODE_COOL_KEY = "@AC_MAIN_OPERATION_MODE_COOL_W";
    public static final String CAP_ACHP_OP_MODE_HEAT_KEY = "@AC_MAIN_OPERATION_MODE_HEAT_W";
    public static final Map<String, String> CAP_AC_OP_MODE = Map.of(CAP_ACHP_OP_MODE_COOL_KEY, "Cool",
            "@AC_MAIN_OPERATION_MODE_DRY_W", "Dry", "@AC_MAIN_OPERATION_MODE_FAN_W", "Fan", CAP_ACHP_OP_MODE_HEAT_KEY,
            "Heat", "@AC_MAIN_OPERATION_MODE_AIRCLEAN_W", "Air Clean", "@AC_MAIN_OPERATION_MODE_ACO_W", "Auto",
            "@AC_MAIN_OPERATION_MODE_AI_W", "AI", "@AC_MAIN_OPERATION_MODE_ENERGY_SAVING_W", "Eco",
            "@AC_MAIN_OPERATION_MODE_AROMA_W", "Aroma", "@AC_MAIN_OPERATION_MODE_ANTIBUGS_W", "Anti Bugs");

    public static final Map<String, String> CAP_AC_FAN_SPEED = Map.ofEntries(
            entry("@AC_MAIN_WIND_STRENGTH_SLOW_W", "Slow"), entry("@AC_MAIN_WIND_STRENGTH_SLOW_LOW_W", "Slower"),
            entry("@AC_MAIN_WIND_STRENGTH_LOW_W", "Low"), entry("@AC_MAIN_WIND_STRENGTH_LOW_MID_W", "Low Mid"),
            entry("@AC_MAIN_WIND_STRENGTH_MID_W", "Mid"), entry("@AC_MAIN_WIND_STRENGTH_MID_HIGH_W", "Mid High"),
            entry("@AC_MAIN_WIND_STRENGTH_HIGH_W", "High"), entry("@AC_MAIN_WIND_STRENGTH_POWER_W", "Power"),
            entry("@AC_MAIN_WIND_STRENGTH_AUTO_W", "Auto"), entry("@AC_MAIN_WIND_STRENGTH_NATURE_W", "Auto"),
            entry("@AC_MAIN_WIND_STRENGTH_LOW_RIGHT_W", "Right Low"),
            entry("@AC_MAIN_WIND_STRENGTH_MID_RIGHT_W", "Right Mid"),
            entry("@AC_MAIN_WIND_STRENGTH_HIGH_RIGHT_W", "Right High"),
            entry("@AC_MAIN_WIND_STRENGTH_LOW_LEFT_W", "Left Low"),
            entry("@AC_MAIN_WIND_STRENGTH_MID_LEFT_W", "Left Mid"),
            entry("@AC_MAIN_WIND_STRENGTH_HIGH_LEFT_W", "Left High"));

    public static final Map<String, String> CAP_AC_COOL_JET = Map.of("@COOL_JET", "Cool Jet");
    public static final Double CAP_HP_AIR_SWITCH = 0.0;
    public static final Double CAP_HP_WATER_SWITCH = 1.0;
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

    // Extended Info Attribute Constants
    public static final String EXTENDED_ATTR_INSTANT_POWER = "InOutInstantPower";
    public static final String EXTENDED_ATTR_FILTER_MAX_TIME_TO_USE = "ChangePeriod";
    public static final String EXTENDED_ATTR_FILTER_USED_TIME = "UseTime";

    // ====================== WASHING MACHINE CONSTANTS =============================
    public static final String WM_COURSE_NOT_SELECTED_VALUE = "NOT_SELECTED";
    public static final String WM_POWER_OFF_VALUE = "POWEROFF";
    public static final String WM_SNAPSHOT_WASHER_DRYER_NODE_V2 = "washerDryer";
    public static final String WM_CHANNEL_STATE_ID = "state";
    public static final String WM_CHANNEL_PROCESS_STATE_ID = "process-state";
    public static final String WM_CHANNEL_COURSE_ID = "course";
    public static final String DR_CHANNEL_DRY_LEVEL_ID = "dry-level";
    public static final String WM_CHANNEL_SMART_COURSE_ID = "smart-course";
    public static final String WM_CHANNEL_TEMP_LEVEL_ID = "temperature-level";
    public static final String WM_CHANNEL_DOOR_LOCK_ID = "door-lock";
    public static final String DR_CHANNEL_CHILD_LOCK_ID = "child-lock";
    public static final String FR_CHANNEL_DOOR_ID = "some-door-open";
    public static final String WM_CHANNEL_RINSE_ID = "rinse";

    public static final String WM_CHANNEL_SPIN_ID = "spin";

    public static final String WM_CHANNEL_REMOTE_START_START_STOP = "rs-start-stop";

    public static final String WM_CHANNEL_REMOTE_COURSE = "rs-course";
    public static final String WM_CHANNEL_REMOTE_START_RINSE = "rs-rinse";
    public static final String WM_CHANNEL_REMOTE_START_TEMP = "rs-temperature-level";
    public static final String WM_CHANNEL_REMOTE_START_SPIN = "rs-spin";
    public static final String WM_CHANNEL_REMOTE_START_ID = "remote-start-flag";
    public static final String WM_CHANNEL_STAND_BY_ID = "stand-by";
    public static final String WM_CHANNEL_REMAIN_TIME_ID = "remain-time";
    public static final String WM_CHANNEL_DELAY_TIME_ID = "delay-time";

    public static final Map<String, String> CAP_WDM_STATE = Map.ofEntries(entry("@WM_STATE_POWER_OFF_W", "Off"),
            entry("@WM_STATE_INITIAL_W", "Initial"), entry("@WM_STATE_PAUSE_W", "Pause"),
            entry("@WM_STATE_RESERVE_W", "Reserved"), entry("@WM_STATE_DETECTING_W", "Detecting"),
            entry("@WM_STATE_RUNNING_W", "Running"), entry("@WM_STATE_RINSING_W", "Rinsing"),
            entry("@WM_STATE_SPINNING_W", "Spinning"), entry("@WM_STATE_COOLDOWN_W", "Cool Down"),
            entry("@WM_STATE_RINSEHOLD_W", "Rinse Hold"), entry("@WM_STATE_WASH_REFRESHING_W", "Refreshing"),
            entry("@WM_STATE_STEAMSOFTENING_W", "Steam Softening"), entry("@WM_STATE_END_W", "End"),
            entry("@WM_STATE_DRYING_W", "Drying"), entry("@WM_STATE_DEMO_W", "Demonstration"),
            entry("@WM_STATE_ADD_DRAIN_W", "Add Drain"), entry("@WM_STATE_LOAD_DISPLAY_W", "Loading Display"),
            entry("@WM_STATE_FRESHCARE_W", "Refreshing"), entry("@WM_STATE_ERROR_AUTO_OFF_W", "Error Auto Off"),
            entry("@WM_STATE_FROZEN_PREVENT_INITIAL_W", "Frozen Preventing"),
            entry("@FROZEN_PREVENT_PAUSE", "Frozen Preventing Paused"),
            entry("@FROZEN_PREVENT_RUNNING", "Frozen Preventing Running"), entry("@AUDIBLE_DIAGNOSIS", "Diagnosing"),
            entry("@WM_STATE_ERROR_W", "Error"));

    public static final Map<String, String> CAP_WDM_PROCESS_STATE = Map.ofEntries(
            entry("@WM_STATE_DETECTING_W", "Detecting"), entry("@WM_STATE_STEAM_W", "Steam"),
            entry("@WM_STATE_DRY_W", "Drying"), entry("@WM_STATE_COOLING_W", "Cooling"),
            entry("@WM_STATE_ANTI_CREASE_W", "Anti Creasing"), entry("@WM_STATE_END_W", "End"),
            entry("@WM_STATE_POWER_OFF_W", "Power Off"), entry("@WM_STATE_INITIAL_W", "Initializing"),
            entry("@WM_STATE_PAUSE_W", "Paused"), entry("@WM_STATE_RESERVE_W", "Reserved"),
            entry("@WM_STATE_RUNNING_W", "Running"), entry("@WM_STATE_RINSING_W", "Rising"),
            entry("@WM_STATE_SPINNING_W", "@WM_STATE_DRYING_W"), entry("WM_STATE_COOLDOWN_W", "Cool Down"),
            entry("@WM_STATE_RINSEHOLD_W", "Rinse Hold"), entry("@WM_STATE_WASH_REFRESHING_W", "Refreshing"),
            entry("@WM_STATE_STEAMSOFTENING_W", "Steam Softening"), entry("@WM_STATE_ERROR_W", "Error"));

    public static final Map<String, String> CAP_DR_DRY_LEVEL = Map.ofEntries(
            entry("@WM_DRY24_DRY_LEVEL_IRON_W", "Iron"), entry("@WM_DRY24_DRY_LEVEL_CUPBOARD_W", "Cupboard"),
            entry("@WM_DRY24_DRY_LEVEL_EXTRA_W", "Extra"));

    public static final Map<String, String> CAP_WM_TEMPERATURE = Map.ofEntries(
            entry("@WM_TERM_NO_SELECT_W", "Not Selected"), entry("@WM_TITAN2_OPTION_TEMP_20_W", "20"),
            entry("@WM_TITAN2_OPTION_TEMP_COLD_W", "Cold"), entry("@WM_TITAN2_OPTION_TEMP_30_W", "30"),
            entry("@WM_TITAN2_OPTION_TEMP_40_W", "40"), entry("@WM_TITAN2_OPTION_TEMP_50_W", "50"),
            entry("@WM_TITAN27_BIG_OPTION_TEMP_TAP_COLD_W", "Tap Cold"),
            entry("@WM_TITAN27_BIG_OPTION_TEMP_COLD_W", "Cold"),
            entry("@WM_TITAN27_BIG_OPTION_TEMP_ECO_WARM_W", "Eco Warm"),
            entry("@WM_TITAN27_BIG_OPTION_TEMP_WARM_W", "Warm"), entry("@WM_TITAN27_BIG_OPTION_TEMP_HOT_W", "Hot"),
            entry("@WM_TITAN27_BIG_OPTION_TEMP_EXTRA_HOT_W", "Extra Hot"));

    public static final Map<String, String> CAP_WM_SPIN = Map.ofEntries(entry("@WM_TERM_NO_SELECT_W", "Not Selected"),
            entry("@WM_TITAN2_OPTION_SPIN_NO_SPIN_W", "No Spin"), entry("@WM_TITAN2_OPTION_SPIN_400_W", "400"),
            entry("@WM_TITAN2_OPTION_SPIN_600_W", "600"), entry("@WM_TITAN2_OPTION_SPIN_700_W", "700"),
            entry("@WM_TITAN2_OPTION_SPIN_800_W", "800"), entry("@WM_TITAN2_OPTION_SPIN_900_W", "900"),
            entry("@WM_TITAN2_OPTION_SPIN_1000_W", "1000"), entry("@WM_TITAN2_OPTION_SPIN_1100_W", "1100"),
            entry("@WM_TITAN2_OPTION_SPIN_1200_W", "1200"), entry("@WM_TITAN2_OPTION_SPIN_1400_W", "1400"),
            entry("@WM_TITAN2_OPTION_SPIN_1600_W", "1600"), entry("@WM_TITAN2_OPTION_SPIN_MAX_W", "Max Spin"),
            entry("@WM_TITAN27_BIG_OPTION_SPIN_NO_SPIN_W", "Drain Only"),
            entry("@WM_TITAN27_BIG_OPTION_SPIN_LOW_W", "Low"), entry("@WM_TITAN27_BIG_OPTION_SPIN_MEDIUM_W", "Medium"),
            entry("@WM_TITAN27_BIG_OPTION_SPIN_HIGH_W", "High"),
            entry("@WM_TITAN27_BIG_OPTION_SPIN_EXTRA_HIGH_W", "Extra High"));

    public static final Map<String, String> CAP_WM_RINSE = Map.ofEntries(entry("@WM_TERM_NO_SELECT_W", "Not Selected"),
            entry("@WM_TITAN2_OPTION_RINSE_NORMAL_W", "Normal"), entry("@WM_TITAN2_OPTION_RINSE_RINSE+_W", "Plus"),
            entry("@WM_TITAN2_OPTION_RINSE_RINSE++_W", "Plus +"),
            entry("@WM_TITAN2_OPTION_RINSE_NORMALHOLD_W", "Normal Hold"),
            entry("@WM_TITAN2_OPTION_RINSE_RINSE+HOLD_W", "Plus Hold"),
            entry("@WM_TITAN27_BIG_OPTION_EXTRA_RINSE_0_W", "Normal"),
            entry("@WM_TITAN27_BIG_OPTION_EXTRA_RINSE_1_W", "Plus"),
            entry("@WM_TITAN27_BIG_OPTION_EXTRA_RINSE_2_W", "Plus +"),
            entry("@WM_TITAN27_BIG_OPTION_EXTRA_RINSE_3_W", "Plus ++"));

    // This is the dictionary os course functions translations for V2
    public static final Map<String, Map<String, String>> CAP_WM_DICT_V2 = Map.of("spin", CAP_WM_SPIN, "rinse",
            CAP_WM_RINSE, "temp", CAP_WM_TEMPERATURE, "state", CAP_WDM_STATE);

    public static final String WM_COMMAND_REMOTE_START_V2 = "WMStart";
    // ==============================================================================
}
