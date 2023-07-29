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
package org.openhab.binding.radiothermostat.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RadioThermostatTstatDTO} is responsible for storing
 * the data from the thermostat 'tstat' JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class RadioThermostatTstatDTO {

    @SerializedName("temp")
    private Double temperature;

    @SerializedName("tmode")
    private Integer mode;

    @SerializedName("fmode")
    private Integer fanMode;

    @SerializedName("program_mode")
    private Integer programMode;

    @SerializedName("t_heat")
    private Double heatTarget;

    @SerializedName("t_cool")
    private Double coolTarget;

    @SerializedName("override")
    private Integer override;

    @SerializedName("hold")
    private Integer hold;

    @SerializedName("tstate")
    private Integer status;

    @SerializedName("fstate")
    private Integer fanStatus;

    @SerializedName("time")
    private RadioThermostatTimeDTO time;

    public RadioThermostatTstatDTO() {
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public Integer getFanMode() {
        return fanMode;
    }

    public void setFanMode(Integer fanMode) {
        this.fanMode = fanMode;
    }

    public Integer getProgramMode() {
        return programMode;
    }

    public void setProgramMode(Integer programMode) {
        this.programMode = programMode;
    }

    public Double getHeatTarget() {
        return heatTarget;
    }

    public void setHeatTarget(Double heatTarget) {
        this.heatTarget = heatTarget;
    }

    public Double getCoolTarget() {
        return coolTarget;
    }

    public void setCoolTarget(Double coolTarget) {
        this.coolTarget = coolTarget;
    }

    public Integer getOverride() {
        return override;
    }

    public void setOverride(Integer override) {
        this.override = override;
    }

    public Integer getHold() {
        return hold;
    }

    public void setHold(Integer hold) {
        this.hold = hold;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getFanStatus() {
        return fanStatus;
    }

    /**
     * Determine if we are in heat mode or cool mode and return that temp value
     *
     * @return {Integer}
     */
    public Integer getSetpoint() {
        if (mode == 1) {
            return heatTarget.intValue();
        } else if (mode == 2) {
            return coolTarget.intValue();
        } else {
            return 0;
        }
    }

    /**
     * Receives "time" node from the JSON response
     *
     * @return {RadioThermostatJsonTime}
     */
    public RadioThermostatTimeDTO getTime() {
        return time;
    }
}
