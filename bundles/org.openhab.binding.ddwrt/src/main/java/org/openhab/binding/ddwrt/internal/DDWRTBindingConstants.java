/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DDWRTBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTBindingConstants {

    private DDWRTBindingConstants() {
    }

    private static final String BINDING_ID = "ddwrt";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_NETWORK = new ThingTypeUID(BINDING_ID, "network");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_RADIO = new ThingTypeUID(BINDING_ID, "radio");
    public static final ThingTypeUID THING_TYPE_WIRELESS_CLIENT = new ThingTypeUID(BINDING_ID, "wirelessClient");
    public static final ThingTypeUID THING_TYPE_FIREWALL_RULE = new ThingTypeUID(BINDING_ID, "firewallRule");

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Objects.requireNonNull(
            Set.of(THING_TYPE_DEVICE, THING_TYPE_RADIO, THING_TYPE_WIRELESS_CLIENT, THING_TYPE_FIREWALL_RULE));

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(DISCOVERABLE_THING_TYPES_UIDS);
    static {
        SUPPORTED_THING_TYPES_UIDS.add(BRIDGE_TYPE_NETWORK);
    }

    // List of network (bridge) channel ids
    public static final String CHANNEL_TOTAL_CLIENTS = "totalClients";
    public static final String CHANNEL_WIRELESS_CLIENTS = "wirelessClients";
    public static final String CHANNEL_WIRED_CLIENTS = "wiredClients";

    // List of common channel ids
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_UPTIME = "uptime";

    // List of device channel ids
    public static final String CHANNEL_CPU_LOAD = "cpuLoad";
    public static final String CHANNEL_CPU_TEMP = "cpuTemp";
    public static final String CHANNEL_WAN_IP = "wanIp";
    public static final String CHANNEL_WAN_IN = "wanIn";
    public static final String CHANNEL_WAN_OUT = "wanOut";
    public static final String CHANNEL_IF_IN = "ifIn";
    public static final String CHANNEL_IF_OUT = "ifOut";
    public static final String CHANNEL_REBOOT = "reboot";

    // List of radio channel ids
    public static final String CHANNEL_ENABLED = "enabled";
    public static final String CHANNEL_CHANNEL = "channel";
    public static final String CHANNEL_SSID = "ssid";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_CLIENT_COUNT = "clientCount";

    // List of wireless client channel ids
    public static final String CHANNEL_MAC_ADDRESS = "macAddress";
    public static final String CHANNEL_HOSTNAME = "hostname";
    public static final String CHANNEL_IP_ADDRESS = "ipAddress";
    public static final String CHANNEL_AP = "ap";
    public static final String CHANNEL_SNR = "snr";
    public static final String CHANNEL_RX_RATE = "rxRate";
    public static final String CHANNEL_TX_RATE = "txRate";
    public static final String CHANNEL_LAST_SEEN = "lastSeen";

    // List of firewall rule channel ids
    public static final String CHANNEL_DESCRIPTION = "description";

    // List of all configuration parameters
    public static final String HOSTNAME = "hostname";
    public static final String HOSTNAMES = "hostnames";
    public static final String PORT = "port";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
}
