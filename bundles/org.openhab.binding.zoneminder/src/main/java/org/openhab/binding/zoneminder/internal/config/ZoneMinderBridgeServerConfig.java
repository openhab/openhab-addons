/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.zoneminder.internal.config;

import org.openhab.binding.zoneminder.internal.ZoneMinderConstants;

/**
 * Configuration data according to zoneminderserver.xml
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderBridgeServerConfig extends ZoneMinderConfig {

    private String hostname;
    private Integer http_port;
    private Integer telnet_port;

    private String protocol;

    private String urlpath;

    private String user;
    private String password;
    private Integer refresh_interval;
    private Integer refresh_interval_disk_usage;
    private Boolean autodiscover_things;

    @Override
    public String getConfigId() {
        return ZoneMinderConstants.BRIDGE_ZONEMINDER_SERVER;
    }

    public String getHostName() {
        return hostname;
    }

    public void setHostName(String hostName) {
        this.hostname = hostName;
    }

    public Integer getHttpPort() {
        if ((http_port == null) || (http_port == 0)) {
            if (getProtocol().equalsIgnoreCase("http")) {
                http_port = 80;
            } else {
                http_port = 443;
            }
        }
        return http_port;
    }

    public void setHttpPort(Integer port) {
        this.http_port = port;
    }

    public Integer getTelnetPort() {
        return telnet_port;
    }

    public void setTelnetPort(Integer telnetPort) {
        this.telnet_port = telnetPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServerBasePath() {
        return urlpath;
    }

    public void setServerBasePath(String urlpath) {
        this.urlpath = urlpath;
    }

    public String getUserName() {
        return user;
    }

    public void setUserName(String userName) {
        this.user = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRefreshInterval() {
        return refresh_interval;
    }

    public void setRefreshInterval(Integer refreshInterval) {
        this.refresh_interval = refreshInterval;
    }

    public Integer getRefreshIntervalLowPriorityTask() {
        return refresh_interval_disk_usage;
    }

    public void setRefreshIntervalDiskUsage(Integer refreshIntervalDiskUsage) {
        this.refresh_interval_disk_usage = refreshIntervalDiskUsage;
    }

    public Boolean getAutodiscoverThings() {
        return autodiscover_things;
    }

    public void setAutodiscoverThings(Boolean autodiscoverThings) {
        this.autodiscover_things = autodiscoverThings;
    }

}
