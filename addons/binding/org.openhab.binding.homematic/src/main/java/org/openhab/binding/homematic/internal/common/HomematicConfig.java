/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.common;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmGatewayInfo;
import org.openhab.binding.homematic.internal.model.HmInterface;

/**
 * The main gateway config class.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicConfig {
    private static final String ISO_ENCODING = "ISO-8859-1";
    private static final String UTF_ENCODING = "UTF-8";

    public static final String GATEWAY_TYPE_AUTO = "AUTO";
    public static final String GATEWAY_TYPE_CCU = "CCU";

    private static final int DEFAULT_PORT_RF = 2001;
    private static final int DEFAULT_PORT_WIRED = 2000;
    private static final int DEFAULT_PORT_HMIP = 2010;
    private static final int DEFAULT_PORT_CUXD = 8701;

    private String gatewayAddress;
    private String gatewayType = GATEWAY_TYPE_AUTO;

    private int rfPort;
    private int wiredPort;
    private int hmIpPort;
    private int cuxdPort;

    private String callbackHost;
    private int callbackPort;

    private Integer aliveInterval = 300;
    private int socketMaxAlive = 900;
    private int timeout = 15;
    private int reconnectInterval = 0;

    private HmGatewayInfo gatewayInfo;

    /**
     * Returns the Homematic gateway address.
     */
    public String getGatewayAddress() {
        return gatewayAddress;
    }

    /**
     * Sets the Homematic gateway address.
     */
    public void setGatewayAddress(String gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

    /**
     * Returns the callback host address.
     */
    public String getCallbackHost() {
        return callbackHost;
    }

    /**
     * Sets the callback host address.
     */
    public void setCallbackHost(String callbackHost) {
        this.callbackHost = callbackHost;
    }

    /**
     * Returns the callback host port.
     */
    public int getCallbackPort() {
        return callbackPort;
    }

    /**
     * Sets the callback host port.
     */
    public void setCallbackPort(int callbackPort) {
        this.callbackPort = callbackPort;
    }

    /**
     * Returns the alive interval in seconds.
     */
    public Integer getAliveInterval() {
        return aliveInterval;
    }

    /**
     * Sets the alive interval in seconds.
     */
    public void setAliveInterval(Integer aliveInterval) {
        this.aliveInterval = aliveInterval;
    }

    /**
     * Returns the HmGatewayInfo.
     */
    public HmGatewayInfo getGatewayInfo() {
        return gatewayInfo;
    }

    /**
     * Sets the HmGatewayInfo.
     */
    public void setGatewayInfo(HmGatewayInfo gatewayInfo) {
        this.gatewayInfo = gatewayInfo;
    }

    /**
     * Returns the max alive time of a socket connection to a Homematic gateway in seconds.
     */
    public int getSocketMaxAlive() {
        return socketMaxAlive;
    }

    /**
     * Sets the max alive time of a socket connection to a Homematic gateway in seconds.
     */
    public void setSocketMaxAlive(int socketMaxAlive) {
        this.socketMaxAlive = socketMaxAlive;
    }

    /**
     * Returns the timeout for the communication to a Homematic gateway in seconds.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout for the communication to a Homematic gateway in seconds.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the interval in seconds to reconnect to the Homematic gateway.
     */
    public int getReconnectInterval() {
        return reconnectInterval;
    }

    /**
     * Sets the interval in seconds to reconnect to the Homematic gateway.
     */
    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    /**
     * Returns the HmGatewayType.
     */
    public String getGatewayType() {
        return gatewayType;
    }

    /**
     * Sets the HmGatewayType.
     */
    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    /**
     * Returns the TclRegaScript url.
     */
    public String getTclRegaUrl() {
        return "http://" + gatewayAddress + ":8181/tclrega.exe";
    }

    /**
     * Returns the Homematic gateway port of the channel.
     */
    public int getRpcPort(HmChannel channel) {
        return getRpcPort(channel.getDevice().getHmInterface());
    }

    /**
     * Returns the Homematic gateway port of the interfaces.
     */
    public int getRpcPort(HmInterface hmInterface) {
        if (gatewayInfo != null) {
            if (HmInterface.RF.equals(hmInterface)) {
                return getRfPort();
            } else if (HmInterface.WIRED.equals(hmInterface)) {
                return getWiredPort();
            } else if (HmInterface.HMIP.equals(hmInterface)) {
                return getHmIpPort();
            } else if (HmInterface.CUXD.equals(hmInterface)) {
                return getCuxdPort();
            }
        }
        return getRfPort();
    }

    private int getRfPort() {
        return rfPort == 0 ? DEFAULT_PORT_RF : rfPort;
    }

    private int getWiredPort() {
        return wiredPort == 0 ? DEFAULT_PORT_WIRED : wiredPort;
    }

    private int getHmIpPort() {
        return hmIpPort == 0 ? DEFAULT_PORT_HMIP : hmIpPort;
    }

    private int getCuxdPort() {
        return cuxdPort == 0 ? DEFAULT_PORT_CUXD : cuxdPort;
    }

    /**
     * Returns true, if a wired port is configured.
     */
    public boolean hasWiredPort() {
        return wiredPort != 0;
    }

    /**
     * Returns true, if a hmIp port is configured.
     */
    public boolean hasHmIpPort() {
        return hmIpPort != 0;
    }

    /**
     * Returns true, if a cuxd port is configured.
     */
    public boolean hasCuxdPort() {
        return cuxdPort != 0;
    }

    /**
     * Returns the encoding of a Homematic gateway.
     */
    public String getEncoding() {
        if (gatewayInfo != null && gatewayInfo.isHomegear()) {
            return UTF_ENCODING;
        } else {
            return ISO_ENCODING;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("gatewayAddress", gatewayAddress).append("callbackHost", callbackHost)
                .append("callbackPort", callbackPort).append("gatewayType", gatewayType).append("rfPort", getRfPort())
                .append("wiredPort", getWiredPort()).append("hmIpPort", hmIpPort).append("cuxdPort", getCuxdPort())
                .append("aliveInterval", aliveInterval).append("reconnectInterval", reconnectInterval)
                .append("timeout", timeout).append("socketMaxAlive", socketMaxAlive);
        return tsb.toString();
    }
}
