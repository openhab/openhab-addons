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
package org.openhab.binding.unifi.internal;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link UniFiBindingConstants} class defines common constants, which are
 * used across the UniFi binding.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Patrik Wimnell - Blocking / Unblocking client support
 */
public class UniFiBindingConstants {

    public static final String BINDING_ID = "unifi";

    // List of all Thing Types
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_WIRED_CLIENT = new ThingTypeUID(BINDING_ID, "wiredClient");
    public static final ThingTypeUID THING_TYPE_WIRELESS_CLIENT = new ThingTypeUID(BINDING_ID, "wirelessClient");

    // List of common wired + wireless client channels
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_SITE = "site";
    public static final String CHANNEL_MAC_ADDRESS = "macAddress";
    public static final String CHANNEL_IP_ADDRESS = "ipAddress";
    public static final String CHANNEL_UPTIME = "uptime";
    public static final String CHANNEL_LAST_SEEN = "lastSeen";
    public static final String CHANNEL_BLOCKED = "blocked";
    public static final String CHANNEL_RECONNECT = "reconnect";

    // List of additional wired client channels
    // ..coming soon..

    // List of additional wireless client channels
    public static final String CHANNEL_AP = "ap";
    public static final String CHANNEL_ESSID = "essid";
    public static final String CHANNEL_RSSI = "rssi";

    // List of all Parameters
    public static final String PARAMETER_HOST = "host";
    public static final String PARAMETER_PORT = "port";
    public static final String PARAMETER_USERNAME = "username";
    public static final String PARAMETER_PASSWORD = "password";
    public static final String PARAMETER_UNIFIOS = "unifios";
    public static final String PARAMETER_SITE = "site";
    public static final String PARAMETER_CID = "cid";
}
