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

    private static final String BINDING_ID = "easee";

    // List of main device types
    public static final String DEVICE_WALLBOX = "wallbox";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WALLBOX = new ThingTypeUID(BINDING_ID, DEVICE_WALLBOX);

    // List of all channel groups
    public static final String CHANNEL_GROUP_NONE = "";
    public static final String CHANNEL_GROUP_CHARGER_STATE = "charger_state";
    public static final String CHANNEL_GROUP_CHARGER_CONFIG = "charger_config";
    public static final String CHANNEL_GROUP_CHARGER_LATEST_SESSION = "charger_latest_session";

    // Channel types
    public static final String CHANNEL_TYPE_SWITCH = "Switch";
    public static final String CHANNEL_TYPE_VOLT = "Number:ElectricPotential";
    public static final String CHANNEL_TYPE_AMPERE = "Number:ElectricCurrent";
    public static final String CHANNEL_TYPE_KWH = "Number:Energy";
    public static final String CHANNEL_TYPE_KW = "Number:Power";
    public static final String CHANNEL_TYPE_DATE = "DateTime";
    public static final String CHANNEL_TYPE_NUMBER = "Number";

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
    public static final String STATE_URL = "https://api.easee.cloud/api/chargers/{id}/state";
    public static final String GET_CONFIGURATION_URL = "https://api.easee.cloud/api/chargers/{id}/config";
    public static final String LATEST_CHARGING_SESSION_URL = "https://api.easee.cloud/api/chargers/{id}/sessions/latest";

    // other
    public static final Date INVALID_DATE = new GregorianCalendar(1900, 1, 1).getTime();
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
