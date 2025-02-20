/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sonnen.internal.communication;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SonnenJsonDataDTO} is the Java class used to map the JSON
 * response to an Object.
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
    int gridValue;
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
    @SerializedName("Pac_total_W")
    int batteryCurrent;
    @SerializedName("EM_OperatingMode")
    String emoperatingMode;
    @SerializedName("OperatingMode")
    private int operatingMode;

    /**
     * @return the batteryCurrent
     */
    public int getbatteryCurrent() {
        return batteryCurrent;
    }

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
     * @return the gridValue. Negative value indicates receiving from Grid. Positive value indicates feeding to Grid.
     */
    public int getGridValue() {
        return gridValue;
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

    /**
     * @return the em_operatingMode
     */
    public String emgetOperationMode() {
        return emoperatingMode;
    }

    /**
     * @return the operatingMode
     */
    public boolean isInAutomaticMode() {
        // 2 for automatic mode
        if (operatingMode == 2) {
            return true;
            // 1 for MANUAL mode
        } else if (operatingMode == 1) {
            return false;
        }
        // in case of Serialization problem return true, as automatic is the normal state of the battery on most
        // cases.
        return true;
    }
}
