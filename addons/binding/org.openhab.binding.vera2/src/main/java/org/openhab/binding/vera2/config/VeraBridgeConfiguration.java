/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera2.config;

import static org.openhab.binding.vera2.VeraBindingConstants.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The {@link VeraBridgeConfiguration} class defines the model for a bridge configuration.
 *
 * @author Dmitriy Ponomarev
 */
public class VeraBridgeConfiguration {
    private String veraControllerIpAddress;
    private Integer veraControllerPort;
    private String veraControllerProtocol;

    private String veraControllerUsername;
    private String veraControllerPassword;

    private Integer pollingInterval;

    public String getVeraIpAddress() {
        return veraControllerIpAddress;
    }

    public void setVeraIpAddress(String ipAddress) {
        this.veraControllerIpAddress = ipAddress;
    }

    public Integer getVeraPort() {
        return veraControllerPort;
    }

    public void setVeraPort(Integer port) {
        this.veraControllerPort = port;
    }

    public String getVeraProtocol() {
        return veraControllerProtocol;
    }

    public void setVeraProtocol(String protocol) {
        this.veraControllerProtocol = protocol;
    }

    public String getVeraUsername() {
        return veraControllerUsername;
    }

    public void setVeraUsername(String username) {
        this.veraControllerUsername = username;
    }

    public String getVeraPassword() {
        return veraControllerPassword;
    }

    public void setVeraPassword(String password) {
        this.veraControllerPassword = password;
    }

    public Integer getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(BRIDGE_CONFIG_vera_SERVER_IP_ADDRESS, this.getVeraIpAddress())
                .append(BRIDGE_CONFIG_vera_SERVER_PORT, this.getVeraPort())
                .append(BRIDGE_CONFIG_POLLING_INTERVAL, this.getPollingInterval()).toString();
    }
}
