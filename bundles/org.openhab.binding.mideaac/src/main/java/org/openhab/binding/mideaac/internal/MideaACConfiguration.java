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

    public String ipAddress = "";

    public String ipPort = "6444";

    public String deviceId = "";

    public String email = "";

    public String password = "";

    public String cloud = "";

    public String token = "";

    public String key = "";

    public int pollingTime = 60;

    public int timeout = 4;

    public boolean promptTone;

    public String version = "";

    /**
     * Check during initialization that the params are valid
     * 
     * @return true(valid), false (not valid)
     */
    public boolean isValid() {
        return !("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort.isBlank() || ipAddress.isBlank());
    }

    /**
     * Check during initialization if discovery is needed
     * 
     * @return true(discovery needed), false (not needed)
     */
    public boolean isDiscoveryNeeded() {
        return ("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort.isBlank() || ipAddress.isBlank()
                || !Utils.validateIP(ipAddress));
    }
}
