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
package org.openhab.binding.homewizard.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard Energy Socket.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 */
@NonNullByDefault
public class HomeWizardEnergyMeterMeasurementPayload {

    @SerializedName(value = "energy_import_kwh", alternate = "total_power_import_kwh")
    private double energyImport;
    @SerializedName(value = "energy_import_t1_kwh", alternate = "total_power_import_t1_kwh")
    private double energyImportT1;
    @SerializedName(value = "energy_import_t2_kwh", alternate = "total_power_import_t2_kwh")
    private double energyImportT2;

    @SerializedName(value = "energy_export_kwh", alternate = "total_power_export_kwh")
    private double energyExport;
    @SerializedName(value = "energy_export_t1_kwh", alternate = "total_power_export_t1_kwh")
    private double energyExportT1;
    @SerializedName(value = "energy_export_t2_kwh", alternate = "total_power_export_t2_kwh")
    private double energyExportT2;

    @SerializedName(value = "power_w", alternate = "active_power_w")
    private double power;
    @SerializedName(value = "power_l1_w", alternate = "active_power_l1_w")
    private double powerL1;
    @SerializedName(value = "power_l2_w", alternate = "active_power_l2_w")
    private double powerL2;
    @SerializedName(value = "power_l3_w", alternate = "active_power_l3_w")
    private double powerL3;

    @SerializedName(value = "voltage_v", alternate = "active_voltage_v")
    private double voltage;
    @SerializedName(value = "voltage_l1_v", alternate = "active_voltage_l1_v")
    private double voltageL1;
    @SerializedName(value = "voltage_l2_v", alternate = "active_voltage_l2_v")
    private double voltageL2;
    @SerializedName(value = "voltage_l3_v", alternate = "active_voltage_l3_v")
    private double voltageL3;

    @SerializedName(value = "current_a", alternate = "active_current_a")
    private double current;
    @SerializedName(value = "current_l1_a", alternate = "active_current_l1_a")
    private double currentL1;
    @SerializedName(value = "current_l2_a", alternate = "active_current_l2_a")
    private double currentL2;
    @SerializedName(value = "current_l3_a", alternate = "active_current_l3_a")
    private double currentL3;

    @SerializedName(value = "frequency_hz", alternate = "active_frequency_hz")
    private double frequency;

    /**
     * Getter for the total imported energy
     *
     * @return total imported energy
     */
    public double getEnergyImport() {
        return energyImport;
    }

    /**
     * Getter for the total imported energy on counter 1
     *
     * @return total imported energy on counter 1
     */
    public double getEnergyImportT1() {
        return energyImportT1;
    }

    /**
     * Getter for the total imported energy on counter 2
     *
     * @return total imported energy on counter 2
     */
    public double getEnergyImportT2() {
        return energyImportT2;
    }

    /**
     * Getter for the total exported energy
     *
     * @return total exported energy
     */
    public double getEnergyExport() {
        return energyExport;
    }

    /**
     * Getter for the total exported energy on counter 1
     *
     * @return total exported energy on counter 1
     */
    public double getEnergyExportT1() {
        return energyExportT1;
    }

    /**
     * Getter for the total exported energy on counter 2
     *
     * @return total exported energy on counter 2
     */
    public double getEnergyExportT2() {
        return energyExportT2;
    }

    /**
     * Getter for the current active total power
     *
     * @return current active total power
     */
    public double getPower() {
        return power;
    }

    /**
     * Getter for the current active total power on phase 2
     *
     * @return current active total power on phase 2
     */
    public double getPowerL1() {
        return powerL1;
    }

    /**
     * Getter for the current active total power on phase 2
     *
     * @return current active total power on phase 2
     */
    public double getPowerL2() {
        return powerL2;
    }

    /**
     * Getter for the current active total power on phase 3
     *
     * @return current active total power on phase 3
     */
    public double getPowerL3() {
        return powerL3;
    }

    /**
     * Getter for the active voltage
     *
     * @return current active voltage
     */
    public double getVoltage() {
        return voltage;
    }

    /**
     * Getter for the active voltage on phase 1
     *
     * @return active voltage on phase 1
     */
    public double getVoltageL1() {
        return voltageL1;
    }

    /**
     * Getter for the active voltage on phase 2
     *
     * @return active voltage on phase 2
     */
    public double getVoltageL2() {
        return voltageL2;
    }

    /**
     * Getter for the active voltage on phase 3
     *
     * @return active voltage on phase 3
     */
    public double getVoltageL3() {
        return voltageL3;
    }

    /**
     * Getter for the active current
     *
     * @return active current
     */
    public double getCurrent() {
        return current;
    }

    /**
     * Getter for the active current on phase 1
     *
     * @return active current on phase 1
     */
    public double getCurrentL1() {
        return currentL1;
    }

    /**
     * Getter for the active current on phase 2
     *
     * @return active current on phase 2
     */
    public double getCurrentL2() {
        return currentL2;
    }

    /**
     * Getter for the active current on phase 3
     *
     * @return active current on phase 3
     */
    public double getCurrentL3() {
        return currentL3;
    }

    /**
     * Getter for the active frequency
     *
     * @return active frequency
     */
    public double getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return String.format("""
                        Energy Meter Data [
                        totalEnergyImport: %f energyImportT1: %f energyImportT2: %f
                        totalEnergyExport: %f energyExportT1: %f energyIExortT2: %f
                        power: %f powerL1: %f powerL2: %f powerL3: %f
                        voltage: %f voltageL1: %f voltageL2: %f voltageL3: %f
                        current: %f currentL1: %f currentL2: %f currentL3: %f frequency: %f
                        ]
                """, energyImport, energyImportT1, energyImportT2, energyExport, energyExportT1, energyExportT2, power,
                powerL1, powerL2, powerL3, voltage, voltageL1, voltageL2, voltageL3, current, currentL1, currentL2,
                currentL3, frequency);
    }
}
