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
package org.openhab.binding.easee.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EaseeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public class EaseeConfiguration {

    private String username = "";
    private String password = "";
    private String siteId = "";

    private Integer asyncTimeout = 120;
    private Integer syncTimeout = 120;
    private Integer dataPollingInterval = 120;

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

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
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

    public Integer getDataPollingInterval() {
        return dataPollingInterval;
    }

    public void setDataPollingInterval(Integer dataPollingInterval) {
        this.dataPollingInterval = dataPollingInterval;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EaseeConfiguration [username=").append(username).append(", password=").append(password)
                .append(", siteId=").append(siteId).append(", asyncTimeout=").append(asyncTimeout)
                .append(", syncTimeout=").append(syncTimeout).append(", dataPollingInterval=")
                .append(dataPollingInterval).append("]");
        return builder.toString();
    }
}
