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
package org.openhab.binding.solax.internal.model.local;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;

/**
 * The {@link X3HybridG4Data} is an implementation of the single phased inverter data interface for X3 Hybrid G4
 * inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class X3HybridG4Data extends CommonLocalDeviceData {

    public X3HybridG4Data(LocalConnectRawDataBean data) {
        super(data);
    }

    // Inverter data

    @Override
    public double getVoltagePhase1() {
        return ((double) getFromRawData(0)) / 10;
    }

    @Override
    public double getVoltagePhase2() {
        return ((double) getFromRawData(1)) / 10;
    }

    @Override
    public double getVoltagePhase3() {
        return ((double) getFromRawData(2)) / 10;
    }

    @Override
    public double getCurrentPhase1() {
        return ((double) getFromRawData(3)) / 10;
    }

    @Override
    public double getCurrentPhase2() {
        return ((double) getFromRawData(4)) / 10;
    }

    @Override
    public double getCurrentPhase3() {
        return ((double) getFromRawData(5)) / 10;
    }

    @Override
    public short getOutputPowerPhase1() {
        return getFromRawData(6);
    }

    @Override
    public short getOutputPowerPhase2() {
        return getFromRawData(7);
    }

    @Override
    public short getOutputPowerPhase3() {
        return getFromRawData(8);
    }

    @Override
    public short getTotalOutputPower() {
        return getFromRawData(9);
    }

    @Override
    public double getPV1Voltage() {
        return ((double) getFromRawData(10)) / 10;
    }

    @Override
    public double getPV2Voltage() {
        return ((double) getFromRawData(11)) / 10;
    }

    @Override
    public double getPV1Current() {
        return ((double) getFromRawData(12)) / 10;
    }

    @Override
    public double getPV2Current() {
        return ((double) getFromRawData(13)) / 10;
    }

    @Override
    public short getPV1Power() {
        return getFromRawData(14);
    }

    @Override
    public short getPV2Power() {
        return getFromRawData(15);
    }

    @Override
    public double getFrequencyPhase1() {
        return ((double) getFromRawData(16)) / 100;
    }

    @Override
    public double getFrequencyPhase2() {
        return ((double) getFromRawData(17)) / 100;
    }

    @Override
    public double getFrequencyPhase3() {
        return ((double) getFromRawData(18)) / 100;
    }

    // Battery

    @Override
    public double getBatteryVoltage() {
        return ((double) getFromRawData(39)) / 100;
    }

    @Override
    public double getBatteryCurrent() {
        return ((double) getFromRawData(40)) / 100;
    }

    @Override
    public short getBatteryPower() {
        return getFromRawData(41);
    }

    @Override
    public short getBatteryTemperature() {
        return getFromRawData(105);
    }

    @Override
    public short getBatteryLevel() {
        return getFromRawData(103);
    }

    // Feed in power

    @Override
    public short getFeedInPower() {
        return (short) (getFromRawData(34) - getFromRawData(35));
    }

    // Totals

    @Override
    public short getPowerUsage() {
        return getFromRawData(47);
    }

    @Override
    public double getTotalEnergy() {
        return ((double) getFromRawData(68)) / 10;
    }

    @Override
    public short getTotalBatteryDischargeEnergy() {
        return getFromRawData(74);
    }

    @Override
    public short getTotalBatteryChargeEnergy() {
        return getFromRawData(76);
    }

    @Override
    public double getTotalPVEnergy() {
        return ((double) getFromRawData(80)) / 10;
    }

    @Override
    public short getTotalFeedInEnergy() {
        return getFromRawData(86);
    }

    @Override
    public double getTotalConsumption() {
        return ((double) getFromRawData(88)) / 10;
    }

    @Override
    public double getTodayEnergy() {
        return ((double) getFromRawData(82)) / 10;
    }

    @Override
    public double getTodayFeedInEnergy() {
        return ((double) getFromRawData(90)) / 100;
    }

    @Override
    public double getTodayConsumption() {
        return ((double) getFromRawData(92)) / 100;
    }

    @Override
    public double getTodayBatteryDischargeEnergy() {
        return ((double) getFromRawData(78)) / 10;
    }

    @Override
    public double getTodayBatteryChargeEnergy() {
        return ((double) getFromRawData(79)) / 10;
    }

    @Override
    public short getInverterWorkModeCode() {
        return getFromRawData(19);
    }
}
