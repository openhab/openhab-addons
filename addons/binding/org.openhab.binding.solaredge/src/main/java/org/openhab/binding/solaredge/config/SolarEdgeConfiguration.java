/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.config;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Bean holding configuration data according to bridge.xml
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class SolarEdgeConfiguration {

    private String username;
    private String password;
    private String solarId;

    private boolean legacyMode = false;

    private Integer asyncTimeout = 120;
    private Integer syncTimeout = 120;
    private Integer liveDataPollingInterval;
    private Integer aggregateDataPollingInterval;

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

    public final String getSolarId() {
        return solarId;
    }

    public final void setSolarId(String solarId) {
        this.solarId = solarId;
    }

    public final Integer getAsyncTimeout() {
        return asyncTimeout;
    }

    public final void setAsyncTimeout(Integer asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }

    public final Integer getSyncTimeout() {
        return syncTimeout;
    }

    public final void setSyncTimeout(Integer syncTimeout) {
        this.syncTimeout = syncTimeout;
    }

    public final boolean isLegacyMode() {
        return legacyMode;
    }

    public final void setLegacyMode(boolean legacyMode) {
        this.legacyMode = legacyMode;
    }

    public final Integer getLiveDataPollingInterval() {
        return liveDataPollingInterval;
    }

    public final void setLiveDataPollingInterval(Integer liveDataPollingInterval) {
        this.liveDataPollingInterval = liveDataPollingInterval;
    }

    public final Integer getAggregateDataPollingInterval() {
        return aggregateDataPollingInterval;
    }

    public final void setAggregateDataPollingInterval(Integer aggregateDataPollingInterval) {
        this.aggregateDataPollingInterval = aggregateDataPollingInterval;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("username", getUsername()).append("password", getPassword())
                .append("solarId", getSolarId()).append("legacyMode", isLegacyMode())
                .append("live data pollingInterval", getLiveDataPollingInterval())
                .append("aggregate data pollingInterval", getAggregateDataPollingInterval())
                .append("asyncTimeout", getAsyncTimeout()).append("syncTimeout", getSyncTimeout()).toString();
    }

}
