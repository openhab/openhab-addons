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

    public String getTokenOrApiKey() {
        return tokenOrApiKey;
    }

    public void setTokenOrApiKey(String tokenOrApiKey) {
        this.tokenOrApiKey = tokenOrApiKey;
    }

    public String getSolarId() {
        return solarId;
    }

    public void setSolarId(String solarId) {
        this.solarId = solarId;
    }

    public Integer getAsyncTimeout() {
        return asyncTimeout;
    }

    public void setAsyncTimeout(Integer asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }

    public Integer getSyncTimeout() {
        return syncTimeout;
    }

    public void setSyncTimeout(Integer syncTimeout) {
        this.syncTimeout = syncTimeout;
    }

    public Integer getLiveDataPollingInterval() {
        return liveDataPollingInterval;
    }

    public void setLiveDataPollingInterval(Integer liveDataPollingInterval) {
        this.liveDataPollingInterval = liveDataPollingInterval;
    }

    public Integer getAggregateDataPollingInterval() {
        return aggregateDataPollingInterval;
    }

    public void setAggregateDataPollingInterval(Integer aggregateDataPollingInterval) {
        this.aggregateDataPollingInterval = aggregateDataPollingInterval;
    }

    public boolean isMeterInstalled() {
        return meterInstalled;
    }

    public void setMeterInstalled(boolean meterInstalled) {
        this.meterInstalled = meterInstalled;
    }

    public boolean isUsePrivateApi() {
        return usePrivateApi;
    }

    public void setUsePrivateApi(boolean usePrivateApi) {
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
