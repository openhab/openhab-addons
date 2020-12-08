/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tivo.internal.service;

import java.util.SortedSet;

/**
 * The Class {@link TivoConfigData} stores the dynamic configuration parameters used within the {@link TivoHandler } and
 * {@link TivoConfigStatusProvider}.
 *
 * @author Jayson Kubilis (DigitalBytes) - Initial contribution
 * @author Andrew Black (AndyXMB) - minor updates, removal of unused DiscoveryService functionality.
 */
public class TivoConfigData {

    private String cfgHost = null;
    private int cfgTcpPort = -1;
    private int cfgNumConnRetry = 0;
    private int cfgPollInterval = 30;
    private boolean cfgPollChanges = false;
    private boolean cfgKeepConnOpen = false;
    private int cfgCmdWait = 0;
    private SortedSet<Integer> cfgIgnoreChannels = null;
    private String cfgIdentifier = "";
    private int cfgMinChannel = 1;
    private int cfgMaxChannel = 9999;
    private boolean cfgIgnoreChannelScan = false;

    /**
     * {@link toString} returns each of the configuration items as a single concatenated string.
     *
     * @return string
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TivoConfigData [cfgHost=" + cfgHost + ", cfgTcpPort=" + cfgTcpPort + ", cfgNumConnRetry="
                + cfgNumConnRetry + ", cfgPollInterval=" + cfgPollInterval + ", cfgPollChanges=" + cfgPollChanges
                + ", cfgKeepConnOpen=" + cfgKeepConnOpen + ", cfgCmdWait=" + cfgCmdWait + ", cfgIgnoreChannels="
                + cfgIgnoreChannels + ", cfgIdentifier=" + cfgIdentifier + ", cfgMinChannel=" + cfgMinChannel
                + ", cfgMaxChannel=" + cfgMaxChannel + ", cfgIgnoreChannelScan=" + cfgIgnoreChannelScan + "]";
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
     * Gets the cfgHost representing the host name or IP address of the device.
     *
     * @return the cfgHost
     */
    public String getCfgHost() {
        return cfgHost;
    }

    /**
     * the cfgHost representing the host name or IP address of the device.
     *
     * @param cfgHost the cfgHost to set
     */
    public void setCfgHost(String cfgHost) {
        this.cfgHost = cfgHost;
    }

    /**
     * Gets the cfgTcp representing the IP port of the Remote Control Protocol service on the device.
     *
     * @return the cfgTcpPort
     */
    public int getCfgTcpPort() {
        return cfgTcpPort;
    }

    /**
     * Sets the cfgTcp representing the IP port of the Remote Control Protocol service on the device (31339).
     *
     * @param cfgTcpPort the cfgTcpPort to set
     */
    public void setCfgTcpPort(int cfgTcpPort) {
        this.cfgTcpPort = cfgTcpPort;
    }

    /**
     * Gets the cfgNumConnRetry value. This determines the number of connection attempts made to the IP/Port of the
     * service and the number of read attempts that are made when a command is submitted to the device, separated by
     * the
     * interval specified in the Command Wait Interval.
     *
     * @return the cfgNumConnRetry
     */
    public int getCfgNumConnRetry() {
        return cfgNumConnRetry;
    }

    /**
     * Sets the cfgNumConnRetry value. This determines the number of connection attempts made to the IP/Port of the
     * service and the number of read attempts that are made when a command is submitted to the device, separated by
     * the
     * interval specified in the Command Wait Interval.
     *
     * @param cfgNumConnRetry the cfgNumConnRetry to set
     */
    public void setCfgNumConnRetry(int cfgNumConnRetry) {
        this.cfgNumConnRetry = cfgNumConnRetry;
    }

    /**
     * Gets the cfgPollInterval representing the interval in seconds between polling attempts to collect any updated
     * status information.
     *
     * @return the cfgPollInterval
     */
    public int getCfgPollInterval() {
        return cfgPollInterval;
    }

