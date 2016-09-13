/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.bravia;

/**
 * Configuration class for the {@link BraviaHandler}
 *
 * @author Tim Roberts
 * @version $Id: $Id
 */
public class BraviaConfig {

    /**
     * IP Address (or host name) of system
     */
    private String ipAddress;

    /**
     * The network interface the system listens on (eth0 or wlan0)
     */
    private String netInterface;

    /**
     * Ping time (in seconds) to keep the connection alive.
     */
    private int ping;

    /**
     * Refresh time (in seconds) to refresh attributes from the system
     */
    private int refresh;

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

    /**
     * Gets the network interface being used
     * 
     * @return the network interface
     */
    public String getNetInterface() {
        return netInterface;
    }

    /**
     * Sets the network interface being used
     * 
     * @param netInterface the network interface
     */
    public void setNetInterface(String netInterface) {
        this.netInterface = netInterface;
    }

    /**
     * Returns the refresh interval (in seconds)
     * 
     * @return the refresh interval (in seconds)
     */
    public int getRefresh() {
        return refresh;
    }

    /**
     * Sets the refresh interval (in seconds)
     * 
     * @param refresh the refresh interval (in seconds)
     */
    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }
}
