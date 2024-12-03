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
package org.openhab.binding.myuplink.internal;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MyUplinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public class MyUplinkBindingConstants {

    public static final String BINDING_ID = "myuplink";

    // List of main device types
    public static final String DEVICE_ACCOUNT = "account";
    public static final String DEVICE_GENERIC_DEVICE = "generic-device";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, DEVICE_ACCOUNT);
    public static final ThingTypeUID THING_TYPE_GENERIC_DEVICE = new ThingTypeUID(BINDING_ID, DEVICE_GENERIC_DEVICE);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT,
            THING_TYPE_GENERIC_DEVICE);

    // Channel types
    public static final String CHANNEL_TYPE_UNIT_NONE = "NO_UNIT";
    public static final String CHANNEL_TYPE_PREFIX_RW = "rw";
    public static final String CHANNEL_TYPE_ENUM_PRFIX = "type-enum-";
    public static final String CHANNEL_TYPE_NUMERIC_PRFIX = "type-numeric-";
    public static final String CHANNEL_TYPE_DEFAULT_DATATYPE = "Number";

    public static final String CHANNEL_TYPE_ENERGY = "type-energy";
    public static final String CHANNEL_TYPE_ENERGY_UNIT = "kWh";
    public static final String CHANNEL_TYPE_PRESSURE = "type-pressure";
    public static final String CHANNEL_TYPE_PRESSURE_UNIT = "bar";
    public static final String CHANNEL_TYPE_PERCENT = "type-percent";
    public static final String CHANNEL_TYPE_PERCENT_UNIT = "%";
    public static final String CHANNEL_TYPE_TEMPERATURE = "type-temperature";
    public static final String CHANNEL_TYPE_TEMPERATURE_UNIT = "°C";
    public static final String CHANNEL_TYPE_FREQUENCY = "type-frequency";
    public static final String CHANNEL_TYPE_FREQUENCY_UNIT = "Hz";
    public static final String CHANNEL_TYPE_FLOW = "type-flow";
    public static final String CHANNEL_TYPE_FLOW_UNIT = "l/m";
    public static final String CHANNEL_TYPE_ELECTRIC_CURRENT = "type-electric-current";
    public static final String CHANNEL_TYPE_ELECTRIC_CURRENT_UNIT = "A";
    public static final String CHANNEL_TYPE_TIME = "type-time";
    public static final String CHANNEL_TYPE_TIME_UNIT = "h";
    public static final String CHANNEL_TYPE_INTEGER = "type-number-integer";
    public static final String CHANNEL_TYPE_DOUBLE = "type-number-double";
    public static final String CHANNEL_TYPE_ON_OFF = "type-on-off";
    public static final String CHANNEL_TYPE_RW_SWITCH = "rwtype-switch";
    public static final String CHANNEL_TYPE_RW_COMMAND = "rwtype-command";
    public static final String CHANNEL_TYPE_RW_MODE = "rwtype-mode";

    public static final String CHANNEL_ID_COMMAND = "command";
    public static final String CHANNEL_ID_SMART_HOME_MODE = "smart-home-mode";

    // JSON Keys
    public static final String JSON_KEY_ROOT_DATA = "data";
    public static final String JSON_KEY_CHANNEL_STR_VAL = "strVal";
    public static final String JSON_KEY_CHANNEL_VALUE = "value";
    public static final String JSON_KEY_CHANNEL_WRITABLE = "writable";
    public static final String JSON_KEY_CHANNEL_ENUM_VALUES = "enumValues";
    public static final String JSON_KEY_CHANNEL_ID = "parameterId";
    public static final String JSON_KEY_CHANNEL_LABEL = "parameterName";
    public static final String JSON_KEY_CHANNEL_UNIT = "parameterUnit";
    public static final String JSON_KEY_CHANNEL_SCALE = "scaleValue";
    public static final String JSON_KEY_CHANNEL_MIN = "minValue";
    public static final String JSON_KEY_CHANNEL_MAX = "maxValue";
    public static final String JSON_KEY_CHANNEL_STEP = "stepValue";
    public static final String JSON_KEY_SYSTEMS = "systems";
    public static final String JSON_KEY_SYSTEM_ID = "systemId";
    public static final String JSON_KEY_DEVICES = "devices";
    public static final String JSON_KEY_GENERIC_ID = "id";
    public static final String JSON_KEY_PRODUCT = "product";
    public static final String JSON_KEY_SERIAL = "serialNumber";
    public static final String JSON_KEY_NAME = "name";
    public static final String JSON_KEY_CURRENT_FW_VERSION = "currentFwVersion";
    public static final String JSON_KEY_CONNECTION_STATE = "connectionState";
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_SMART_HOME_MODE = "smartHomeMode";

    public static final String JSON_KEY_AUTH_ACCESS_TOKEN = "access_token";
    public static final String JSON_KEY_AUTH_EXPIRES_IN = "expires_in";

    public static final String JSON_ENUM_KEY_TEXT = "text";
    public static final String JSON_ENUM_ORD_0 = "0";
    public static final String JSON_ENUM_ORD_1 = "1";
    public static final String JSON_ENUM_ORD_4 = "4";
    public static final String JSON_ENUM_ORD_6 = "6";
    public static final String JSON_ENUM_ORD_10 = "10";
    public static final String JSON_ENUM_ORD_20 = "20";
    public static final String JSON_ENUM_ORD_30 = "30";
    public static final String JSON_ENUM_ORD_40 = "40";
    public static final String JSON_ENUM_ORD_60 = "60";
    public static final String JSON_ENUM_ORD_100 = "100";
    public static final String JSON_ENUM_VAL_OFF = "off";
    public static final String JSON_ENUM_VAL_ON = "on";
    public static final String JSON_ENUM_VAL_HOT_WATER = "hot water";
    public static final String JSON_ENUM_VAL_HEATING = "heating";
    public static final String JSON_ENUM_VAL_POOL = "pool";
    public static final String JSON_ENUM_VAL_STARTS = "starts";
    public static final String JSON_ENUM_VAL_RUNS = "runs";
    public static final String JSON_ENUM_VAL_ALARM = "alarm";
    public static final String JSON_ENUM_VAL_BLOCKED = "blocked";
    public static final String JSON_ENUM_VAL_ACTIVE = "active";

    public static final String JSON_VAL_CONNECTION_CONNECTED = "Connected";
    public static final String JSON_VAL_DECIMAL_SEPARATOR = ".";

    // web request constants
    public static final long WEB_REQUEST_INITIAL_DELAY = 10;
    public static final long WEB_REQUEST_INTERVAL = 5;
    public static final int WEB_REQUEST_QUEUE_MAX_SIZE = 20;
    public static final int WEB_REQUEST_TOKEN_EXPIRY_BUFFER_MINUTES = 5;
    public static final int WEB_REQUEST_TOKEN_MAX_AGE_MINUTES = 45;
    public static final String WEB_REQUEST_PARAM_PAGE_KEY = "page";
    public static final String WEB_REQUEST_PARAM_PAGE_SIZE_KEY = "itemsPerPage";
    public static final String WEB_REQUEST_PATCH_CONTENT_TYPE = "application/json-patch+json";
    public static final int WEB_REQUEST_PARAM_PAGE_SIZE_VALUE = 100;
    public static final String WEB_REQUEST_BEARER_TOKEN_PREFIX = "Bearer ";
    public static final String LOGIN_BASIC_AUTH_PREFIX = "Basic ";
    public static final String LOGIN_FIELD_SCOPE_KEY = "scope";
    public static final String LOGIN_FIELD_SCOPE_VALUE = "READSYSTEM WRITESYSTEM";
    public static final String LOGIN_FIELD_GRANT_TYPE_KEY = "grant_type";
    public static final String LOGIN_FIELD_GRANT_TYPE_VALUE = "client_credentials";

    // URLs
    private static final String API_BASE_URL = "https://api.myuplink.com";
    public static final String LOGIN_URL = API_BASE_URL + "/oauth/token";
    public static final String GET_SYSTEMS_URL = API_BASE_URL + "/v2/systems/me";
    public static final String GET_SMART_HOME_MODE_URL = API_BASE_URL + "/v2/systems/{systemId}/smart-home-mode";
    public static final String SET_SMART_HOME_MODE_URL = GET_SMART_HOME_MODE_URL;
    public static final String GET_DEVICE_POINTS = API_BASE_URL + "/v2/devices/{deviceId}/points";
    public static final String SET_DEVICE_POINTS = GET_DEVICE_POINTS;

    // Status Keys
    public static final String STATUS_TOKEN_VALIDATED = "@text/status.token.validated";
    public static final String STATUS_WAITING_FOR_BRIDGE = "@text/status.waiting.for.bridge";
    public static final String STATUS_WAITING_FOR_LOGIN = "@text/status.waiting.for.login";
    public static final String STATUS_NO_VALID_DATA = "@text/status.no.valid.data";
    public static final String STATUS_NO_CONNECTION = "@text/status.no.connection";
    public static final String STATUS_DEVICE_NOT_FOUND = "@text/status.device.not.found";
    public static final String STATUS_CONFIG_ERROR_NO_CLIENT_ID = "@text/status.config.error.no.client.id";
    public static final String STATUS_CONFIG_ERROR_NO_CLIENT_SECRET = "@text/status.config.error.no.client.secret";

    // other
    public static final long POLLING_INITIAL_DELAY = 5;

    public static final String GENERIC_NO_VAL = "---";
    public static final String EMPTY = "";

    public static final String THING_CONFIG_ID = "deviceId";
    public static final String THING_CONFIG_SYSTEM_ID = "systemId";
    public static final String THING_CONFIG_SERIAL = "serial";
    public static final String THING_CONFIG_CURRENT_FW_VERSION = "currentFwVersion";

    public static final Instant OUTDATED_DATE = Instant.EPOCH;

    public static final String PARAMETER_NAME_WRITE_COMMAND = "writeCommand";
    public static final String PARAMETER_NAME_VALIDATION_REGEXP = "validationExpression";
    public static final String DEFAULT_VALIDATION_EXPRESSION = "[0-9]+";
}
