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
package org.openhab.binding.easee.internal;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    public static final String DEVICE_CIRCUIT = "circuit";
    public static final String DEVICE_CHARGER = "charger";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SITE = new ThingTypeUID(BINDING_ID, DEVICE_SITE);
    public static final ThingTypeUID THING_TYPE_CIRCUIT = new ThingTypeUID(BINDING_ID, DEVICE_CIRCUIT);
    public static final ThingTypeUID THING_TYPE_CHARGER = new ThingTypeUID(BINDING_ID, DEVICE_CHARGER);

    // List of all channel groups
    public static final String CHANNEL_GROUP_NONE = "";
    public static final String CHANNEL_GROUP_SITE_INFO = "info";
    public static final String CHANNEL_GROUP_CHARGER_STATE = "state";
    public static final String CHANNEL_GROUP_CHARGER_CONFIG = "config";
    public static final String CHANNEL_GROUP_CHARGER_LATEST_SESSION = "latest_session";
    public static final String CHANNEL_GROUP_CIRCUIT_DYNAMIC_CURRENT = "dynamic_current";

    // Channel types
    public static final String CHANNEL_TYPE_SWITCH = "Switch";
    public static final String CHANNEL_TYPE_VOLT = "Number:ElectricPotential";
    public static final String CHANNEL_TYPE_AMPERE = "Number:ElectricCurrent";
    public static final String CHANNEL_TYPE_KWH = "Number:Energy";
    public static final String CHANNEL_TYPE_KW = "Number:Power";
    public static final String CHANNEL_TYPE_DATE = "DateTime";
    public static final String CHANNEL_TYPE_STRING = "String";
    public static final String CHANNEL_TYPE_NUMBER = "Number";

    public static final String CHANNEL_TYPEPREFIX_RW = "rw";

    public static final String CHANNEL_TYPENAME_INTEGER = "type-integer";

    // JSON Keys
    public static final String JSON_KEY_GENERIC_ID = "id";
    public static final String JSON_KEY_GENERIC_NAME = "name";
    public static final String JSON_KEY_CIRCUIT_NAME = "panelName";
    public static final String JSON_KEY_CIRCUITS = "circuits";
    public static final String JSON_KEY_CHARGERS = "chargers";
    public static final String JSON_KEY_BACK_PLATE = "backPlate";
    public static final String JSON_KEY_MASTER_BACK_PLATE_ID = "masterBackPlateId";
    public static final String JSON_KEY_ONLINE = "isOnline";
    public static final String JSON_KEY_SITE_KEY = "siteKey";
    public static final String JSON_KEY_ERROR_TITLE = "title";
    public static final String JSON_KEY_AUTH_ACCESS_TOKEN = "accessToken";
    public static final String JSON_KEY_AUTH_REFRESH_TOKEN = "refreshToken";
    public static final String JSON_KEY_AUTH_EXPIRES_IN = "expiresIn";

    // Write Commands
    public static final String COMMAND_CHANGE_CONFIGURATION = "ChangeConfiguration";
    public static final String COMMAND_SEND_COMMAND = "SendCommand";

    // web request constants
    public static final long WEB_REQUEST_INITIAL_DELAY = TimeUnit.SECONDS.toMillis(30);
    public static final long WEB_REQUEST_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    public static final int WEB_REQUEST_QUEUE_MAX_SIZE = 20;
    public static final int WEB_REQUEST_TOKEN_EXPIRY_BUFFER_MINUTES = 5;
    public static final int WEB_REQUEST_TOKEN_MAX_AGE_MINUTES = 60;
    public static final String WEB_REQUEST_BEARER_TOKEN_PREFIX = "Bearer ";

    // URLs
    public static final String LOGIN_URL = "https://api.easee.cloud/api/accounts/login";
    public static final String REFRESH_TOKEN_URL = "https://api.easee.cloud/api/accounts/refresh_token";
    public static final String GET_SITE_URL = "https://api.easee.cloud/api/sites/{siteId}";
    public static final String STATE_URL = "https://api.easee.cloud/api/chargers/{id}/state";
    public static final String GET_CONFIGURATION_URL = "https://api.easee.cloud/api/chargers/{id}/config";
    public static final String CHANGE_CONFIGURATION_URL = "https://api.easee.cloud/api/chargers/{id}/settings";
    public static final String COMMANDS_URL = "https://api.easee.cloud/api/chargers/{id}/commands/{command}";
    public static final String LATEST_CHARGING_SESSION_URL = "https://api.easee.cloud/api/chargers/{id}/sessions/latest";
    public static final String DYNAMIC_CIRCUIT_CURRENT_URL = "https://api.easee.cloud/api/sites/{siteId}/circuits/{circuitId}/dynamicCurrent";

    // other
    public static final long POLLING_INITIAL_DELAY = 1;

    public static final String THING_CONFIG_ID = "id";
    public static final String THING_CONFIG_SITE_ID = "siteId";

    public static final Date INVALID_DATE = new GregorianCalendar(1900, 1, 1).getTime();
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String PARAMETER_NAME_WRITE_COMMAND = "writeCommand";
    public static final String PARAMETER_NAME_VALIDATION_REGEXP = "validationExpression";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SITE, THING_TYPE_CIRCUIT,
            THING_TYPE_CHARGER);
}
