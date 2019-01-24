/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.apache.commons.lang.builder.ToStringBuilder;
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

    private Integer customChannel01 = 0;
    private Integer customChannel02 = 0;
    private Integer customChannel03 = 0;
    private Integer customChannel04 = 0;
    private Integer customChannel05 = 0;
    private Integer customChannel06 = 0;
    private Integer customChannel07 = 0;
    private Integer customChannel08 = 0;

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

    public Integer getCustomChannel01() {
        return customChannel01;
    }

    public void setCustomChannel01(Integer customChannel01) {
        this.customChannel01 = customChannel01;
    }

    public Integer getCustomChannel02() {
        return customChannel02;
    }

    public void setCustomChannel02(Integer customChannel02) {
        this.customChannel02 = customChannel02;
    }

    public Integer getCustomChannel03() {
        return customChannel03;
    }

    public void setCustomChannel03(Integer customChannel03) {
        this.customChannel03 = customChannel03;
    }

    public Integer getCustomChannel04() {
        return customChannel04;
    }

    public void setCustomChannel04(Integer customChannel04) {
        this.customChannel04 = customChannel04;
    }

    public Integer getCustomChannel05() {
        return customChannel05;
    }

    public void setCustomChannel05(Integer customChannel05) {
        this.customChannel05 = customChannel05;
    }

    public Integer getCustomChannel06() {
        return customChannel06;
    }

    public void setCustomChannel06(Integer customChannel06) {
        this.customChannel06 = customChannel06;
    }

    public Integer getCustomChannel07() {
        return customChannel07;
    }

    public void setCustomChannel07(Integer customChannel07) {
        this.customChannel07 = customChannel07;
    }

    public Integer getCustomChannel08() {
        return customChannel08;
    }

    public void setCustomChannel08(Integer customChannel08) {
        this.customChannel08 = customChannel08;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("user", getUser()).append("password", getPassword())
                .append("nibeId", getNibeId()).append("pollingInterval", getPollingInterval())
                .append("houseKeepingInterval", getHouseKeepingInterval()).append("asyncTimeout", getAsyncTimeout())
                .append("syncTimeout", getSyncTimeout()).append("customChannel01", getCustomChannel01())
                .append("customChannel02", getCustomChannel02()).append("customChannel03", getCustomChannel03())
                .append("customChannel04", getCustomChannel04()).append("customChannel05", getCustomChannel05())
                .append("customChannel06", getCustomChannel06()).append("customChannel07", getCustomChannel07())
                .append("customChannel08", getCustomChannel08()).toString();
    }
}
