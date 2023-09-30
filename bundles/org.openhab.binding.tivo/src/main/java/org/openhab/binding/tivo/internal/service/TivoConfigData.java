/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tivo.internal.service;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Class {@link TivoConfigData} stores the dynamic configuration parameters used within the {@link TiVoHandler} and
 * {@link TivoStatusProvider}.
 *
 * @author Jayson Kubilis (DigitalBytes) - Initial contribution
 * @author Andrew Black (AndyXMB) - minor updates, removal of unused DiscoveryService functionality.
 * @author Michael Lobstein - Updated for OH3
 */

@NonNullByDefault
public class TivoConfigData {
    private @Nullable String host = null;
    private int tcpPort = 31339;
    private int numRetry = 0;
    private int pollInterval = 30;
    private boolean pollForChanges = false;
    private boolean keepConActive = false;
    private int cmdWaitInterval = 0;
    private String cfgIdentifier = "";

    /**
     * {@link toString} returns each of the configuration items as a single concatenated string.
     *
     * @return string
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TivoConfigData [host=" + host + ", tcpPort=" + tcpPort + ", numRetry=" + numRetry + ", pollInterval="
                + pollInterval + ", pollForChanges=" + pollForChanges + ", keepConActive=" + keepConActive
                + ", cmdWaitInterval=" + cmdWaitInterval + ", cfgIdentifier=" + cfgIdentifier + "]";
    }

    /**
     * Gets the cfgIdentifier representing the thing name of the TiVo device.
     *
     * @return the cfgIdentifier
     */
    public String getCfgIdentifier() {
        return this.cfgIdentifier;
    }

    /**
     * Sets the cfgIdentifier representing the thing name of the TiVo device.
     *
     * @param cfgIdentifier the cfgIdentifier to set
     */
    public void setCfgIdentifier(String cfgIdentifier) {
        this.cfgIdentifier = cfgIdentifier;
    }

    /**
     * Gets the host representing the host name or IP address of the device.
     *
     * @return the host
     */
    public @Nullable String getHost() {
        return host;
    }

    /**
     * the host representing the host name or IP address of the device.
     *
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the cfgTcp representing the IP port of the Remote Control Protocol service on the device.
     *
     * @return the tcpPort
     */
    public int getTcpPort() {
        return tcpPort;
    }

    /**
     * Sets the cfgTcp representing the IP port of the Remote Control Protocol service on the device (31339).
     *
     * @param tcpPort the tcpPort to set
     */
    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    /**
     * Gets the numRetry value. This determines the number of connection attempts made to the IP/Port of the
     * service and the number of read attempts that are made when a command is submitted to the device, separated by
     * the
     * interval specified in the Command Wait Interval.
     *
     * @return the numRetry
     */
    public int getNumRetry() {
        return numRetry;
    }

    /**
     * Sets the numRetry value. This determines the number of connection attempts made to the IP/Port of the
     * service and the number of read attempts that are made when a command is submitted to the device, separated by
     * the
     * interval specified in the Command Wait Interval.
     *
     * @param numRetry the numRetry to set
     */
    public void setNumRetry(int numRetry) {
        this.numRetry = numRetry;
    }

    /**
     * Gets the pollInterval representing the interval in seconds between polling attempts to collect any updated
     * status information.
     *
     * @return the pollInterval
     */
    public int getPollInterval() {
        return pollInterval;
    }

    /**
     * Sets the pollInterval representing the interval in seconds between polling attempts to collect any updated
     * status information.
     *
     * @param pollInterval the pollInterval to set
     */
    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    /**
     * Checks if is cfg poll changes.
     *
     * @return the pollForChanges
     */
    public boolean doPollChanges() {
        return pollForChanges;
    }

    /**
     * Sets the cfg poll changes.
     *
     * @param pollForChanges the pollForChanges to set
     */
    public void setPollForChanges(boolean pollForChanges) {
        this.pollForChanges = pollForChanges;
    }

    /**
     * Checks if is cfg keep conn open.
     *
     * @return the keepConActive
     */
    public boolean isKeepConnActive() {
        return keepConActive;
    }

    /**
     * Sets the cfg keep conn open.
     *
     * @param keepConActive the keepConActive to set
     */
    public void setKeepConnActive(boolean keepConActive) {
        this.keepConActive = keepConActive;
    }

    /**
     * Gets the cfg cmd wait.
     *
     * @return the cmdWaitInterval
     */
    public int getCmdWaitInterval() {
        return cmdWaitInterval;
    }

    /**
     * Sets the cfg cmd wait.
     *
     * @param cmdWaitInterval the cmdWaitInterval to set
     */
    public void setCmdWaitInterval(int cmdWaitInterval) {
        this.cmdWaitInterval = cmdWaitInterval;
    }
}
