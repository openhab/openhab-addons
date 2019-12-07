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
package org.openhab.binding.philipsair.internal.model;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holds status of particular features of the Air Purifier thing
 *
 * @author Michał Boroński - Initial contribution
 *@Nullable
    
 */
public class PhilipsAirPurifierData {
    @SerializedName("om")
    @Expose
    private String fanSpeed;
    @SerializedName("pwr")
    @Expose
    private int power;
    @SerializedName("cl")
    @Expose
    private boolean childLock;
    @SerializedName("aqil")
    @Expose
    private int lightLevel;
    @SerializedName("uil")
    @Expose
    private int buttons;
    @SerializedName("dt")
    @Expose
    private int timer;
    @SerializedName("dtrs")
    @Expose
    private int timerLeft;
    @SerializedName("mode")
    @Expose
    private String mode;
    @SerializedName("pm25")
    @Expose
    private int pm25;
    @SerializedName("iaql")
    @Expose
    private int allergenLevel;
    @SerializedName("aqit")
    @Expose
    private int aqit;
    @SerializedName("ddp")
    @Expose
    private int displayIndex;
    @SerializedName("err")
    @Expose
    private int errorCode;

    @SerializedName("rh")
    @Expose
    private int humidity;

    @SerializedName("rhset")
    @Expose
    private int humiditySetpoint;

    @SerializedName("temp")
    @Expose
    private int temperature;

    @SerializedName("func")
    @Expose
    @Nullable
    private String function;

    @SerializedName("wl")
    @Expose
    private int waterLevel;

    public String getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(String fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int pwr) {
        this.power = pwr;
    }

    public boolean getChildLock() {
        return childLock;
    }

    public void setChildLock(boolean childLock) {
        this.childLock = childLock;
    }

    public int getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
    }

    public int getButtons() {
        return buttons;
    }

    public void setButtons(int buttons) {
        this.buttons = buttons;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getTimerLeft() {
        return timerLeft;
    }

    public void setTimerLeft(int timerLeft) {
        this.timerLeft = timerLeft;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getPm25() {
        return pm25;
    }

    public void setPm25(int pm25) {
        this.pm25 = pm25;
    }

    public int getAllergenLevel() {
        return allergenLevel;
    }

    public void setAllergenLevel(int allergenLevel) {
        this.allergenLevel = allergenLevel;
    }

    public int getAqit() {
        return aqit;
    }

    public void setAqit(int aqit) {
        this.aqit = aqit;
    }

    public int getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(int displayIndex) {
        this.displayIndex = displayIndex;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getHumiditySetpoint() {
        return humiditySetpoint;
    }

    public void setHumiditySetpoint(int humiditySetpoint) {
        this.humiditySetpoint = humiditySetpoint;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }
}