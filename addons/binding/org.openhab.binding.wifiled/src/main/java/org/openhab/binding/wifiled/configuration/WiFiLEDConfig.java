/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.configuration;

import org.openhab.binding.wifiled.handler.WiFiLEDDriver.Protocol;

/**
 * The {@link WiFiLEDConfig} class holds the configuration properties of the thing.
 *
 * @author Osman Basha - Initial contribution
 * @author Patrick Hofmann - change type of protocol String -> Protocol
 */
public class WiFiLEDConfig {

    private String ip;
    private Integer port;
    private Integer pollingPeriod;
    private Protocol protocol;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPollingPeriod() {
        return pollingPeriod;
    }

    public void setPollingPeriod(Integer pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

}
