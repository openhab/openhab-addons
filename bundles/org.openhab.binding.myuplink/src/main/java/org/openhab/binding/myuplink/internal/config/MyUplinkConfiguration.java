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

    private int dataPollingInterval = 60;
    private static final int ASYNC_TIMEOUT = 120;
    private static final int SYNC_TIMEOUT = 120;

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
        return ASYNC_TIMEOUT;
    }

    /**
     * @return the syncTimeout
     */
    public Integer getSyncTimeout() {
        return SYNC_TIMEOUT;
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
                .append(clientSecret).append(", dataPollingInterval=").append(dataPollingInterval).append("]");
        return builder.toString();
    }
}
