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
package org.openhab.binding.carnet.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CarNetApiConstants} defines various Carnet API contants
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetApiConstants {
    // HTTP header attributes
    public static final String CNAPI_HEADER_APP = "X-App-Name";
    public static final String CNAPI_HEADER_VERS = "X-App-Version";
    public static final String CNAPI_HEADER_VERS_VALUE = "1.0.0";
    public static final String CNAPI_HEADER_USER_AGENT = "okhttp/3.10.0";
    public static final String CNAPI_HEADER_AUTHORIZATION = "Authorization";
    public static final String CNAPI_HEADER_CLIENTID = "X-Client-Id";

    public static final String API_STATUS_MSG_PREFIX = "api-status";
    public static final String API_STATUS_CLASS_SECURUTY = "VSR.security";
    public static final String API_STATUS_GW_ERROR = "gw.error";

    // URIs: {0}=brand, {1} = country, {2} = VIN, {3} = userId
    public static final String CNAPI_URI_PROFILE = "https://customer-profile.apps.emea.vwapps.io/v1/customers/";
    public static final String CNAPI_URI_GET_TOKEN = "core/auth/v1/{0}/{1}/token";
    public static final String CNAPI_URI_VEHICLE_MANAGEMENT = "vehicleMgmt/vehicledata/v2/{0}/{1}/vehicles/{2}";
    public static final String CNAPI_URI_VEHICLE_LIST = "usermanagement/users/v1/{0}/{1}/vehicles";
    public static final String CNAPI_URI_VEHICLE_DETAILS = "vehicleMgmt/vehicledata/v2/{0}/{1}/vehicles/{2}";
    public static final String CNAPI_URI_VEHICLE_DATA = "bs/vsr/v1/{0}/{1}/vehicles/{2}/requests";
    public static final String CNAPI_URI_CLIMATER_TIMER = "bs/departuretimer/v1/{0}/{1}/vehicles/{2}/timer";
    public static final String CNAPI_URI_CHARGER_STATUS = "bs/batterycharge/v1/{0}/{1}/vehicles/{2}/charger";
    public static final String CNAPI_URI_DESTINATIONS = "destinationfeedservice/mydestinations/v1/{0}/{1}/vehicles/{2}/destinations";
    public static final String CNAPI_URI_GETTRIP = "bs/tripstatistics/v1/{0}/{1}/vehicles/{2}/tripdata/{3}?type={4}";

    public static final String CNAPI_VWG_MAL_1A_CONNECT = "https://mal-1a.prd.ece.vwg-connect.com/api";
    public static final String CNAPI_VWG_MAL_3A_CONNECT = "https://mal-3a.prd.eu.dp.vwg-connect.com/api";
    public static final String CNAPI_URL_GET_SEC_REGISTER = "https://mbboauth-1d.prd.ece.vwg-connect.com/mbbcoauth/mobile/oauth2/v1/register";
    public static final String CNAPI_URL_GET_SEC_TOKEN = "https://mbboauth-1d.prd.ece.vwg-connect.com/mbbcoauth/mobile/oauth2/v1/token";
    public static final String CNAPI_VW_TOKEN_URL = "https://mbboauth-1d.prd.ece.vwg-connect.com/mbbcoauth/mobile/oauth2/v1/token";
    public static final String CNAPI_URL_LOGIN = "https://login.apps.emea.vwapps.io/login/v1";

    // public static final String CNAPI_VWURL_OPERATIONS = "rolesrights/operationlist/v3/vehicles/{2}";
    public static final String CNAPI_VWURL_HOMEREGION = CNAPI_VWG_MAL_1A_CONNECT + "/cs/vds/v1/vehicles/{2}/homeRegion";

    public static final String CNAPI_OAUTH_BASE_URL = "https://identity.vwgroup.io";
    public static final String CNAPI_OAUTH_AUTHORIZE_URL = CNAPI_OAUTH_BASE_URL + "/oidc/v1/authorize";

    public static final String CNAPI_URL_DEF_GET_TOKEN = "https://tokenrefreshservice.apps.emea.vwapps.io/refreshTokens";

    public static final String CNAPI_SERVICE_APP_MEDIA = "appmedia_v1";
    public static final String CNAPI_SERVICE_CALENDAR = "app_calendar_v1";
    public static final String CNAPI_SERVICE_CAR_FINDER = "carfinder_v1";
    public static final String CNAPI_SERVICE_GEOFENCING = "geofence_v1";
    public static final String CNAPI_SERVICE_MOBILE_KEY = "mobilekey_v1";
    public static final String CNAPI_SERVICE_DESTINATIONS = "zieleinspeisung_v1";
    public static final String CNAPI_SERVICE_PICTURE_NAV1 = "picturenav_v1";
    public static final String CNAPI_SERVICE_PICTURE_NAV3 = "picturenav_v3";
    public static final String CNAPI_SERVICE_REMOTE_BATTERY_CHARGE = "rbatterycharge_v1";
    public static final String CNAPI_SERVICE_REMOTE_DEPARTURE_TIMER = "timerprogramming_v1";
    public static final String CNAPI_SERVICE_REMOTE_HEATING = "rheating_v1";
    public static final String CNAPI_SERVICE_REMOTE_HONK_AND_FLASH = "rhonk_v1";
    public static final String CNAPI_SERVICE_REMOTE_LOCK_UNLOCK = "rlu_v1";
    public static final String CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION = "rclima_v1";
    public static final String CNAPI_SERVICE_REMOTE_TRIP_STATISTICS = "trip_statistic_v1";
    public static final String CNAPI_SERVICE_SPEED_ALERT = "speedalert_v1";
    public static final String CNAPI_SERVICE_THEFT_ALARM = "dwap";
    public static final String CNAPI_SERVICE_TRAVELGUIDE = "travelguide_v1";
    public static final String CNAPI_SERVICE_VALET_ALERT = "valetalert_v1";
    public static final String CNAPI_SERVICE_VEHICLE_STATUS_REPORT = "statusreport_v1";

    // Service actions
    public static final String CNAPI_ACTION_CAR_FINDER_FIND_CAR = "FIND_CAR";
    public static final String CNAPI_ACTION_GEOFENCING_DELETE_VIOLATION = "D_ALERT";
    public static final String CNAPI_ACTION_GEOFENCING_GET_ALERTS = "G_ALERTS";
    public static final String CNAPI_ACTION_GEOFENCING_GET_DEFINITION_LIST = "G_DLIST";
    public static final String CNAPI_ACTION_GEOFENCING_GET_DEFINITION_LIST_STATUS = "G_DLSTATUS";
    public static final String CNAPI_ACTION_GEOFENCING_SAVE_DEFINITION_LIST = "P_DLIST";
    public static final String CNAPI_ACTION_MOBILE_KEY_ACCEPT_PERMISSION = "P_PERMISSION_ACCEPT";
    public static final String CNAPI_ACTION_MOBILE_KEY_CONFIRM_MOBILE_KEY_CREATION = "PU_MKCONFIRM";
    public static final String CNAPI_ACTION_MOBILE_KEY_CREATE_MOBILE_KEY = "P_MKCREATE";
    public static final String CNAPI_ACTION_MOBILE_KEY_GRANT_PERMISSION_CONFIRMATION_BY_VTAN = "P_VTAN";
    public static final String CNAPI_ACTION_MOBILE_KEY_GRANT_PERMISSION_START_PERMISSION = "P_PERMISSION";
    public static final String CNAPI_ACTION_MOBILE_KEY_READ_KEY_SYSTEM_USER_VIEW = "G_KEYSYSTEMUSERVIEW";
    public static final String CNAPI_ACTION_MOBILE_KEY_READ_MOBILE_KEYS = "G_MOBILEKEYS";
    public static final String CNAPI_ACTION_MOBILE_KEY_READ_PERMISSIONS = "G_PERMISSIONS";
    public static final String CNAPI_ACTION_MOBILE_KEY_READ_SERVICE_STATUS_VIEW = "G_SERVICESTATUSVIEW";
    public static final String CNAPI_ACTION_MOBILE_KEY_RETURN_MOBILE_KEY = "D_MOBILEKEY";
    public static final String CNAPI_ACTION_MOBILE_KEY_RETURN_PERMISSION = "D_PERMISSION_NUTZER";
    public static final String CNAPI_ACTION_MOBILE_KEY_REVOKE_PERMISSION = "D_PERMISSION";
    public static final String CNAPI_ACTION_MOBILE_KEY_UPDATE_PERMISSION_FRONTEND_ALIAS_PERMISSIONS = "PU_PERMISSION";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_GET_STATUS = "G_STATUS";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_START_CHARGING = "P_START";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_START_CHARGING_NOW = "START_CHARGING_NOW";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_START_CHARGING_NO_SET = "P_START_NOSET";
    public static final String CNAPI_ACTION_REMOTE_BATTERY_CHARGE_START_CHARGING_TIMER_BASED = "START_CHARGING_TIMER_BASED";
    public static final String CNAPI_ACTION_REMOTE_DEPARTURE_TIMER_GET_STATUS = "G_STATUS";
    public static final String CNAPI_ACTION_REMOTE_HEATING_GET_REQUEST_STATUS = "G_RQSTAT";
    public static final String CNAPI_ACTION_REMOTE_HEATING_GET_STATUS = "G_STAT";
    public static final String CNAPI_ACTION_REMOTE_HEATING_QUICK_START = "P_QSACT";
    public static final String CNAPI_ACTION_REMOTE_HEATING_QUICK_STOP = "P_QSTOPACT";
    public static final String CNAPI_ACTION_REMOTE_HEATING_SET_DEPARTURE_TIMERS = "P_DTACT";
    public static final String CNAPI_ACTION_REMOTE_HONK_AND_FLASH_PERFORM_REQUEST = "P_VREQ";
    public static final String CNAPI_ACTION_REMOTE_HONK_AND_FLASH_REQUEST_HISTORY = "G_RHIST";
    public static final String CNAPI_ACTION_REMOTE_HONK_AND_FLASH_REQUEST_STATUS = "G_REQSTATUS";
    public static final String CNAPI_ACTION_REMOTE_LOCK_UNLOCK_GET_LAST_ACTIONS = "G_LACT";
    public static final String CNAPI_ACTION_REMOTE_LOCK_UNLOCK_GET_REQUEST_STATUS = "G_RQSTAT";
    public static final String CNAPI_ACTION_REMOTE_LOCK_UNLOCK_LOCK = "LOCK";
    public static final String CNAPI_ACTION_REMOTE_LOCK_UNLOCK_UNLOCK = "UNLOCK";
    public static final String CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_GET_STATUS = "G_STATUS";
    public static final String CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_START_AUX_OR_AUTO = "P_START_CLIMA_AU";
    public static final String CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_START_ELECTRIC = "P_START_CLIMA_EL";
    public static final String CNAPI_ACTION_REMOTE_TRIP_STATISTICS_DELETE_STATISTICS = "D_TRIPDATA";
    public static final String CNAPI_ACTION_REMOTE_TRIP_STATISTICS_GET_STATISTICS = "G_TRIPDATA";
    public static final String CNAPI_ACTION_SPEED_ALERT_DELETE_VIOLATION = "D_ALERT";
    public static final String CNAPI_ACTION_SPEED_ALERT_GET_ALERTS = "G_ALERTS";
    public static final String CNAPI_ACTION_SPEED_ALERT_GET_DEFINITION_LIST = "G_DLIST";
    public static final String CNAPI_ACTION_SPEED_ALERT_GET_DEFINITION_LIST_STATUS = "G_DLSTATUS";
    public static final String CNAPI_ACTION_SPEED_ALERT_SAVE_DEFINITION_LIST = "P_DLIST";
    public static final String CNAPI_ACTION_THEFT_ALARM_DELETE_WARNING = "D_NHIST";
    public static final String CNAPI_ACTION_THEFT_ALARM_GET_WARNINGS = "G_NHIST";
    public static final String CNAPI_ACTION_VALET_ALERT_DELETE_DEFINITION = "D_DEF";
    public static final String CNAPI_ACTION_VALET_ALERT_DELETE_VIOLATION = "D_ALERT";
    public static final String CNAPI_ACTION_VALET_ALERT_GET_ALERTS = "G_ALERTS";
    public static final String CNAPI_ACTION_VALET_ALERT_GET_DEFINITION = "G_DEF";
    public static final String CNAPI_ACTION_VALET_ALERT_GET_DEFINITION_STATUS = "G_DSTATUS";
    public static final String CNAPI_ACTION_VALET_ALERT_SAVE_DEFINITION = "P_DEF";
    public static final String CNAPI_ACTION_VEHICLE_STATUS_REPORT_GET_CURRENT_VEHICLE_DATA = "G_CVDATA";
    public static final String CNAPI_ACTION_VEHICLE_STATUS_REPORT_GET_CURRENT_VEHICLE_DATA_BY_ID = "G_CVDATAID";
    public static final String CNAPI_ACTION_VEHICLE_STATUS_REPORT_GET_REQUEST_STATUS = "G_RQSTAT";
    public static final String CNAPI_ACTION_VEHICLE_STATUS_REPORT_GET_STORED_VEHICLE_DATA = "G_SVDATA";

    public static final String CNAPI_HEATER_SOURCE_AUX = "auxiliary";
    public static final String CNAPI_HEATER_SOURCE_ELECTRIC = "electric";
    public static final String CNAPI_HEATER_SOURCE_AUTOMATIC = "automatic";

    public static final String CNAPI_SERVICE_TRIPSTATS = "tripstatistics";
    public static final String CNAPI_TRIP_SHORT_TERM = "shortTerm";
    public static final String CNAPI_TRIP_LONG_TERM = "longTerm";

    public static final String CNAPI_REQUEST_SUCCESSFUL = "request_successful";
    public static final String CNAPI_REQUEST_IN_PROGRESS = "request_in_progress";
    public static final String CNAPI_REQUEST_NOT_FOUND = "request_not_found";
    public static final String CNAPI_REQUEST_FAIL = "request_fail";
    public static final String CNAPI_REQUEST_QUEUED = "queued"; // rclima
    public static final String CNAPI_REQUEST_FETCHED = "fetched"; // rclima
    public static final String CNAPI_REQUEST_FAILED = "failed";
    public static final String CNAPI_REQUEST_ERROR = "api_error";
    public static final String CNAPI_REQUEST_TIMEOUT = "timoute";
}
