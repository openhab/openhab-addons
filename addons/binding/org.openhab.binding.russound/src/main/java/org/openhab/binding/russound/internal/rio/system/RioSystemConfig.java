/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.system;

/**
 * Configuration class for the {@link RioSystemHandler}
 *
 * @author Tim Roberts
 */
public class RioSystemConfig {

    /**
     * IP Address (or host name) of system
     */
    private String ipAddress;

    /**
     * Ping time (in seconds) to keep the connection alive.
     */
    private int ping;

    /**
     * Polling time (in seconds) to attempt a reconnect if the socket session has failed
     */
    private int retryPolling;

    /**
     * Returns the IP address or host name
     *
     * @return the IP address or host name
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the IP address or host name
     *
     * @param ipAddress the IP Address or host name
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Gets the polling (in seconds) to reconnect
     *
     * @return the polling (in seconds) to reconnect
     */
    public int getRetryPolling() {
        return retryPolling;
    }

    /**
     * Sets the polling (in seconds) to reconnect
     *
     * @param retryPolling the polling (in seconds to reconnect)
     */
    public void setRetryPolling(int retryPolling) {
        this.retryPolling = retryPolling;
    }

    /**
     * Gets the ping interval (in seconds)
     *
     * @return the ping interval (in seconds)
     */
    public int getPing() {
        return ping;
    }

    /**
     * Sets the ping interval (in seconds)
     *
     * @param ping the ping interval (in seconds)
     */
    public void setPing(int ping) {
        this.ping = ping;
    }
}
