/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holds status of particular features of the Air Purifier thing that can be changed by the user via commands
 *
 * @author Michał Boroński - Initial contribution
 * @Nullable
 *
 */
public class PhilipsAirPurifierWritableDataDTO {

    @SerializedName(PhilipsAirBindingConstants.FAN_MODE)
    @Expose
    private String fanSpeed;
    @SerializedName(PhilipsAirBindingConstants.POWER)
    @Expose
    private String power;
    @SerializedName(PhilipsAirBindingConstants.CHILD_LOCK)
    @Expose
    private Boolean childLock;
    @SerializedName(PhilipsAirBindingConstants.LED_LIGHT_LEVEL)
    @Expose
    private Integer lightLevel;
    @SerializedName(PhilipsAirBindingConstants.BUTTONS_LIGHT)
    @Expose
    private String buttons;
    @SerializedName(PhilipsAirBindingConstants.AUTO_TIMEOFF)
    @Expose
    private Integer timer;
    @SerializedName(PhilipsAirBindingConstants.MODE)
    @Expose
    private String mode;
    @SerializedName(PhilipsAirBindingConstants.AIR_QUALITY_NOTIFICATION_THRESHOLD)
    @Expose
    private Integer aqit;
    @SerializedName(PhilipsAirBindingConstants.DISPLAYED_INDEX)
    @Expose
    private String displayIndex;
    @SerializedName(PhilipsAirBindingConstants.HUMIDITY_SETPOINT)
    @Expose
    private Integer humiditySetpoint;
    @SerializedName(PhilipsAirBindingConstants.FUNCTION)
    @Expose
    @Nullable
    private String function;

    public String getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(String fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String pwr) {
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

    public String getButtons() {
        return buttons;
    }

    public void setButtons(String buttons) {
        this.buttons = buttons;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getAqit() {
        return aqit;
    }

    public void setAqit(int aqit) {
        this.aqit = aqit;
    }

    public String getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(String displayIndex) {
        this.displayIndex = displayIndex;
    }

    public int getHumiditySetpoint() {
        return humiditySetpoint;
    }

    public void setHumiditySetpoint(int humiditySetpoint) {
        this.humiditySetpoint = humiditySetpoint;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
