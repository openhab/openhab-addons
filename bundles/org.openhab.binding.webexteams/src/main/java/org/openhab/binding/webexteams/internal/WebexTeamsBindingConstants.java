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
package org.openhab.binding.webexteams.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WebexTeamsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tom Deckers - Initial contribution
 */
@NonNullByDefault
public class WebexTeamsBindingConstants {

    private static final String BINDING_ID = "webexteams";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");

    // List of all Channel ids
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_LASTACTIVITY = "lastactivity";

    // List of properties
    public static final String PROPERTY_WEBEX_NAME = "name";
    public static final String PROPERTY_WEBEX_TYPE = "type";

    // OAuth constants
    public static final String OAUTH_REDIRECT_URL = "https://files.ducbase.com/authcode/index.html";
    public static final String OAUTH_TOKEN_URL = "https://webexapis.com/v1/access_token";
    public static final String OAUTH_AUTH_URL = "https://webexapis.com/v1/authorize";
    public static final String OAUTH_AUTHORIZATION_URL = "https://webexapis.com/v1/authorize";
    public static final String OAUTH_SCOPE = "spark:all";
    public static final String WEBEX_ALIAS = "/connectwebex";
    public static final String WEBEX_RES_ALIAS = "/res";

    public static final String WEBEX_API_ENDPOINT = "https://webexapis.com/v1";

    // other
    public static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
}
