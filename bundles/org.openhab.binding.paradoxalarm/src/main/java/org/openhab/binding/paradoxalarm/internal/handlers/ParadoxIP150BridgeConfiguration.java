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
package org.openhab.binding.paradoxalarm.internal.handlers;

/**
 * The {@link ParadoxIP150BridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxIP150BridgeConfiguration {

    private int refresh;
    private String ip150Password;
    private String pcPassword;
    private String ipAddress;
    private int port;
    private String panelType;
    private int reconnectWaitTime;
    private Integer maxZones;
    private Integer maxPartitions;
    private boolean encrypt;

    public int getRefresh() {
        return refresh;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    public String getIp150Password() {
        return ip150Password;
    }

    public void setIp150Password(String ip150Password) {
        this.ip150Password = ip150Password;
    }

    public String getPcPassword() {
        return pcPassword;
    }

    public void setPcPassword(String pcPassword) {
        this.pcPassword = pcPassword;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPanelType() {
        return panelType;
    }

    public void setPanelType(String panelType) {
        this.panelType = panelType;
    }

    public int getReconnectWaitTime() {
        return reconnectWaitTime;
    }

    public void setReconnectWaitTime(int reconnectWaitTime) {
        this.reconnectWaitTime = reconnectWaitTime;
    }

    public Integer getMaxZones() {
        return maxZones;
    }

    public void setMaxZones(Integer maxZones) {
        this.maxZones = maxZones;
    }

    public Integer getMaxPartitions() {
        return maxPartitions;
    }

    public void setMaxPartitions(Integer maxPartitions) {
        this.maxPartitions = maxPartitions;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }
}
