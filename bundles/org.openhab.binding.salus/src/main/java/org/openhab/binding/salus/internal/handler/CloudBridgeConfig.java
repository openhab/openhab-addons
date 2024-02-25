/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.salus.internal.handler;

import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusCloud.DEFAULT_URL;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class CloudBridgeConfig {
    private String username = "";
    private String password = "";
    private String url = "";
    private long refreshInterval = 30;
    private long propertiesRefreshInterval = 5;
    private int maxHttpRetries = 3;

    public CloudBridgeConfig() {
    }

    public CloudBridgeConfig(String username, String password, String url, long refreshInterval,
            long propertiesRefreshInterval) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.refreshInterval = refreshInterval;
        this.propertiesRefreshInterval = propertiesRefreshInterval;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        if (url.isBlank()) {
            return DEFAULT_URL;
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public long getPropertiesRefreshInterval() {
        return propertiesRefreshInterval;
    }

    public void setPropertiesRefreshInterval(long propertiesRefreshInterval) {
        this.propertiesRefreshInterval = propertiesRefreshInterval;
    }

    public int getMaxHttpRetries() {
        return maxHttpRetries;
    }

    public void setMaxHttpRetries(int maxHttpRetries) {
        this.maxHttpRetries = maxHttpRetries;
    }

    public boolean isValid() {
        return !username.isBlank() && !password.isBlank();
    }

    @Override
    public String toString() {
        return "CloudBridgeConfig{" + "username='" + username + '\'' + ", password='<SECRET>'" + ", url='" + url + '\''
                + ", refreshInterval=" + refreshInterval + ", propertiesRefreshInterval=" + propertiesRefreshInterval
                + '}';
    }
}
