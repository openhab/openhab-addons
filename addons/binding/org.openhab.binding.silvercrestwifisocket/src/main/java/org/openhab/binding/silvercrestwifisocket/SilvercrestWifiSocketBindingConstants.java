/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.silvercrestwifisocket;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SilvercrestWifiSocketBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jaime Vaz - Initial contribution
 */
public class SilvercrestWifiSocketBindingConstants {

    /**
     * The binding id.
     */
    public static final String BINDING_ID = "silvercrestwifisocket";

    /**
     * List of all Thing Type UIDs.
     */
    public final static ThingTypeUID THING_TYPE_WIFI_SOCKET = new ThingTypeUID(BINDING_ID, "wifiSocket");

    /**
     * List of all Channel ids
     */
    public final static String WIFI_SOCKET_CHANNEL_ID = "switch";

    /**
     * The supported thing types.
     */
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_WIFI_SOCKET);

    // -------------- Configuration arguments ----------------
    /**
     * Mac address configuration argument key.
     */
    public final static String MAC_ADDRESS_ARG = "macAddress";
    /**
     * Wifi socket update interval configuration argument key.
     */
    public final static String UPDATE_INTERVAL_ARG = "updateInterval";
    /**
     * Host address configuration argument key.
     */
    public final static String HOST_ADDRESS_ARG = "hostAddress";

    // -------------- Default values ----------------
    /**
     * Default Wifi socket refresh interval.
     */
    public static final long DEFAULT_REFRESH_INTERVAL = 60;

    /**
     * Default Wifi socket default UDP port.
     */
    public static final int WIFI_SOCKET_DEFAULT_UDP_PORT = 8530;

    /**
     * Discovery timeout in seconds.
     */
    public static final int DISCOVERY_TIMEOUT_SECONDS = 4;
}