    /**
     * Sets the cfgPollInterval representing the interval in seconds between polling attempts to collect any updated
     * status information.
     *
     * @param cfgPollInterval the cfgPollInterval to set
     */
    public void setCfgPollInterval(int cfgPollInterval) {
        this.cfgPollInterval = cfgPollInterval;
    }

    /**
     * Checks if is cfg poll changes.
     *
     * @return the cfgPollChanges
     */
    public boolean doPollChanges() {
        return cfgPollChanges;
    }

    /**
     * Sets the cfg poll changes.
     *
     * @param cfgPollChanges the cfgPollChanges to set
     */
    public void setCfgPollChanges(boolean cfgPollChanges) {
        this.cfgPollChanges = cfgPollChanges;
    }

    /**
     * Checks if is cfg keep conn open.
     *
     * @return the cfgKeepConnOpen
     */
    public boolean isCfgKeepConnOpen() {
        return cfgKeepConnOpen;
    }

    /**
     * Sets the cfg keep conn open.
     *
     * @param cfgKeepConnOpen the cfgKeepConnOpen to set
     */
    public void setCfgKeepConnOpen(boolean cfgKeepConnOpen) {
        this.cfgKeepConnOpen = cfgKeepConnOpen;
    }

    /**
     * Gets the cfg cmd wait.
     *
     * @return the cfgCmdWait
     */
    public int getCfgCmdWait() {
        return cfgCmdWait;
    }

    /**
     * Sets the cfg cmd wait.
     *
     * @param cfgCmdWait the cfgCmdWait to set
     */
    public void setCfgCmdWait(int cfgCmdWait) {
        this.cfgCmdWait = cfgCmdWait;
    }

    /**
     * Gets the cfg ignore channels.
     *
     * @return the cfgIgnoreChannels
     */
    public SortedSet<Integer> getCfgIgnoreChannels() {
        return cfgIgnoreChannels;
    }

    /**
     * Adds the cfg ignore channels.
     *
     * @param pChannel the channel
     * @return the cfgIgnoreChannels
     */
    public SortedSet<Integer> addCfgIgnoreChannels(Integer pChannel) {
        cfgIgnoreChannels.add(pChannel);
        return cfgIgnoreChannels;
    }

    /**
     * Sets the cfg ignore channels.
     *
     * @param cfgIgnoreChannels the cfgIgnoreChannels to set
     */
    public void setCfgIgnoreChannels(SortedSet<Integer> cfgIgnoreChannels) {
        this.cfgIgnoreChannels = cfgIgnoreChannels;
    }

    /**
     * Sets the cfg min channel.
     *
     * @param cfgMinChannel the new cfg min channel
     */
    public void setCfgMinChannel(Integer cfgMinChannel) {
        this.cfgMinChannel = cfgMinChannel;
    }

    /**
     * Gets the cfg min channel.
     *
     * @return the cfg min channel
     */
    public Integer getCfgMinChannel() {
        return cfgMinChannel;
    }

    /**
     * Sets the cfg max channel.
     *
     * @param cfgMaxChannel the new cfg max channel
     */
    public void setCfgMaxChannel(Integer cfgMaxChannel) {
        this.cfgMaxChannel = cfgMaxChannel;
    }

    /**
     * Gets the cfg max channel.
     *
     * @return the cfg max channel
     */
    public Integer getCfgMaxChannel() {
        return cfgMaxChannel;
    }

    /**
     * Sets the value of cfgIgnoreChannelScan.
     *
     * @param cfgIgnoreChannelScan the new cfg ignore channel scan
     */
    public void setCfgIgnoreChannelScan(Boolean cfgIgnoreChannelScan) {
        this.cfgIgnoreChannelScan = cfgIgnoreChannelScan;
    }

    /**
     * {@link doChannelScan} Gets the cfg ignore channel scan.
     *
     * @return boolean value of cfgIgnoreChannelScan.
     */
    public Boolean doChannelScan() {
        return cfgIgnoreChannelScan;
    }

}
