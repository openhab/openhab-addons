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
package org.openhab.binding.mecmeter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MecMeterDeviceConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Florian Pazour - Initial contribution
 * @author Klaus Berger - Initial contribution
 */
@NonNullByDefault
public class MecMeterDeviceConfiguration {
    public String ip = "";
    public String password = "12345";
    public int refreshInterval = 5;

    public String getIp() {
        return ip;
    }

    public void setIp(String inetaddress) {
        ip = inetaddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pw) {
        password = pw;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int ri) {
        refreshInterval = ri;
    }

    public @Nullable String isValid() {
        if (ip.isBlank()) {
            return "Missing IP";
        }
        if (password.isBlank()) {
            return "Password is missing";
        }
        return null;
    }
}
