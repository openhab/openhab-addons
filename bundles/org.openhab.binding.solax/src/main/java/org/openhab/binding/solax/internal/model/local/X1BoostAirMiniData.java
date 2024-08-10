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
 * The {@link X1BoostAirMiniData} is an implementation of the single phased inverter data interface for X1 Mini
 * / X1 Air Mini or X1 Boost Mini inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class X1BoostAirMiniData extends CommonLocalDeviceData {

    public X1BoostAirMiniData(LocalConnectRawDataBean data) {
        super(data);
    }

    @Override
    public double getInverterVoltage() {
        return (double) getFromRawData(0) / 10;
    }

    @Override
    public double getInverterCurrent() {
        return (double) getFromRawData(1) / 10;
    }

    @Override
    public short getInverterOutputPower() {
        return getFromRawData(2);
    }

    @Override
    public double getPV1Voltage() {
        return (double) getFromRawData(3) / 10;
    }

    @Override
    public double getPV2Voltage() {
        return (double) getFromRawData(4) / 10;
    }

    @Override
    public double getPV1Current() {
        return (double) getFromRawData(5) / 10;
    }

    @Override
    public double getPV2Current() {
        return (double) getFromRawData(6) / 10;
    }

    @Override
    public short getPV1Power() {
        return getFromRawData(7);
    }

    @Override
    public short getPV2Power() {
        return getFromRawData(8);
    }

    @Override
    public double getInverterFrequency() {
        return (double) getFromRawData(9) / 100;
    }

    @Override
    public double getTotalEnergy() {
        return (double) getFromRawData(11) / 10;
    }

    @Override
    public double getTodayEnergy() {
        return (double) getFromRawData(13) / 10;
    }

    @Override
    public short getPowerUsage() {
        return (short) Math.round((double) getFromRawData(43) / 10);
    }
}
