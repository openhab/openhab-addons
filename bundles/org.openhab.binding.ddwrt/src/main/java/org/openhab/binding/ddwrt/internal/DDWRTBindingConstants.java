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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_TYPE_NETWORK, THING_TYPE_DEVICE);

    // List of network Channel ids
    public static final String CHANNEL_TOTAL_CLIENTS = "totalClients";
    public static final String CHANNEL_WIRELESS_CLIENTS = "wirelessClients";
    public static final String CHANNEL_WIRED_CLIENTS = "wiredClients";
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_UPTIME = "uptime";
    public static final String CHANNEL_WAN_IP = "wanIp";
    
    // List of all configuration parameters
    public static final String HOSTNAME = "hostname";
    public static final String PORT = "port";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
}
