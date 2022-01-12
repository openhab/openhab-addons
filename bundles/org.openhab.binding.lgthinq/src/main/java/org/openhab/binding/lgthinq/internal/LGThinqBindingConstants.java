/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;

/**
 * The {@link LGThinqBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqBindingConstants {

    public static final String BINDING_ID = "lgthinq";

    public static final String THINQ_USER_DATA_FOLDER = OpenHAB.getUserDataFolder() + File.separator + "thinq";
    public static String THINQ_CONNECTION_DATA_FILE = THINQ_USER_DATA_FOLDER + File.separator + "thinqbridge.json";

    public static final String V2_AUTH_PATH = "/oauth/1.0/oauth2/token";
    public static final String V2_USER_INFO = "/users/profile";
    public static final String V2_API_KEY = "VGhpblEyLjAgU0VSVklDRQ==";
    public static final String V2_CLIENT_ID = "65260af7e8e6547b51fdccf930097c51eb9885a508d3fddfa9ee6cdec22ae1bd";
    public static final String V2_SVC_PHASE = "OP";
    public static final String V2_APP_LEVEL = "PRD";
    public static final String V2_APP_OS = "LINUX";
    public static final String V2_APP_TYPE = "NUTS";
    public static final String V2_APP_VER = "3.0.1700";

    public static final String V2_LS_PATH = "/service/application/dashboard";
    public static final String V2_DEVICE_CONFIG_PATH = "service/devices/";
    public static final String V2_CTRL_DEVICE_CONFIG_PATH = "service/devices/%s/control-sync";
    public static final String V1_START_MON_PATH = "rti/rtiMon";
    public static final String V1_POOL_MON_PATH = "rti/rtiResult";
    public static final String V1_CONTROL_OP = "rti/rtiControl";
    public static final String GATEWAY_URL = "https://route.lgthinq.com:46030/v1/service/application/gateway-uri";
    public static final String SECURITY_KEY = "nuts_securitykey";
    public static final String SVC_CODE = "SVC202";
    public static final String OAUTH_SECRET_KEY = "c053c2a6ddeb7ad97cb0eed0dcb31cf8";
    public static final String OAUTH_CLIENT_KEY = "LGAO722A02";
    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss +0000";
    public static final String APPLICATION_KEY = "6V1V8H2BN5P9ZQGOI5DAQ92YZBDO3EK9";
    public static final String V2_EMP_SESS_URL = "https://emp-oauth.lgecloud.com/emp/oauth2/token/empsession";
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

    public static final int SEARCH_TIME = 20;
    // delay between each devices's scan for state changes (in seconds)
    public static final int DEFAULT_STATE_POOLING_UPDATE_DELAY = 30;
    // CHANNEL IDS
    public static final String CHANNEL_MOD_OP_ID = "op_mode";
    public static final String CHANNEL_FAN_SPEED_ID = "fan_speed";
    public static final String CHANNEL_POWER_ID = "power";
    public static final String CHANNEL_TARGET_TEMP_ID = "target_temperature";
    public static final String CHANNEL_CURRENT_TEMP_ID = "current_temperature";
}
