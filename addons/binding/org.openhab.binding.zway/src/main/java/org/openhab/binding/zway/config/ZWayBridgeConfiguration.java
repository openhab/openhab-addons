/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zway.config;

import static org.openhab.binding.zway.ZWayBindingConstants.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The {@link ZWayBridgeConfiguration} class defines the model for a bridge configuration.
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayBridgeConfiguration {
    private String mOpenHabAlias;

    private String mOpenHabIpAddress;
    private Integer mOpenHabPort;
    private String mOpenHabProtocol;

    private String mZWayIpAddress;
    private Integer mZWayPort;
    private String mZWayProtocol;

    private String mZWayUsername;
    private String mZWayPassword;

    private Integer mPollingInterval;
    private Boolean mObserverMechanismEnabled;

    public String getOpenHabAlias() {
        return mOpenHabAlias;
    }

    public void setOpenHabAlias(String openHabAlias) {
        this.mOpenHabAlias = openHabAlias;
    }

    public String getOpenHabIpAddress() {
        return mOpenHabIpAddress;
    }

    public void setOpenHabIpAddress(String ipAddress) {
        this.mOpenHabIpAddress = ipAddress;
    }

    public Integer getOpenHabPort() {
        return mOpenHabPort;
    }

    public void setOpenHabPort(Integer port) {
        this.mOpenHabPort = port;
    }

    public String getOpenHabProtocol() {
        return mOpenHabProtocol;
    }

    public void setOpenHabProtocol(String protocol) {
        this.mOpenHabProtocol = protocol;
    }

    public String getZWayIpAddress() {
        return mZWayIpAddress;
    }

    public void setZWayIpAddress(String ipAddress) {
        this.mZWayIpAddress = ipAddress;
    }

    public Integer getZWayPort() {
        return mZWayPort;
    }

    public void setZWayPort(Integer port) {
        this.mZWayPort = port;
    }

    public String getZWayProtocol() {
        return mZWayProtocol;
    }

    public void setZWayProtocol(String protocol) {
        this.mZWayProtocol = protocol;
    }

    public String getZWayUsername() {
        return mZWayUsername;
    }

    public void setZWayUsername(String username) {
        this.mZWayUsername = username;
    }

    public String getZWayPassword() {
        return mZWayPassword;
    }

    public void setZWayPassword(String password) {
        this.mZWayPassword = password;
    }

    public Integer getPollingInterval() {
        return mPollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval) {
        this.mPollingInterval = pollingInterval;
    }

    public Boolean getObserverMechanismEnabled() {
        return mObserverMechanismEnabled;
    }

    public void setObserverMechanismEnabled(Boolean observerMechanismEnabled) {
        this.mObserverMechanismEnabled = observerMechanismEnabled;
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
