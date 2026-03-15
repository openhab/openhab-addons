/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MideaACConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - OH addons changes, modified checks and defaults
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
    public String email = "nethome+us@mailinator.com";

    /**
     * Cloud Account Password
     */
    public String password = "password1";

    /**
     * Cloud Provider
     */
    public String cloud = "NetHome Plus";

    /**
     * Token 128 hex length
     */
    public String token = "";

    /**
     * Key 64 hex length
     */
    public String key = "";

    /**
     * Poll Frequency - seconds
     */
    public int pollingTime = 60;

    /**
     * Energy Update Frequency while running
     * (if supported) in minutes
     */
    public int energyPoll = 0;

    /**
     * Key and Token Update Frequency in hours
     * 0 to disable. Minimum 24 hours best practice if used
     */
    public int keyTokenUpdate = 0;

    /**
     * Socket Timeout in seconds
     */
    public int timeout = 4;

    /**
     * Prompt tone from indoor unit with a Set Command
     */
    public boolean promptTone = false;

    /**
     * AC Version
     */
    public int version = 3;

    /**
     * Choose between Energy Decoding methods
     * true = BCD, false = binary
     */
    public boolean energyDecode = true;

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
     * Check during initialization if discovery is possible. This needs a valid IP
     * 
     * @return true(discovery needed), false (not needed)
     */
    public boolean isDiscoveryPossible() {
        return Utils.validateIP(ipAddress);
    }

    /**
     * Check during initialization if key and token can be obtained
     * from the cloud.
     * 
     * @return true (yes they can), false (they cannot)
     */
    public boolean isTokenKeyObtainable() {
        return !email.isBlank() && !password.isBlank() && !cloud.isBlank();
    }

    /**
     * Check during initialization if cloud, key and token are true for v3
     * 
     * @return true (Valid, all items are present) false (key, token and/or provider missing)
     */
    public boolean isV3ConfigValid() {
        return isHexString(key, 64) && isHexString(token, 128) && !cloud.isBlank();
    }

    private boolean isHexString(String str, int length) {
        return str.length() == length && Pattern.matches("[0-9a-fA-F]{" + length + "}", str);
    }
}
