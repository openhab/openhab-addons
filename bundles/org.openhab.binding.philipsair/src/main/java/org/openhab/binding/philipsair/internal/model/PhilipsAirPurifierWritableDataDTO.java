/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * Holds status of particular features of the Air Purifier thing that can be changed by the user via commands
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Update property definitions
 */
public class PhilipsAirPurifierWritableDataDTO {

    @SerializedName("om")
    @Expose
    private @Nullable String fanSpeed;
    @SerializedName("pwr")
    @Expose
    private @Nullable String power;
    @SerializedName("cl")
    @Expose
    private @Nullable Boolean childLock;
    @SerializedName("aqil")
    @Expose
    private @Nullable Integer lightLevel;
    @SerializedName("uil")
    @Expose
    private @Nullable String buttons;
    @SerializedName("dt")
    @Expose
    private @Nullable Integer timer;
    @SerializedName("mode")
    @Expose
    private @Nullable String mode;
    @SerializedName("aqit")
    @Expose
    private @Nullable Integer aqit;
    @SerializedName("ddp")
    @Expose
    private @Nullable String displayIndex;
    @SerializedName("rhset")
    @Expose
    private @Nullable Integer humiditySetpoint;
    @SerializedName("func")
    @Expose
    private @Nullable String function;

    public @Nullable String getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(String fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public @Nullable String getPower() {
        return power;
    }

    public void setPower(String pwr) {
        this.power = pwr;
    }

    public @Nullable Boolean getChildLock() {
        return childLock;
    }

    public void setChildLock(boolean childLock) {
        this.childLock = childLock;
    }

    public @Nullable Integer getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
    }

    public @Nullable String getButtons() {
        return buttons;
    }

    public void setButtons(String buttons) {
        this.buttons = buttons;
    }

    public @Nullable Integer getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public @Nullable String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public @Nullable Integer getAqit() {
        return aqit;
    }

    public void setAqit(int aqit) {
        this.aqit = aqit;
    }

    public @Nullable String getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(String displayIndex) {
        this.displayIndex = displayIndex;
    }

    public @Nullable Integer getHumiditySetpoint() {
        return humiditySetpoint;
    }

    public void setHumiditySetpoint(int humiditySetpoint) {
        this.humiditySetpoint = humiditySetpoint;
    }

    public @Nullable String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
