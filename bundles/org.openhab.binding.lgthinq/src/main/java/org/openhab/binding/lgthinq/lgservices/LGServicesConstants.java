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
package org.openhab.binding.lgthinq.lgservices;

import static java.util.Map.entry;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The LGServicesConstants constants class for lg services
 *
 * @author Nemer Daud - Initial Contribution
 */
@NonNullByDefault
public class LGServicesConstants {
    // Extended Info Attribute Constants
    public static final String CAP_EXTRA_ATTR_INSTANT_POWER = "InOutInstantPower";
    public static final String CAP_EXTRA_ATTR_FILTER_MAX_TIME_TO_USE = "ChangePeriod";
    public static final String CAP_EXTRA_ATTR_FILTER_USED_TIME = "UseTime";
    public static final String LG_ROOT_TAG_V1 = "lgedmRoot";
    public static final String LG_API_V1_CONTROL_OP = "rti/rtiControl";
    // === LG API protocol constants
    public static final String LG_API_API_KEY_V2 = "VGhpblEyLjAgU0VSVklDRQ==";
    public static final String LG_API_APPLICATION_KEY = "6V1V8H2BN5P9ZQGOI5DAQ92YZBDO3EK9";
    public static final String LG_API_APP_LEVEL = "PRD";
    public static final String LG_API_APP_OS = "ANDROID";
    public static final String LG_API_APP_TYPE = "NUTS";
    public static final String LG_API_APP_VER = "5.0.2800";
    // the client id is a SHA512 hash of the phone MFR,MODEL,SERIAL,
    // and the build id of the thinq app it can also just be a random
    // string, we use the same client id used for oauth
    public static final String LG_API_CLIENT_ID = "LGAO221A02";
    public static final String LG_API_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss +0000";
    public static final String LG_API_GATEWAY_SERVICE_PATH_V2 = "/v1/service/application/gateway-uri";
    public static final String LG_API_GATEWAY_URL_V2 = "https://route.lgthinq.com:46030"
            + LG_API_GATEWAY_SERVICE_PATH_V2;
    public static final String LG_API_MESSAGE_ID = "wideq";
    public static final String LG_API_OAUTH_CLIENT_KEY = "LGAO722A02";
    public static final String LG_API_OAUTH_SEARCH_KEY_PATH = "/searchKey";
    public static final String LG_API_OAUTH_SECRET_KEY = "c053c2a6ddeb7ad97cb0eed0dcb31cf8";
    public static final String LG_API_PLATFORM_TYPE_V1 = "thinq1";
    public static final String LG_API_PLATFORM_TYPE_V2 = "thinq2";
    public static final String LG_API_PRE_LOGIN_PATH = "/preLogin";
    public static final String LG_API_SVC_CODE = "SVC202";
    public static final String LG_API_SVC_PHASE = "OP";
    public static final String LG_API_V1_MON_DATA_PATH = "rti/rtiResult";
    public static final String LG_API_V1_START_MON_PATH = "rti/rtiMon";
    public static final String LG_API_V2_API_KEY = "VGhpblEyLjAgU0VSVklDRQ==";
    public static final String LG_API_V2_APP_LEVEL = "PRD";
    public static final String LG_API_V2_APP_OS = "ANDROID";
    public static final String LG_API_V2_APP_TYPE = "NUTS";
    public static final String LG_API_V2_APP_VER = "5.0.2800";
    public static final String LG_API_V2_AUTH_PATH = "/oauth/1.0/oauth2/token";
    public static final String LG_API_V2_CTRL_DEVICE_CONFIG_PATH = "service/devices/%s/%s";
    public static final String LG_API_V2_DEVICE_CONFIG_PATH = "service/devices/";
    public static final String LG_API_V2_EMP_SESS_PATH = "/emp/oauth2/token/empsession";
    public static final String LG_API_V2_EMP_SESS_URL = "https://emp-oauth.lgecloud.com" + LG_API_V2_EMP_SESS_PATH;
    public static final String LG_API_V2_LS_PATH = "/service/application/dashboard";
    public static final String LG_API_V2_SESSION_LOGIN_PATH = "/emp/v2.0/account/session/";
    public static final String LG_API_V2_SVC_PHASE = "OP";
    public static final String LG_API_V2_USER_INFO = "/users/profile";
    public static final Double FREEZER_TEMPERATURE_IGNORE_VALUE = 255.0;
    public static final Double FRIDGE_TEMPERATURE_IGNORE_VALUE = 255.0;
    public static final String RE_TEMP_UNIT_CELSIUS = "CELSIUS";
    public static final String RE_TEMP_UNIT_CELSIUS_SYMBOL = "°C";
    public static final Set<String> RE_CELSIUS_UNIT_VALUES = Set.of("01", "1", "C", "CELSIUS",
            RE_TEMP_UNIT_CELSIUS_SYMBOL);
    public static final String RE_TEMP_UNIT_FAHRENHEIT = "FAHRENHEIT";
    public static final String RE_TEMP_UNIT_FAHRENHEIT_SYMBOL = "°F";
    public static final Map<String, String> CAP_RE_TEMP_UNIT_V2_MAP = Map.of(RE_TEMP_UNIT_CELSIUS,
            RE_TEMP_UNIT_CELSIUS_SYMBOL, RE_TEMP_UNIT_FAHRENHEIT, RE_TEMP_UNIT_FAHRENHEIT_SYMBOL);
    public static final Set<String> RE_FAHRENHEIT_UNIT_VALUES = Set.of("02", "2", "F", "FAHRENHEIT",
            RE_TEMP_UNIT_FAHRENHEIT_SYMBOL);
    public static final Set<String> RE_DOOR_OPEN_VALUES = Set.of("1", "01", "OPEN");
    public static final Set<String> RE_DOOR_CLOSE_VALUES = Set.of("0", "00", "CLOSE");
    public static final String RE_SNAPSHOT_NODE_V2 = "refState";
    public static final String RE_SET_CONTROL_COMMAND_NAME_V1 = "SetControl";
    public static final Map<String, String> CAP_RE_SMART_SAVING_MODE = Map.of("@CP_TERM_USE_NOT_W", "Disabled",
            "@RE_SMARTSAVING_MODE_NIGHT_W", "Night Mode", "@RE_SMARTSAVING_MODE_CUSTOM_W", "Custom Mode");
    public static final Map<String, String> CAP_RE_ON_OFF = Map.of("@CP_OFF_EN_W", "Off", "@CP_ON_EN_W", "On");
    public static final Map<String, String> CAP_RE_LABEL_ON_OFF = Map.of("OFF", "Off", "ON", "On", "IGNORE",
            "Not Available");
    public static final Map<String, String> CAP_RE_LABEL_CLOSE_OPEN = Map.of("CLOSE", "Closed", "OPEN", "Open",
            "IGNORE", "Not Available");
    public static final Map<String, String> CAP_RE_EXPRESS_FREEZE_MODES = Map.of("@CP_OFF_EN_W", "Express Mode Off",
            "@CP_ON_EN_W", "Express Freeze On", "@RE_MAIN_SPEED_FREEZE_TERM_W", "Rapid Freeze On");
    public static final Map<String, String> CAP_RE_FRESH_AIR_FILTER_MAP = Map.ofEntries(/* v1 */ entry("1", "Off"),
            entry("2", "Auto Mode"), entry("3", "Power Mode"), entry("4", "Replace Filter"),
            /* v2 */ entry("OFF", "Off"), entry("AUTO", "Auto Mode"), entry("POWER", "Power Mode"),
            entry("REPLACE", "Replace Filter"), entry("SMART_STORAGE_POWER", "Smart Storage Power"),
            entry("SMART_STORAGE_OFF", "Smart Storage Off"), entry("SMART_STORAGE_ON", "Smart Storage On"),
            entry("IGNORE", "Not Available"));
    public static final Map<String, String> CAP_RE_SMART_SAVING_V2_MODE = Map.of("OFF", "Off", "NIGHT_ON", "Night Mode",
            "CUSTOM_ON", "Custom Mode", "SMARTGRID_DR_ON", "Demand Response", "SMARTGRID_DD_ON", "Delay Defrost",
            "IGNORE", "Not Available");
    public static final Map<String, String> CAP_RE_WATER_FILTER = Map.ofEntries(entry("0_MONTH", "0 Month Used"),
            entry("0", "0 Month Used"), entry("1_MONTH", "1 Month Used"), entry("1", "1 Month Used"),
            entry("2_MONTH", "2 Month Used"), entry("2", "2 Month Used"), entry("3_MONTH", "3 Month Used"),
            entry("3", "3 Month Used"), entry("4_MONTH", "4 Month Used"), entry("4", "4 Month Used"),
            entry("5_MONTH", "5 Month Used"), entry("5", "5 Month Used"), entry("6_MONTH", "6 Month Used"),
            entry("6", "6 Month Used"), entry("7_MONTH", "7 Month Used"), entry("8_MONTH", "8 Month Used"),
            entry("9_MONTH", "9 Month Used"), entry("10_MONTH", "10 Month Used"), entry("11_MONTH", "11 Month Used"),
            entry("12_MONTH", "12 Month Used"), entry("IGNORE", "Not Available"));
    public static final String CAP_RE_WATER_FILTER_USED_POSTFIX = "Month(s) Used";
    // === Device Definition/Capability Constants
    public static final String CAP_ACHP_OP_MODE_COOL_KEY = "@AC_MAIN_OPERATION_MODE_COOL_W";
    public static final String CAP_ACHP_OP_MODE_HEAT_KEY = "@AC_MAIN_OPERATION_MODE_HEAT_W";
    public static final Map<String, String> CAP_AC_OP_MODE = Map.of(CAP_ACHP_OP_MODE_COOL_KEY, "Cool",
            "@AC_MAIN_OPERATION_MODE_DRY_W", "Dry", "@AC_MAIN_OPERATION_MODE_FAN_W", "Fan", CAP_ACHP_OP_MODE_HEAT_KEY,
            "Heat", "@AC_MAIN_OPERATION_MODE_AIRCLEAN_W", "Air Clean", "@AC_MAIN_OPERATION_MODE_ACO_W", "Auto",
            "@AC_MAIN_OPERATION_MODE_AI_W", "AI", "@AC_MAIN_OPERATION_MODE_ENERGY_SAVING_W", "Eco",
            "@AC_MAIN_OPERATION_MODE_AROMA_W", "Aroma", "@AC_MAIN_OPERATION_MODE_ANTIBUGS_W", "Anti Bugs");
    public static final Map<String, String> CAP_AC_STEP_UP_DOWN_MODE = Map.of("@OFF", "Off", "@1", "Upper", "@2", "Up",
            "@3", "Middle Up", "@4", "Middle Down", "@5", "Down", "@6", "Far Down", "@100", "Circular");
    public static final Map<String, String> CAP_AC_STEP_LEFT_RIGHT_MODE = Map.of("@OFF", "Off", "@1", "Lefter", "@2",
            "Left", "@3", "Middle", "@4", "Right", "@5", "Righter", "@13", "Left to Middle", "@35", "Middle to Right",
            "@100", "Circular");
    // Sub Modes support
    public static final String CAP_AC_SUB_MODE_COOL_JET = "@AC_MAIN_WIND_MODE_COOL_JET_W";
    public static final String CAP_AC_SUB_MODE_STEP_UP_DOWN = "@AC_MAIN_WIND_DIRECTION_STEP_UP_DOWN_W";
    public static final String CAP_AC_SUB_MODE_STEP_LEFT_RIGHT = "@AC_MAIN_WIND_DIRECTION_STEP_LEFT_RIGHT_W";
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
    public static final String CAP_AC_ENERGYSAVING = "@ENERGYSAVING";
    public static final String CAP_AC_AIRCLEAN = "@AIRCLEAN";
    // ====================
    public static final String CAP_AC_COMMAND_OFF = "@OFF";
    public static final String CAP_AC_COMMAND_ON = "@ON";
    public static final String CAP_AC_AIR_CLEAN_COMMAND_ON = "@AC_MAIN_AIRCLEAN_ON_W";
    public static final String CAP_AC_AIR_CLEAN_COMMAND_OFF = "@AC_MAIN_AIRCLEAN_OFF_W";
    public static final String WMD_COURSE_NOT_SELECTED_VALUE = "NOT_SELECTED";
    public static final String WMD_POWER_OFF_VALUE = "POWEROFF";
    public static final String WMD_SNAPSHOT_WASHER_DRYER_NODE_V2 = "washerDryer";
    public static final String WM_LOST_WASHING_STATE_KEY = "WASHING";
    public static final String WM_LOST_WASHING_STATE_VALUE = "@WM_STATE_WASHING_W";
    public static final Map<String, String> CAP_WMD_STATE = Map.ofEntries(entry("@WM_STATE_POWER_OFF_W", "Off"),
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
            entry("@WM_STATE_ERROR_W", "Error"),
            // This last one is not defined in the cap file
            entry(WM_LOST_WASHING_STATE_VALUE, "Washing"));
    public static final Map<String, String> CAP_WMD_PROCESS_STATE = Map.ofEntries(
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
    public static final Map<String, String> CAP_WMD_TEMPERATURE = Map.ofEntries(
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
            CAP_WM_RINSE, "temp", CAP_WMD_TEMPERATURE, "state", CAP_WMD_STATE);
    public static final String WMD_COMMAND_REMOTE_START_V2 = "WMStart";
    /**
     * ============ Dish Washer's Label/Feature Translation Constants =============
     */
    public static final String DW_SNAPSHOT_WASHER_DRYER_NODE_V2 = "dishwasher";
    public static final String DW_POWER_OFF_VALUE = "POWEROFF";
    public static final String DW_STATE_COMPLETE = "END";
    public static final Map<String, String> CAP_DW_DOOR_STATE = Map.of("@CP_OFF_EN_W", "Close", "@CP_ON_EN_W",
            "Opened");
    public static final Map<String, String> CAP_DW_PROCESS_STATE = Map.ofEntries(entry("@DW_STATE_INITIAL_W", "None"),
            entry("@DW_STATE_RESERVE_W", "Reserved"), entry("@DW_STATE_RUNNING_W", "Running"),
            entry("@DW_STATE_RINSING_W", "Rising"), entry("@DW_STATE_DRYING_W", "Drying"),
            entry("@DW_STATE_COMPLETE_W", "Complete"), entry("@DW_STATE_NIGHTDRY_W", "Night Dry"),
            entry("@DW_STATE_CANCEL_W", "Cancelled"));
    public static final Map<String, String> CAP_DW_STATE = Map.ofEntries(entry("@DW_STATE_POWER_OFF_W", "Off"),
            entry("@DW_STATE_INITIAL_W", "Initial"), entry("@DW_STATE_RUNNING_W", "Running"),
            entry("@DW_STATE_PAUSE_W", "Paused"), entry("@DW_STATE_STANDBY_W", "Stand By"),
            entry("@DW_STATE_COMPLETE_W", "Complete"), entry("@DW_STATE_POWER_FAIL_W", "Power Fail"));
}
