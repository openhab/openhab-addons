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
package org.openhab.binding.easee.internal;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EaseeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public class EaseeBindingConstants {

    public static final String BINDING_ID = "easee";

    // List of main device types
    public static final String DEVICE_SITE = "site";
    public static final String DEVICE_MASTER_CHARGER = "mastercharger";
    public static final String DEVICE_CHARGER = "charger";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SITE = new ThingTypeUID(BINDING_ID, DEVICE_SITE);
    public static final ThingTypeUID THING_TYPE_MASTER_CHARGER = new ThingTypeUID(BINDING_ID, DEVICE_MASTER_CHARGER);
    public static final ThingTypeUID THING_TYPE_CHARGER = new ThingTypeUID(BINDING_ID, DEVICE_CHARGER);

    // List of all channel groups
    public static final String CHANNEL_GROUP_NONE = "";
    public static final String CHANNEL_GROUP_SITE_INFO = "info";
    public static final String CHANNEL_GROUP_CHARGER = "charger";
    public static final String CHANNEL_GROUP_CHARGER_STATE = "state";
    public static final String CHANNEL_GROUP_CHARGER_CONFIG = "config";
    public static final String CHANNEL_GROUP_CHARGER_COMMANDS = "commands";
    public static final String CHANNEL_GROUP_CHARGER_LATEST_SESSION = "latestSession";
    public static final String CHANNEL_GROUP_CIRCUIT_DYNAMIC_CURRENT = "dynamicCurrent";
    public static final String CHANNEL_GROUP_CIRCUIT_SETTINGS = "settings";

    // Channel types
    public static final String CHANNEL_TYPE_SWITCH = "Switch";
    public static final String CHANNEL_TYPE_VOLT = "Number:ElectricPotential";
    public static final String CHANNEL_TYPE_AMPERE = "Number:ElectricCurrent";
    public static final String CHANNEL_TYPE_KWH = "Number:Energy";
    public static final String CHANNEL_TYPE_POWER = "Number:Power";
    public static final String CHANNEL_TYPE_DATE = "DateTime";
    public static final String CHANNEL_TYPE_STRING = "String";
    public static final String CHANNEL_TYPE_NUMBER = "Number";

    public static final String CHANNEL_TYPEPREFIX_RW = "rw";

    public static final String CHANNEL_TYPENAME_INTEGER = "type-integer";
    public static final String CHANNEL_TYPENAME_RSSI = "type-rssi";

    // Channels with specific handling
    public static final String CHANNEL_CHARGER_OP_MODE = "chargerOpMode";
    public static final String CHANNEL_CHARGER_DYNAMIC_CURRENT = "dynamicChargerCurrent";
    public static final String CHANNEL_CHARGER_REASON_FOR_NO_CURRENT = "reasonForNoCurrent";
    public static final String CHANNEL_CHARGER_START_STOP = "startStop";
    public static final String CHANNEL_CHARGER_PAUSE_RESUME = "pauseResume";
    public static final String CHANNEL_CIRCUIT_DYNAMIC_CURRENTS = "dynamicCurrents";
    public static final String CHANNEL_CIRCUIT_DYNAMIC_CURRENT_PHASE1 = "phase1";
    public static final String CHANNEL_CIRCUIT_DYNAMIC_CURRENT_PHASE2 = "phase2";
    public static final String CHANNEL_CIRCUIT_DYNAMIC_CURRENT_PHASE3 = "phase3";
    public static final String CHANNEL_CIRCUIT_MAX_CURRENTS = "maxCurrents";
    public static final String CHANNEL_CIRCUIT_MAX_CURRENT_PHASE1 = "maxCircuitCurrentP1";
    public static final String CHANNEL_CIRCUIT_MAX_CURRENT_PHASE2 = "maxCircuitCurrentP2";
    public static final String CHANNEL_CIRCUIT_MAX_CURRENT_PHASE3 = "maxCircuitCurrentP3";
    public static final String CHANNEL_CIRCUIT_OFFLINE_MAX_CURRENTS = "offlineMaxCurrents";
    public static final String CHANNEL_CIRCUIT_OFFLINE_MAX_CURRENT_PHASE1 = "offlineMaxCircuitCurrentP1";
    public static final String CHANNEL_CIRCUIT_OFFLINE_MAX_CURRENT_PHASE2 = "offlineMaxCircuitCurrentP2";
    public static final String CHANNEL_CIRCUIT_OFFLINE_MAX_CURRENT_PHASE3 = "offlineMaxCircuitCurrentP3";

    // JSON Keys
    public static final String JSON_KEY_GENERIC_ID = "id";
    public static final String JSON_KEY_GENERIC_NAME = "name";
    public static final String JSON_KEY_CIRCUIT_NAME = "panelName";
    public static final String JSON_KEY_CIRCUIT_ID = "circuitId";
    public static final String JSON_KEY_CHARGER_ID = "chargerID";
    public static final String JSON_KEY_CIRCUITS = "circuits";
    public static final String JSON_KEY_CHARGERS = "chargers";
    public static final String JSON_KEY_BACK_PLATE = "backPlate";
    public static final String JSON_KEY_MASTER_BACK_PLATE = "masterBackPlate";
    public static final String JSON_KEY_MASTER_BACK_PLATE_ID = "masterBackPlateId";
    public static final String JSON_KEY_ONLINE = "isOnline";
    public static final String JSON_KEY_SITE_KEY = "siteKey";
    public static final String JSON_KEY_ERROR_TITLE = "title";
    public static final String JSON_KEY_AUTH_ACCESS_TOKEN = "accessToken";
    public static final String JSON_KEY_AUTH_REFRESH_TOKEN = "refreshToken";
    public static final String JSON_KEY_AUTH_EXPIRES_IN = "expiresIn";
    public static final String JSON_KEY_CIRCUIT_STATES = "circuitStates";
    public static final String JSON_KEY_CHARGER_STATES = "chargerStates";
    public static final String JSON_KEY_CHARGER_STATE = "chargerState";

    // Write Commands
    public static final String COMMAND_CHANGE_CONFIGURATION = "ChangeConfiguration";
    public static final String COMMAND_SEND_COMMAND = "SendCommand";
    public static final String COMMAND_SEND_COMMAND_START_STOP = "SendCommandStartStop";
    public static final String COMMAND_SEND_COMMAND_PAUSE_RESUME = "SendCommandPauseResume";
    public static final String COMMAND_SET_CIRCUIT_SETTINGS = "SetCircuitSettings";
    public static final String COMMAND_SET_DYNAMIC_CIRCUIT_CURRENTS = "SetDynamicCircuitCurrents";
    public static final String COMMAND_SET_MAX_CIRCUIT_CURRENTS = "SetMaxCircuitCurrents";
    public static final String COMMAND_SET_OFFLINE_MAX_CIRCUIT_CURRENTS = "SetOfflineMaxCircuitCurrents";

    // Command Values
    public static final String CMD_VAL_START_CHARGING = "start_charging";
    public static final String CMD_VAL_STOP_CHARGING = "stop_charging";
    public static final String CMD_VAL_PAUSE_CHARGING = "pause_charging";
    public static final String CMD_VAL_RESUME_CHARGING = "resume_charging";

    // web request constants
    public static final long WEB_REQUEST_INITIAL_DELAY = 30;
    public static final long WEB_REQUEST_INTERVAL = 5;
    public static final int WEB_REQUEST_QUEUE_MAX_SIZE = 20;
    public static final int WEB_REQUEST_TOKEN_EXPIRY_BUFFER_MINUTES = 5;
    public static final int WEB_REQUEST_TOKEN_MAX_AGE_MINUTES = 60;
    public static final String WEB_REQUEST_BEARER_TOKEN_PREFIX = "Bearer ";

    // URLs
    private static final String API_BASE_URL = "https://api.easee.com/api";
    public static final String LOGIN_URL = API_BASE_URL + "/accounts/login";
    public static final String REFRESH_TOKEN_URL = API_BASE_URL + "/accounts/refresh_token";
    public static final String GET_SITE_URL = API_BASE_URL + "/sites/{siteId}";
    public static final String CHARGER_URL = API_BASE_URL + "/chargers/{id}";
    public static final String SITE_STATE_URL = API_BASE_URL + "/sites/{siteId}/state";
    public static final String GET_CONFIGURATION_URL = API_BASE_URL + "/chargers/{id}/config";
    public static final String CHANGE_CONFIGURATION_URL = API_BASE_URL + "/chargers/{id}/settings";
    public static final String COMMANDS_URL = API_BASE_URL + "/chargers/{id}/commands/{command}";
    public static final String LATEST_CHARGING_SESSION_URL = API_BASE_URL + "/chargers/{id}/sessions/latest";
    public static final String DYNAMIC_CIRCUIT_CURRENT_URL = API_BASE_URL
            + "/sites/{siteId}/circuits/{circuitId}/dynamicCurrent";
    public static final String CIRCUIT_SETTINGS_URL = API_BASE_URL + "/sites/{siteId}/circuits/{circuitId}/settings";

    // Status Keys
    public static final String STATUS_TOKEN_VALIDATED = "@text/status.token.validated";
    public static final String STATUS_WAITING_FOR_BRIDGE = "@text/status.waiting.for.bridge";
    public static final String STATUS_WAITING_FOR_LOGIN = "@text/status.waiting.for.login";
    public static final String STATUS_NO_VALID_DATA = "@text/status.no.valid.data";
    public static final String STATUS_NO_CONNECTION = "@text/status.no.connection";

    // other
    public static final long POLLING_INITIAL_DELAY = 1;

    public static final String GENERIC_YES = "Yes";
    public static final String GENERIC_NO = "No";
    public static final double CHARGER_DYNAMIC_CURRENT_PAUSE = 0;
    public static final int CHARGER_REASON_FOR_NO_CURRENT_CIRCUIT_LIMIT = 2;
    public static final int CHARGER_REASON_FOR_NO_CURRENT_CHARGER_LIMIT = 52;

    public static final String THING_CONFIG_ID = "id";
    public static final String THING_CONFIG_SITE_ID = "siteId";
    public static final String THING_CONFIG_CIRCUIT_ID = "circuitId";
    public static final String THING_CONFIG_CIRCUIT_NAME = "circuitName";
    public static final String THING_CONFIG_IS_MASTER = "isMaster";
    public static final String THING_CONFIG_BACK_PLATE_ID = "backPlateId";
    public static final String THING_CONFIG_MASTER_BACK_PLATE_ID = "masterBackPlateId";

    public static final Instant OUTDATED_DATE = Instant.EPOCH;
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String PARAMETER_NAME_WRITE_COMMAND = "writeCommand";
    public static final String PARAMETER_NAME_VALIDATION_REGEXP = "validationExpression";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SITE,
            THING_TYPE_MASTER_CHARGER, THING_TYPE_CHARGER);
}
