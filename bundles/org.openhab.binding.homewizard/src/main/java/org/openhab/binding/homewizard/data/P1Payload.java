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
package org.openhab.binding.homewizard.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class that provides storage for the json object obtained from the P1 meter API
 *
 * @author Daniël van Os - Initial contribution
 *
 */
@NonNullByDefault
public class P1Payload {
    private int smr_version;
    private String meter_model = "";
    private String wifi_ssid = "";
    private int wifi_strength;
    private double total_power_import_t1_kwh;
    private double total_power_import_t2_kwh;
    private double total_power_export_t1_kwh;
    private double total_power_export_t2_kwh;
    private double active_power_w;
    private double active_power_l1_w;
    private double active_power_l2_w;
    private double active_power_l3_w;
    private double total_gas_m3;
    private long gas_timestamp;

    /**
     * Getter for the smart meter version
     *
     * @return The most recent smart meter version obtained from the API
     */
    public int getSmr_version() {
        return smr_version;
    }

    /**
     * Setter for the smart meter version
     *
     * @param smrVersion The smart meter version to set
     */
    public void setSmr_version(int smrVersion) {
        smr_version = smrVersion;
    }

    /**
     * Getter for the meter model
     *
     * @return meter model
     */
    public String getMeter_model() {
        return meter_model;
    }

    /**
     * Setter for the meter model
     *
     * @param meterModel meter model
     */
    public void setMeter_model(String meterModel) {
        meter_model = meterModel;
    }

    /**
     * Getter for the meter's wifi ssid
     *
     * @return the meter's wifi sid
     */
    public String getWifi_ssid() {
        return wifi_ssid;
    }

    /**
     * Setter for the wifi ssid
     *
     * @param wifiSsid wifi ssid
     */
    public void setWifi_ssid(String wifiSsid) {
        wifi_ssid = wifiSsid;
    }

    /**
     * Getter for the wifi rssi
     *
     * @return wifi rssi
     */
    public int getWifi_strength() {
        return wifi_strength;
    }

    /**
     * Setter for the wifi rssi
     *
     * @param wifiStrength wifi rssi
     */
    public void setWifi_strength(int wifiStrength) {
        wifi_strength = wifiStrength;
    }

    /**
     * Getter for the total imported power on counter 1
     *
     * @return total imported power on counter 1
     */
    public double getTotal_power_import_t1_kwh() {
        return total_power_import_t1_kwh;
    }

    /**
     * Setter for the total imported power on counter 1
     *
     * @param totalPowerImportT1Kwh total imported power on counter 1
     */
    public void setTotal_power_import_t1_kwh(double totalPowerImportT1Kwh) {
        total_power_import_t1_kwh = totalPowerImportT1Kwh;
    }

    /**
     * Getter for the total imported power on counter 2
     *
     * @return total imported power on counter 2
     */
    public double getTotal_power_import_t2_kwh() {
        return total_power_import_t2_kwh;
    }

    /**
     * Setter for the total imported power on counter 2
     *
     * @param totalPowerImportT2Kwh
     */
    public void setTotal_power_import_t2_kwh(double totalPowerImportT2Kwh) {
        total_power_import_t2_kwh = totalPowerImportT2Kwh;
    }

    /**
     * Getter for the total exported power on counter 1
     *
     * @return total exported power on counter 1
     */
    public double getTotal_power_export_t1_kwh() {
        return total_power_export_t1_kwh;
    }

    /**
     * Setter for the total exported power on counter 1
     *
     * @param totalPowerExportT1Kwh
     */
    public void setTotal_power_export_t1_kwh(double totalPowerExportT1Kwh) {
        total_power_export_t1_kwh = totalPowerExportT1Kwh;
    }

    /**
     * Getter for the total exported power on counter 2
     *
     * @return total exported power on counter 2
     */
    public double getTotal_power_export_t2_kwh() {
        return total_power_export_t2_kwh;
    }

    /**
     * Setter for the total exported power on counter 2
     *
     * @param totalPowerExportT2Kwh
     */
    public void setTotal_power_export_t2_kwh(double totalPowerExportT2Kwh) {
        total_power_export_t2_kwh = totalPowerExportT2Kwh;
    }

    /**
     * Getter for the current active total power
     *
     * @return current active total power
     */
    public double getActive_power_w() {
        return active_power_w;
    }

    /**
     * Setter for the current active total power
     *
     * @param activePowerW
     */
    public void setActive_power_w(double activePowerW) {
        active_power_w = activePowerW;
    }

    /**
     * Getter for the current active total power on phase 1
     *
     * @return current active total power on phase 1
     */
    public double getActive_power_l1_w() {
        return active_power_l1_w;
    }

    /**
     * Setter for the current active power on phase 1
     *
     * @param activePowerL1W current active total power on phase 1
     */
    public void setActive_power_l1_w(double activePowerL1W) {
        active_power_l1_w = activePowerL1W;
    }

    /**
     * Getter for the current active total power on phase 2
     *
     * @return current active total power on phase 2
     */
    public double getActive_power_l2_w() {
        return active_power_l2_w;
    }

    /**
     * Setter for the current active power on phase 2
     *
     * @param activePowerL2W current active total power on phase 2
     */
    public void setActive_power_l2_w(double activePowerL2W) {
        active_power_l2_w = activePowerL2W;
    }

    /**
     * Getter for the current active total power on phase 3
     *
     * @return current active total power on phase 3
     */
    public double getActive_power_l3_w() {
        return active_power_l3_w;
    }

    /**
     * Setter for the current active power on phase 3
     *
     * @param activePowerL3W current active total power on phase 3
     */
    public void setActive_power_l3_w(double activePowerL3W) {
        active_power_l3_w = activePowerL3W;
    }

    /**
     * Getter for the total imported gas volume
     *
     * @return total imported gas volume
     */
    public double getTotal_gas_m3() {
        return total_gas_m3;
    }

    /**
     * Setter for the total imported gas volume
     *
     * @param totalGasM3 total imported gas volume
     */
    public void setTotal_gas_m3(double totalGasM3) {
        total_gas_m3 = totalGasM3;
    }

    /**
     * Getter for the time stamp of the last gas update
     *
     * @return time stamp of the last gas update
     */
    public long getGas_timestamp() {
        return gas_timestamp;
    }

    /**
     * Setter for the time stamp of the last gas update
     *
     * @param gasTimestamp time stamp of the last gas update
     */
    public void setGas_timestamp(long gasTimestamp) {
        gas_timestamp = gasTimestamp;
    }

    @Override
    public String toString() {
        return String.format("P1 [version: %d model: %s ssid: %s signal: %d"
                + " imp1: %f imp2: %f exp1: %f exp2: %f active: %f active1: %f active2: %f active3: %f gas: %f timestamp: %d]",
                smr_version, meter_model, wifi_ssid, wifi_strength, total_power_import_t1_kwh,
                total_power_import_t2_kwh, total_power_export_t1_kwh, total_power_export_t2_kwh, active_power_w,
                active_power_l1_w, active_power_l2_w, active_power_l3_w, total_gas_m3, gas_timestamp);
    }
}
