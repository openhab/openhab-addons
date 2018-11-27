/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal.gson;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link KonnectedModuleGson} is responsible to hold
 * data that models pin information which can be sent to a Konnected Module
 *
 * @author Zachary Christiansen - Initial contribution
 *
 */
public class KonnectedModuleGson {

    private Integer pin;
    private String temp;
    private String humi;
    private String state;
    @SerializedName("Auth_Token")
    private String authToken;
    private String momentary;
    private String pause;
    private String times;
    @SerializedName("poll_interval")
    private Integer pollInterval;
    private String addr;

    public Integer getPin() {
        return pin;
    }

    public void setPin(Integer setPin) {
        this.pin = setPin;
    }

    public Integer getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Integer setPollInterval) {
        this.pollInterval = setPollInterval;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String setTemp) {
        this.temp = setTemp;
    }

    public String getHumi() {
        return humi;
    }

    public void setHumi(String setHumi) {
        this.humi = setHumi;
    }

    public String getState() {
        return state;
    }

    public void setState(String setState) {
        this.state = setState;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getMomentary() {
        return momentary;
    }

    public void setMomentary(String setMomentary) {
        this.momentary = setMomentary;
    }

    public String getPause() {
        return pause;
    }

    public void setPause(String setPause) {
        this.pause = setPause;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String setTimes) {
        this.times = setTimes;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String setAddr) {
        this.addr = setAddr;
    }
}
