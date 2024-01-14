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
package org.openhab.binding.solax.internal.model.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.connectivity.rawdata.LocalConnectRawDataBean;

/**
 * The {@link X3HybridG4InverterData} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class X3HybridG4InverterData extends CommonInverterData {

    public X3HybridG4InverterData(LocalConnectRawDataBean data) {
        super(data);
    }

    // Inverter data

    @Override
    public double getVoltagePhase1() {
        return ((double) getData(0)) / 10;
    }

    @Override
    public double getVoltagePhase2() {
        return ((double) getData(1)) / 10;
    }

    @Override
    public double getVoltagePhase3() {
        return ((double) getData(2)) / 10;
    }

    @Override
    public double getCurrentPhase1() {
        return ((double) getData(3)) / 10;
    }

    @Override
    public double getCurrentPhase2() {
        return ((double) getData(4)) / 10;
    }

    @Override
    public double getCurrentPhase3() {
        return ((double) getData(5)) / 10;
    }

    @Override
    public short getOutputPowerPhase1() {
        return getData(6);
    }

    @Override
    public short getOutputPowerPhase2() {
        return getData(7);
    }

    @Override
    public short getOutputPowerPhase3() {
        return getData(8);
    }

    @Override
    public short getTotalOutputPower() {
        return getData(9);
    }

    @Override
    public double getPV1Voltage() {
        return ((double) getData(10)) / 10;
    }

    @Override
    public double getPV2Voltage() {
        return ((double) getData(11)) / 10;
    }

    @Override
    public double getPV1Current() {
        return ((double) getData(12)) / 10;
    }

    @Override
    public double getPV2Current() {
        return ((double) getData(13)) / 10;
    }

    @Override
    public short getPV1Power() {
        return getData(14);
    }

    @Override
    public short getPV2Power() {
        return getData(15);
    }

    @Override
    public double getFrequencyPhase1() {
        return ((double) getData(16)) / 100;
    }

    @Override
    public double getFrequencyPhase2() {
        return ((double) getData(17)) / 100;
    }

    @Override
    public double getFrequencyPhase3() {
        return ((double) getData(18)) / 100;
    }

    // Battery

    @Override
    public double getBatteryVoltage() {
        return ((double) getData(39)) / 100;
    }

    @Override
    public double getBatteryCurrent() {
        return ((double) getData(40)) / 100;
    }

    @Override
    public short getBatteryPower() {
        return getData(41);
    }

    @Override
    public short getBatteryTemperature() {
        return getData(105);
    }

    @Override
    public short getBatteryLevel() {
        return getData(103);
    }

    // Feed in power

    @Override
    public short getFeedInPower() {
        return (short) (getData(34) - getData(35));
    }

    // Totals

    @Override
    public short getPowerUsage() {
        return getData(47);
    }

    @Override
    public double getTotalEnergy() {
        return ((double) getData(68)) / 10;
    }

    @Override
    public short getTotalBatteryDischargeEnergy() {
        return getData(74);
    }

    @Override
    public short getTotalBatteryChargeEnergy() {
        return getData(76);
    }

    @Override
    public double getTotalPVEnergy() {
        return ((double) getData(80)) / 10;
    }

    @Override
    public short getTotalFeedInEnergy() {
        return getData(86);
    }

    @Override
    public double getTotalConsumption() {
        return ((double) getData(88)) / 10;
    }

    @Override
    public double getTodayEnergy() {
        return ((double) getData(82)) / 10;
    }

    @Override
    public double getTodayFeedInEnergy() {
        return ((double) getData(90)) / 100;
    }

    @Override
    public double getTodayConsumption() {
        return ((double) getData(92)) / 100;
    }

    @Override
    public double getTodayBatteryDischargeEnergy() {
        return ((double) getData(78)) / 10;
    }

    @Override
    public double getTodayBatteryChargeEnergy() {
        return ((double) getData(79)) / 10;
    }
}
