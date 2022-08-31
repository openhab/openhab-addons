/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EmbyBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyBridgeConfiguration {

    public String api = "";
    private String ipAddress = "";
    private int port = 0;
    private int httpPort = 0;
    private int refreshInterval = 0;
    private int notificationTimeout = 0;
    private int notificationVolume = 0;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Integer refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public int getNotificationTimeout() {
        return notificationTimeout;
    }

    public void setNotificationTimeout(Integer notificationTimeout) {
        this.notificationTimeout = notificationTimeout;
    }

    public int getNotificationVolume() {
        return notificationVolume;
    }

    public void setNotificationVolume(Integer notificationVolume) {
        this.notificationVolume = notificationVolume;
    }
}
