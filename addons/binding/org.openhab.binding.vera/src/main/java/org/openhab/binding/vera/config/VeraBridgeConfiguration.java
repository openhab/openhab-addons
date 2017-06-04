/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera.config;

import static org.openhab.binding.vera.VeraBindingConstants.*;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The {@link VeraBridgeConfiguration} class defines the model for a bridge configuration.
 *
 * @author Dmitriy Ponomarev
 */
public class VeraBridgeConfiguration {
    private String veraIpAddress;
    private Integer veraPort;
    private Integer pollingInterval;
    private Boolean clearNames;
    private String defaulRoomName;
    private Boolean homekitIntegration;

    public String getVeraIpAddress() {
        return veraIpAddress;
    }

    public void setVeraIpAddress(String ipAddress) {
        this.veraIpAddress = ipAddress;
    }

    public Integer getVeraPort() {
        return veraPort;
    }

    public void setVeraPort(Integer port) {
        this.veraPort = port;
    }

    public Integer getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public Boolean getClearNames() {
        return clearNames;
    }

    public void setClearNames(Boolean clearNames) {
        this.clearNames = clearNames;
    }

    public String getDefaulRoomName() {
        return defaulRoomName;
    }

    public void setDefaulRoomName(String defaulRoomName) {
        this.defaulRoomName = defaulRoomName;
    }

    public Boolean getHomekitIntegration() {
        return homekitIntegration;
    }

    public void setHomekitIntegration(Boolean homekitIntegration) {
        this.homekitIntegration = homekitIntegration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(BRIDGE_CONFIG_VERA_SERVER_IP_ADDRESS, this.getVeraIpAddress())
                .append(BRIDGE_CONFIG_VERA_SERVER_PORT, this.getVeraPort())
                .append(BRIDGE_CONFIG_POLLING_INTERVAL, this.getPollingInterval())
                .append(BRIDGE_CONFIG_CLEAR_NAMES, this.getClearNames())
                .append(BRIDGE_CONFIG_DEFAULT_ROOM_NAME, this.getDefaulRoomName())
                .append(BRIDGE_CONFIG_HOMEKIT_INTEGRATION, this.getHomekitIntegration()).toString();
    }
}
