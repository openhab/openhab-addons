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
package org.openhab.binding.tasmotaplug.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TasmotaDTO} is responsible for storing
 * all of the JSON data objects retrieved from the Tasmota plug
 *
 * @author Michael Lobstein - Initial contribution
 */
public class TasmotaDTO {

    @SerializedName("StatusSNS")
    private TasmotaStatus status = new TasmotaStatus();

    public TasmotaDTO() {
    }

    public TasmotaStatus getStatus() {
        return status;
    }

    public void setStatus(TasmotaStatus status) {
        this.status = status;
    }

    public class TasmotaStatus {

        @SerializedName("Time")
        private String time = "";

        @SerializedName("ENERGY")
        private Energy energy = new Energy();

        public TasmotaStatus() {
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Energy getEnergy() {
            return energy;
        }

        public void setEnergy(Energy energy) {
            this.energy = energy;
        }
    }

    public class Energy {

        @SerializedName("Voltage")
        private Integer voltage = 0;

        @SerializedName("Current")
        private Double current = 0d;

        @SerializedName("Power")
        private Integer activePower = 0;

        @SerializedName("ApparentPower")
        private Integer apparentPower = 0;

        @SerializedName("ReactivePower")
        private Integer reactivePower = 0;

        @SerializedName("Factor")
        private Double powerFactor = 0d;

        @SerializedName("Today")
        private Double energyToday = 0d;

        @SerializedName("Yesterday")
        private Double energyYesterday = 0d;

        @SerializedName("Total")
        private Double energyTotal = 0d;

        @SerializedName("TotalStartTime")
        private String energyTotalStart = "";

        public Energy() {
        }

        public Integer getVoltage() {
            return voltage;
        }

        public void setVoltage(Integer voltage) {
            this.voltage = voltage;
        }

        public Double getCurrent() {
            return current;
        }

        public void setCurrent(Double current) {
            this.current = current;
        }

        public Integer getActivePower() {
            return activePower;
        }

        public void setActivePower(Integer activePower) {
            this.activePower = activePower;
        }

        public Integer getApparentPower() {
            return apparentPower;
        }

        public void setApparentPower(Integer apparentPower) {
            this.apparentPower = apparentPower;
        }

        public Integer getReactivePower() {
            return reactivePower;
        }

        public void setReactivePower(Integer reactivePower) {
            this.reactivePower = reactivePower;
        }

        public Double getPowerFactor() {
            return powerFactor;
        }

        public void setPowerFactor(Double powerFactor) {
            this.powerFactor = powerFactor;
        }

        public Double getEnergyToday() {
            return energyToday;
        }

        public void setEnergyToday(Double energyToday) {
            this.energyToday = energyToday;
        }

        public Double getEnergyYesterday() {
            return energyYesterday;
        }

        public void setEnergyYesterday(Double energyYesterday) {
            this.energyYesterday = energyYesterday;
        }

        public Double getEnergyTotal() {
            return energyTotal;
        }

        public void setEnergyTotal(Double energyTotal) {
            this.energyTotal = energyTotal;
        }

        public String getEnergyTotalStart() {
            return energyTotalStart;
        }

        public void setEnergyTotalStart(String energyTotalStart) {
            this.energyTotalStart = energyTotalStart;
        }
    }
}
