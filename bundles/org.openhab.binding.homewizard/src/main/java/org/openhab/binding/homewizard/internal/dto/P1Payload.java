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
package org.openhab.binding.homewizard.internal.dto;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json object obtained from the P1 meter API
 *
 * @author Daniël van Os - Initial contribution
 * @author Leo Siepel - Clean-up and additional fields
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

    private int activePowerW;
    private int activePowerL1W;
    private int activePowerL2W;
    private int activePowerL3W;
    private double totalGasM3;
    private long gasTimestamp = 0;

    @SerializedName("any_power_fail_count")
    private int anyPowerFailCount;

    @SerializedName("long_power_fail_count")
    private int longPowerFailCount;

    @SerializedName("active_voltage_v")
    private int activeVoltage;
    @SerializedName("active_voltage_l1_v")
    private int activeVoltageL1;
    @SerializedName("active_voltage_l2_v")
    private int activeVoltageL2;
    @SerializedName("active_voltage_l3_v")
    private int activeVoltageL3;

    @SerializedName("active_current_a")
    private double activeCurrent;
    @SerializedName("active_current_l1_a")
    private double activeCurrentL1;
    @SerializedName("active_current_l2_a")
    private double activeCurrentL2;
    @SerializedName("active_current_l3_a")
    private double activeCurrentL3;

    /**
     * Getter for the smart meter version
     *
     * @return The most recent smart meter version obtained from the API
     */
    public int getSmrVersion() {
        return smrVersion;
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
     * Getter for the meter's wifi ssid
     *
     * @return the meter's wifi sid
     */
    public String getWifiSsid() {
        return wifiSsid;
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
     * Getter for the total imported energy on counter 1
     *
     * @return total imported energy on counter 1
     */
    public double getTotalEnergyImportT1Kwh() {
        return totalEnergyImportT1Kwh;
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
     * Getter for the total exported energy on counter 1
     *
     * @return total exported energy on counter 1
     */
    public double getTotalEnergyExportT1Kwh() {
        return totalEnergyExportT1Kwh;
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
     * Getter for the count of any power failures
     *
     * @return count of any power failures
     */
    public int getAnyPowerFailCount() {
        return anyPowerFailCount;
    }

    /**
     * Getter for the count of long power failures
     *
     * @return count of long power failures
     */
    public int getLongPowerFailCount() {
        return longPowerFailCount;
    }

    /**
     * Getter for the active voltage
     *
     * @return current active voltage
     */
    public int getActiveVoltage() {
        return activeVoltage;
    }

    /**
     * Getter for the active voltage on phase 1
     *
     * @return active voltage on phase 1
     */
    public int getActiveVoltageL1() {
        return activeVoltageL1;
    }

    /**
     * Getter for the active voltage on phase 2
     *
     * @return active voltage on phase 2
     */
    public int getActiveVoltageL2() {
        return activeVoltageL2;
    }

    /**
     * Getter for the active voltage on phase 3
     *
     * @return active voltage on phase 3
     */
    public int getActiveVoltageL3() {
        return activeVoltageL3;
    }

    /**
     * Getter for the active current (sum of all phases)
     *
     * @return active current (all phases)
     */
    public double getActiveCurrent() {
        return activeCurrent;
    }

    /**
     * Getter for the active current on phase 1
     *
     * @return active current on phase 1
     */
    public double getActiveCurrentL1() {
        return activeCurrentL1;
    }

    /**
     * Getter for the active current on phase 2
     *
     * @return active current on phase 2
     */
    public double getActiveCurrentL2() {
        return activeCurrentL2;
    }

    /**
     * Getter for the active current on phase 3
     *
     * @return active current on phase 3
     */
    public double getActiveCurrentL3() {
        return activeCurrentL3;
    }

    /**
     * Getter for the current active total power
     *
     * @return current active total power
     */
    public int getActivePowerW() {
        return activePowerW;
    }

    /**
     * Getter for the current active total power on phase 2
     *
     * @return current active total power on phase 2
     */
    public int getActivePowerL1W() {
        return activePowerL1W;
    }

    /**
     * Getter for the current active total power on phase 2
     *
     * @return current active total power on phase 2
     */
    public int getActivePowerL2W() {
        return activePowerL2W;
    }

    /**
     * Getter for the current active total power on phase 3
     *
     * @return current active total power on phase 3
     */
    public int getActivePowerL3W() {
        return activePowerL3W;
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
     * Getter for the time stamp of the last gas update
     *
     * @return time stamp of the last gas update as ZonedDateTime
     */
    public @Nullable ZonedDateTime getGasTimestamp() {
        long dtv = gasTimestamp;
        if (dtv < 1) {
            return null;
        }

        // 210119164000
        int seconds = (int) (dtv % 100);

        dtv /= 100;
        int minutes = (int) (dtv % 100);

        dtv /= 100;
        int hours = (int) (dtv % 100);

        dtv /= 100;
        int day = (int) (dtv % 100);

        dtv /= 100;
        int month = (int) (dtv % 100);

        dtv /= 100;
        int year = (int) (dtv + 2000);

        try {
            return ZonedDateTime.of(year, month, day, hours, minutes, seconds, 0, ZoneId.systemDefault());
        } catch (DateTimeException e) {
            LoggerFactory.getLogger(P1Payload.class).warn("Unable to parse Gas timestamp: {}", gasTimestamp);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format(
                """
                        P1 [version: %d model: %s ssid: %s signal: %d\
                         imp1: %f imp2: %f exp1: %f exp2: %f active: %f active1: %f active2: %f active3: %f gas: %f timestamp: %.0f]\
                        """,
                smrVersion, meterModel, wifiSsid, wifiStrength, totalEnergyImportT1Kwh, totalEnergyImportT2Kwh,
                totalEnergyExportT1Kwh, totalEnergyExportT2Kwh, activePowerW, activePowerL1W, activePowerL2W,
                activePowerL3W, totalGasM3, gasTimestamp);
    }
}
