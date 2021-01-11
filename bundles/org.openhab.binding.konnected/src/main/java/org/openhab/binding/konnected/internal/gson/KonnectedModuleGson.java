/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Arrays;

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
    private String state;
    @SerializedName("Auth_Token")
    private String authToken;
    private String momentary;
    private String pause;
    private String times;
    @SerializedName("poll_interval")
    private Integer pollInterval;
    private String addr;

    private Integer getPin() {

        return Arrays.asList(PIN_TO_ZONE).indexOf(pin);
    }

    private void setPin(Integer setPin) {

        this.pin = Arrays.asList(PIN_TO_ZONE).get(setPin);
    }

    private Integer getZone() {

        switch (zone) {
            case PRO_MODULE_ALARM1:
                return Integer.decode("13");

            case PRO_MODULE_OUT1:
                return Integer.decode("14");

            case PRO_MODULE_ALARM2_OUT2:
                return Integer.decode("15");
            default:
                return Integer.decode(zone);
        }
    }

    private void setZone(Integer setZone) {

        switch (setZone) {
            case 13:
                this.zone = PRO_MODULE_ALARM1;
                break;
            case 14:
                this.zone = PRO_MODULE_OUT1;
                break;
            case 15:
                this.zone = PRO_MODULE_ALARM2_OUT2;
                break;
            default:
                this.zone = Integer.toString(setZone);

        }

        ;
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

    public void setPinZone(String ThingID, Integer sentZone) {
        switch (ThingID) {
            case PRO_MODULE:
                this.setZone(sentZone);
                break;
            case WIFI_MODULE:
                this.setPin(sentZone);
                break;
        }
    }

    public Integer getPinZone(String ThingID) {

        switch (ThingID) {
            case PRO_MODULE:
                return this.getZone();

            default:
                return this.getPin();

        }
    }
}
