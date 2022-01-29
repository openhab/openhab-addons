/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json object obtained from the P1 meter API
 *
 * @author Daniël van Os - Initial contribution
 *
 */
@NonNullByDefault
public class P1Payload {
    private int smrVersion = 0;
    private String meterModel = "";
    private String wifiSsid = "";
    private int wifiStrength = 0;

    @SerializedName("total_power_import_t1_kwh")
    private double totalEnergyImportT1Kwh;
    @SerializedName("total_power_import_t2_kwh")
    private double totalEnergyImportT2Kwh;
    @SerializedName("total_power_export_t1_kwh")
    private double totalEnergyExportT1Kwh;
    @SerializedName("total_power_export_t2_kwh")
    private double totalEnergyExportT2Kwh;

    private double activePowerW;
    private double activePowerL1W;
    private double activePowerL2W;
    private double activePowerL3W;
    private double totalGasM3;
    private long gasTimestamp = 0;

    /**
     * Getter for the smart meter version
     *
     * @return The most recent smart meter version obtained from the API
     */
    public int getSmrVersion() {
        return smrVersion;
    }

    /**
     * Setter for the smart meter version
     *
     * @param smrVersion The smart meter version to set
     */
    public void setSmrVersion(int smrVersion) {
        this.smrVersion = smrVersion;
    }

    /**
     * Getter for the meter model
     *
     * @return meter model
     */
    public String getMeterModel() {
        return meterModel;
    }

    /**
     * Setter for the meter model
     *
     * @param meterModel meter model
     */
    public void setMeterModel(String meterModel) {
        this.meterModel = meterModel;
    }

    /**
     * Getter for the meter's wifi ssid
     *
     * @return the meter's wifi sid
     */
    public String getWifiSsid() {
        return wifiSsid;
    }

    /**
     * Setter for the wifi ssid
     *
     * @param wifiSsid wifi ssid
     */
    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid;
    }

    /**
     * Getter for the wifi rssi
     *
     * @return wifi rssi
     */
    public int getWifiStrength() {
        return wifiStrength;
    }

    /**
     * Setter for the wifi rssi
     *
     * @param wifiStrength wifi rssi
     */
    public void setWifiStrength(int wifiStrength) {
        this.wifiStrength = wifiStrength;
    }

    /**
     * Getter for the total imported energy on counter 1
     *
     * @return total imported energy on counter 1
     */
    public double getTotalEnergyImportT1Kwh() {
        return totalEnergyImportT1Kwh;
    }

    /**
     * Setter for the total imported energy on counter 1
     *
     * @param totalEnergyImportT1Kwh total imported energy on counter 1
     */
    public void setTotalEnergyImportT1Kwh(double totalEnergyImportT1Kwh) {
        this.totalEnergyImportT1Kwh = totalEnergyImportT1Kwh;
    }

    /**
     * Getter for the total imported energy on counter 2
     *
     * @return total imported energy on counter 2
     */
    public double getTotalEnergyImportT2Kwh() {
        return totalEnergyImportT2Kwh;
    }

    /**
     * Setter for the total imported energy on counter 2
     *
     * @param totalEnergyImportT2Kwh
     */
    public void setTotalEnergyImportT2Kwh(double totalEnergyImportT2Kwh) {
        this.totalEnergyImportT2Kwh = totalEnergyImportT2Kwh;
    }

    /**
     * Getter for the total exported energy on counter 1
     *
     * @return total exported energy on counter 1
     */
    public double getTotalEnergyExportT1Kwh() {
        return totalEnergyExportT1Kwh;
    }

    /**
     * Setter for the total exported energy on counter 1
     *
     * @param totalEnergyExportT1Kwh
     */
    public void setTotalEnergyExportT1Kwh(double totalEnergyExportT1Kwh) {
        this.totalEnergyExportT1Kwh = totalEnergyExportT1Kwh;
    }

    /**
     * Getter for the total exported energy on counter 2
     *
     * @return total exported energy on counter 2
     */
    public double getTotalEnergyExportT2Kwh() {
        return totalEnergyExportT2Kwh;
    }

    /**
     * Setter for the total exported energy on counter 2
     *
     * @param totalEnergyExportT2Kwh
     */
    public void setTotalEnergyExportT2Kwh(double totalEnergyExportT2Kwh) {
        this.totalEnergyExportT2Kwh = totalEnergyExportT2Kwh;
    }

    /**
     * Getter for the current active total power
     *
     * @return current active total power
     */
    public double getActivePowerW() {
        return activePowerW;
    }

    /**
     * Setter for the current active total power
     *
     * @param activePowerW
     */
    public void setActivePowerW(double activePowerW) {
        this.activePowerW = activePowerW;
    }

    /**
     * Getter for the current active total power on phase 1
     *
     * @return current active total power on phase 1
     */
    public double getActivePowerL1W() {
        return activePowerL1W;
    }

    /**
     * Setter for the current active power on phase 1
     *
     * @param activePowerL1W current active total power on phase 1
     */
    public void setActivePowerL1W(double activePowerL1W) {
        this.activePowerL1W = activePowerL1W;
    }

    /**
     * Getter for the current active total power on phase 2
     *
     * @return current active total power on phase 2
     */
    public double getActivePowerL2W() {
        return activePowerL2W;
    }

    /**
     * Setter for the current active power on phase 2
     *
     * @param activePowerL2W current active total power on phase 2
     */
    public void setActivePowerL2W(double activePowerL2W) {
        this.activePowerL2W = activePowerL2W;
    }

    /**
     * Getter for the current active total power on phase 3
     *
     * @return current active total power on phase 3
     */
    public double getActivePowerL3W() {
        return activePowerL3W;
    }

    /**
     * Setter for the current active power on phase 3
     *
     * @param activePowerL3W current active total power on phase 3
     */
    public void setActivePowerL3W(double activePowerL3W) {
        this.activePowerL3W = activePowerL3W;
    }

    /**
     * Getter for the total imported gas volume
     *
     * @return total imported gas volume
     */
    public double getTotalGasM3() {
        return totalGasM3;
    }

    /**
     * Setter for the total imported gas volume
     *
     * @param totalGasM3 total imported gas volume
     */
    public void setTotalGasM3(double totalGasM3) {
        this.totalGasM3 = totalGasM3;
    }

    /**
     * Getter for the time stamp of the last gas update
     *
     * @return time stamp of the last gas update
     */
    public long getGasTimestamp() {
        return gasTimestamp;
    }

    /**
     * Setter for the time stamp of the last gas update
     *
     * @param gasTimestamp time stamp of the last gas update
     */
    public void setGasTimestamp(long gasTimestamp) {
        this.gasTimestamp = gasTimestamp;
    }

    @Override
    public String toString() {
        return String.format("P1 [version: %d model: %s ssid: %s signal: %d"
                + " imp1: %f imp2: %f exp1: %f exp2: %f active: %f active1: %f active2: %f active3: %f gas: %f timestamp: %.0f]",
                smrVersion, meterModel, wifiSsid, wifiStrength, totalEnergyImportT1Kwh, totalEnergyImportT2Kwh,
                totalEnergyExportT1Kwh, totalEnergyExportT2Kwh, activePowerW, activePowerL1W, activePowerL2W,
                activePowerL3W, totalGasM3, gasTimestamp);
    }
}
