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
package org.openhab.binding.tapocontrol.internal.devices.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TapoBridgeConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Christian Wild - Initial contribution
 */

@NonNullByDefault
public final class TapoBridgeConfiguration {
    /* THING CONFIGUTATION PROPERTYS */
    public static final String CONFIG_EMAIL = "username";
    public static final String CONFIG_PASS = "password";
    public static final String CONFIG_DISCOVERY_CLOUD = "cloudDiscovery";
    public static final String CONFIG_DISCOVERY_UDP = "udpDiscovery";
    public static final String CONFIG_DISCOVERY_ONLINE = "onlyLocalOnlineDevices";
    public static final String CONFIG_BROADCAST_ADDRESS = "broadcastAddress";
    public static final String CONFIG_DISCOVERY_INTERVAL = "discoveryInterval";

    /* DEFAULT & FIXED CONFIGURATIONS */
    public static final Integer CONFIG_CLOUD_FIXED_INTERVAL = 1440;

    /* thing configuration parameter. */
    public String username = "";
    public String password = "";
    public String broadcastAddress = "255.255.255.255";
    public boolean cloudDiscovery = false;
    public boolean udpDiscovery = false;
    public boolean onlyLocalOnlineDevices = false;
    public int reconnectInterval = CONFIG_CLOUD_FIXED_INTERVAL;
    public int discoveryInterval = 60;
}
