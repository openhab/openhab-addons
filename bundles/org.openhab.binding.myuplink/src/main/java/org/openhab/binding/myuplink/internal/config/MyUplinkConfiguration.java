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
package org.openhab.binding.myuplink.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MyUplinkConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public class MyUplinkConfiguration {

    private String clientId = "";
    private String clientSecret = "";

    private Integer asyncTimeout = 120;
    private Integer syncTimeout = 120;
    private Integer dataPollingInterval = 120;

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the clientSecret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * @param clientSecret the clientSecret to set
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * @return the asyncTimeout
     */
    public Integer getAsyncTimeout() {
        return asyncTimeout;
    }

    /**
     * @param asyncTimeout the asyncTimeout to set
     */
    public void setAsyncTimeout(Integer asyncTimeout) {
        this.asyncTimeout = asyncTimeout;
    }

    /**
     * @return the syncTimeout
     */
    public Integer getSyncTimeout() {
        return syncTimeout;
    }

    /**
     * @param syncTimeout the syncTimeout to set
     */
    public void setSyncTimeout(Integer syncTimeout) {
        this.syncTimeout = syncTimeout;
    }

    /**
     * @return the dataPollingInterval
     */
    public Integer getDataPollingInterval() {
        return dataPollingInterval;
    }

    /**
     * @param dataPollingInterval the dataPollingInterval to set
     */
    public void setDataPollingInterval(Integer dataPollingInterval) {
        this.dataPollingInterval = dataPollingInterval;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MyUplinkConfiguration [clientId=").append(clientId).append(", clientSecret=")
                .append(clientSecret).append(", asyncTimeout=").append(asyncTimeout).append(", syncTimeout=")
                .append(syncTimeout).append(", dataPollingInterval=").append(dataPollingInterval).append("]");
        return builder.toString();
    }
}
