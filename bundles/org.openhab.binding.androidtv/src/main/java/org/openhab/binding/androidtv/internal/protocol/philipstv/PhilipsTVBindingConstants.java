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
package org.openhab.binding.androidtv.internal.protocol.philipstv;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PhilipsTVBindingConstants} class defines common constants, which are used across the
 * whole binding.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class PhilipsTVBindingConstants {

    // Config Parameters
    public static final String HOST = "host";

    public static final String PORT = "philipstvPort";

    public static final String MAC_ADDRESS = "macAddress";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String HTTPS = "https";

    // Connection specific values
    static final int CONNECT_TIMEOUT_MILLISECONDS = 3 * 1000;

    static final int SOCKET_TIMEOUT_MILLISECONDS = 1000;

    static final int MAX_REQUEST_RETRIES = 3;

    // Default port for jointspace v6
    public static final int DEFAULT_PORT = 1926;

    // Powerstates
    public static final String POWER_ON = "On";

    public static final String POWER_OFF = "Off";

    public static final String STANDBY = "Standby";

    public static final String STANDBYKEEP = "StandbyKeep";

    public static final String STANDBY_MSG = "online.standby";

    public static final String EMPTY = "";

    // REST Paths
    public static final String SLASH = "/";

    private static final String API_VERSION = "6";

    public static final String BASE_PATH = SLASH + API_VERSION + SLASH;

    public static final String VOLUME_PATH = BASE_PATH + "audio" + SLASH + "volume";

    public static final String KEY_CODE_PATH = BASE_PATH + "input" + SLASH + "key";

    public static final String TV_POWERSTATE_PATH = BASE_PATH + "powerstate";

    public static final String GET_AVAILABLE_APP_LIST_PATH = BASE_PATH + "applications";

    public static final String GET_NETWORK_DEVICES_PATH = BASE_PATH + "network" + SLASH + "devices";

    private static final String ACTIVITIES_BASE_PATH = BASE_PATH + "activities" + SLASH;

    public static final String GET_AVAILABLE_TV_CHANNEL_LIST_PATH = BASE_PATH + "channeldb" + SLASH + "tv" + SLASH
            + "channelLists" + SLASH + "all";

    public static final String TV_CHANNEL_PATH = ACTIVITIES_BASE_PATH + "tv";
    public static final String GET_CURRENT_APP_PATH = ACTIVITIES_BASE_PATH + "current";
    public static final String LAUNCH_APP_PATH = ACTIVITIES_BASE_PATH + "launch";

    private static final String AMBILIGHT_BASE_PATH = BASE_PATH + "ambilight" + SLASH;
    public static final String AMBILIGHT_POWERSTATE_PATH = AMBILIGHT_BASE_PATH + "power";
    public static final String AMBILIGHT_CONFIG_PATH = AMBILIGHT_BASE_PATH + "currentconfiguration";
    public static final String AMBILIGHT_MODE_PATH = AMBILIGHT_BASE_PATH + "mode";
    public static final String AMBILIGHT_CACHED_PATH = AMBILIGHT_BASE_PATH + "cached";
    public static final String AMBILIGHT_TOPOLOGY_PATH = AMBILIGHT_BASE_PATH + "topology";
    public static final String AMBILIGHT_LOUNGE_PATH = AMBILIGHT_BASE_PATH + "lounge";

    private static final String SETTINGS_BASE_PATH = BASE_PATH + "menuitems" + SLASH + "settings" + SLASH;
    public static final String UPDATE_SETTINGS_PATH = SETTINGS_BASE_PATH + "update";
    public static final String CURRENT_SETTINGS_PATH = SETTINGS_BASE_PATH + "current";
    public static final String STRUCTURE_SETTINGS_PATH = SETTINGS_BASE_PATH + "structure";

    // Logging messages
    public static final String TV_OFFLINE_MSG = "offline.tv-is-not-reachable-and-should-therefore-be-off";
    public static final String TV_NOT_LISTENING_MSG = "offline.tv-does-not-accept-commands-at-the-moment";
}
