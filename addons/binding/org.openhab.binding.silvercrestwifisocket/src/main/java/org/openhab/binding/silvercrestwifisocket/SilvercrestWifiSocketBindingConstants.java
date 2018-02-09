/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.silvercrestwifisocket;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketVendor;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SilvercrestWifiSocketBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jaime Vaz - Initial contribution
 * @author Christian Heimerl - for integration of EasyHome
 */
public class SilvercrestWifiSocketBindingConstants {

    /**
     * The binding id.
     */
    public static final String BINDING_ID = "silvercrestwifisocket";

    /**
     * List of all Thing Type UIDs.
     */
    public static final ThingTypeUID THING_TYPE_WIFI_SOCKET = new ThingTypeUID(BINDING_ID, "wifiSocket");

    /**
     * List of all Channel ids
     */
    public static final String WIFI_SOCKET_CHANNEL_ID = "switch";

    /**
     * The supported thing types.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_WIFI_SOCKET);

    // -------------- Configuration arguments ----------------
    /**
     * Mac address configuration argument key.
     */
    public static final String MAC_ADDRESS_ARG = "macAddress";
    /**
     * Wifi socket update interval configuration argument key.
     */
    public static final String UPDATE_INTERVAL_ARG = "updateInterval";
    /**
     * Host address configuration argument key.
     */
    public static final String HOST_ADDRESS_ARG = "hostAddress";
    /**
     * Host address configuration argument key.
     */
    public static final String VENDOR_ARG = "vendor";

    // -------------- Default values ----------------
    /**
     * Default Wifi socket refresh interval.
     */
    public static final long DEFAULT_REFRESH_INTERVAL = 60;

    /**
     * Default Wifi socket vendor.
     */
    public static final SilvercrestWifiSocketVendor DEFAULT_VENDOR = SilvercrestWifiSocketVendor.LIDL_SILVERCREST;

    /**
     * Default Wifi socket default UDP port.
     */
    public static final int WIFI_SOCKET_DEFAULT_UDP_PORT = 8530;

    /**
     * Discovery timeout in seconds.
     */
    public static final int DISCOVERY_TIMEOUT_SECONDS = 4;
}
