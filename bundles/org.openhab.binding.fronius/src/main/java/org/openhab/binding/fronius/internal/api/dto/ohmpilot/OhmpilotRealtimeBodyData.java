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
package org.openhab.binding.fronius.internal.api.dto.ohmpilot;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OhmpilotRealtimeBodyData} is responsible for storing
 * the "Data" node of the {@link OhmpilotRealtimeBody}.
 *
 * @author Hannes Spenger - Initial contribution
 */
public class OhmpilotRealtimeBodyData {
    @SerializedName("Details")
    private OhmpilotRealtimeDetails details;
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

    public OhmpilotRealtimeDetails getDetails() {
        if (details == null) {
            details = new OhmpilotRealtimeDetails();
        }
        return details;
    }

    public double getEnergyRealWACSumConsumed() {
        return energyRealWACSumConsumed;
    }

    public double getPowerPACSum() {
        return powerPACSum;
    }

    public double getTemperatureChannel1() {
        return temperatureChannel1;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getStateCode() {
        return stateCode;
    }
}
