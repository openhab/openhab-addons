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
package org.openhab.binding.denonmarantz.internal.config;

import java.math.BigDecimal;
import java.util.List;

import org.openhab.binding.denonmarantz.internal.connector.DenonMarantzConnector;

/**
 * Configuration class for the Denon Marantz binding.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 *
 */
public class DenonMarantzConfiguration {

    /**
     * The hostname (or IP Address) of the Denon Marantz AVR
     */
    public String host;

    /**
     * Whether Telnet communication is enabled
     */
    public Boolean telnetEnabled;

    /**
     * The telnet port
     */
    public Integer telnetPort;

    /**
     * The HTTP port
     */
    public Integer httpPort;

    /**
     * The interval to poll the AVR over HTTP for changes
     */
    public Integer httpPollingInterval;

    // Default maximum volume
    public static final BigDecimal MAX_VOLUME = new BigDecimal("98");

    private DenonMarantzConnector connector;

    private Integer zoneCount;

    private BigDecimal mainVolumeMax = MAX_VOLUME;

    public List<String> inputOptions;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Boolean isTelnet() {
        return telnetEnabled;
    }

    public void setTelnet(boolean telnet) {
        this.telnetEnabled = telnet;
    }

    public Integer getTelnetPort() {
        return telnetPort;
    }

    public void setTelnetPort(Integer telnetPort) {
        this.telnetPort = telnetPort;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public DenonMarantzConnector getConnector() {
        return connector;
    }

    public void setConnector(DenonMarantzConnector connector) {
        this.connector = connector;
    }

    public BigDecimal getMainVolumeMax() {
        return mainVolumeMax;
    }

    public void setMainVolumeMax(BigDecimal mainVolumeMax) {
        this.mainVolumeMax = mainVolumeMax;
    }

    public Integer getZoneCount() {
        return zoneCount;
    }

    public void setZoneCount(Integer count) {
        Integer zoneCount = count;
        this.zoneCount = zoneCount;
    }
}
