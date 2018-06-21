/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
 */
public class SolarEdgeConfiguration {

    private String tokenOrApiKey;
    private String solarId;

    private boolean meterInstalled = false;
    private boolean usePrivateApi = false;

    private Integer asyncTimeout = 120;
    private Integer syncTimeout = 120;
    private Integer liveDataPollingInterval;
    private Integer aggregateDataPollingInterval;

    public final String getTokenOrApiKey() {
        return tokenOrApiKey;
    }

    public final void setTokenOrApiKey(String tokenOrApiKey) {
        this.tokenOrApiKey = tokenOrApiKey;
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

    public final boolean isMeterInstalled() {
        return meterInstalled;
    }

    public final void setMeterInstalled(boolean meterInstalled) {
        this.meterInstalled = meterInstalled;
    }

    public final boolean isUsePrivateApi() {
        return usePrivateApi;
    }

    public final void setUsePrivateApi(boolean usePrivateApi) {
        this.usePrivateApi = usePrivateApi;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tokenOrApiKey", getTokenOrApiKey()).append("solarId", getSolarId())
                .append("meterInstalled", isMeterInstalled()).append("usePrivateApi", isUsePrivateApi())
                .append("live data pollingInterval", getLiveDataPollingInterval())
                .append("aggregate data pollingInterval", getAggregateDataPollingInterval())
                .append("asyncTimeout", getAsyncTimeout()).append("syncTimeout", getSyncTimeout()).toString();
    }

}
