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
 * The {@link PowerFlowRealtimeSite} is responsible for storing
 * the "site" node
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

    public void setMode(String mode) {
        this.mode = mode;
    }

    public double getPgrid() {
        return pgrid;
    }

    public void setPgrid(double pgrid) {
        this.pgrid = pgrid;
    }

    public double getPload() {
        return pload;
    }

    public void setPload(double pload) {
        this.pload = pload;
    }

    public double getPakku() {
        return pakku;
    }

    public void setPakku(double pakku) {
        this.pakku = pakku;
    }

    public double getPpv() {
        return ppv;
    }

    public void setPpv(double ppv) {
        this.ppv = ppv;
    }

    public double getRelSelfConsumption() {
        return relSelfConsumption;
    }

    public void setRelSelfConsumption(double relSelfConsumption) {
        this.relSelfConsumption = relSelfConsumption;
    }

    public double getRelAutonomy() {
        return relAutonomy;
    }

    public void setRelAutonomy(double relAutonomy) {
        this.relAutonomy = relAutonomy;
    }

    public double geteDay() {
        return eDay;
    }

    public void seteDay(double eDay) {
        this.eDay = eDay;
    }

    public double geteYear() {
        return eYear;
    }

    public void seteYear(double eYear) {
        this.eYear = eYear;
    }

    public double geteTotal() {
        return eTotal;
    }

    public void seteTotal(double eTotal) {
        this.eTotal = eTotal;
    }

    public String getMeterLocation() {
        return meterLocation;
    }

    public void setMeterLocation(String meterLocation) {
        this.meterLocation = meterLocation;
    }
}
