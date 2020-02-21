/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sonyps4.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonyps4.internal.discovery.SonyPS4Discovery;

/**
 * The {@link SonyPS4Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class SonyPS4Configuration {

    /**
     * Constant field used in {@link SonyPS4Discovery} to set the configuration property during discovery.
     * Value of this field needs to match {@link #userCredential}.
     */
    public static final String USER_CREDENTIAL = "userCredential";

    /**
     * Constant field used in config update to see if pairingCode is set.
     * Value of this field needs to match {@link #pairingCode}.
     */
    public static final String PAIRING_CODE = "pairingCode";

    /**
     * Constant field used in {@link SonyPS4Discovery} to set the configuration property during discovery.
     * Value of this field needs to match {@link #ipAddress}.
     */
    public static final String IP_ADDRESS = "ipAddress";

    /**
     * Constant field used in {@link SonyPS4Discovery} to set the configuration property during discovery.
     * Value of this field needs to match {@link #ipPort}.
     */
    public static final String IP_PORT = "ipPort";

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
     * IP-address of PS4.
     */
    public String ipAddress = "";

    /**
     * IP-port of PS4.
     */
    public int ipPort = SonyPS4BindingConstants.DEFAULT_COMMUNICATION_PORT;

    /**
     * host-id of PS4.
     */
    public String hostId = "";

    @Override
    public String toString() {
        return "IP" + ipAddress + ", User-credential" + userCredential + ", Port" + ipPort + ", HostId" + hostId + ".";
    }
}
