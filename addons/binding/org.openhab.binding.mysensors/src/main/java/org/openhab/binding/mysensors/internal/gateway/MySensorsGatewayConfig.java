/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.gateway;

/**
 * Holds the configuration and parameters of the MySensors gateway.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public class MySensorsGatewayConfig {

    /**
     * Is a serial or ip gateway?
     */
    private MySensorsGatewayType gatewayType;

    // GLOBALS
    /**
     * Delay at which messages are send from the internal queue to the MySensors network
     */
    private Integer sendDelay;

    /**
     * Should nodes send imperial or metric values?
     */
    private Boolean imperial;

    /**
     * Should the startup check of the bridge at boot skipped?
     */
    private Boolean startupCheckEnabled;

    /**
     * Network sanity check enabled?
     */
    private Boolean networkSanCheckEnabled;

    /**
     * Determines interval to start NetworkSanityCheck
     */
    private Integer networkSanCheckInterval;

    /**
     * Connection will wait this number of attempts before disconnecting
     */
    private Integer networkSanCheckConnectionFailAttempts;

    /**
     * Network sanity checker will also send heartbeats to all known nodes
     */
    private boolean networkSanCheckSendHeartbeat;

    /**
     * Disconnect nodes that fail to answer to heartbeat request
     */
    private Integer networkSanCheckSendHeartbeatFailAttempts;

    // SERIAL
    /**
     * Serial port the gateway is attached to
     */
    private String serialPort;

    /**
     * Baud rate used to connect the serial port
     */
    private Integer baudRate;

    /**
     * try hard reset of serial port using DTR
     */
    private boolean hardReset;

    // Ip
    /**
     * ip address the gateway is attached to
     */
    private String ipAddress;

    /**
     * tcp port the gateway is running at
     */
    private Integer tcpPort;

    /**
     * Name of the MQTT broker defined
     */
    private String brokerName;

    /**
     * Name of the MQTT topic to subscribe to
     */
    private String topicSubscribe;

    /**
     * Name of the MQTT topic to publish to
     */
    private String topicPublish;

    public MySensorsGatewayType getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(MySensorsGatewayType gatewayType) {
        this.gatewayType = gatewayType;
    }

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(Integer tcpPort) {
        this.tcpPort = tcpPort;
    }

    public Integer getSendDelay() {
        return sendDelay;
    }

    public void setSendDelay(Integer sendDelay) {
        this.sendDelay = sendDelay;
    }

    public Integer getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(Integer baudRate) {
        this.baudRate = baudRate;
    }

    public Boolean getImperial() {
        return imperial;
    }

    public void setImperial(Boolean imperial) {
        this.imperial = imperial;
    }

    public Boolean getStartupCheck() {
        return startupCheckEnabled;
    }

    public void setStartupCheck(Boolean startupCheckEnabled) {
        this.startupCheckEnabled = startupCheckEnabled;
    }

    public Boolean getEnableNetworkSanCheck() {
        return networkSanCheckEnabled;
    }

    public void setEnableNetworkSanCheck(Boolean enableNetworkSanCheck) {
        this.networkSanCheckEnabled = enableNetworkSanCheck;
    }

    public Integer getSanityCheckerInterval() {
        return networkSanCheckInterval;
    }

    public void setSanityCheckerInterval(Integer sanityCheckerInterval) {
        this.networkSanCheckInterval = sanityCheckerInterval;
    }

    public Integer getSanCheckConnectionFailAttempts() {
        return networkSanCheckConnectionFailAttempts;
    }

    public void setSanCheckConnectionFailAttempts(Integer sanCheckConnectionFailAttempts) {
        this.networkSanCheckConnectionFailAttempts = sanCheckConnectionFailAttempts;
    }

    public boolean getSanCheckSendHeartbeat() {
        return networkSanCheckSendHeartbeat;
    }

    public void setSanCheckSendHeartbeat(boolean sanCheckSendHeartbeat) {
        this.networkSanCheckSendHeartbeat = sanCheckSendHeartbeat;
    }

    public Integer getSanCheckSendHeartbeatFailAttempts() {
        return networkSanCheckSendHeartbeatFailAttempts;
    }

    public void setSanCheckSendHeartbeatFailAttempts(Integer sanCheckSendHeartbeatFailAttempts) {
        this.networkSanCheckSendHeartbeatFailAttempts = sanCheckSendHeartbeatFailAttempts;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getTopicSubscribe() {
        return topicSubscribe;
    }

    public void setTopicSubscribe(String topicSubscribe) {
        this.topicSubscribe = topicSubscribe;
    }

    public String getTopicPublish() {
        return topicPublish;
    }

    public void setTopicPublish(String topicPublish) {
        this.topicPublish = topicPublish;
    }

    public boolean isHardReset() {
        return hardReset;
    }

    public void setHardReset(boolean hardReset) {
        this.hardReset = hardReset;
    }

    @Override
    public String toString() {
        return "MySensorsGatewayConfig [gatewayType=" + gatewayType + ", sendDelay=" + sendDelay + ", imperial="
                + imperial + ", skipStartupCheck=" + startupCheckEnabled + ", enableNetworkSanCheck="
                + networkSanCheckEnabled + ", sanityCheckerInterval=" + networkSanCheckInterval
                + ", sanCheckConnectionFailAttempts=" + networkSanCheckConnectionFailAttempts
                + ", sanCheckSendHeartbeat=" + networkSanCheckSendHeartbeat + ", sanCheckSendHeartbeatFailAttempts="
                + networkSanCheckSendHeartbeatFailAttempts + ", serialPort=" + serialPort + ", baudRate=" + baudRate
                + ", ipAddress=" + ipAddress + ", tcpPort=" + tcpPort + ", brokerName=" + brokerName
                + ", topicSubscribe=" + topicSubscribe + ", topicPublish=" + topicPublish + "]";
    }
}
