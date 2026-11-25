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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Live system status.
 *
 * @param stateOfCharge in %
 * @param maxCellVoltage in V, 10 mV resolution
 * @param minCellVoltage in V, 10 mV resolution
 * @param stateOfHealth in %
 * @param current in A, positive while discharging
 * @param voltage pack voltage in V
 * @param maxCellTemperature in °C
 * @param minCellTemperature in °C
 * @param bmuTemperature in °C
 * @param errorBits system error bitmap
 * @param parameterTable parameter table version, e.g. {@code 9.2}
 * @param outputVoltage voltage behind the relay in V
 * @param chargedEnergy lifetime charged energy in kWh
 * @param dischargedEnergy lifetime discharged energy in kWh
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public record SystemStatus(int stateOfCharge, BigDecimal maxCellVoltage, BigDecimal minCellVoltage, int stateOfHealth,
        BigDecimal current, BigDecimal voltage, int maxCellTemperature, int minCellTemperature, int bmuTemperature,
        int errorBits, String parameterTable, BigDecimal outputVoltage, BigDecimal chargedEnergy,
        BigDecimal dischargedEnergy) {
}
