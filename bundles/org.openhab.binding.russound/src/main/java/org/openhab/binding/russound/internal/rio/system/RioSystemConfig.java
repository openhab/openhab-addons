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
package org.openhab.binding.russound.internal.rio.system;

/**
 * Configuration class for the {@link RioSystemHandler}
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioSystemConfig {

    /**
     * Constant defined for the "ipAddress" configuration field
     */
    public static final String IP_ADDRESS = "ipAddress";

    /**
     * Constant defined for the "ping" configuration field
     */
    public static final String PING = "ping";

    /**
     * Constant defined for the "retryPolling" configuration field
     */
    public static final String RETRY_POLLING = "retryPolling";

    /**
     * Constant defined for the "scanDevice" configuration field
     */
    public static final String SCAN_DEVICE = "scanDevice";

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
     * Whether to scan the device at startup (and create zones, source, etc dynamically)
     */
    private boolean scanDevice;

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
     * Whether the device should be scanned at startup
     *
     * @return true to scan, false otherwise
     */
    public boolean isScanDevice() {
        return scanDevice;
    }

    /**
     * Sets whether the device should be scanned at startup
     *
     * @param scanDevice true to scan, false otherwise
     */
    public void setScanDevice(boolean scanDevice) {
        this.scanDevice = scanDevice;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
        result = prime * result + ping;
        result = prime * result + retryPolling;
        result = prime * result + (scanDevice ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RioSystemConfig other = (RioSystemConfig) obj;
        if (ipAddress == null) {
            if (other.ipAddress != null) {
                return false;
            }
        } else if (!ipAddress.equals(other.ipAddress)) {
            return false;
        }
        if (ping != other.ping) {
            return false;
        }
        if (retryPolling != other.retryPolling) {
            return false;
        }
        if (scanDevice != other.scanDevice) {
            return false;
        }
        return true;
    }
}
