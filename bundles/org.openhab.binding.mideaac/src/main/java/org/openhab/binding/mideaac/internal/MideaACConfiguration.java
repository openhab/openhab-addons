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
package org.openhab.binding.mideaac.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MideaACConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - OH addons changes
 */
@NonNullByDefault
public class MideaACConfiguration {

    /**
     * IP Address
     */
    public String ipAddress = "";

    /**
     * IP Port
     */
    public int ipPort = 6444;

    /**
     * Device ID
     */
    public String deviceId = "0";

    /**
     * Cloud Account email
     */
    public String email = "";

    /**
     * Cloud Account Password
     */
    public String password = "";

    /**
     * Cloud Provider
     */
    public String cloud = "";

    /**
     * Token
     */
    public String token = "";

    /**
     * Key
     */
    public String key = "";

    /**
     * Poll Frequency
     */
    public int pollingTime = 60;

    /**
     * Socket Timeout
     */
    public int timeout = 4;

    /**
     * Prompt tone from indoor unit with a Set Command
     */
    public boolean promptTone = false;

    /**
     * AC Version
     */
    public int version = 0;

    /**
     * Check during initialization that the params are valid
     * 
     * @return true(valid), false (not valid)
     */
    public boolean isValid() {
        return !("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort <= 0 || ipAddress.isBlank()
                || version <= 1);
    }

    /**
     * Check during initialization if discovery is needed
     * 
     * @return true(discovery needed), false (not needed)
     */
    public boolean isDiscoveryNeeded() {
        return ("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort <= 0 || ipAddress.isBlank()
                || !Utils.validateIP(ipAddress) || version <= 1);
    }

    /**
     * Check during initialization if key and token can be obtained
     * from the cloud.
     * 
     * @return true (yes they can), false (they cannot)
     */
    public boolean isTokenKeyObtainable() {
        return (!email.isBlank() && !password.isBlank() && !"".equals(cloud));
    }

    /**
     * Check during initialization if cloud, key and token are true for v3
     * 
     * @return true (Valid, all items are present) false (key, token and/or provider missing)
     */
    public boolean isV3ConfigValid() {
        return (!key.isBlank() && !token.isBlank() && !"".equals(cloud));
    }
}
