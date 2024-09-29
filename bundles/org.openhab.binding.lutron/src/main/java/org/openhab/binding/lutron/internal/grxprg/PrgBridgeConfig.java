/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.grxprg;

/**
 * Configuration class for the GRX-PRG/GRX-CI-PRG bridge
 *
 * @author Tim Roberts - Initial contribution
 */
public class PrgBridgeConfig {

    /**
     * IP Address (or host name) of switch
     */
    private String ipAddress;

    /**
     * The username to log in with
     */
    private String userName;

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
}
