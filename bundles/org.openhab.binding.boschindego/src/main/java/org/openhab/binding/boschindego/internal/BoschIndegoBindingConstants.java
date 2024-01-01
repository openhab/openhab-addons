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
package org.openhab.binding.boschindego.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BoschIndegoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jonas Fleck - Initial contribution
 */
@NonNullByDefault
public class BoschIndegoBindingConstants {

    public static final String BINDING_ID = "boschindego";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_INDEGO = new ThingTypeUID(BINDING_ID, "indego");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_INDEGO);

    // List of all Channel ids
    public static final String STATE = "state";
    public static final String TEXTUAL_STATE = "textualstate";
    public static final String MOWED = "mowed";
    public static final String ERRORCODE = "errorcode";
    public static final String STATECODE = "statecode";
    public static final String READY = "ready";
    public static final String LAST_CUTTING = "lastCutting";
    public static final String NEXT_CUTTING = "nextCutting";
    public static final String BATTERY_VOLTAGE = "batteryVoltage";
    public static final String BATTERY_LEVEL = "batteryLevel";
    public static final String LOW_BATTERY = "lowBattery";
    public static final String BATTERY_TEMPERATURE = "batteryTemperature";
    public static final String GARDEN_SIZE = "gardenSize";
    public static final String GARDEN_MAP = "gardenMap";

    // Device properties
    public static final String PROPERTY_BARE_TOOL_NUMBER = "bareToolNumber";
    public static final String PROPERTY_SERVICE_COUNTER = "serviceCounter";
    public static final String PROPERTY_NEEDS_SERVICE = "needsService";
    public static final String PROPERTY_RENEW_DATE = "renewDate";

    // Bosch SingleKey ID OAuth2
    private static final String BSK_BASE_URI = "https://prodindego.b2clogin.com/prodindego.onmicrosoft.com/b2c_1a_signup_signin/oauth2/v2.0/";
    public static final String BSK_CLIENT_ID = "65bb8c9d-1070-4fb4-aa95-853618acc876";
    public static final String BSK_AUTH_URI = BSK_BASE_URI + "authorize";
    public static final String BSK_TOKEN_URI = BSK_BASE_URI + "token";
    public static final String BSK_REDIRECT_URI = "com.bosch.indegoconnect://login";
    public static final String BSK_SCOPE = "openid offline_access https://prodindego.onmicrosoft.com/indego-mobile-api/Indego.Mower.User";
}
