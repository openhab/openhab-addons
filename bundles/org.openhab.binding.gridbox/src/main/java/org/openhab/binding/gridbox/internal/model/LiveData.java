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
package org.openhab.binding.gridbox.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * {@link LiveData} is a gson-mapped class representing the response of a call to the live data endpoint of the GridBox
 * API
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class LiveData {

    private static final double ZERO_THRESHOLD = 1e-15;

    @SerializedName("batteries")
    @Expose
    private List<Battery> batteries = new ArrayList<>();

    @SerializedName("battery")
    @Expose
    @Nullable
    private BatterySummary battery;

    @SerializedName("consumption")
    @Expose
    private long consumption;

    @SerializedName("directConsumption")
    @Expose
    private long directConsumption;

    @SerializedName("directConsumptionEV")
    @Expose
    private long directConsumptionEV;

    @SerializedName("directConsumptionHeatPump")
    @Expose
    private long directConsumptionHeatPump;

    @SerializedName("directConsumptionHeater")
    @Expose
    private long directConsumptionHeater;

    @SerializedName("directConsumptionHousehold")
    @Expose
    private long directConsumptionHousehold;

    @SerializedName("directConsumptionRate")
    @Expose
    private double directConsumptionRate;

    @SerializedName("evChargingStation")
    @Expose
    @Nullable
    private EvChargingStationSummary evChargingStation;

    @SerializedName("evChargingStations")
    @Expose
    private List<EvChargingStation> evChargingStations = new ArrayList<>();

    @SerializedName("grid")
    @Expose
    private long grid;

    @SerializedName("gridMeterReadingNegative")

    @Expose
    private long gridMeterReadingNegative;

    @SerializedName("gridMeterReadingPositive")
    @Expose
    private long gridMeterReadingPositive;

    @SerializedName("heatPump")
    @Expose
    private long heatPump;

    @SerializedName("heatPumps")
    @Expose
    private List<HeatPump> heatPumps = new ArrayList<>();

    @SerializedName("l1CurtailmentPower")
    @Expose
    private long l1CurtailmentPower;

    @SerializedName("l2CurtailmentPower")
    @Expose
    private long l2CurtailmentPower;

    @SerializedName("l3CurtailmentPower")
    @Expose
    private long l3CurtailmentPower;

    @SerializedName("measuredAt")
    @Expose
    @Nullable
    private String measuredAt;

    @SerializedName("photovoltaic")
    @Expose
    private long photovoltaic;

    @SerializedName("production")
    @Expose
    private long production;

    @SerializedName("selfConsumption")
    @Expose
    private long selfConsumption;

    @SerializedName("selfConsumptionRate")
    @Expose
    private double selfConsumptionRate;

    @SerializedName("selfSufficiencyRate")
    @Expose
    private long selfSufficiencyRate;

    @SerializedName("selfSupply")
    @Expose
    private long selfSupply;

    @SerializedName("totalConsumption")
    @Expose
    private long totalConsumption;

    public List<Battery> getBatteries() {
        return batteries;
    }

    public void setBatteries(List<Battery> batteries) {
        this.batteries = batteries;
    }

    @Nullable
    public BatterySummary getBattery() {
        return battery;
    }

    public void setBattery(BatterySummary battery) {
        this.battery = battery;
    }

    public long getConsumption() {
        return consumption;
    }

    public void setConsumption(long consumption) {
        this.consumption = consumption;
    }

    public long getDirectConsumption() {
        return directConsumption;
    }

    public void setDirectConsumption(long directConsumption) {
        this.directConsumption = directConsumption;
    }

    public long getDirectConsumptionEV() {
        return directConsumptionEV;
    }

    public void setDirectConsumptionEV(long directConsumptionEV) {
        this.directConsumptionEV = directConsumptionEV;
    }

    public long getDirectConsumptionHeatPump() {
        return directConsumptionHeatPump;
    }

    public void setDirectConsumptionHeatPump(long directConsumptionHeatPump) {
        this.directConsumptionHeatPump = directConsumptionHeatPump;
    }

    public long getDirectConsumptionHeater() {
        return directConsumptionHeater;
    }

    public void setDirectConsumptionHeater(long directConsumptionHeater) {
        this.directConsumptionHeater = directConsumptionHeater;
    }

    public long getDirectConsumptionHousehold() {
        return directConsumptionHousehold;
    }

    public void setDirectConsumptionHousehold(long directConsumptionHousehold) {
        this.directConsumptionHousehold = directConsumptionHousehold;
    }

    public double getDirectConsumptionRate() {
        return directConsumptionRate;
    }

    public void setDirectConsumptionRate(double directConsumptionRate) {
        this.directConsumptionRate = directConsumptionRate;
    }

    @Nullable
    public EvChargingStationSummary getEvChargingStation() {
        return evChargingStation;
    }

    public void setEvChargingStation(EvChargingStationSummary evChargingStation) {
        this.evChargingStation = evChargingStation;
    }

    public List<EvChargingStation> getEvChargingStations() {
        return evChargingStations;
    }

    public void setEvChargingStations(List<EvChargingStation> evChargingStations) {
        this.evChargingStations = evChargingStations;
    }

    public long getGrid() {
        return grid;
    }

    public void setGrid(long grid) {
        this.grid = grid;
    }

    public long getGridMeterReadingNegative() {
        return gridMeterReadingNegative;
    }

    public void setGridMeterReadingNegative(long gridMeterReadingNegative) {
        this.gridMeterReadingNegative = gridMeterReadingNegative;
    }

    public long getGridMeterReadingPositive() {
        return gridMeterReadingPositive;
    }

    public void setGridMeterReadingPositive(long gridMeterReadingPositive) {
        this.gridMeterReadingPositive = gridMeterReadingPositive;
    }

    public long getHeatPump() {
        return heatPump;
    }

    public void setHeatPump(long heatPump) {
        this.heatPump = heatPump;
    }

    public List<HeatPump> getHeatPumps() {
        return heatPumps;
    }

    public void setHeatPumps(List<HeatPump> heatPumps) {
        this.heatPumps = heatPumps;
    }

    public long getL1CurtailmentPower() {
        return l1CurtailmentPower;
    }

    public void setL1CurtailmentPower(long l1CurtailmentPower) {
        this.l1CurtailmentPower = l1CurtailmentPower;
    }

    public long getL2CurtailmentPower() {
        return l2CurtailmentPower;
    }

    public void setL2CurtailmentPower(long l2CurtailmentPower) {
        this.l2CurtailmentPower = l2CurtailmentPower;
    }

    public long getL3CurtailmentPower() {
        return l3CurtailmentPower;
    }

    public void setL3CurtailmentPower(long l3CurtailmentPower) {
        this.l3CurtailmentPower = l3CurtailmentPower;
    }

    @Nullable
    public String getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(String measuredAt) {
        this.measuredAt = measuredAt;
    }

    public long getPhotovoltaic() {
        return photovoltaic;
    }

    public void setPhotovoltaic(long photovoltaic) {
        this.photovoltaic = photovoltaic;
    }

    public long getProduction() {
        return production;
    }

    public void setProduction(long production) {
        this.production = production;
    }

    public long getSelfConsumption() {
        return selfConsumption;
    }

    public void setSelfConsumption(long selfConsumption) {
        this.selfConsumption = selfConsumption;
    }

    public double getSelfConsumptionRate() {
        return selfConsumptionRate;
    }

    public void setSelfConsumptionRate(double selfConsumptionRate) {
        this.selfConsumptionRate = selfConsumptionRate;
    }

    public long getSelfSufficiencyRate() {
        return selfSufficiencyRate;
    }

    public void setSelfSufficiencyRate(long selfSufficiencyRate) {
        this.selfSufficiencyRate = selfSufficiencyRate;
    }

    public long getSelfSupply() {
        return selfSupply;
    }

    public void setSelfSupply(long selfSupply) {
        this.selfSupply = selfSupply;
    }

    public long getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(long totalConsumption) {
        this.totalConsumption = totalConsumption;
    }

    public boolean allValuesZero() {
        return isZero(consumption) && isZero(directConsumption) && isZero(directConsumptionEV)
                && isZero(directConsumptionHeatPump) && isZero(directConsumptionHeater)
                && isZero(directConsumptionHousehold) && isZero(directConsumptionRate) && isZero(grid)
                && isZero(gridMeterReadingNegative) && isZero(gridMeterReadingPositive) && isZero(heatPump)
                && isZero(l1CurtailmentPower) && isZero(l2CurtailmentPower) && isZero(l3CurtailmentPower)
                && isZero(photovoltaic) && isZero(production) && isZero(selfConsumption) && isZero(selfConsumptionRate)
                && isZero(selfSufficiencyRate) && isZero(selfSupply) && isZero(totalConsumption);
    }

    private boolean isZero(double value) {
        return Math.abs(value) < ZERO_THRESHOLD;
    }

    private boolean isZero(long value) {
        return value == 0;
    }

    @Override
    public String toString() {
        return "LiveData [batteries=" + batteries + ", battery=" + battery + ", consumption=" + consumption
                + ", directConsumption=" + directConsumption + ", directConsumptionEV=" + directConsumptionEV
                + ", directConsumptionHeatPump=" + directConsumptionHeatPump + ", directConsumptionHeater="
                + directConsumptionHeater + ", directConsumptionHousehold=" + directConsumptionHousehold
                + ", directConsumptionRate=" + directConsumptionRate + ", evChargingStation=" + evChargingStation
                + ", evChargingStations=" + evChargingStations + ", grid=" + grid + ", gridMeterReadingNegative="
                + gridMeterReadingNegative + ", gridMeterReadingPositive=" + gridMeterReadingPositive + ", heatPump="
                + heatPump + ", heatPumps=" + heatPumps + ", l1CurtailmentPower=" + l1CurtailmentPower
                + ", l2CurtailmentPower=" + l2CurtailmentPower + ", l3CurtailmentPower=" + l3CurtailmentPower
                + ", measuredAt=" + measuredAt + ", photovoltaic=" + photovoltaic + ", production=" + production
                + ", selfConsumption=" + selfConsumption + ", selfConsumptionRate=" + selfConsumptionRate
                + ", selfSufficiencyRate=" + selfSufficiencyRate + ", selfSupply=" + selfSupply + ", totalConsumption="
                + totalConsumption + "]";
    }
}
