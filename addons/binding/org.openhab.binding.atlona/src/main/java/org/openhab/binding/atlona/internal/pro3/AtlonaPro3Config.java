/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal.pro3;

import org.openhab.binding.atlona.internal.discovery.AtlonaDiscovery;

/**
 * Configuration class for the Atlona Pro3 line of switchers
 *
 * @author Tim Roberts
 */
public class AtlonaPro3Config {

    /**
     * Constant field used in {@link AtlonaDiscovery} to set the config property during discovery. Value of this field
     * needs to match {@link #ipAddress}
     */
    public static final String IpAddress = "ipAddress";

    /**
     * IP Address (or host name) of switch
     */
    private String ipAddress;

    /**
     * Optional username to login in with. Only used if the switch has it's "Telnet Login" option turned on
     */
    private String userName;

    /**
     * Optional password to login in with. Only used if the switch has it's "Telnet Login" option turned on
     */
    private String password;

    /**
     * Polling time (in seconds) to refresh state from the switch itself. Only useful if something else modifies the
     * switch (usually through the front panel or the IR link)
     */
    private int polling;

    /**
     * Ping time (in seconds) to keep the connection alive. Should be less than the IP Timeout on the switch.
     */
    private int ping;

    /**
     * Polling time (in seconds) to attempt a reconnect if the socket session has failed
     */
    private int retryPolling;

    /**
     * Returns the IP address or host name of the switch
     *
     * @return the IP address or host name of the swtich
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the IP address or host name of the switch
     *
     * @param ipAddress the IP Address or host name of the switch
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Gets the username used to login with
     *
     * @return the username used to login with
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the username used to login with
     *
     * @param userName the username used to login with
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the password used to login with
     *
     * @return the password used to login with
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password used to login with
     *
     * @param password the password used to login with
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the polling (in seconds) to refresh state
     *
     * @return the polling (in seconds) to refresh state
     */
    public int getPolling() {
        return polling;
    }

    /**
     * Sets the polling (in seconds) to refresh state
     *
     * @param polling the polling (in seconds) to refresh state
     */
    public void setPolling(int polling) {
        this.polling = polling;
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
