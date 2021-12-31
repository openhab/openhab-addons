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
package org.openhab.binding.sonnen.communication;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SonnenJsonDataDTO} is the Java class used to map the JSON
 * response to a Oven request.
 *
 * @author Christian Feininger - Initial contribution
 */
public class SonnenJsonDataDTO {
    @SerializedName("BatteryCharging")
    boolean batteryCharging;
    @SerializedName("BatteryDischarging")
    boolean batteryDischarging;
    @SerializedName("Consumption_W")
    int consumptionHouse;
    @SerializedName("GridFeedIn_W")
    int gridFeedIn;
    @SerializedName("Production_W")
    int solarProduction;
    @SerializedName("USOC")
    int batteryChargingLevel;
    @SerializedName("FlowConsumptionBattery")
    boolean flowConsumptionBattery;
    @SerializedName("FlowConsumptionGrid")
    boolean flowConsumptionGrid;
    @SerializedName("FlowConsumptionProduction")
    boolean flowConsumptionProduction;
    @SerializedName("FlowGridBattery")
    boolean flowGridBattery;
    @SerializedName("FlowProductionBattery")
    boolean flowProductionBattery;
    @SerializedName("FlowProductionGrid")
    boolean flowProductionGrid;

    /**
     * @return the batteryCharging
     */
    public boolean isBatteryCharging() {
        return batteryCharging;
    }

    /**
     * @return the batteryDischarging
     */
    public boolean isBatteryDischarging() {
        return batteryDischarging;
    }

    /**
     * @return the consumptionHouse
     */
    public int getConsumptionHouse() {
        return consumptionHouse;
    }

    /**
     * @return the gridFeedIn
     */
    public int getGridFeedIn() {
        return gridFeedIn;
    }

    /**
     * @return the solarProduction
     */
    public int getSolarProduction() {
        return solarProduction;
    }

    /**
     * @return the batteryChargingLevel
     */
    public int getBatteryChargingLevel() {
        return batteryChargingLevel;
    }

    /**
     * @return the flowConsumptionBattery
     */
    public boolean isFlowConsumptionBattery() {
        return flowConsumptionBattery;
    }

    /**
     * @return the flowConsumptionGrid
     */
    public boolean isFlowConsumptionGrid() {
        return flowConsumptionGrid;
    }

    /**
     * @return the flowConsumptionProduction
     */
    public boolean isFlowConsumptionProduction() {
        return flowConsumptionProduction;
    }

    /**
     * @return the flowGridBattery
     */
    public boolean isFlowGridBattery() {
        return flowGridBattery;
    }

    /**
     * @return the flowProductionBattery
     */
    public boolean isFlowProductionBattery() {
        return flowProductionBattery;
    }

    /**
     * @return the flowProductionGrid
     */
    public boolean isFlowProductionGrid() {
        return flowProductionGrid;
    }

    /***
     * JSON response
     *
     * @return JSON response as object
     */
    public SonnenJsonDataDTO getResponse() {
        return this;
    }
}
