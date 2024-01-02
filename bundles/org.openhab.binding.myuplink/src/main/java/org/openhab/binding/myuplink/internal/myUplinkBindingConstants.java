/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

    private static final String BINDING_ID = "myuplink";

    // List of main device types
    public static final String DEVICE_ACCOUNT = "account";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, DEVICE_ACCOUNT);

    // List of all channel groups
    public static final String CHANNEL_GROUP_NONE = "";
    // TODO: add content

    // Channel types
    // TODO: add content
    public static final String CHANNEL_TYPEPREFIX_RW = "rw";

    // Channels with specific handling
    // TODO: add content

    // JSON Keys
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_AUTH_ACCESS_TOKEN = "access_token";
    public static final String JSON_KEY_AUTH_EXPIRES_IN = "expires_in";
    // TODO: add content

    // Write Commands
    // TODO: add content

    // Command Values
    // TODO: add content

    // web request constants
    // TODO: add content
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

    // TODO: add content

    // Status Keys
    public static final String STATUS_TOKEN_VALIDATED = "@text/status.token.validated";
    public static final String STATUS_WAITING_FOR_BRIDGE = "@text/status.waiting.for.bridge";
    public static final String STATUS_WAITING_FOR_LOGIN = "@text/status.waiting.for.login";
    public static final String STATUS_NO_VALID_DATA = "@text/status.no.valid.data";
    public static final String STATUS_NO_CONNECTION = "@text/status.no.connection";

    // other
    public static final long POLLING_INITIAL_DELAY = 1;

    public static final Instant OUTDATED_DATE = Instant.EPOCH;

    public static final String PARAMETER_NAME_WRITE_COMMAND = "writeCommand";
    public static final String PARAMETER_NAME_VALIDATION_REGEXP = "validationExpression";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT);
}
