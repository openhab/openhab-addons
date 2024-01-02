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
package org.openhab.binding.solarwatt.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolarwattBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class SolarwattBridgeConfiguration {
    private static final int DEFAULT_RESCAN_MINUTES = 5;
    private static final int DEFAULT_REFRESH_SECONDS = 30;

    /**
     * Hostname or ip where the solarwatt energymanager is reachable.
     *
     * Energy manager does not set a default name via DHCP.
     */
    public String hostname = "";

    /**
     * Refresh interval for updating devices data
     */
    public int refresh = DEFAULT_REFRESH_SECONDS;

    /**
     * Refresh interval for reading of devices (not the devices data)
     */
    public int rescan = DEFAULT_RESCAN_MINUTES;
}
