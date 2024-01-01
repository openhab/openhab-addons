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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OhmpilotRealtimeBodyDataDTO} is responsible for storing
 * the "data" node of the JSON response
 *
 * @author Hannes Spenger - Initial contribution
 */
public class OhmpilotRealtimeBodyDataDTO {
    @SerializedName("Details")
    private OhmpilotRealtimeDetailsDTO details;
    @SerializedName("EnergyReal_WAC_Sum_Consumed")
    private double energyRealWACSumConsumed;
    @SerializedName("PowerReal_PAC_Sum")
    private double powerPACSum;
    @SerializedName("Temperature_Channel_1")
    private double temperatureChannel1;
    @SerializedName("CodeOfError")
    private int errorCode;
    @SerializedName("CodeOfState")
    private int stateCode;

    public OhmpilotRealtimeDetailsDTO getDetails() {
        if (details == null) {
            details = new OhmpilotRealtimeDetailsDTO();
        }
        return details;
    }

    public void setDetails(OhmpilotRealtimeDetailsDTO details) {
        this.details = details;
    }

    public double getEnergyRealWACSumConsumed() {
        return energyRealWACSumConsumed;
    }

    public void setEnergyRealWACSumConsumed(double energyRealWACSumConsumed) {
        this.energyRealWACSumConsumed = energyRealWACSumConsumed;
    }

    public double getPowerPACSum() {
        return powerPACSum;
    }

    public void setPowerPACSum(double powerPACSum) {
        this.powerPACSum = powerPACSum;
    }

    public double getTemperatureChannel1() {
        return temperatureChannel1;
    }

    public void setTemperatureChannel1(double temperatureChannel1) {
        this.temperatureChannel1 = temperatureChannel1;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }
}
