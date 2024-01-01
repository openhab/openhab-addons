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
package org.openhab.binding.konnected.internal.gson;

import static org.openhab.binding.konnected.internal.KonnectedBindingConstants.*;

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
    private String zone;
    private String temp;
    private String humi;
    private Integer state;
    @SerializedName("Auth_Token")
    private String authToken;
    private Integer momentary;
    private Integer pause;
    private Integer times;
    @SerializedName("poll_interval")
    private Integer pollInterval;
    private String addr;

    public Integer getPin() {
        return pin;
    }

    public void setPin(Integer pin) {
        this.pin = pin;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer setState) {
        this.state = setState;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getMomentary() {
        return momentary;
    }

    public void setMomentary(Integer setMomentary) {
        this.momentary = setMomentary;
    }

    public Integer getPause() {
        return pause;
    }

    public void setPause(Integer setPause) {
        this.pause = setPause;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer setTimes) {
        this.times = setTimes;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String setAddr) {
        this.addr = setAddr;
    }

    public void setZone(String thingId, String zone) {
        if (isEsp8266(thingId)) {
            setPin(ESP8266_ZONE_TO_PIN.get(zone));
        } else {
            setZone(zone);
        }
    }

    public String getZone(String thingId) {
        return isEsp8266(thingId) ? ESP8266_PIN_TO_ZONE.get(pin) : getZone();
    }

    private boolean isEsp8266(String thingId) {
        return WIFI_MODULE.equals(thingId);
    }
}
