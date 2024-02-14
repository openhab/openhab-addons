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
 * The {@link X3HybridG4InverterData} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Henrik Tóth - Initial contribution
 *         (based on X1/X3 G4 parser from Konstantin Polihronov)
 */
@NonNullByDefault
public class X3MicOrProG2InverterData extends CommonLocalInverterData {

    public X3MicOrProG2InverterData(LocalConnectRawDataBean data) {
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
    public double getPV1Voltage() {
        return ((double) getData(9)) / 10;
    }

    @Override
    public double getPV2Voltage() {
        return ((double) getData(10)) / 10;
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
        return getData(15);
    }

    @Override
    public short getPV2Power() {
        return getData(16);
    }

    @Override
    public double getFrequencyPhase1() {
        return ((double) getData(18)) / 100;
    }

    @Override
    public double getFrequencyPhase2() {
        return ((double) getData(19)) / 100;
    }

    @Override
    public double getFrequencyPhase3() {
        return ((double) getData(20)) / 100;
    }

    @Override
    public short getInverterWorkModeCode() {
        return getData(21);
    }

    @Override
    public double getTotalEnergy() {
        return ((double) getData(22)) / 10;
    }

    @Override
    public double getTodayEnergy() {
        return ((double) getData(24)) / 10;
    }

    @Override
    public short getInverterTemperature1() {
        return getData(26);
    }

    @Override
    public short getInverterTemperature2() {
        return getData(27);
    }

    @Override
    public short getTotalOutputPower() {
        return getData(78);
    }
}
