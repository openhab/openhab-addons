/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
 * @author Alexander Friese - initial contribution
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

    private Integer customChannel01;
    private Integer customChannel02;
    private Integer customChannel03;
    private Integer customChannel04;
    private Integer customChannel05;
    private Integer customChannel06;
    private Integer customChannel07;
    private Integer customChannel08;

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

    public final Integer getCustomChannel01() {
        return customChannel01;
    }

    public final void setCustomChannel01(Integer customChannel01) {
        this.customChannel01 = customChannel01;
    }

    public final Integer getCustomChannel02() {
        return customChannel02;
    }

    public final void setCustomChannel02(Integer customChannel02) {
        this.customChannel02 = customChannel02;
    }

    public final Integer getCustomChannel03() {
        return customChannel03;
    }

    public final void setCustomChannel03(Integer customChannel03) {
        this.customChannel03 = customChannel03;
    }

    public final Integer getCustomChannel04() {
        return customChannel04;
    }

    public final void setCustomChannel04(Integer customChannel04) {
        this.customChannel04 = customChannel04;
    }

    public final Integer getCustomChannel05() {
        return customChannel05;
    }

    public final void setCustomChannel05(Integer customChannel05) {
        this.customChannel05 = customChannel05;
    }

    public final Integer getCustomChannel06() {
        return customChannel06;
    }

    public final void setCustomChannel06(Integer customChannel06) {
        this.customChannel06 = customChannel06;
    }

    public final Integer getCustomChannel07() {
        return customChannel07;
    }

    public final void setCustomChannel07(Integer customChannel07) {
        this.customChannel07 = customChannel07;
    }

    public final Integer getCustomChannel08() {
        return customChannel08;
    }

    public final void setCustomChannel08(Integer customChannel08) {
        this.customChannel08 = customChannel08;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("user", getUser()).append("password", getPassword())
                .append("nibeId", getNibeId()).append("pollingInterval", getPollingInterval())
                .append("houseKeepingInterval", getHouseKeepingInterval()).append("asyncTimeout", getAsyncTimeout())
                .append("syncTimeout", getSyncTimeout()).toString();
    }
}
