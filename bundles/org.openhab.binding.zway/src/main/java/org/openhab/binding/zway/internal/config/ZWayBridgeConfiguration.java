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
package org.openhab.binding.zway.internal.config;

import static org.openhab.binding.zway.internal.ZWayBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ZWayBridgeConfiguration} class defines the model for a bridge configuration.
 *
 * @author Patrick Hecker - Initial contribution, remove openHAB configuration
 */
@NonNullByDefault
public class ZWayBridgeConfiguration {
    private String zwayServerIpAddress = "localhost";
    private Integer zwayServerPort = 8083;
    private String zwayServerProtocol = "http";

    private String zwayServerUsername = "admin";
    private @Nullable String zwayServerPassword;

    private Integer pollingInterval = 3600;

    public String getZWayIpAddress() {
        return zwayServerIpAddress;
    }

    public void setZWayIpAddress(String ipAddress) {
        this.zwayServerIpAddress = ipAddress;
    }

    public Integer getZWayPort() {
        return zwayServerPort;
    }

    public void setZWayPort(Integer port) {
        this.zwayServerPort = port;
    }

    public String getZWayProtocol() {
        return zwayServerProtocol;
    }

    public void setZWayProtocol(String protocol) {
        this.zwayServerProtocol = protocol;
    }

    public String getZWayUsername() {
        return zwayServerUsername;
    }

    public void setZWayUsername(String username) {
        this.zwayServerUsername = username;
    }

    public @Nullable String getZWayPassword() {
        return zwayServerPassword;
    }

    public void setZWayPassword(String password) {
        this.zwayServerPassword = password;
    }

    public Integer getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ " + BRIDGE_CONFIG_ZWAY_SERVER_IP_ADDRESS + "=" + getZWayIpAddress()
                + ", " + BRIDGE_CONFIG_ZWAY_SERVER_PORT + "=" + getZWayPort() + ", "
                + BRIDGE_CONFIG_ZWAY_SERVER_PROTOCOL + "=" + getZWayProtocol() + ", "
                + BRIDGE_CONFIG_ZWAY_SERVER_USERNAME + "=" + getZWayUsername() + ", "
                + BRIDGE_CONFIG_ZWAY_SERVER_PASSWORD + "=" + getZWayPassword() + ", " + BRIDGE_CONFIG_POLLING_INTERVAL
                + "=" + this.getPollingInterval() + "}";
    }
}
