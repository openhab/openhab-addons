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
package org.openhab.binding.modbus.sungrow.internal.dto;

import java.util.Optional;

/**
 * Model for Sungrow compatible inverter data
 *
 * @author Ferdinand Schwenk - Initial contribution
 *
 */

public class InverterModelBlock13k {

    /**
     * System state of the inverter
     */
    public Integer systemState;

    /**
     * Running State of the inverter
     */
    public Integer runningState;

    /**
     * Daily PV Generation
     */
    public Integer dailyPVGeneration;

    /**
     * Total PV Generation
     */
    public Long totalPVGeneration;

    /**
     * Daily export power from PV
     */
    public Integer dailyPVExport;

    /**
     * Total export energy from PV
     */
    public Long totalPVExport;

    /**
     * Load Power
     */
    public Long loadPower;

    /**
     * Export Power
     */
    public Long exportPower;

    /**
     * Daily battery charge energy from PV
     */
    public Integer dailyBatteryCharge;

    /**
     * Total battery charge energy from PV
     */
    public Long totalBatteryCharge;

    /**
     * CO2 Reduction
     */
    public Long co2Reduction;

    /**
     * Daily direct energy consumption
     */
    public Integer dailyDirectConsumption;

    /**
     * Total direct energy consumption
     */
    public Long totalDirectConsumption;

    /**
     * Battery voltage
     */
    public Integer batteryVoltage;

    /**
     * Battery current
     */
    public Integer batteryCurrent;

    /**
     * Battery power
     */
    public Integer batteryPower;

    /**
     * Battery level
     */
    public Integer batteryLevel;

    /**
     * Battery state of health
     */
    public Integer batteryHealth;

    /**
     * Battery temperature
     */
    public Short batteryTemperature;

    /**
     * Daily battery discharge energy
     */
    public Integer dailyBatteryDischarge;

    /**
     * Total battery discharge energy
     */
    public Long totalBatteryDischarge;

    /**
     * Today Self-consumption
     */
    public Integer todaySelfConsumption;

    /**
     * Grid state
     */
    public Integer gridState;

    /**
     * AC Phase A Current value
     */
    public Short acCurrentPhaseA;

    /**
     * AC Phase B Current value
     */
    public Optional<Short> acCurrentPhaseB;

    /**
     * AC Phase C Current value
     */
    public Optional<Short> acCurrentPhaseC;

    /**
     * Total active Power
     */
    public Long totalActivePower;

    /**
     * daily import Energy
     */
    public Integer dailyImportEnergy;

    /**
     * Total import Energy
     */
    public Long totalImportEnergy;

    /**
     * Battery capacity
     */
    public Integer batteryCapacity;

    /**
     * Daily charge energy
     */
    public Integer dailyChargeEnergy;

    /**
     * Total charge energy
     */
    public Long totalChargeEnergy;

    /**
     * DRM state
     */
    public Integer drmState;

    /**
     * daily export Energy
     */
    public Integer dailyExportEnergy;

    /**
     * Total export Energy
     */
    public Long totalExportEnergy;

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + "[\n" + "  acCurrentPhaseA=" + this.acCurrentPhaseA + "\n"
                + "  acCurrentPhaseB=" + this.acCurrentPhaseB + "\n" + "  acCurrentPhaseC=" + this.acCurrentPhaseC
                + "\n" + "  batteryCapacity=" + this.batteryCapacity + "\n" + "  batteryCurrent=" + this.batteryCurrent
                + "\n" + "  batteryHealth=" + this.batteryHealth + "\n" + "  batteryLevel=" + this.batteryLevel + "\n"
                + "  batteryPower=" + this.batteryPower + "\n" + "  batteryTemperature=" + this.batteryTemperature
                + "\n" + "  batteryVoltage=" + this.batteryVoltage + "\n" + "  co2Reduction=" + this.co2Reduction + "\n"
                + "  dailyBatteryCharge=" + this.dailyBatteryCharge + "\n" + "  dailyBatteryDischarge="
                + this.dailyBatteryDischarge + "\n" + "  dailyChargeEnergy=" + this.dailyChargeEnergy + "\n"
                + "  dailyDirectConsumption=" + this.dailyDirectConsumption + "\n" + "  dailyExportEnergy="
                + this.dailyExportEnergy + "\n" + "  dailyImportEnergy=" + this.dailyImportEnergy + "\n"
                + "  dailyPVExport=" + this.dailyPVExport + "\n" + "  dailyPVGeneration=" + this.dailyPVGeneration
                + "\n" + "  drmState=" + this.drmState + "\n" + "  exportPower=" + this.exportPower + "\n"
                + "  gridState=" + this.gridState + "\n" + "  loadPower=" + this.loadPower + "\n" + "  runningState="
                + this.runningState + "\n" + "  systemState=" + this.systemState + "\n" + "  totalActivePower="
                + this.totalActivePower + "\n" + "  totalBatteryCharge=" + this.totalBatteryCharge + "\n"
                + "  totalBatteryDischarge=" + this.totalBatteryDischarge + "\n" + "  totalChargeEnergy="
                + this.totalChargeEnergy + "\n" + "  totalDirectConsumption=" + this.totalDirectConsumption + "\n"
                + "  totalExportEnergy=" + this.totalExportEnergy + "\n" + "  totalImportEnergy="
                + this.totalImportEnergy + "\n" + "  totalPVExport=" + this.totalPVExport + "\n"
                + "  totalPVGeneration=" + this.totalPVGeneration + "\n" + "  todaySelfConsumption="
                + this.todaySelfConsumption + "\n" + "]";
    }
}
