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
package org.openhab.binding.lutron.internal.config;

import org.apache.commons.lang.StringUtils;

/**
 * Configuration settings for an {@link org.openhab.binding.lutron.internal.handler.IPBridgeHandler}.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added reconnect and heartbeat parameters
 */
public class IPBridgeConfig {
    private String ipAddress;
    private String user;
    private String password;
    private String discoveryFile;
    private int reconnect;
    private int heartbeat;

    public boolean sameConnectionParameters(IPBridgeConfig config) {
        return StringUtils.equals(ipAddress, config.ipAddress) && StringUtils.equals(user, config.user)
                && StringUtils.equals(password, config.password) && (reconnect == config.reconnect)
                && (heartbeat == config.heartbeat);
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDiscoveryFile() {
        return discoveryFile;
    }

    public void setDiscoveryFile(String discoveryFile) {
        this.discoveryFile = discoveryFile;
    }

    public int getReconnect() {
        return reconnect;
    }

    public void setReconnect(int reconnect) {
        this.reconnect = reconnect;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }
}
