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
package org.openhab.binding.fronius.internal.api.dto.powerflow;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PowerFlowRealtimeSite} is responsible for storing
 * the "Site" node of the {@link PowerFlowRealtimeBodyData}.
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class PowerFlowRealtimeSite {
    @SerializedName("Mode")
    private String mode;
    @SerializedName("P_Grid")
    private double pgrid;
    @SerializedName("P_Load")
    private double pload;
    @SerializedName("P_Akku")
    private double pakku;
    @SerializedName("P_PV")
    private double ppv;
    @SerializedName("rel_SelfConsumption")
    private double relSelfConsumption;
    @SerializedName("rel_Autonomy")
    private double relAutonomy;
    @SerializedName("E_Day")
    private double eDay;
    @SerializedName("E_Year")
    private double eYear;
    @SerializedName("E_Total")
    private double eTotal;
    @SerializedName("Meter_Location")
    private String meterLocation;

    public String getMode() {
        if (mode == null) {
            mode = "";
        }
        return mode;
    }

    public double getPgrid() {
        return pgrid;
    }

    public double getPload() {
        return pload;
    }

    public double getPakku() {
        return pakku;
    }

    public double getPpv() {
        return ppv;
    }

    public double getRelSelfConsumption() {
        return relSelfConsumption;
    }

    public double getRelAutonomy() {
        return relAutonomy;
    }

    public double geteDay() {
        return eDay;
    }

    public double geteYear() {
        return eYear;
    }

    public double geteTotal() {
        return eTotal;
    }

    public String getMeterLocation() {
        return meterLocation;
    }
}
