/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal;

/**
 * The {@link EmbyBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class EmbyBridgeConfiguration {

    public String api;

    private String ipAddress;
    private Integer port;
    private Integer httpPort;
    private Integer refreshInterval;
    private Integer notificationTimeout;
    private Integer notificationVolume;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    /*
     * public String getHttpUser() {
     * return httpUser;
     * }
     *
     * public void setHttpUser(String httpUser) {
     * this.httpUser = httpUser;
     * }
     *
     * public String getHttpPassword() {
     * return httpPassword;
     * }
     *
     * public void setHttpPassword(String httpPassword) {
     * this.httpPassword = httpPassword;
     * }
     */
    public Integer getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Integer refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public Integer getNotificationTimeout() {
        return notificationTimeout;
    }

    public void setNotificationTimeout(Integer notificationTimeout) {
        this.notificationTimeout = notificationTimeout;
    }

    public Integer getNotificationVolume() {
        return notificationVolume;
    }

    public void setNotificationVolume(Integer notificationVolume) {
        this.notificationVolume = notificationVolume;
    }
}
