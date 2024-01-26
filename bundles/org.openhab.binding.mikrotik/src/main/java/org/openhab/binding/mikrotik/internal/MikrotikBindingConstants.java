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
package org.openhab.binding.mikrotik.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MikrotikBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class MikrotikBindingConstants {

    private static final String BINDING_ID = "mikrotik";

    public static final String PROPERTY_MODEL = "modelId";
    public static final String PROPERTY_FIRMWARE = "firmware";
    public static final String PROPERTY_SERIAL_NUMBER = "serial";

    // List of all Thing Types
    public static final ThingTypeUID THING_TYPE_ROUTEROS = new ThingTypeUID(BINDING_ID, "routeros");
    public static final ThingTypeUID THING_TYPE_INTERFACE = new ThingTypeUID(BINDING_ID, "interface");
    public static final ThingTypeUID THING_TYPE_WIRELESS_CLIENT = new ThingTypeUID(BINDING_ID, "wifiRegistration");

    // RouterOS system stats
    public static final String CHANNEL_FREE_SPACE = "freeSpace";
    public static final String CHANNEL_TOTAL_SPACE = "totalSpace";
    public static final String CHANNEL_USED_SPACE = "usedSpace";
    public static final String CHANNEL_FREE_MEM = "freeMemory";
    public static final String CHANNEL_TOTAL_MEM = "totalMemory";
    public static final String CHANNEL_USED_MEM = "usedMemory";
    public static final String CHANNEL_CPU_LOAD = "cpuLoad";

    public static final String CHANNEL_COMMENT = "comment";

    // List of common interface channels
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_TYPE = "type";
    public static final String CHANNEL_MAC = "macAddress";
    public static final String CHANNEL_ENABLED = "enabled";
    public static final String CHANNEL_CONNECTED = "connected"; // used for wifi client as well
    public static final String CHANNEL_LAST_LINK_DOWN_TIME = "lastLinkDownTime";
    public static final String CHANNEL_LAST_LINK_UP_TIME = "lastLinkUpTime";
    public static final String CHANNEL_LINK_DOWNS = "linkDowns";
    public static final String CHANNEL_TX_DATA_RATE = "txRate";
    public static final String CHANNEL_RX_DATA_RATE = "rxRate";
    public static final String CHANNEL_TX_PACKET_RATE = "txPacketRate";
    public static final String CHANNEL_RX_PACKET_RATE = "rxPacketRate";
    public static final String CHANNEL_TX_BYTES = "txBytes";
    public static final String CHANNEL_RX_BYTES = "rxBytes";
    public static final String CHANNEL_TX_PACKETS = "txPackets";
    public static final String CHANNEL_RX_PACKETS = "rxPackets";
    public static final String CHANNEL_TX_DROPS = "txDrops";
    public static final String CHANNEL_RX_DROPS = "rxDrops";
    public static final String CHANNEL_TX_ERRORS = "txErrors";
    public static final String CHANNEL_RX_ERRORS = "rxErrors";

    // Ethernet interface channel list
    public static final String CHANNEL_DEFAULT_NAME = "defaultName";
    public static final String CHANNEL_RATE = "rate";

    // CAPsMAN interface channel list
    public static final String CHANNEL_INTERFACE = "interface";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_REGISTERED_CLIENTS = "registeredClients";
    public static final String CHANNEL_AUTHORIZED_CLIENTS = "authorizedClients";
    public static final String CHANNEL_CONTINUOUS = "continuous";

    // PPP interface shared channel list
    public static final String CHANNEL_UP_SINCE = "upSince";

    // Wireless client channels
    public static final String CHANNEL_LAST_SEEN = "lastSeen";
    public static final String CHANNEL_SSID = "ssid";
    public static final String CHANNEL_SIGNAL = "signal";

    // List of common wired + wireless client channels
    public static final String CHANNEL_SITE = "site";
    public static final String CHANNEL_IP_ADDRESS = "ipAddress";
    public static final String CHANNEL_BLOCKED = "blocked";
    public static final String CHANNEL_RECONNECT = "reconnect";
}
