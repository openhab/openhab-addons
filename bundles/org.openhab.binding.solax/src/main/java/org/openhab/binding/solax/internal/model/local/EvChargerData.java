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
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.util.ByteUtil;

/**
 * The {@link EvChargerData} is the data representation of the EV charger, retrieved from the raw data array.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class EvChargerData extends CommonLocalDeviceData {

    public EvChargerData(LocalConnectRawDataBean bean) {
        super(bean);
    }

    @Override
    public InverterType getInverterType() {
        return InverterType.UNKNOWN;
    }

    public String getDeviceState() {
        return String.valueOf(getFromRawData(0));
    }

    public String getDeviceMode() {
        return String.valueOf(getFromRawData(1));
    }

    public double getEqSingle() {
        return getFromRawData(12).doubleValue() / 10;
    }

    public double getEqTotal() {
        return ((double) ByteUtil.read32BitSigned(getFromRawData(14), getFromRawData(15))) / 10;
    }

    public short getTotalChargePower() {
        return getFromRawData(11);
    }

    @Override
    public double getVoltagePhase1() {
        return getFromRawData(2).doubleValue() / 100;
    }

    @Override
    public double getVoltagePhase2() {
        return getFromRawData(3).doubleValue() / 100;
    }

    @Override
    public double getVoltagePhase3() {
        return getFromRawData(4).doubleValue() / 100;
    }

    @Override
    public double getCurrentPhase1() {
        return getFromRawData(5).doubleValue() / 100;
    }

    @Override
    public double getCurrentPhase2() {
        return getFromRawData(6).doubleValue() / 100;
    }

    @Override
    public double getCurrentPhase3() {
        return getFromRawData(7).doubleValue() / 100;
    }

    @Override
    public short getOutputPowerPhase1() {
        return getFromRawData(8);
    }

    @Override
    public short getOutputPowerPhase2() {
        return getFromRawData(9);
    }

    @Override
    public short getOutputPowerPhase3() {
        return getFromRawData(10);
    }

    public double getExternalCurrentPhase1() {
        return getFromRawData(16).doubleValue() / 100;
    }

    public double getExternalCurrentPhase2() {
        return getFromRawData(17).doubleValue() / 100;
    }

    public double getExternalCurrentPhase3() {
        return getFromRawData(18).doubleValue() / 100;
    }

    public double getExternalPowerPhase1() {
        return getFromRawData(19).intValue();
    }

    public double getExternalPowerPhase2() {
        return getFromRawData(20).intValue();
    }

    public double getExternalPowerPhase3() {
        return getFromRawData(21).intValue();
    }

    public double getExternalTotalPower() {
        return getFromRawData(22).intValue();
    }

    public short getPlugTemperature() {
        return getFromRawData(23);
    }

    public short getInternalTemperature() {
        return getFromRawData(24);
    }

    public short getCPState() {
        return getFromRawData(26);
    }

    public int getChargingDuration() {
        return ByteUtil.read32BitSigned(getFromRawData(80), getFromRawData(81));
    }

    public short getOccpOfflineMode() {
        return getFromRawData(85);
    }

    public short getTypePower() {
        return getFromRawData(87);
    }

    public short getTypePhase() {
        return getFromRawData(88);
    }

    public short getTypeCharger() {
        return getFromRawData(89);
    }
}
