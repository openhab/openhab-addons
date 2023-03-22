/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nibeuplink.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Bean holding configuration data according to bridge.xml
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class NibeUplinkConfiguration {

    private @Nullable String user;
    private @Nullable String password;
    private @Nullable String nibeId;

    private Integer asyncTimeout = 120;
    private Integer syncTimeout = 120;
    private Integer pollingInterval = 60;
    private Integer houseKeepingInterval = 3600;

    public @Nullable String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public @Nullable String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public @Nullable String getNibeId() {
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

    public Integer getHouseKeepingInterval() {
        return houseKeepingInterval;
    }

    public void setHouseKeepingInterval(Integer houseKeepingInterval) {
        this.houseKeepingInterval = houseKeepingInterval;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ user=" + getUser() + ", password=" + getPassword() + ", nibeId="
                + getNibeId() + ", pollingInterval=" + getPollingInterval() + ", houseKeepingInterval="
                + getHouseKeepingInterval() + ", asyncTimeout=" + getAsyncTimeout() + ", syncTimeout="
                + getSyncTimeout() + "}";
    }
}
