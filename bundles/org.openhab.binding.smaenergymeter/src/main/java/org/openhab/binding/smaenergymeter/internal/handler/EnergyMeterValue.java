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
package org.openhab.binding.smaenergymeter.internal.handler;

import static org.openhab.binding.smaenergymeter.internal.handler.Unit.*;

import java.util.Arrays;

/**
 * The {@link EnergyMeterValue} enum which defines the different possible measurement values of SMA EnergyMeter
 *
 * @author Lars Repenning - Initial contribution
 */
public enum EnergyMeterValue {
    CONSUME(1, W, kWh),
    SUPPLY(2, W, kWh),
    QCONSUM(3, VAr, kVArh),
    QSUPPLY(4, VAr, kVArh),
    SCONSUME(9, VA, kVAh),
    SSUPPLY(10, VA, kVAh),
    COSPHI(13, DEG, DEG),
    FREQUENCE(14, Hz, Hz),

    L1_CONSUME(21, W, kWh),
    L1_SUPPLY(22, W, kWh),
    L1_QCONSUM(23, VAr, kVArh),
    L1_QSUPPLY(24, VAr, kVArh),
    L1_SCONSUME(29, VA, kVAh),
    L1_SSUPPLY(30, VA, kVAh),
    L1_CURRENT(31, A, A),
    L1_VOLTAGE(32, V, V),
    L1_COSPHI(33, DEG, DEG),
    L1_FREQUENCE(34, Hz, Hz),

    L2_CONSUME(41, W, kWh),
    L2_SUPPLY(42, W, kWh),
    L2_QCONSUM(43, VAr, kVArh),
    L2_QSUPPLY(44, VAr, kVArh),
    L2_SCONSUME(49, VA, kVAh),
    L2_SSUPPLY(50, VA, kVAh),
    L2_CURRENT(51, A, A),
    L2_VOLTAGE(52, V, V),
    L2_COSPHI(53, DEG, DEG),
    L2_FREQUENCE(54, Hz, Hz),

    L3_CONSUME(61, W, kWh),
    L3_SUPPLY(62, W, kWh),
    L3_QCONSUM(63, VAr, kVArh),
    L3_QSUPPLY(64, VAr, kVArh),
    L3_SCONSUME(69, VA, kVAh),
    L3_SSUPPLY(70, VA, kVAh),
    L3_CURRENT(71, A, A),
    L3_VOLTAGE(72, V, V),
    L3_COSPHI(73, DEG, DEG),
    L3_FREQUENCE(74, Hz, Hz),

    SPEEDWIRE(36864, NONE, NONE);

    private int channel;
    private Unit currentUnitOfMeasurement;
    private Unit totalUnitOfMeasurement;

    EnergyMeterValue(int channel, Unit currentUnitOfMeasurement, Unit totalUnitOfMeasurement) {
        this.channel = channel;
        this.currentUnitOfMeasurement = currentUnitOfMeasurement;
        this.totalUnitOfMeasurement = totalUnitOfMeasurement;
    }

    public Unit getCurrentUnitOfMeasurement() {
        return currentUnitOfMeasurement;
    }

    public Unit getTotalUnitOfMeasurement() {
        return totalUnitOfMeasurement;
    }

    public static EnergyMeterValue getMeasuredUnit(int channel) {
        return Arrays.stream(EnergyMeterValue.values()).filter(mv -> mv.channel == channel).findFirst().orElse(null);
    }
}
