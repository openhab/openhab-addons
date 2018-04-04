/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zway.internal.config;

import static org.openhab.binding.zway.ZWayBindingConstants.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The {@link ZWayBridgeConfiguration} class defines the model for a bridge configuration.
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayBridgeConfiguration {
    private String openHABAlias;

    private String openHABIpAddress;
    private Integer openHABPort;
    private String openHABProtocol;

    private String zwayServerIpAddress;
    private Integer zwayServerPort;
    private String zwayServerProtocol;

    private String zwayServerUsername;
    private String zwayServerPassword;

    private Integer pollingInterval;
    private Boolean observerMechanismEnabled;

    public String getOpenHabAlias() {
        return openHABAlias;
    }

    public void setOpenHabAlias(String openHabAlias) {
        this.openHABAlias = openHabAlias;
    }

    public String getOpenHabIpAddress() {
        return openHABIpAddress;
    }

    public void setOpenHabIpAddress(String ipAddress) {
        this.openHABIpAddress = ipAddress;
    }

    public Integer getOpenHabPort() {
        return openHABPort;
    }

    public void setOpenHabPort(Integer port) {
        this.openHABPort = port;
    }

    public String getOpenHabProtocol() {
        return openHABProtocol;
    }

    public void setOpenHabProtocol(String protocol) {
        this.openHABProtocol = protocol;
    }

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

    public String getZWayPassword() {
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

    public Boolean getObserverMechanismEnabled() {
        return observerMechanismEnabled;
    }

    public void setObserverMechanismEnabled(Boolean observerMechanismEnabled) {
        this.observerMechanismEnabled = observerMechanismEnabled;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(BRIDGE_CONFIG_OPENHAB_ALIAS, this.getOpenHabAlias())
                .append(BRIDGE_CONFIG_OPENHAB_IP_ADDRESS, this.getOpenHabIpAddress())
                .append(BRIDGE_CONFIG_OPENHAB_PORT, this.getOpenHabPort())
                .append(BRIDGE_CONFIG_OPENHAB_PROTOCOL, this.getOpenHabProtocol())
                .append(BRIDGE_CONFIG_ZWAY_SERVER_IP_ADDRESS, this.getZWayIpAddress())
                .append(BRIDGE_CONFIG_ZWAY_SERVER_PORT, this.getZWayPort())
                .append(BRIDGE_CONFIG_ZWAY_SERVER_PROTOCOL, this.getZWayProtocol())
                .append(BRIDGE_CONFIG_ZWAY_SERVER_USERNAME, this.getZWayUsername())
                .append(BRIDGE_CONFIG_ZWAY_SERVER_PASSWORD, this.getZWayPassword())
                .append(BRIDGE_CONFIG_POLLING_INTERVAL, this.getPollingInterval())
                .append(BRIDGE_CONFIG_OBSERVER_MECHANISM_ENABLED, this.getObserverMechanismEnabled()).toString();
    }
}
