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
 * The {@link LGThinqBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqBindingConstants {

    public static final String BINDING_ID = "lgthinq";
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_AIR_CONDITIONER = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.AIR_CONDITIONER.deviceTypeId()); // deviceType from AirConditioner
    public static final ThingTypeUID THING_TYPE_WASHING_MACHINE = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.WASHING_MACHINE.deviceTypeId()); // deviceType from AirConditioner
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AIR_CONDITIONER,
            THING_TYPE_WASHING_MACHINE, THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_AIR_CONDITIONER,
            THING_TYPE_WASHING_MACHINE);

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
    public static final String V2_CTRL_DEVICE_CONFIG_PATH = "service/devices/%s/control-sync";
    public static final String V1_START_MON_PATH = "rti/rtiMon";
    public static final String V1_MON_DATA_PATH = "rti/rtiResult";
    public static final String V1_CONTROL_OP = "rti/rtiControl";
    public static final String OAUTH_SEARCH_KEY_PATH = "/searchKey";
    public static final String GATEWAY_SERVICE_PATH_V2 = "/v1/service/application/gateway-uri";
    public static final String GATEWAY_SERVICE_PATH_V1 = "/api/common/gatewayUriList";
    public static final String GATEWAY_URL_V2 = "https://route.lgthinq.com:46030" + GATEWAY_SERVICE_PATH_V2;
    public static final String PRE_LOGIN_PATH = "/preLogin";
    public static final String SECURITY_KEY = "nuts_securitykey";
    public static final String APP_KEY = "wideq";
    public static final String DATA_ROOT = "result";
    public static final String POST_DATA_ROOT = "lgedmRoot";
    public static final String RETURN_CODE_ROOT = "resultCode";
    public static final String RETURN_MESSAGE_ROOT = "returnMsg";
    public static final String SVC_CODE = "SVC202";
    public static final String OAUTH_SECRET_KEY = "c053c2a6ddeb7ad97cb0eed0dcb31cf8";
    public static final String OAUTH_CLIENT_KEY = "LGAO722A02";
    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss +0000";
    public static final String DEFAULT_COUNTRY = "US";
    public static final String DEFAULT_LANGUAGE = "en-US";
    public static final String APPLICATION_KEY = "6V1V8H2BN5P9ZQGOI5DAQ92YZBDO3EK9";
    public static final String V2_EMP_SESS_PATH = "/emp/oauth2/token/empsession";
    public static final String V2_EMP_SESS_URL = "https://emp-oauth.lgecloud.com" + V2_EMP_SESS_PATH;
    public static final String API_KEY_V2 = "VGhpblEyLjAgU0VSVklDRQ==";

    public static final String API_KEY_V1 = "wideq";
    public static final String API_SECURITY_KEY_V1 = "nuts_securitykey";

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

    public static final Map<String, String> ERROR_CODE_RESPONSE = Map.ofEntries(Map.entry("0000", "OK"),
            Map.entry("0001", "PARTIAL_OK"), Map.entry("0103", "OPERATION_IN_PROGRESS_DEVICE"),
            Map.entry("0007", "PORTAL_INTERWORKING_ERROR"), Map.entry("0104", "PROCESSING_REFRIGERATOR"),
            Map.entry("0111", "RESPONSE_DELAY_DEVICE"), Map.entry("8107", "SERVICE_SERVER_ERROR"),
            Map.entry("8102", "SSP_ERROR"), Map.entry("9020", "TIME_OUT"), Map.entry("8104", "WRONG_XML_OR_URI"),
            Map.entry("9000", "AWS_IOT_ERROR"), Map.entry("8105", "AWS_S3_ERROR"), Map.entry("8106", "AWS_SQS_ERROR"),
            Map.entry("9002", "BASE64_DECODING_ERROR"), Map.entry("9001", "BASE64_ENCODING_ERROR"),
            Map.entry("8103", "CLIP_ERROR"), Map.entry("0105", "CONTROL_ERROR_REFRIGERATOR"),
            Map.entry("9003", "CREATE_SESSION_FAIL"), Map.entry("9004", "DB_PROCESSING_FAIL"),
            Map.entry("8101", "DM_ERROR"), Map.entry("0013", "DUPLICATED_ALIAS"), Map.entry("0008", "DUPLICATED_DATA"),
            Map.entry("0004", "DUPLICATED_LOGIN"), Map.entry("0102", "EMP_AUTHENTICATION_FAILED"),
            Map.entry("8900", "ETC_COMMUNICATION_ERROR"), Map.entry("9999", "ETC_ERROR"),
            Map.entry("0112", "EXCEEDING_LIMIT"), Map.entry("0119", "EXPIRED_CUSTOMER_NUMBER"),
            Map.entry("9005", "EXPIRES_SESSION_BY_WITHDRAWAL"), Map.entry("0100", "FAIL"),
            Map.entry("8001", "INACTIVE_API"), Map.entry("0107", "INSUFFICIENT_STORAGE_SPACE"),
            Map.entry("9010", "INVAILD_CSR"), Map.entry("0002", "INVALID_BODY"),
            Map.entry("0118", "INVALID_CUSTOMER_NUMBER"), Map.entry("0003", "INVALID_HEADER"),
            Map.entry("0301", "INVALID_PUSH_TOKEN"), Map.entry("0116", "INVALID_REQUEST_DATA_FOR_DIAGNOSIS"),
            Map.entry("0014", "MISMATCH_DEVICE_GROUP"), Map.entry("0114", "MISMATCH_LOGIN_SESSION"),
            Map.entry("0006", "MISMATCH_NONCE"), Map.entry("0115", "MISMATCH_REGISTRED_DEVICE"),
            Map.entry("0110", "NOT_AGREED_TERMS"), Map.entry("0106", "NOT_CONNECTED_DEVICE"),
            Map.entry("0120", "NOT_CONTRACT_CUSTOMER_NUMBER"), Map.entry("0010", "NOT_EXIST_DATA"),
            Map.entry("0009", "NOT_EXIST_DEVICE"), Map.entry("0117", "NOT_EXIST_MODEL_JSON"),
            Map.entry("0121", "NOT_REGISTERED_SMART_CARE"), Map.entry("0012", "NOT_SUPPORTED_COMMAND"),
            Map.entry("8000", "NOT_SUPPORTED_COUNTRY"), Map.entry("0005", "NOT_SUPPORTED_SERVICE"),
            Map.entry("0109", "NO_INFORMATION_DR"), Map.entry("0108", "NO_INFORMATION_SLEEP_MODE"),
            Map.entry("0011", "NO_PERMISSION"), Map.entry("0113", "NO_PERMISION_MODIFY_RECIPE"),
            Map.entry("0101", "NO_REGISTERED_DEVICE"), Map.entry("9006", "NO_USER_INFORMATION"));

    // ====================== AIR CONDITIONER DEVICE CONSTANTS =============================
    // CHANNEL IDS
    public static final String CHANNEL_MOD_OP_ID = "op_mode";
    public static final String CHANNEL_FAN_SPEED_ID = "fan_speed";
    public static final String CHANNEL_POWER_ID = "power";
    public static final String CHANNEL_TARGET_TEMP_ID = "target_temperature";
    public static final String CHANNEL_CURRENT_TEMP_ID = "current_temperature";

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
            Map.entry("@AC_MAIN_WIND_STRENGTH_AUTO_W", "Auto"), Map.entry("@AC_MAIN_WIND_STRENGTH_NATURE_W", "Nature"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_LOW_RIGHT_W", "Right Low"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_MID_RIGHT_W", "Right Mid"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_HIGH_RIGHT_W", "Right High"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_LOW_LEFT_W", "Left Low"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_MID_LEFT_W", "Left Mid"),
            Map.entry("@AC_MAIN_WIND_STRENGTH_HIGH_LEFT_W", "Left High"));

    // ====================== WASHING MACHINE CONSTANTS =============================
    public static final String WM_POWER_OFF_VALUE = "POWEROFF";
    public static final String WM_SNAPSHOT_WASHER_DRYER_NODE = "washerDryer";
    public static final String WM_CHANNEL_STATE_ID = "state";
    public static final String WM_CHANNEL_COURSE_ID = "course";
    public static final String WM_CHANNEL_SMART_COURSE_ID = "smart-course";
    public static final String WM_CHANNEL_TEMP_LEVEL_ID = "temperature-level";
    public static final String WM_CHANNEL_DOOR_LOCK_ID = "door-lock";

    public static final Map<String, String> CAP_WP_STATE = Map.ofEntries(Map.entry("@WM_STATE_POWER_OFF_W", "Off"),
            Map.entry("@WM_STATE_INITIAL_W", "Initial"), Map.entry("@WM_STATE_PAUSE_W", "Pause"),
            Map.entry("@WM_STATE_RESERVE_W", "Reverse"), Map.entry("@WM_STATE_DETECTING_W", "Detecting"),
            Map.entry("@WM_STATE_RUNNING_W", "Running"), Map.entry("@WM_STATE_RINSING_W", "Rinsing"),
            Map.entry("@WM_STATE_SPINNING_W", "Spinning"), Map.entry("@WM_STATE_COOLDOWN_W", "Cool Down"),
            Map.entry("@WM_STATE_RINSEHOLD_W", "Rinse Hold"), Map.entry("@WM_STATE_WASH_REFRESHING_W", "Refreshing"),
            Map.entry("@WM_STATE_STEAMSOFTENING_W", "Steam Softening"), Map.entry("@WM_STATE_END_W", "End"),
            Map.entry("@WM_STATE_DRYING_W", "Drying"), Map.entry("@WM_STATE_DEMO_W", "Demonstration"),
            Map.entry("@WM_STATE_ERROR_W", "Error"));

    // ==============================================================================
}
