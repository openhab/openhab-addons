/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.config;

import java.math.BigDecimal;
import java.util.List;

import org.openhab.binding.denonmarantz.internal.DenonMarantzConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for the Denon Marantz binding.
 *
 * @author Jan-Willem Veldhuis
 *
 */
public class DenonMarantzConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DenonMarantzConfiguration.class);

    /**
     * The hostname (or IP Address) of the Denon Marantz AVR
     */
    public String host;

    /**
     * Whether Telnet communication is enabled
     */
    public boolean telnetEnabled;

    /**
     * The telnet port
     */
    public int telnetPort;

    /**
     * The HTTP port
     */
    public int httpPort;

    /**
     * The interval to poll the AVR over HTTP for changes
     */
    public int httpPollingInterval;

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

    public boolean isTelnet() {
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

    public void setZoneCount(Integer zoneCount) {
        if (zoneCount > 3) {
            logger.info("Current implementation allows up to 3 zones. {} zones detected. Defaulting to 3.", zoneCount);
            zoneCount = 3;
        }
        this.zoneCount = zoneCount;
    }

}
