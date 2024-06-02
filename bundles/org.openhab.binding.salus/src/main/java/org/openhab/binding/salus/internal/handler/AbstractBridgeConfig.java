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
public abstract class AbstractBridgeConfig {
    protected String username = "";
    protected String password = "";
    protected String url = "";
    protected long refreshInterval = 30;
    protected long propertiesRefreshInterval = 5;
    protected int maxHttpRetries = 3;

    public AbstractBridgeConfig() {
    }

    public AbstractBridgeConfig(String username, String password, String url, long refreshInterval,
            long propertiesRefreshInterval, int maxHttpRetries) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.refreshInterval = refreshInterval;
        this.propertiesRefreshInterval = propertiesRefreshInterval;
        this.maxHttpRetries = maxHttpRetries;
    }

    public boolean isValid() {
        return !username.isBlank() && !password.isBlank();
    }

    public final String getUsername() {
        return username;
    }

    public final void setUsername(String username) {
        this.username = username;
    }

    public final String getPassword() {
        return password;
    }

    public final void setPassword(String password) {
        this.password = password;
    }

    public final String getUrl() {
        if (url.isBlank()) {
            return DEFAULT_URL;
        }
        return url;
    }

    public final void setUrl(String url) {
        this.url = url;
    }

    public final long getRefreshInterval() {
        return refreshInterval;
    }

    public final void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public final long getPropertiesRefreshInterval() {
        return propertiesRefreshInterval;
    }

    public final void setPropertiesRefreshInterval(long propertiesRefreshInterval) {
        this.propertiesRefreshInterval = propertiesRefreshInterval;
    }

    public final int getMaxHttpRetries() {
        return maxHttpRetries;
    }

    public final void setMaxHttpRetries(int maxHttpRetries) {
        this.maxHttpRetries = maxHttpRetries;
    }
}
