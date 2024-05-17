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
    public static final String DEVICE_GENERIC_DEVICE = "genericDevice";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, DEVICE_ACCOUNT);
    public static final ThingTypeUID THING_TYPE_GENERIC_DEVICE = new ThingTypeUID(BINDING_ID, DEVICE_GENERIC_DEVICE);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT,
            THING_TYPE_GENERIC_DEVICE);

    // List of all channel groups
    public static final String CHANNEL_GROUP_NONE = "";

    // Channel types
    public static final String CHANNEL_TYPE_UNIT_NONE = "NO_UNIT";
    public static final String CHANNEL_TYPE_PREFIX_RW = "rw";

    // Channels with specific handling
    // TODO: add content

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
    public static final String JSON_KEY_SYSTEMS = "systems";
    public static final String JSON_KEY_DEVICES = "devices";
    public static final String JSON_KEY_GENERIC_ID = "id";
    public static final String JSON_KEY_PRODUCT = "product";
    public static final String JSON_KEY_SERIAL = "serialNumber";
    public static final String JSON_KEY_NAME = "name";
    public static final String JSON_KEY_CURRENT_FW_VERSION = "currentFwVersion";
    public static final String JSON_KEY_CONNECTION_STATE = "connectionState";

    public static final String JSON_KEY_AUTH_ACCESS_TOKEN = "access_token";
    public static final String JSON_KEY_AUTH_EXPIRES_IN = "expires_in";

    public static final String JSON_ENUM_KEY_TEXT = "text";
    public static final String JSON_ENUM_ORD_0 = "0";
    public static final String JSON_ENUM_ORD_1 = "1";
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

    public static final String JSON_VAL_CONNECTION_CONNECTED = "Connected";
    public static final String JSON_VAL_DECIMAL_SEPARATOR = ".";

    // Write Commands
    // TODO: add content

    // Command Values
    // TODO: add content

    // web request constants
    public static final long WEB_REQUEST_INITIAL_DELAY = 30;
    public static final long WEB_REQUEST_INTERVAL = 5;
    public static final int WEB_REQUEST_QUEUE_MAX_SIZE = 20;
    public static final int WEB_REQUEST_TOKEN_EXPIRY_BUFFER_MINUTES = 5;
    public static final int WEB_REQUEST_TOKEN_MAX_AGE_MINUTES = 45;
    public static final String WEB_REQUEST_PARAM_PAGE_KEY = "page";
    public static final String WEB_REQUEST_PARAM_PAGE_SIZE_KEY = "itemsPerPage";
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
    public static final String GET_DEVICE_POINTS = API_BASE_URL + "/v2/devices/{deviceId}/points";

    // Status Keys
    public static final String STATUS_TOKEN_VALIDATED = "@text/status.token.validated";
    public static final String STATUS_WAITING_FOR_BRIDGE = "@text/status.waiting.for.bridge";
    public static final String STATUS_WAITING_FOR_LOGIN = "@text/status.waiting.for.login";
    public static final String STATUS_NO_VALID_DATA = "@text/status.no.valid.data";
    public static final String STATUS_NO_CONNECTION = "@text/status.no.connection";
    public static final String STATUS_DEVICE_NOT_FOUND = "@text/status.device.not.found";

    // other
    public static final long POLLING_INITIAL_DELAY = 1;

    public static final String GENERIC_NO_VAL = "---";

    public static final String THING_CONFIG_ID = "deviceId";
    public static final String THING_CONFIG_SERIAL = "serial";
    public static final String THING_CONFIG_CURRENT_FW_VERSION = "currentFwVersion";

    public static final Instant OUTDATED_DATE = Instant.EPOCH;

    public static final String PARAMETER_NAME_WRITE_COMMAND = "writeCommand";
    public static final String PARAMETER_NAME_VALIDATION_REGEXP = "validationExpression";
}
