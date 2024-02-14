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
 * The {@link X1HybridG4InverterData} is an implementation of the single phased inverter data interface for X1 Hybrid G4
 * inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class X1HybridG4InverterData extends CommonLocalInverterData {

    public X1HybridG4InverterData(LocalConnectRawDataBean data) {
        super(data);
    }

    @Override
    public double getInverterVoltage() {
        return ((double) getData(0)) / 10;
    }

    @Override
    public double getInverterCurrent() {
        return ((double) getData(1)) / 10;
    }

    @Override
    public short getInverterOutputPower() {
        return getData(2);
    }

    @Override
    public double getInverterFrequency() {
        return ((double) getData(3)) / 100;
    }

    @Override
    public short getFeedInPower() {
        return getData(32);
    }

    @Override
    public double getPV1Voltage() {
        return ((double) getData(4)) / 10;
    }

    @Override
    public double getPV2Voltage() {
        return ((double) getData(5)) / 10;
    }

    @Override
    public double getPV1Current() {
        return ((double) getData(6)) / 10;
    }

    @Override
    public double getPV2Current() {
        return ((double) getData(7)) / 10;
    }

    @Override
    public short getPV1Power() {
        return getData(8);
    }

    @Override
    public short getPV2Power() {
        return getData(9);
    }

    @Override
    public double getBatteryVoltage() {
        return ((double) getData(14)) / 100;
    }

    @Override
    public double getBatteryCurrent() {
        return ((double) getData(15)) / 100;
    }

    @Override
    public short getBatteryPower() {
        return getData(16);
    }

    @Override
    public short getBatteryTemperature() {
        return getData(17);
    }

    @Override
    public short getBatteryLevel() {
        return getData(18);
    }

    @Override
    public short getInverterWorkModeCode() {
        return getData(10);
    }
}
