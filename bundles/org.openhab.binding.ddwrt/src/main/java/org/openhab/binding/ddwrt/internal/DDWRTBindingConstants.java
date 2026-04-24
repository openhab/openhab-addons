/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
    public static final ThingTypeUID THING_TYPE_WIRELESS_CLIENT = new ThingTypeUID(BINDING_ID, "wireless-client");
    public static final ThingTypeUID THING_TYPE_FIREWALL_RULE = new ThingTypeUID(BINDING_ID, "firewall-rule");

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Objects.requireNonNull(
            Set.of(THING_TYPE_DEVICE, THING_TYPE_RADIO, THING_TYPE_WIRELESS_CLIENT, THING_TYPE_FIREWALL_RULE));

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(DISCOVERABLE_THING_TYPES_UIDS);
    static {
        SUPPORTED_THING_TYPES_UIDS.add(BRIDGE_TYPE_NETWORK);
    }

    // List of network (bridge) channel ids
    public static final String CHANNEL_TOTAL_CLIENTS = "total-clients";
    public static final String CHANNEL_WIRELESS_CLIENTS = "wireless-clients";
    public static final String CHANNEL_WIRED_CLIENTS = "wired-clients";

    // List of common channel ids
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_UPTIME = "uptime";

    // List of device channel ids
    public static final String CHANNEL_CPU_LOAD = "cpu-load";
    public static final String CHANNEL_CPU_TEMP = "cpu-temp";
    public static final String CHANNEL_FIRMWARE = "firmware";
    public static final String CHANNEL_WAN_IP = "wan-ip";
    public static final String CHANNEL_WAN_IN = "wan-in";
    public static final String CHANNEL_WAN_OUT = "wan-out";
    public static final String CHANNEL_IF_IN = "if-in";
    public static final String CHANNEL_IF_OUT = "if-out";
    public static final String CHANNEL_REBOOT = "reboot";

    // List of radio channel ids
    public static final String CHANNEL_ENABLED = "enabled";
    public static final String CHANNEL_CHANNEL = "channel";
    public static final String CHANNEL_SSID = "ssid";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_CLIENT_COUNT = "client-count";
    public static final String CHANNEL_ASSOCLIST = "assoc-list";

    // List of wireless client channel ids
    public static final String CHANNEL_MAC_ADDRESS = "mac-address";
    public static final String CHANNEL_HOSTNAME = "hostname";
    public static final String CHANNEL_IP_ADDRESS = "ip-address";
    public static final String CHANNEL_AP = "ap";
    public static final String CHANNEL_AP_MAC = "ap-mac";
    public static final String CHANNEL_SNR = "snr";
    public static final String CHANNEL_RX_RATE = "rx-rate";
    public static final String CHANNEL_TX_RATE = "tx-rate";
    public static final String CHANNEL_LAST_SEEN = "last-seen";

    // List of firewall rule channel ids
    public static final String CHANNEL_DESCRIPTION = "description";

    // List of syslog event channel ids (modeled after logreader binding)
    public static final String CHANNEL_LAST_WARNING_EVENT = "last-warning-event";
    public static final String CHANNEL_LAST_ERROR_EVENT = "last-error-event";
    public static final String CHANNEL_WARNING_EVENTS = "warning-events";
    public static final String CHANNEL_ERROR_EVENTS = "error-events";
    public static final String CHANNEL_LAST_DHCP_EVENT = "last-dhcp-event";
    public static final String CHANNEL_LAST_WIRELESS_EVENT = "last-wireless-event";
    public static final String CHANNEL_WARNING_EVENT = "warning-event";
    public static final String CHANNEL_ERROR_EVENT = "error-event";
    public static final String CHANNEL_DHCP_EVENT = "dhcp-event";
    public static final String CHANNEL_WIRELESS_EVENT = "wireless-event";

    // List of all configuration parameters
    public static final String HOSTNAME = "hostname";
    public static final String HOSTNAMES = "hostnames";
    public static final String PORT = "port";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
}
