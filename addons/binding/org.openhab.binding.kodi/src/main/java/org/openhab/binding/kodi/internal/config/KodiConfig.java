/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.config;

/**
 * Thing configuration from openHab.
 *
 * @author Christoph Weitkamp - Added channels for thumbnail and fanart
 * @author Christoph Weitkamp - Improvements for playing audio notifications
 */
public class KodiConfig {
    private String ipAddress;
    private Integer port;
    private Integer httpPort;
    private String httpUser;
    private String httpPassword;
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

    public String getHttpUser() {
        return httpUser;
    }

    public void setHttpUser(String httpUser) {
        this.httpUser = httpUser;
    }

    public String getHttpPassword() {
        return httpPassword;
    }

    public void setHttpPassword(String httpPassword) {
        this.httpPassword = httpPassword;
    }

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
