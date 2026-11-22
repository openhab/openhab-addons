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
 * @param warningBits1 first tower warning bitmap
 * @param warningBits2 second tower warning bitmap, same meaning of the bits as the first
 * @param warningBits3 third tower warning bitmap
 * @param errorBits tower error bitmap
 * @param balancingCellNumbers numbers of the cells currently balancing, 1-based, ascending
 * @param cellVoltages cell voltages in mV, index 0 = cell 1; only the cells that were read
 * @param temperatures sensor temperatures in °C, index 0 = sensor 1
 * @param serialNumber serial number of the tower BMS as reported
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public record TowerData(int tower, BigDecimal stateOfCharge, int stateOfHealth, BigDecimal voltage,
        BigDecimal outputVoltage, BigDecimal current, long chargedEnergyWh, long dischargedEnergyWh, int warningBits1,
        int warningBits2, int warningBits3, int errorBits, List<Integer> balancingCellNumbers,
        List<Integer> cellVoltages, List<Integer> temperatures, String bmsFirmwareA, String bmsFirmwareB,
        String serialNumber) {

    public TowerData {
        cellVoltages = List.copyOf(cellVoltages);
        temperatures = List.copyOf(temperatures);
        balancingCellNumbers = List.copyOf(balancingCellNumbers);
    }

    /**
     * @return the number of cells currently balancing
     */
    public int balancingCells() {
        return balancingCellNumbers.size();
    }

    /**
     * @return this measurement with the cell voltages and the balancing cells replaced
     */
    public TowerData withCells(List<Integer> cellVoltages, List<Integer> balancingCellNumbers) {
        return new TowerData(tower, stateOfCharge, stateOfHealth, voltage, outputVoltage, current, chargedEnergyWh,
                dischargedEnergyWh, warningBits1, warningBits2, warningBits3, errorBits, balancingCellNumbers,
                cellVoltages, temperatures, bmsFirmwareA, bmsFirmwareB, serialNumber);
    }
}
