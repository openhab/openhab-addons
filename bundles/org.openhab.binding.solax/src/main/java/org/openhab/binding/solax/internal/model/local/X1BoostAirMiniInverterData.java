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
 * The {@link X1BoostAirMiniInverterData} is an implementation of the single phased inverter data interface for X1 Mini
 * / X1 Air Mini or X1 Boost Mini inverter.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class X1BoostAirMiniInverterData extends CommonLocalInverterData {

    public X1BoostAirMiniInverterData(LocalConnectRawDataBean data) {
        super(data);
    }

    @Override
    public double getInverterVoltage() {
        return (double) getData(0) / 10;
    }

    @Override
    public double getInverterCurrent() {
        return (double) getData(1) / 10;
    }

    @Override
    public short getInverterOutputPower() {
        return getData(2);
    }

    @Override
    public double getPV1Voltage() {
        return (double) getData(3) / 10;
    }

    @Override
    public double getPV2Voltage() {
        return (double) getData(4) / 10;
    }

    @Override
    public double getPV1Current() {
        return (double) getData(5) / 10;
    }

    @Override
    public double getPV2Current() {
        return (double) getData(6) / 10;
    }

    @Override
    public short getPV1Power() {
        return getData(7);
    }

    @Override
    public short getPV2Power() {
        return getData(8);
    }

    @Override
    public double getInverterFrequency() {
        return (double) getData(9) / 100;
    }

    @Override
    public double getTotalEnergy() {
        return (double) getData(11) / 10;
    }

    @Override
    public double getTodayEnergy() {
        return (double) getData(13) / 10;
    }

    @Override
    public short getPowerUsage() {
        return (short) Math.round((double) getData(43) / 10);
    }
}
