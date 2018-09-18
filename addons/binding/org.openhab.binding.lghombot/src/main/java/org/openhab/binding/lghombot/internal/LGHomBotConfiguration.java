/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lghombot.internal;

import org.openhab.binding.lghombot.internal.discovery.LGHomBotDiscovery;

/**
 * The {@link LGHomBotConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
public class LGHomBotConfiguration {

    /**
     * Constant field used in {@link LGHomBotDiscovery} to set the configuration property during discovery. Value of
     * this field needs to match {@link #ipAddress}
     */
    public static final String IP_ADDRESS = "ipAddress";

    /**
     * IP Address (or host name) of HomBot
     */
    private String ipAddress;

    /**
     * Port used by the HomBot
     */
    private Integer port;

    /**
     * Polling time (in seconds) to refresh state from the HomBot itself.
     */
    private Integer pollingPeriod;

    /**
     * Gets the IP address or host name of the HomBot
     *
     * @return the IP address or host name of the HomBot
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the IP address or host name of the HomBot
     *
     * @param ipAddress the IP Address or host name of the HomBot
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Gets the port used by the HomBot
     *
     * @return the port used by the HomBot
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the port used by the HomBot
     *
     * @param port the port used by the HomBot
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Gets the polling period (in seconds) to refresh state
     *
     * @return the polling period (in seconds) to refresh state
     */
    public Integer getPollingPeriod() {
        return pollingPeriod;
    }

    /**
     * Sets the polling period (in seconds) to refresh state
     *
     * @param pollingPeriod the polling period (in seconds) to refresh state
     */
    public void setPollingPeriod(Integer pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }

}
