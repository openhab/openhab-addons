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
package org.openhab.binding.radiothermostat.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RadioThermostatJsonResponse} is responsible for storing
 * the data from the thermostat 'tstat' JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatJsonResponse {

    @SerializedName("temp")
    private Double temperature = new Double(0);

    @SerializedName("tmode")
    private @Nullable Integer mode;

    @SerializedName("fmode")
    private @Nullable Integer fanMode;

    @SerializedName("program_mode")
    private @Nullable Integer programMode;

    @SerializedName("t_heat")
    private Integer heatTarget = 0;

    @SerializedName("t_cool")
    private Integer coolTarget = 0;

    @SerializedName("override")
    private @Nullable Integer override;

    @SerializedName("hold")
    private @Nullable Integer hold;

    @SerializedName("tstate")
    private @Nullable Integer status;

    @SerializedName("fstate")
    private @Nullable Integer fanStatus;

    @SerializedName("time")
    private @Nullable RadioThermostatJsonTime time;

    public RadioThermostatJsonResponse() {
    }

    public Double getTemperature() {
        return temperature;
    }

    public @Nullable Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public @Nullable Integer getFanMode() {
        return fanMode;
    }

    public void setFanMode(Integer fanMode) {
        this.fanMode = fanMode;
    }

    public @Nullable Integer getProgramMode() {
        return programMode;
    }

    public void setProgramMode(Integer programMode) {
        this.programMode = programMode;
    }

    public @Nullable Integer getHeatTarget() {
        return heatTarget;
    }

    public void setHeatTarget(Integer heatTarget) {
        this.heatTarget = heatTarget;
    }

    public @Nullable Integer getCoolTarget() {
        return coolTarget;
    }

    public void setCoolTarget(Integer coolTarget) {
        this.coolTarget = coolTarget;
    }

    public @Nullable Integer getOverride() {
        return override;
    }

    public @Nullable Integer getHold() {
        return hold;
    }

    public void setHold(Integer hold) {
        this.hold = hold;
    }

    public @Nullable Integer getStatus() {
        return status;
    }

    public @Nullable Integer getFanStatus() {
        return fanStatus;
    }

    /**
     * Determine if we are in heat mode or cool mode and return that temp value
     *
     * @return {Integer}
     */
    public Integer getSetpoint() {
        if (mode == 1) {
            return heatTarget;
        } else if (mode == 2) {
            return coolTarget;
        } else {
            return 0;
        }
    }

    /**
     * Receives "time" node from the JSON response
     *
     * @return {RadioThermostatJsonTime}
     */
    public @Nullable RadioThermostatJsonTime getTime() {
        return time;
    }

}
