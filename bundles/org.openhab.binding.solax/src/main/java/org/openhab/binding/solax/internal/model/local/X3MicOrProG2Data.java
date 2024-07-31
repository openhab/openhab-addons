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
 * The {@link X3HybridG4Data} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Henrik Tóth - Initial contribution
 *         (based on X1/X3 G4 parser from Konstantin Polihronov)
 */
@NonNullByDefault
public class X3MicOrProG2Data extends CommonLocalDeviceData {

    public X3MicOrProG2Data(LocalConnectRawDataBean data) {
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
    public double getPV1Voltage() {
        return ((double) getFromRawData(9)) / 10;
    }

    @Override
    public double getPV2Voltage() {
        return ((double) getFromRawData(10)) / 10;
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
        return getFromRawData(15);
    }

    @Override
    public short getPV2Power() {
        return getFromRawData(16);
    }

    @Override
    public double getFrequencyPhase1() {
        return ((double) getFromRawData(18)) / 100;
    }

    @Override
    public double getFrequencyPhase2() {
        return ((double) getFromRawData(19)) / 100;
    }

    @Override
    public double getFrequencyPhase3() {
        return ((double) getFromRawData(20)) / 100;
    }

    @Override
    public short getInverterWorkModeCode() {
        return getFromRawData(21);
    }

    @Override
    public double getTotalEnergy() {
        return ((double) getFromRawData(22)) / 10;
    }

    @Override
    public double getTodayEnergy() {
        return ((double) getFromRawData(24)) / 10;
    }

    @Override
    public short getInverterTemperature1() {
        return getFromRawData(26);
    }

    @Override
    public short getInverterTemperature2() {
        return getFromRawData(27);
    }

    @Override
    public short getTotalOutputPower() {
        return getFromRawData(78);
    }
}
