/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WizLightingBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sriram Balakrishnan - Initial contribution
 */
public class WizLightingBindingConstants {

    /**
     * The binding id.
     */
    public static final String BINDING_ID = "wizlighting";

    /**
     * List of all Thing Type UIDs.
     */
    public static final ThingTypeUID THING_TYPE_WIZ_BULB = new ThingTypeUID(BINDING_ID, "bulb");

    /**
     * List of all Channel ids
     */
    public static final String BULB_SWITCH_CHANNEL_ID = "switch";
    public static final String BULB_COLOR_CHANNEL_ID = "color";
    public static final String BULB_SCENE_CHANNEL_ID = "scene";
    public static final String BULB_SPEED_CHANNEL_ID = "speed";

    /**
     * The supported thing types.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(THING_TYPE_WIZ_BULB)));

    // -------------- Configuration arguments ----------------
    /**
     * Mac address configuration argument key.
     */
    public static final String MAC_ADDRESS_ARG = "hostMacAddress";

    /**
     * Wifi socket update interval configuration argument key.
     */
    public static final String UPDATE_INTERVAL_ARG = "updateInterval";

    /**
     * Host address configuration argument key.
     */
    public static final String HOST_ADDRESS_ARG = "hostAddress";

    /**
     * Home id configuration argument key.
     */
    public static final String HOME_ID_ARG = "homeId";

    // -------------- Default values ----------------
    /**
     * Default Wifi socket refresh interval.
     */
    public static final long DEFAULT_REFRESH_INTERVAL = 60;

    /**
     * Default Wifi socket default UDP port.
     */
    public static final int BULB_DEFAULT_UDP_PORT = 38899;

    /**
     * Default listener socket default UDP port.
     */
    public static final int LISTENER_DEFAULT_UDP_PORT = 38900;

    /**
     * Discovery timeout in seconds.
     */
    public static final int DISCOVERY_TIMEOUT_SECONDS = 4;

    /**
     * Invite Token (one time use only). Needed to auto discover and can be obtained using ios or android app
     */
    public static final String DISCOVERY_INVITE_TOKEN = "inviteToken";

    /**
     * OAuth token end point URL For obtaining access token. Only needed for auto discovery
     */
    public static final String DISCOVERY_API_OAUTH_URL = "https://api.wiz.world/oauth/v2/token";

    /**
     * URL to obtain HomeId
     */
    public static final String DISCOVERY_API_USER_DETAILS_URL = "https://api.wiz.world/api/v2/user/me";

    /**
     * URL to obtain list of all bulbs once registered
     */
    public static final String DISCOVERY_API_HOME_DETAILS_URL = "https://api.wiz.world/api/v2/home/";

    /**
     * Register Invite code URL
     */
    public static final String DISCOVERY_API_INVITE_TOKEN_URL = "https://api.wiz.world/api/v2/invitation/";

    /**
     * Basic Authentication Token for the wiz.world API (probably from the iOS or Android app)
     */
    public static final String DISCOVERY_API_AUTHENTICATION_TOKEN = "Basic Ml81ZHE1cHFzNnI0b3dvNDAwb284MGNvd2d3azA0Y2drb2t3azB3bzgwa3drb2M4d2NjczpNV0ppWTJFeU1Ea3dNR0V3TVdJM1pqTTFOREppWm1RMU1qTm1NV05qTVRVNE9UZzFOag==";

    /**
     * File name for the token temporary storage
     */
    public static final String INVITE_TOKEN_TEMP_FILE = "wizlighting.token";

}
