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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgapi.model.DeviceTypes;
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
    public static final ThingTypeUID THING_TYPE_AIR_CONDITIONER = new ThingTypeUID(BINDING_ID,
            "" + DeviceTypes.AIR_CONDITIONER.deviceTypeId()); // deviceType from AirConditioner
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_AIR_CONDITIONER);

    public static final String THINQ_USER_DATA_FOLDER = OpenHAB.getUserDataFolder() + File.separator + "thinq";
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
    public static final String V1_POOL_MON_PATH = "rti/rtiResult";
    public static final String V1_CONTROL_OP = "rti/rtiControl";
    public static final String OAUTH_SEARCH_KEY_PATH = "/searchKey";
    public static final String GATEWAY_SERVICE_PATH = "/v1/service/application/gateway-uri";
    public static String GATEWAY_URL = "https://route.lgthinq.com:46030" + GATEWAY_SERVICE_PATH;
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
    public static String V2_EMP_SESS_URL = "https://emp-oauth.lgecloud.com/emp/oauth2/token/empsession";
    // v2
    public static final String API_KEY = "VGhpblEyLjAgU0VSVklDRQ==";

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
    public static final String MODEL_URL_INFO = "model_url_indo";
    public static final String PLATFORM_TYPE = "platform_type";
    public static final String PLATFORM_TYPE_V1 = "thinq1";
    public static final String PLATFORM_TYPE_V2 = "thinq2";
    static final Set<String> SUPPORTED_LG_PLATFORMS = Set.of(PLATFORM_TYPE_V1, PLATFORM_TYPE_V2);

    public static final int SEARCH_TIME = 20;
    // delay between each devices's scan for state changes (in seconds)
    public static final int DEFAULT_STATE_POOLING_UPDATE_DELAY = 30;
    // CHANNEL IDS
    public static final String CHANNEL_MOD_OP_ID = "op_mode";
    public static final String CHANNEL_FAN_SPEED_ID = "fan_speed";
    public static final String CHANNEL_POWER_ID = "power";
    public static final String CHANNEL_TARGET_TEMP_ID = "target_temperature";
    public static final String CHANNEL_CURRENT_TEMP_ID = "current_temperature";

    public static final Map<String, String> CAP_OP_MODE = Map.of("@AC_MAIN_OPERATION_MODE_COOL_W", "Cool",
            "@AC_MAIN_OPERATION_MODE_DRY_W", "Dry", "@AC_MAIN_OPERATION_MODE_FAN_W", "Fan",
            "@AC_MAIN_OPERATION_MODE_HEAT_W", "Heat", "@AC_MAIN_OPERATION_MODE_AIRCLEAN_W", "Air Clean",
            "@AC_MAIN_OPERATION_MODE_ACO_W", "Auto", "@AC_MAIN_OPERATION_MODE_AI_W", "AI",
            "@AC_MAIN_OPERATION_MODE_ENERGY_SAVING_W", "Eco", "@AC_MAIN_OPERATION_MODE_AROMA_W", "Aroma",
            "@AC_MAIN_OPERATION_MODE_ANTIBUGS_W", "Anti Bugs");

    public static final Map<String, String> CAP_FAN_SPEED = Map.ofEntries(
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
}
