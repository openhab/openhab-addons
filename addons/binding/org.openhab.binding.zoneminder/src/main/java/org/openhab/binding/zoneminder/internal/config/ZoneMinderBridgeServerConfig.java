/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.config;

import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.RefreshPriority;

/**
 * Configuration data according to zoneminderserver.xml
 *
 * @author Martin S. Eskildsen - Initial contribution
 *
 */
public class ZoneMinderBridgeServerConfig extends ZoneMinderConfig {

    private Integer portHttp;
    private Integer portTelnet;

    private String protocol;
    private String host;

    private String urlSite;
    private String urlApi;

    private String user;
    private String password;
    private Integer refreshNormal;
    private Integer refreshLow;
    private String diskUsageRefresh;
    private Boolean autodiscover;

    private Boolean useSpecificUserStreaming;
    private String streamingUser;
    private String streamingPassword;

    @Override
    public String getConfigId() {
        return ZoneMinderConstants.BRIDGE_ZONEMINDER_SERVER;
    }

    public String getHost() {
        return host;
    }

    public void setHostName(String host) {
        this.host = host;
    }

    public Integer getHttpPort() {
        if ((portHttp == null) || (portHttp == 0)) {
            if (getProtocol().equalsIgnoreCase("http")) {
                portHttp = 80;
            } else {
                portHttp = 443;
            }
        }
        return portHttp;
    }

    public void setHttpPort(Integer port) {
        this.portHttp = port;
    }

    public Integer getTelnetPort() {
        return portTelnet;
    }

    public void setTelnetPort(Integer telnetPort) {
        this.portTelnet = telnetPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServerBasePath() {
        return urlSite;
    }

    public void setServerBasePath(String urlpath) {
        this.urlSite = urlpath;
    }

    public String getServerApiPath() {
        return urlApi;
    }

    public void setServerApiPath(String apiPath) {
        this.urlApi = apiPath;
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

    public Integer getRefreshIntervalNormal() {
        return this.refreshNormal;
    }

    public void setRefreshIntervalNormal(Integer refreshInterval) {
        this.refreshNormal = refreshInterval;
    }

    public Integer getRefreshIntervalLow() {
        return this.refreshLow;
    }

    public void setRefreshIntervalDiskUsage(Integer refreshInterval) {
        this.refreshLow = refreshInterval;
    }

    public Boolean getAutodiscoverThings() {
        return autodiscover;
    }

    public void setAutodiscoverThings(Boolean autodiscoverThings) {
        this.autodiscover = autodiscoverThings;
    }

    public RefreshPriority getDiskUsageRefresh() {
        return getRefreshPriorityEnum(diskUsageRefresh);
    }

    public Boolean getUseSpecificUserStreaming() {
        return useSpecificUserStreaming;
    }

    public String getStreamingUser() {
        if (!getUseSpecificUserStreaming()) {
            return getUserName();
        }
        return streamingUser;
    }

    public String getStreamingPassword() {
        if (!getUseSpecificUserStreaming()) {
            return getPassword();
        }
        return streamingPassword;
    }
}
