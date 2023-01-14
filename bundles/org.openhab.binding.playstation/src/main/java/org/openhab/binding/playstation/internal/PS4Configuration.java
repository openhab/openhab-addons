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
package org.openhab.binding.playstation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PS4Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class PS4Configuration {

    /**
     * User-credential for the PS4.
     */
    public String userCredential = "";

    /**
     * pass code for user (4 digits).
     */
    public String passCode = "";

    /**
     * pairing code for this device (8 digits).
     */
    public String pairingCode = "";

    /**
     * Timeout of connection in seconds.
     */
    public int connectionTimeout = 60;

    /**
     * Automatic connection as soon as PS4 is turned on.
     */
    public boolean autoConnect = false;

    /**
     * Size of artwork for applications.
     */
    public int artworkSize = 320;

    /**
     * IP-address of OpenHABs network interface.
     * This should only be used if the PS4 is on a sub-net
     * different from the one configured in OpenHAB.
     */
    public String outboundIP = "";

    /**
     * IP-address of PS4.
     */
    public String ipAddress = "";

    /**
     * IP-port of PS4.
     */
    public int ipPort = PlayStationBindingConstants.DEFAULT_COMMUNICATION_PORT;

    /**
     * host-id of PS4.
     */
    public String hostId = "";

    @Override
    public String toString() {
        return "IP" + ipAddress + ", User-credential" + userCredential + ", Port" + ipPort + ", HostId" + hostId + ".";
    }
}
