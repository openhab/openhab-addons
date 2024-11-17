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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterType;

/**
 * The {@link LocalData} Interface for the parsed inverter data in meaningful format
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public interface LocalData {

    @Nullable
    default String getWifiSerial() {
        return getData().getSn();
    }

    @Nullable
    default String getWifiVersion() {
        return getData().getVer();
    }

    InverterType getInverterType();

    @Nullable
    default String getRawData() {
        return getData().getRawData();
    }

    LocalConnectRawDataBean getData();

    default double getPV1Voltage() {
        return Integer.MIN_VALUE;
    }

    default double getPV1Current() {
        return Integer.MIN_VALUE;
    }

    default short getPV1Power() {
        return Short.MIN_VALUE;
    }

    default double getPV2Voltage() {
        return Integer.MIN_VALUE;
    }

    default double getPV2Current() {
        return Integer.MIN_VALUE;
    }

    default short getPV2Power() {
        return Short.MIN_VALUE;
    }

    default double getPVTotalPower() {
        return getPV1Power() + getPV2Power();
    }

    default double getPVTotalCurrent() {
        return getPV1Current() + getPV2Current();
    }

    default double getBatteryVoltage() {
        return Integer.MIN_VALUE;
    }

    default double getBatteryCurrent() {
        return Integer.MIN_VALUE;
    }

    default short getBatteryPower() {
        return Short.MIN_VALUE;
    }

    default short getBatteryTemperature() {
        return Short.MIN_VALUE;
    }

    default short getInverterTemperature1() {
        return Short.MIN_VALUE;
    }

    default short getInverterTemperature2() {
        return Short.MIN_VALUE;
    }

    default short getBatteryLevel() {
        return Short.MIN_VALUE;
    }

    default int getFeedInPower() {
        return Short.MIN_VALUE;
    }

    default short getPowerUsage() {
        return Short.MIN_VALUE;
    }

    default double getTotalEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTotalBatteryDischargeEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTotalBatteryChargeEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTotalPVEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTotalFeedInEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTotalConsumption() {
        return Integer.MIN_VALUE;
    }

    default double getTodayEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTodayFeedInEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTodayConsumption() {
        return Integer.MIN_VALUE;
    }

    default double getTodayBatteryDischargeEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTodayBatteryChargeEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getInverterVoltage() {
        return Integer.MIN_VALUE;
    }

    default double getInverterCurrent() {
        return Integer.MIN_VALUE;
    }

    default short getInverterOutputPower() {
        return Short.MIN_VALUE;
    }

    default double getInverterFrequency() {
        return Integer.MIN_VALUE;
    }

    default double getVoltagePhase1() {
        return Integer.MIN_VALUE;
    }

    default double getVoltagePhase2() {
        return Integer.MIN_VALUE;
    }

    default double getVoltagePhase3() {
        return Integer.MIN_VALUE;
    }

    default double getCurrentPhase1() {
        return Integer.MIN_VALUE;
    }

    default double getCurrentPhase2() {
        return Integer.MIN_VALUE;
    }

    default double getCurrentPhase3() {
        return Integer.MIN_VALUE;
    }

    default short getOutputPowerPhase1() {
        return Short.MIN_VALUE;
    }

    default short getOutputPowerPhase2() {
        return Short.MIN_VALUE;
    }

    default short getOutputPowerPhase3() {
        return Short.MIN_VALUE;
    }

    default short getTotalOutputPower() {
        return Short.MIN_VALUE;
    }

    default double getFrequencyPhase1() {
        return Integer.MIN_VALUE;
    }

    default double getFrequencyPhase2() {
        return Integer.MIN_VALUE;
    }

    default double getFrequencyPhase3() {
        return Integer.MIN_VALUE;
    }

    default short getInverterWorkModeCode() {
        return Short.MIN_VALUE;
    }

    default String getInverterWorkMode() {
        return String.valueOf(getInverterWorkModeCode());
    }

    default String toStringDetailed() {
        return "WifiSerial = " + getWifiSerial() + ", WifiVersion = " + getWifiVersion() + ", InverterType = "
                + getInverterType() + ", BatteryPower = " + getBatteryPower() + "W, Battery SoC = " + getBatteryLevel()
                + "%, FeedIn Power = " + getFeedInPower() + "W, Total PV Power = " + (getPV1Power() + getPV2Power())
                + "W";
    }
}
