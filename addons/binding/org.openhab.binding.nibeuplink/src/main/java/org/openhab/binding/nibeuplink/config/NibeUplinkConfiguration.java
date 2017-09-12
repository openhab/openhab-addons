/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.config;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Bean holding configuration data according to bridge.xml
 *
 * @author afriese - Initial contribution
 *
 */
public class NibeUplinkConfiguration {

    private String user;
    private String password;
    private String nibeId;

    private Integer asyncTimeout = 120;
    private Integer syncTimeout = 120;
    private Integer pollingInterval = 60;
    private Integer houseKeepingInterval = 3600;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNibeId() {
        return nibeId;
    }

    public void setNibeId(String nibeId) {
        this.nibeId = nibeId;
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

    public Integer getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public final Integer getHouseKeepingInterval() {
        return houseKeepingInterval;
    }

    public final void setHouseKeepingInterval(Integer houseKeepingInterval) {
        this.houseKeepingInterval = houseKeepingInterval;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("user", getUser()).append("password", getPassword())
                .append("nibeId", getNibeId()).append("pollingInterval", getPollingInterval())
                .append("houseKeepingInterval", getHouseKeepingInterval()).append("asyncTimeout", getAsyncTimeout())
                .append("syncTimeout", getSyncTimeout()).toString();
    }
}
