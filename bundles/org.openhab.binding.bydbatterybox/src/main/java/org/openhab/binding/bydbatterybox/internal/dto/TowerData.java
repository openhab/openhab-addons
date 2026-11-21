/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bydbatterybox.internal.dto;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of one cell measurement of a tower.
 *
 * @param tower tower number, 1-based
 * @param stateOfCharge in %, 0.1 % resolution
 * @param stateOfHealth in %
 * @param voltage in V
 * @param outputVoltage in V
 * @param current in A, positive while discharging
 * @param chargedEnergyWh lifetime charged energy of the tower in Wh
 * @param dischargedEnergyWh lifetime discharged energy of the tower in Wh
 * @param stateBits tower warning bitmap
 * @param balancingCells number of cells currently balancing
 * @param cellVoltages cell voltages in mV, index 0 = cell 1; only the cells that were read
 * @param temperatures sensor temperatures in °C, index 0 = sensor 1
 * @param serialNumber serial number of the tower BMS as reported
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public record TowerData(int tower, BigDecimal stateOfCharge, int stateOfHealth, BigDecimal voltage,
        BigDecimal outputVoltage, BigDecimal current, long chargedEnergyWh, long dischargedEnergyWh, int stateBits,
        int balancingCells, List<Integer> cellVoltages, List<Integer> temperatures, String bmsFirmwareA,
        String bmsFirmwareB, String serialNumber) {

    public TowerData {
        cellVoltages = List.copyOf(cellVoltages);
        temperatures = List.copyOf(temperatures);
    }

    /**
     * @return this measurement with the cell voltages and the balancing cell count replaced
     */
    public TowerData withCells(List<Integer> cellVoltages, int balancingCells) {
        return new TowerData(tower, stateOfCharge, stateOfHealth, voltage, outputVoltage, current, chargedEnergyWh,
                dischargedEnergyWh, stateBits, balancingCells, cellVoltages, temperatures, bmsFirmwareA, bmsFirmwareB,
                serialNumber);
    }
}
