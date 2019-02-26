/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;

/**
 * Configuration for the {@link HueBridgeHandler}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HueBridgeConfig {
    private @NonNullByDefault({}) String ipAddress;
    private @Nullable String userName;
    private int pollingInterval = 10;
    private int sensorPollingInterval = 500;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public @Nullable String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public int getSensorPollingInterval() {
        return sensorPollingInterval;
    }

    public void setSensorPollingInterval(int sensorPollingInterval) {
        this.sensorPollingInterval = sensorPollingInterval;
    }
}
