/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.silvercrestwifisocket.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.silvercrestwifisocket.internal.enums.SilvercrestWifiSocketVendor;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SilvercrestWifiSocketBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jaime Vaz - Initial contribution
 * @author Christian Heimerl - for integration of EasyHome
 */
@NonNullByDefault
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
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_WIFI_SOCKET);

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
